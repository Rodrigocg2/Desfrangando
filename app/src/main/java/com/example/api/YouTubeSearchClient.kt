package com.example.api

import android.util.Log
import com.example.model.CachedYouTubeVideo
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

object YouTubeSearchClient {
    private const val TAG = "YOUTUBE_SEARCH_CLIENT"
    private val client by lazy { 
        OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build() 
    }

    /**
     * Searches YouTube for the given exercise and returns a CachedYouTubeVideo.
     * It tries multiple search variations to find a relevant instructional video.
     */
    fun searchVideo(exerciseName: String): CachedYouTubeVideo? {
        if (exerciseName.isBlank()) return null
        
        val queries = listOf(
            "$exerciseName execução correta musculação",
            "$exerciseName técnica correta",
            "$exerciseName como fazer corretamente",
            "$exerciseName execução perfeita",
            exerciseName,
            "$exerciseName musculação"
        )
        
        val maxTests = 20
        var testedCount = 0

        for (query in queries) {
            try {
                Log.d(TAG, "Tentando busca no YouTube para '$exerciseName' com a query: '$query'")
                val videoList = performWebSearch(query, exerciseName)
                
                for (video in videoList) {
                    if (testedCount >= maxTests) {
                        Log.w(TAG, "Atingido o limite de $maxTests tentativas de vídeos.")
                        return null
                    }
                    
                    testedCount++
                    Log.d(TAG, "YOUTUBE: VideoId ${video.videoId}. Verificando se é embeddable: Tentativa $testedCount")
                    
                    if (isVideoEmbeddable(video.videoId)) {
                        Log.d(TAG, "YOUTUBE: Embeddable true. Selecionado: ${video.title} (${video.videoId})")
                        return video
                    } else {
                        Log.d(TAG, "YOUTUBE: Embeddable false. Ignorado: ${video.videoId}.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao buscar com query '$query': ${e.message}", e)
            }
        }
        return null
    }

    private fun isVideoEmbeddable(videoId: String): Boolean {
        try {
            // A forma mais confiável de verificar vídeos bloqueados sem chave da API 
            // é pela API aberta do oEmbed do YouTube. Vídeos privados ou não-embeddables retornam 401.
            val oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
            val oembedRequest = Request.Builder().url(oembedUrl).build()
            client.newCall(oembedRequest).execute().use { oembedResponse ->
                if (oembedResponse.isSuccessful) {
                    return true
                }
            }
            
            // Fallback scraping
            val url = "https://www.youtube.com/watch?v=$videoId"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
                .header("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Cookie", "SOCS=CAESEwgDEgk0ODE3Nzk3MjQaAnB0IAEaBgiA_KugBg; CONSENT=YES+cb.20230530-04-p0.pt+FX+111; GPS=1;")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return false
                val html = response.body?.string() ?: return false
                
                if (html.contains("\"playableInEmbed\":true") || html.contains("\"playableInEmbed\": true")) {
                    return true
                } else if (html.contains("\"playableInEmbed\":false") || html.contains("\"playableInEmbed\": false")) {
                    return false
                }
                
                return false // Se não temos certeza, melhor rejeitar para evitar o erro 150
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar watch page $videoId: ${e.message}")
            return false
        }
    }

    private fun performWebSearch(query: String, exerciseName: String): List<CachedYouTubeVideo> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        // Use active query params and search url
        val url = "https://www.youtube.com/results?search_query=$encodedQuery"
        
        Log.d(TAG, "Fazendo requisicao HTTP de busca para: $url")
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
            .header("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
            .header("Cookie", "SOCS=CAESEwgDEgk0ODE3Nzk3MjQaAnB0IAEaBgiA_KugBg; CONSENT=YES+cb.20230530-04-p0.pt+FX+111; GPS=1;")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e(TAG, "erro HTTP do servidor: ${response.code}")
                return emptyList()
            }
            val html = response.body?.string() ?: return emptyList()
            Log.d(TAG, "HTML recebido com sucesso. Tamanho: ${html.length} caracteres.")
            
            return parseYouTubeHtml(html, exerciseName)
        }
    }

    private fun parseYouTubeHtml(html: String, exerciseName: String): List<CachedYouTubeVideo> {
        val list = mutableListOf<CachedYouTubeVideo>()
        
        val videoRendererRegex = """"videoId"\s*:\s*"([a-zA-Z0-9_-]{11})"""".toRegex()
        val watchUrlRegex = """/watch\?v=([a-zA-Z0-9_-]{11})""".toRegex()
        val embedUrlRegex = """/embed/([a-zA-Z0-9_-]{11})""".toRegex()
        val shortUrlRegex = """/shorts/([a-zA-Z0-9_-]{11})""".toRegex()
        
        val foundIds = mutableSetOf<String>()
        videoRendererRegex.findAll(html).forEach { foundIds.add(it.groupValues[1]) }
        watchUrlRegex.findAll(html).forEach { foundIds.add(it.groupValues[1]) }
        embedUrlRegex.findAll(html).forEach { foundIds.add(it.groupValues[1]) }
        shortUrlRegex.findAll(html).forEach { foundIds.add(it.groupValues[1]) }
        
        val allIds = foundIds.filter { id ->
            id != "v" && id.length == 11 && id.all { it.isLetterOrDigit() || it == '_' || it == '-' }
        }.distinct()
        
        Log.d(TAG, "Total de IDs brutos extraídos: ${allIds.size}. Amostra: ${allIds.take(5)}")
        
        for (id in allIds) {
            var title = "$exerciseName - Execução Correta"
            var channel = "Instrutor Musculação"
            
            try {
                val index = html.indexOf(id)
                if (index != -1) {
                    val startIndex = (index - 500).coerceAtLeast(0)
                    val endIndex = (index + 4000).coerceAtMost(html.length)
                    val snippet = html.substring(startIndex, endIndex)
                    
                    // Regex for title inside typical videoRenderer JSON
                    val titleRegex = """"title"\s*:\s*\{\s*"runs"\s*:\s*\[\s*\{\s*"text"\s*:\s*"([^"]+)"""".toRegex()
                    val titleMatch = titleRegex.find(snippet)
                    if (titleMatch != null) {
                        val rawTitle = titleMatch.groupValues[1]
                        if (rawTitle.isNotBlank() && !rawTitle.startsWith("{") && !rawTitle.endsWith("}")) {
                            title = cleanJsonString(rawTitle)
                        }
                    } else {
                        val altTitleRegex = """"title"\s*:\s*\{\s*"simpleText"\s*:\s*"([^"]+)"""".toRegex()
                        altTitleRegex.find(snippet)?.let {
                            val rawTitle = it.groupValues[1]
                            if (rawTitle.isNotBlank()) title = cleanJsonString(rawTitle)
                        }
                    }
                    
                    // Regex for channel name
                    val channelRegex = """"longBylineText"\s*:\s*\{\s*"runs"\s*:\s*\[\s*\{\s*"text"\s*:\s*"([^"]+)"""".toRegex()
                    val channelMatch = channelRegex.find(snippet)
                    if (channelMatch != null) {
                        val rawChannel = channelMatch.groupValues[1]
                        if (rawChannel.isNotBlank() && !rawChannel.startsWith("{")) {
                            channel = cleanJsonString(rawChannel)
                        }
                    } else {
                        val ownerRegex = """"ownerText"\s*:\s*\{\s*"runs"\s*:\s*\[\s*\{\s*"text"\s*:\s*"([^"]+)"""".toRegex()
                        ownerRegex.find(snippet)?.let {
                            val rawChannel = it.groupValues[1]
                            if (rawChannel.isNotBlank() && !rawChannel.startsWith("{")) channel = cleanJsonString(rawChannel)
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Erro ao extrair metadados para ID: $id", ex)
            }
            
            // Fix any unicode-escaped sequences (like Portuguese characters)
            if (title.contains("\\u")) {
                title = unicodeDecode(title)
            }
            if (channel.contains("\\u")) {
                channel = unicodeDecode(channel)
            }
            
            val thumbnailUrl = "https://img.youtube.com/vi/$id/0.jpg"
            list.add(
                CachedYouTubeVideo(
                    exerciseName = exerciseName,
                    videoId = id,
                    title = title,
                    channel = channel,
                    thumbnailUrl = thumbnailUrl,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        return list
    }

    private fun cleanJsonString(str: String): String {
        return str
            .replace("\\u0026", "&")
            .replace("\\\"", "\"")
            .replace("\\\'", "'")
            .replace("&amp;", "&")
            .replace("\"", "")
            .trim()
    }

    private fun unicodeDecode(str: String): String {
        val regex = """\\u([0-9a-fA-F]{4})""".toRegex()
        return regex.replace(str) { matchResult ->
            try {
                matchResult.groupValues[1].toInt(16).toChar().toString()
            } catch (e: Exception) {
                matchResult.value
            }
        }
    }
}
