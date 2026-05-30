import java.net.HttpURLConnection
import java.net.URL

fun check(videoId: String) {
    try {
        val url = URL("https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        val code = conn.responseCode
        println("Video $videoId: $code")
    } catch (e: Exception) {
        println("Error: $e")
    }
}

fun main() {
    check("M7FIvfx5J10") // standard video
    check("c2k96xZ8r-k") // maybe embed disabled? Or we will just search for one
}
