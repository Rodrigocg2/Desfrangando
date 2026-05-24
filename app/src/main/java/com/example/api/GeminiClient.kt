package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.model.WorkoutExercise
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder().build()

    suspend fun generateWorkout(
        splitType: String,
        focus: String,
        specialNotes: String,
        experienceLevel: String,
        workoutsPerDay: Int,
        workoutsPerWeek: Int
    ): GeneratedWorkoutResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured. Falling back to high-grade local generation.")
            return@withContext GeneratedWorkoutResult.Success(
                listOf(
                    getLocalFallbackWorkout(splitType, focus, specialNotes, experienceLevel, workoutsPerDay, workoutsPerWeek, "A"),
                    getLocalFallbackWorkout(splitType, focus, specialNotes, experienceLevel, workoutsPerDay, workoutsPerWeek, "B"),
                    getLocalFallbackWorkout(splitType, focus, specialNotes, experienceLevel, workoutsPerDay, workoutsPerWeek, "C")
                ),
                isLocalFallback = true
            )
        }

        val systemInstruction = """
            Você é um Treinador Científico de Musculação de Alto Rendimento (PhD em Cinesiologia) e Personal Trainer Premium renomado de altíssimo nível.
            Você gera rotinas impecáveis, estratégicas, realistas, seguras e humanas. Você NUNCA gera treinos aleatórios ou desequilibrados. No seu banco virtual gigante de exercícios, selecione sempre nomenclaturas claras e precisas.
            Regra Fundamental: Você DEVE responder EXCLUSIVAMENTE com o objeto JSON válido, sem tags markdown (como ```json ou ```) e sem qualquer texto explicativo fora do JSON.
            
            DIRETRIZES DE SELEÇÃO INTELIGENTE POR NÍVEL (MANDATÓRIO):
            - USUÁRIO INICIANTE:
              * Priorizar: Uso de máquinas guiadas, exercícios simples, movimentos altamente estáveis e seguros, menor demanda de coordenação motora.
              * Evitar absolutamente: Exercícios extremamente técnicos/complexos (ex: Terra, Agachamento frontal, barra fixa livre), volume excessivo ou técnicas avançadas de intensidade.
              * Exemplo Peito: Supino Vertical Máquina, Voador Peitoral (Pec Deck), Flexão Inclinada na Barra.
              * Exemplo Pernas: Leg Press, Cadeira Extensora, Mesa Flexora, Agachamento Guiado (Smith).
            - USUÁRIO INTERMEDIÁRIO:
              * Adicionar: Exercícios livres (barra, halteres), exercícios compostos tradicionais, maior intensidade e maior volume.
              * Exemplo Peito: Supino Reto com Barra, Supino Inclinado com Halteres, Crossover.
              * Exemplo Pernas: Agachamento Livre com Barra, Afundo com Halteres, Stiff.
            - USUÁRIO AVANÇADO:
              * Permitir: Exercícios complexos, pesados, alta intensidade (RPE 9-10), exercícios livres avançados. Maximize técnicas intensivas.
              * Técnicas Avançadas permitidas: "Drop-set", "Rest-pause", "Bi-set", "Excêntrica Lenta (tempo cadenciado)".
              * Exemplo Peito: Supino Reto Pesado com Barra, Paralelas Livres com Carga, Crossover com Drop-set, Supino Halter com Rest-pause.
              * Exemplo Pernas: Levantamento Terra Clássico, Agachamento Frontal com Barra, técnicas de exaustão nas extensoras/flexoras (Drop-set, Rest-pause).

            DIRETRIZES DE ESTILO, MOVIMENTO E BIOMECÂNICA:
            Considere sempre a segurança das articulações, a estabilidade, a coordenação motora necessária e a eficiência biomecânica.
            Distribua os músculos de forma a evitar Overtraining, fadiga excessiva ou treinar o mesmo músculo principal em dias seguidos.

            ORDEM SEQUENCIAL IMPECÁVEL (Siga estritamente):
            1. Comece com 1 Exercício de Aquecimento/Mobilidade específico ("Aquecimento" no campo trainingPhase).
            2. Progrida para Exercício Composto Pesado principal ("Principal" ou "Composto Pesado" no campo trainingPhase).
            3. Exercícios Compostos Auxiliares de suporte ("Principal" no campo trainingPhase).
            4. Exercícios Isoladores focados ("Principal" ou "Acessório" no campo trainingPhase).
            5. Abdômen focado ("Principal" ou "Acessório" no campo trainingPhase).
            6. Cardio ou Alongamento restaurativo no final ("Alongamento" ou "Cardio" no campo trainingPhase).

            REGRAS DE SÉRIES, REPETIÇÕES E TEMPO/DESCANSO (ADAPTADO POR OBJETIVO):
            - HIPERTROFIA:
              * Compostos -> 4 séries. Isoladores -> 3 séries.
              * Repetições: 8-12 reps por série. Tempo de cadência controlado (Ex: 3-1-1-0).
              * Descanso: 60-90 segundos (restSeconds: 60 a 90).
            - FORÇA:
              * 4-6 séries por exercício.
              * Repetições: 3-6 reps de alta carga. Tempo lento (Ex: 4-1-1-0).
              * Descanso: 120-180 segundos (restSeconds: 120 a 180) para plena recuperação de ATP.
            - EMAGRECIMENTO / DEFINIÇÃO:
              * 3-4 séries.
              * Repetições: 12-20 reps com alta queima metabólica.
              * Descanso: 30-60 segundos (restSeconds: 30 a 60) de intervalo curto.

            REGRAS DE DURAÇÃO DO TREINO (MANDATÓRIO):
            Limite rigorosamente a quantidade TOTAL de exercícios no treino de acordo com o tempo disponível informado:
            - 30 minutos de treino = Exatamente 5 exercícios no total (Gere exatamente 5 itens na listagem).
            - 45 minutos de treino = Exatamente 6 exercícios no total (Gere exatamente 6 itens na listagem).
            - 60 minutos de treino = Exatamente 7 exercícios no total (Gere exatamente 7 itens na listagem).
            - 90 minutos de treino = Exatamente 8 exercícios no total (Gere exatamente 8 itens na listagem).
            Cada treino na sequência deve ter rigorosamente este tamanho total para poder ser realizado no tempo exato.
        """.trimIndent()

        val prompt = """
            Gere um programa de treinamento de musculação ultra-personalizado e profissional de acordo com as seguintes regras de ouro cinesiológicas:

            DADOS E PREPARO DO ATLETA:
            - Divisão Escolhida pelo Usuário: $splitType
            - Dias de Treino na Semana: $workoutsPerWeek dias por semana
            - Objetivo Principal: $focus (Hipertrofia, Emagrecimento, Definição, Força, Condicionamento, Recomposição corporal)
            - Nível de Treinabilidade: $experienceLevel (Iniciante, Intermediário, Avançado)
            - Informações adicionais / Notas do Perfil: $specialNotes

            REGRAS DE DIVISÃO DE TREINO E ENQUADRAMENTO DA IA:
            1. Se a Divisão for "ABC":
               - Crie detalhadamente uma divisão de treino contendo 3 treinos diferentes (Treino A, Treino B, Treino C) que dividam harmonicamente o corpo inteiro. Exemplo clássico: Treino A (Peito + Tríceps), Treino B (Costas + Bíceps), Treino C (Pernas + Ombros + Abdômen).
            2. Se a Divisão for "ABCD":
               - Crie 4 treinos separados (Treino A, Treino B, Treino C, Treino D). Exemplo: A (Peito), B (Costas), C (Pernas), D (Ombros + Braços).
            3. Se a Divisão for "ABCDE":
               - Crie 5 treinos diferentes (Treino A, Treino B, Treino C, Treino D, Treino E), um para cada dia.
            4. Se a Divisão for "Push Pull Legs":
               - Organize em movimentos de "Empurrar" (Peito/Ombro/Tríceps), "Puxar" (Costas/Bíceps) e "Membros Inferiores". Se workoutsPerWeek = 6, crie uma estrutura que se repete 2x, ou divida-a adequadamente.
            5. Se a Divisão for "Upper Lower":
               - Crie treinos para Membros Superiores (Upper) e Membros Inferiores (Lower). Se o usuário treina 4 dias, crie Upper A, Lower A, Upper B, Lower B.
            6. Distribuição inteligente conforme dias por semana (workoutsPerWeek):
               - Se treina 3 dias: Distribua o corpo inteiro nos 3 treinos, evite sobrecarga sistêmica, equilibre recuperação. Exemplo: Dia 1 (Peito, Ombro, Tríceps), Dia 2 (Costas, Bíceps), Dia 3 (Pernas, Abdômen).
               - Se treina 4 dias: Divida melhor os grupos musculares, permitindo mais exercícios e volume por seção. Exemplo: Dia 1 (Peito), Dia 2 (Costas), Dia 3 (Pernas), Dia 4 (Ombros + Braços).
               - Se treina 5 ou 6 dias: Incremente o volume semanal estrategicamente, aumente as técnicas avançadas e melhore o foco por grupo muscular.

            REGRAS DE VOLUME E DURAÇÃO (IMPORTANTÍSSIMO):
            Adapte estritamente o número total de exercícios do treino gerado com base no tempo/duração escolhido pelo usuário nas notas/opções:
            - Se Duração do Treino for 30 minutos -> Gere EXATAMENTE 5 exercícios no total.
            - Se Duração do Treino for 45 minutos -> Gere EXATAMENTE 6 exercícios no total.
            - Se Duração do Treino for 60 minutos -> Gere EXATAMENTE 7 exercícios no total.
            - Se Duração do Treino for 90 minutos -> Gere EXATAMENTE 8 exercícios no total.
            O tempo total do treino deve ser rigorosamente calculado considerando: Séries * (Tempo de Execução da repetição + Intervalo de Descanso). Nunca exceda o tempo disponível de aula ou crie treinos impraticáveis.

            REGRAS DE DESCANSO ENTRE AS SÉRIES (ADAPTADO POR OBJETIVO):
            Configure o campo "restSeconds" para cada exercício baseado exatamente no objetivo:
            - Se Objetivo for "Hipertrofia" ou "Recomposição corporal" -> Descanso de 60 a 90 segundos por série (60 a 90).
            - Se Objetivo for "Força" -> Descanso tensional de 120 a 180 segundos por série (120 a 180).
            - Se Objetivo for "Emagrecimento" ou "Definição" -> Descanso metabólico curto de 30 a 60 segundos por série (30 a 60).
            - Se Objetivo for "Condicionamento" ou "Endurance" ou "Cardio" -> Descanso de 30 a 45 segundos por série (30 a 45).

            ORDEM DOS EXERCÍCIOS:
            Estruture a lista de exercícios na ordem sequencial perfeita de execução na academia:
            1. Comece SEMPRE com exercícios compostos e multiarticulares de alta demanda (ex: Supino, Agachamento, Levantamento Terra, Puxadas).
            2. Progrida para exercícios isoladores e acessórios (ex: Elevações laterais, Roscas, Extensões de tríceps).
            3. Finalize com exercícios abdominais, panturrilhas ou cardio leve, aplicando técnicas metabólicas quando apropriado de acordo com o nível.

            ADAPTAÇÃO E SEGURANÇA POR NÍVEL:
            - Nível "Iniciante": Menos volume, priorize exercícios simples e com maior uso de máquinas guiadas para preservar as articulações e guiar o padrão motor. Sem técnicas de alta falha.
            - Nível "Intermediário": Volume moderado, inclua exercícios livres clássicos excelentes com foco em padrão de movimento correto.
            - Nível "Avançado": Permita técnicas de alta intensidade controladas nas últimas séries dos exercícios principais/acessórios, tais como "Drop-set", "Rest-pause", ou "Bi-set".
            - SEGURANÇA MÁXIMA: A IA NUNCA deve repetir treinos pesados para o mesmo músculo em dias seguidos (respeite a recuperação de 48h-72h) e NUNCA coloque pernas pesadas 2 dias seguidos.

            SUA TAREFA É CRIAR UM CICLO COMPLETO DE $workoutsPerWeek TREINOS:
            Retorne EXATAMENTE UM ARRAY JSON com todos os treinos da semana, usando o formato abaixo para cada treino:
            [
               {
                  "title": "Treino A: Puxada & Costas Pesado",
                  "splitType": "$splitType",
                  "focus": "$focus",
                  "exercises": [
                     {
                        "exerciseId": "identificador_unico_minusculo_com_sublinhado",
                        "name": "Nome do exercício em Português",
                        "muscleGroup": "Grupo muscular principal",
                        "targetMuscleDetail": "Porção muscular foco",
                        "sets": 4,
                        "repsRange": "8-12",
                        "tempo": "3-1-1-0",
                        "restSeconds": 90,
                        "advancedTechnique": "Nenhuma",
                        "intensityRPE": 8,
                        "notes": "Dica executiva",
                        "trainingPhase": "Principal"
                     }
                  ]
               },
               {
                  "title": "Treino B: ..." // próximo treino
               }
            ]
         """.trimIndent()

        try {
            // Build Gemini request body
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", prompt)
                     }))
                }))
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", systemInstruction)
                    }))
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.4)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorMsg = response.body?.string() ?: "Unknown error"
                    Log.e(TAG, "Gemini API request failed: $errorMsg")
                    return@withContext GeneratedWorkoutResult.Error("API Error: ${response.code} - Fallback local ativado.")
                }

                val responseBodyStr = response.body?.string() ?: ""
                Log.d(TAG, "Response: $responseBodyStr")

                val jsonResponse = JSONObject(responseBodyStr)
                val candidates = jsonResponse.getJSONArray("candidates")
                val textResponse = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanedJson = cleanJsonString(textResponse)
                val generatedWorkout = parseWorkoutCycleJson(cleanedJson)
                if (generatedWorkout.isNotEmpty()) {
                    GeneratedWorkoutResult.Success(generatedWorkout, isLocalFallback = false)
                } else {
                    GeneratedWorkoutResult.Error("Erro ao analisar a resposta gerada. Gerando localmente.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini flow: ", e)
            GeneratedWorkoutResult.Success(
                listOf(
                    getLocalFallbackWorkout(splitType, focus, specialNotes, experienceLevel, workoutsPerDay, workoutsPerWeek, "A"),
                    getLocalFallbackWorkout(splitType, focus, specialNotes, experienceLevel, workoutsPerDay, workoutsPerWeek, "B"),
                    getLocalFallbackWorkout(splitType, focus, specialNotes, experienceLevel, workoutsPerDay, workoutsPerWeek, "C")
                ),
                isLocalFallback = true
            )
        }
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.substring(7)
        } else if (str.startsWith("```")) {
            str = str.substring(3)
        }
        if (str.endsWith("```")) {
            str = str.substring(0, str.length - 3)
        }
        return str.trim()
    }

    private fun parseWorkoutCycleJson(jsonStr: String): List<GeneratedWorkout> {
        return try {
            val listType = Types.newParameterizedType(List::class.java, GeneratedWorkout::class.java)
            val adapter = moshi.adapter<List<GeneratedWorkout>>(listType)
            adapter.fromJson(jsonStr) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Moshi parsing failed for array", e)
            val list = mutableListOf<GeneratedWorkout>()
            try {
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val title = json.getString("title")
                    val split = json.getString("splitType")
                    val focus = json.getString("focus")
                    val exercisesArray = json.getJSONArray("exercises")
                    val exercisesList = mutableListOf<WorkoutExercise>()
                    for (j in 0 until exercisesArray.length()) {
                        val exerciseJson = exercisesArray.getJSONObject(j)
                        exercisesList.add(
                            WorkoutExercise(
                                exerciseId = exerciseJson.optString("exerciseId", "custom_${System.currentTimeMillis()}"),
                                name = exerciseJson.getString("name"),
                                muscleGroup = exerciseJson.getString("muscleGroup"),
                                targetMuscleDetail = exerciseJson.optString("targetMuscleDetail", "Músculo principal"),
                                sets = exerciseJson.optInt("sets", 4),
                                repsRange = exerciseJson.optString("repsRange", "8-12"),
                                tempo = exerciseJson.optString("tempo", "2-0-2-0"),
                                restSeconds = exerciseJson.optInt("restSeconds", 90),
                                advancedTechnique = exerciseJson.optString("advancedTechnique", "Nenhuma"),
                                intensityRPE = exerciseJson.optInt("intensityRPE", 8),
                                notes = exerciseJson.optString("notes", ""),
                                trainingPhase = exerciseJson.optString("trainingPhase", "Principal")
                            )
                        )
                    }
                    list.add(GeneratedWorkout(title, split, focus, exercisesList))
                }
                list
            } catch (je: Exception) {
                Log.e(TAG, "Manual JSON array parsing failed", je)
                emptyList()
            }
        }
    }

    private fun parseWorkoutJson(jsonStr: String): GeneratedWorkout? {
        return try {
            val adapter = moshi.adapter(GeneratedWorkout::class.java)
            adapter.fromJson(jsonStr)
        } catch (e: Exception) {
            Log.e(TAG, "Moshi parsing failed", e)
            // Manual parsing fallback if moshi fails on dynamic keys
            try {
                val json = JSONObject(jsonStr)
                val title = json.getString("title")
                val split = json.getString("splitType")
                val focus = json.getString("focus")
                val exercisesArray = json.getJSONArray("exercises")
                val exercisesList = mutableListOf<WorkoutExercise>()
                for (i in 0 until exercisesArray.length()) {
                    val exerciseJson = exercisesArray.getJSONObject(i)
                    exercisesList.add(
                        WorkoutExercise(
                            exerciseId = exerciseJson.optString("exerciseId", "custom_${System.currentTimeMillis()}"),
                            name = exerciseJson.getString("name"),
                            muscleGroup = exerciseJson.getString("muscleGroup"),
                            targetMuscleDetail = exerciseJson.optString("targetMuscleDetail", "Músculo principal"),
                            sets = exerciseJson.optInt("sets", 4),
                            repsRange = exerciseJson.optString("repsRange", "8-12"),
                            tempo = exerciseJson.optString("tempo", "3-0-1-0"),
                            restSeconds = exerciseJson.optInt("restSeconds", 90),
                            advancedTechnique = exerciseJson.optString("advancedTechnique", "Nenhuma"),
                            intensityRPE = exerciseJson.optInt("intensityRPE", 9),
                            notes = exerciseJson.optString("notes", "Manuseie com técnica impecável e tensão."),
                            trainingPhase = exerciseJson.optString("trainingPhase", "Principal")
                        )
                    )
                }
                GeneratedWorkout(title, split, focus, exercisesList)
            } catch (ex: Exception) {
                Log.e(TAG, "Manual JSON parsing failed", ex)
                null
            }
        }
    }

    private fun getLocalFallbackWorkout(
        splitType: String,
        focus: String,
        specialNotes: String,
        experienceLevel: String,
        workoutsPerDay: Int,
        workoutsPerWeek: Int,
        dayId: String = "A"
    ): GeneratedWorkout {
        val title = if (workoutsPerDay > 1) {
            "Treino $dayId: Elite AM/PM - $splitType ($focus)"
        } else {
            "Treino $dayId: Elite $experienceLevel - $splitType ($focus)"
        }

        // 1. Determine size (total exercises) based on duration in specialNotes or default
        val limit = when {
            specialNotes.contains("30 min", ignoreCase = true) || specialNotes.contains("30min", ignoreCase = true) -> 5
            specialNotes.contains("45 min", ignoreCase = true) || specialNotes.contains("45min", ignoreCase = true) -> 6
            specialNotes.contains("60 min", ignoreCase = true) || specialNotes.contains("60min", ignoreCase = true) -> 7
            specialNotes.contains("90 min", ignoreCase = true) || specialNotes.contains("90min", ignoreCase = true) -> 8
            else -> 7 // Default duration
        }

        val level = experienceLevel.trim()
        val isBeginner = level.startsWith("Iniciante", ignoreCase = true)
        val isAdvanced = level.startsWith("Avançado", ignoreCase = true) || level.contains("Avançado", ignoreCase = true) || level.contains("Advanced", ignoreCase = true)

        // 2. Determine sets, reps, rests, tempo, advancedTechnique based on Objective (Focus)
        val splitUpper = splitType.uppercase()
        val isHipertrofia = focus.contains("Hipertrofia", ignoreCase = true) || focus.contains("Recomposição", ignoreCase = true)
        val isForca = focus.contains("Força", ignoreCase = true)
        val isEmagrecimento = focus.contains("Emagrecimento", ignoreCase = true) || focus.contains("Definição", ignoreCase = true)

        // Warmup (Aquecimento)
        val warmupExercise = when {
            splitUpper.contains("PERNA") || splitUpper.contains("LEGS") || splitUpper.contains("GLÚTEO") || splitUpper.contains("INFERIORES") -> {
                WorkoutExercise(
                    exerciseId = "mobilidade_quadril",
                    name = "Aquecimento: Mobilidade de Quadril & Tornozelos",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Articulações coxofemoral e tornozelos",
                    sets = 2,
                    repsRange = "12 reps",
                    tempo = "2-2-2-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 4,
                    notes = "Ativação articular mecânica e preparação para agachamento profundo.",
                    trainingPhase = "Aquecimento"
                )
            }
            splitUpper.contains("PULL") || splitUpper.contains("COSTAS") || splitUpper.contains("PUXAR") -> {
                WorkoutExercise(
                    exerciseId = "mobilidade_coluna",
                    name = "Aquecimento: Gato-Camelo e Soltura Torácica",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Eretores de espinha e lombar",
                    sets = 2,
                    repsRange = "15 reps",
                    tempo = "3-2-3-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 4,
                    notes = "Trabalhe a flexão/extensão torácica lenta para proteção da coluna dorsal.",
                    trainingPhase = "Aquecimento"
                )
            }
            else -> { // PUSH, PEITO, BRAÇO, OMBRO
                WorkoutExercise(
                    exerciseId = "manguito_rotador",
                    name = "Aquecimento: Manguito Rotador e Cápsula Articular",
                    muscleGroup = "Ombro",
                    targetMuscleDetail = "Estabilizadores escapulares e manguito",
                    sets = 2,
                    repsRange = "15 ativações",
                    tempo = "2-0-2-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 4,
                    notes = "Ative rotação externa e interna de ombros de forma suave para lubrificação articular.",
                    trainingPhase = "Aquecimento"
                )
            }
        }

        // Compostos Pesados (Heavy Compounds)
        val heavyCompounds = mutableListOf<WorkoutExercise>()
        // Compostos Auxiliares (Auxiliary Compounds)
        val auxiliaryCompounds = mutableListOf<WorkoutExercise>()
        // Isoladores (Isolators)
        val isolators = mutableListOf<WorkoutExercise>()
        // Abdômen (Abs)
        val abs = mutableListOf<WorkoutExercise>()
        // Cardio / Alongamento (Cardio / Stretching)
        val cardioOrStretch = mutableListOf<WorkoutExercise>()

        // Helper to configure sets, reps, rest, techniques based on level & objective (Focus)
        fun buildExercise(
            id: String,
            name: String,
            muscle: String,
            detail: String,
            isCompound: Boolean,
            beginnerAlternative: String? = null,
            advancedAlternative: String? = null,
            advancedTech: String = "Nenhuma",
            rpe: Int = 8,
            notes: String = ""
        ): WorkoutExercise {
            val finalName = when {
                isBeginner && beginnerAlternative != null -> beginnerAlternative
                isAdvanced && advancedAlternative != null -> advancedAlternative
                else -> name
            }

            val finalTech = if (isAdvanced) advancedTech else "Nenhuma"
            val finalRpe = if (isBeginner) 7 else if (isAdvanced) 9 else rpe

            val finalSets = when {
                isForca -> if (isBeginner) 4 else 5
                isHipertrofia -> if (isCompound) 4 else 3
                isEmagrecimento -> if (isBeginner) 3 else 4
                else -> if (isCompound) 4 else 3
            }

            val finalReps = when {
                isForca -> "3-6 reps"
                isHipertrofia -> "8-12 reps"
                isEmagrecimento -> "12-20 reps"
                else -> "10-12 reps"
            }

            val finalRest = when {
                isForca -> if (isBeginner) 120 else 150
                isHipertrofia -> if (isCompound) 90 else 60
                isEmagrecimento -> if (isCompound) 45 else 30
                else -> 60
            }

            val finalTempo = when {
                isForca -> "4-1-1-0"
                isHipertrofia -> "3-1-1-0"
                isEmagrecimento -> "2-0-1-0"
                else -> "3-0-1-0"
            }

            return WorkoutExercise(
                exerciseId = id,
                name = finalName,
                muscleGroup = muscle,
                targetMuscleDetail = detail,
                sets = finalSets,
                repsRange = finalReps,
                tempo = finalTempo,
                restSeconds = finalRest,
                advancedTechnique = finalTech,
                intensityRPE = finalRpe,
                notes = notes,
                trainingPhase = if (isCompound) "Principal" else "Acessório"
            )
        }

        // Populating lists based on Split Type
        if (splitUpper.contains("PERNA") || splitUpper.contains("LEGS") || splitUpper.contains("GLÚTEO") || splitUpper.contains("INFERIORES")) {
            // Legs Split
            heavyCompounds.add(buildExercise(
                id = "agachamento_guiado",
                name = "Agachamento Livre com Barra",
                muscle = "Pernas",
                detail = "Quadríceps Geral e Glúteos",
                isCompound = true,
                beginnerAlternative = "Agachamento Guiado no Smith",
                advancedAlternative = "Agachamento Livre Pesado com Barra",
                advancedTech = "Rest-Pause na última série",
                rpe = 8,
                notes = "Mantenha o bracing abdominal focado e controle a descida (fase excêntrica)."
            ))
            heavyCompounds.add(buildExercise(
                id = "leg_press_45",
                name = "Leg Press 45º Linear",
                muscle = "Pernas",
                detail = "Quadríceps e Glúteo Maior",
                isCompound = true,
                rpe = 8,
                notes = "Posicione os pés de forma paralela, empurre mantendo a estabilidade axial do quadril."
            ))

            auxiliaryCompounds.add(buildExercise(
                id = "afundo_halteres",
                name = "Afundo com Halteres",
                muscle = "Pernas",
                detail = "Quadríceps unilateral e Glúteo Médio",
                isCompound = true,
                notes = "Foque no alinhamento do joelho com a ponta do pé para preservar a patela."
            ))
            auxiliaryCompounds.add(buildExercise(
                id = "stiff_barra",
                name = "Stiff com Halteres",
                muscle = "Pernas",
                detail = "Posteriores de Coxa e Glúteos",
                isCompound = true,
                beginnerAlternative = "Mesa Flexora Horizontal",
                advancedAlternative = "Stiff com Barra Pesado",
                advancedTech = "Rest-Pause",
                notes = "Incline o tronco empurrando o quadril para trás com a coluna perfeitamente neutra."
            ))

            isolators.add(buildExercise(
                id = "cadeira_extensora",
                name = "Cadeira Extensora",
                muscle = "Pernas",
                detail = "Vasto Lateral, Vasto Medial e Reto Femoral",
                isCompound = false,
                advancedTech = "Drop-set na última",
                rpe = 8,
                notes = "Aplique um pico de contração de 1 segundo no topo com máxima intenção."
            ))
            isolators.add(buildExercise(
                id = "mesa_flexora",
                name = "Mesa Flexora",
                muscle = "Pernas",
                detail = "Isquiotibiais / Semitendinoso",
                isCompound = false,
                notes = "Estabilize o quadril no banco e puxe o calcanhar com controle biomecânico."
            ))
            isolators.add(buildExercise(
                id = "elevacao_panturrilha",
                name = "Gêmeos em Pé (Panturrilhas)",
                muscle = "Pernas",
                detail = "Gastrocnêmio",
                isCompound = false,
                notes = "Execute amplitude máxima: alongamento profundo embaixo e contração total em cima."
            ))

            abs.add(buildExercise(
                id = "abdominal_infra",
                name = "Abdominal Reto no Solo Inframax",
                muscle = "Abdômen",
                detail = "Reto Abdominal Inferior",
                isCompound = false,
                notes = "Eleve o quadril acionando a musculatura interna do transverso."
            ))

            cardioOrStretch.add(WorkoutExercise(
                exerciseId = "alongamento_pernas",
                name = "Alongamento e Descompressão de Posteriores",
                muscleGroup = "Geral",
                targetMuscleDetail = "Fáscia de Membros Inferiores",
                sets = 1,
                repsRange = "45 segundos",
                tempo = "Isométrico",
                restSeconds = 30,
                advancedTechnique = "Soltura Fascial",
                intensityRPE = 5,
                notes = "Restaure a amplitude de movimento respirando profundamente e acalmando o sistema nervoso.",
                trainingPhase = "Alongamento"
            ))

        } else if (splitUpper.contains("PULL") || splitUpper.contains("COSTAS") || splitUpper.contains("PUXAR")) {
            // Costas Split (Pull)
            heavyCompounds.add(buildExercise(
                id = "puxada_pulley_frente",
                name = "Puxada Pulley Frente Aberta",
                muscle = "Dorso",
                detail = "Latíssimo do Dorso",
                isCompound = true,
                beginnerAlternative = "Puxada Aberta Máquina",
                advancedAlternative = "Puxada Pulley Frente Pesada",
                advancedTech = "Rest-Pause",
                rpe = 8,
                notes = "Puxe direcionando os cotovelos para baixo, esmagando a porção externa do latíssimo."
            ))
            heavyCompounds.add(buildExercise(
                id = "remada_curvada_barra",
                name = "Remada Curvada com Barra",
                muscle = "Dorso",
                detail = "Dorso Geral e Romboides",
                isCompound = true,
                beginnerAlternative = "Remada Baixa Polia com Triângulo",
                advancedAlternative = "Levantamento Terra Clássico",
                notes = "Mantenha o bracing e puxe a barra na direção da cicatriz umbilical."
            ))

            auxiliaryCompounds.add(buildExercise(
                id = "remada_serrote",
                name = "Remada Unilateral com Halter (Serrote)",
                muscle = "Dorso",
                detail = "Dorso Unilateral",
                isCompound = true,
                notes = "Mantenha as escápulas engajadas e trabalhe o movimento isolado sem rotacionar o tronco."
            ))
            auxiliaryCompounds.add(buildExercise(
                id = "pulldown_cabo",
                name = "Pulldown com Corda no Cabo",
                muscle = "Dorso",
                detail = "Latíssimo do Dorso Inferior",
                isCompound = true,
                notes = "Mantenha os braços semi-estendidos gerando tensão constante na fase excêntrica."
            ))

            isolators.add(buildExercise(
                id = "rosca_direta_barraw",
                name = "Rosca Direta com Barra W",
                muscle = "Braços",
                detail = "Bíceps Braquial cabeça longa",
                isCompound = false,
                notes = "Evite balanço do tronco. Foque o torque mecânico estritamente nos cotovelos."
            ))
            isolators.add(buildExercise(
                id = "rosca_martelo_halteres",
                name = "Rosca Martelo com Halteres",
                muscle = "Braços",
                detail = "Braquiorradial e Braquial",
                isCompound = false,
                advancedTech = "Drop-set na última",
                notes = "Mantenha pegada neutra constante esmagando os antebraços."
            ))
            isolators.add(buildExercise(
                id = "crucifixo_invertido",
                name = "Crucifixo Invertido com Halteres",
                muscle = "Ombro",
                detail = "Deltoide Posterior",
                isCompound = false,
                notes = "Excelente para equilíbrio biomecânico do ombro e estabilidade postural."
            ))

            abs.add(buildExercise(
                id = "abdominal_infra",
                name = "Abdominal Reto Solo",
                muscle = "Abdômen",
                detail = "Reto Abdominal",
                isCompound = false,
                notes = "Flexione o tronco de forma concentrada sem puxar o pescoço."
            ))

            cardioOrStretch.add(WorkoutExercise(
                exerciseId = "alongamento_pendurado",
                name = "Alongamento Descompressivo de Dorsais",
                muscleGroup = "Geral",
                targetMuscleDetail = "Fáscia muscular do dorso",
                sets = 1,
                repsRange = "45 segundos",
                tempo = "Isométrico",
                restSeconds = 30,
                advancedTechnique = "Mobilidade",
                intensityRPE = 5,
                notes = "Pendure-se suavemente para tracionar a lombar e hidratar os discos intervertebrais.",
                trainingPhase = "Alongamento"
            ))

        } else if (splitUpper.contains("PUSH") || splitUpper.contains("PEITO") || splitUpper.contains("EMPURRAR")) {
            // Peito Split (Push)
            heavyCompounds.add(buildExercise(
                id = "supino_reto_barra",
                name = "Supino Reto com Barra",
                muscle = "Peito",
                detail = "Peitoral Maior Central",
                isCompound = true,
                beginnerAlternative = "Supino Vertical Máquina",
                advancedAlternative = "Supino Reto Pesado com Barra",
                advancedTech = "Rest-Pause na última",
                rpe = 8,
                notes = "Aduza as escápulas no banco estabilizando a cintura escapular para proteção de ombros."
            ))
            heavyCompounds.add(buildExercise(
                id = "supino_inclinado_halteres",
                name = "Supino Inclinado com Halteres",
                muscle = "Peito",
                detail = "Fibras Claviculares Superiores",
                isCompound = true,
                notes = "Incline o banco a 30-40 graus para focar a tensão nas fibras superiores do peitoral."
            ))

            auxiliaryCompounds.add(buildExercise(
                id = "desenvolvimento_ombros_sentado",
                name = "Desenvolvimento de Ombros com Halteres",
                muscle = "Ombro",
                detail = "Deltoide Anterior",
                isCompound = true,
                notes = "Suba verticalmente desacelerando a descida no plano escapular."
            ))
            auxiliaryCompounds.add(buildExercise(
                id = "peck_deck",
                name = "Voador Peitoral (Pec Deck)",
                muscle = "Peito",
                detail = "Fibras Internas do Peito",
                isCompound = true,
                notes = "Mantenha o peito aberto e esmague na fase concêntrica máxima."
            ))

            isolators.add(buildExercise(
                id = "elevacao_lateral_halteres",
                name = "Elevação Lateral com Halteres",
                muscle = "Ombro",
                detail = "Deltoide Lateral",
                isCompound = false,
                advancedTech = "Drop-set triplo",
                rpe = 8,
                notes = "Controle o movimento sem impulsos, projetando os braços sutilmente para frente."
            ))
            isolators.add(buildExercise(
                id = "triceps_testa_cabo",
                name = "Tríceps Testa no Cabo",
                muscle = "Braços",
                detail = "Tríceps Cabeça Longa",
                isCompound = false,
                notes = "Mantenha os cotovelos paralelos apontados para a polia ativando a porção longa."
            ))
            isolators.add(buildExercise(
                id = "triceps_corda_pulley",
                name = "Tríceps Corda Pulley",
                muscle = "Braços",
                detail = "Tríceps Cabeça Lateral",
                isCompound = false,
                notes = "Afaste a corda no final da descida para contrair a cabeça lateral ao máximo."
            ))

            abs.add(buildExercise(
                id = "abdominal_crunch_solo",
                name = "Abdominal Crunch Solo",
                muscle = "Abdômen",
                detail = "Reto Abdominal Geral",
                isCompound = false,
                notes = "Esmague o abdômen contra o chão mantendo força constante na volta."
            ))

            cardioOrStretch.add(WorkoutExercise(
                exerciseId = "alongamento_peito",
                name = "Alongamento de Peitoral na Coluna de Acesso",
                muscleGroup = "Geral",
                targetMuscleDetail = "Fáscia de Peitoral e Ombros",
                sets = 1,
                repsRange = "45 segundos",
                tempo = "Isométrico",
                restSeconds = 30,
                advancedTechnique = "Soltura Fascial",
                intensityRPE = 5,
                notes = "Relaxe o peito abrindo os braços fixados na coluna para reequilibrar a postura.",
                trainingPhase = "Alongamento"
            ))

        } else if (splitUpper.contains("BRAÇO") || splitUpper.contains("ARM") || splitUpper.contains("OMBRO")) {
            // Arms / shoulders Split
            heavyCompounds.add(buildExercise(
                id = "desenvolvimento_halteres",
                name = "Desenvolvimento de Ombros Sentado",
                muscle = "Ombro",
                detail = "Deltoide Anterior e Lateral",
                isCompound = true,
                notes = "Excelente exercício composto multiarticular focado nos deltoides."
            ))
            heavyCompounds.add(buildExercise(
                id = "triceps_paralelas",
                name = "Tríceps Paralelas na Máquina",
                muscle = "Braços",
                detail = "Tríceps Geral e Peitoral Inferior",
                isCompound = true,
                beginnerAlternative = "Tríceps Banco Máquina",
                advancedAlternative = "Mergulho nas Paralelas Livres com Carga",
                notes = "Mantenha os ombros firmes estabilizados para focar no tríceps."
            ))

            auxiliaryCompounds.add(buildExercise(
                id = "rosca_direta_polia",
                name = "Rosca Direta no Cabo Polia",
                muscle = "Braços",
                detail = "Bíceps Braquial cabeça curta",
                isCompound = true,
                notes = "O cabo mantém tension mecanica continua em todo o arco do movimento."
            ))

            isolators.add(buildExercise(
                id = "triceps_corda",
                name = "Tríceps Corda no Pulley",
                muscle = "Braços",
                detail = "Tríceps Cabeça Lateral",
                isCompound = false,
                advancedTech = "Drop-set",
                notes = "Puxe abrindo as pontas da corda na parte final do movimento."
            ))
            isolators.add(buildExercise(
                id = "rosca_martelo_alternada",
                name = "Rosca Martelo Alternada",
                muscle = "Braços",
                detail = "Braquiorradial",
                isCompound = false,
                notes = "Controle o movimento sem impulsos com o tronco."
            ))
            isolators.add(buildExercise(
                id = "elevacao_lateral",
                name = "Elevação Lateral com Halteres",
                muscle = "Ombro",
                detail = "Deltoide Lateral",
                isCompound = false,
                notes = "Mantenha o foco absoluto na contração das porções laterais."
            ))

            abs.add(buildExercise(
                id = "abdominal_prancha",
                name = "Prancha Isométrica Ativa",
                muscle = "Abdômen",
                detail = "Core e Transverso Abdominal",
                isCompound = false,
                notes = "Mantenha o glúteo e core em forte bracing."
            ))

            cardioOrStretch.add(WorkoutExercise(
                exerciseId = "alongamento_ombros",
                name = "Alongamento e Soltura para Ombros",
                muscleGroup = "Geral",
                targetMuscleDetail = "Cápsula e fáscias de braço",
                sets = 1,
                repsRange = "45 segundos",
                tempo = "Isométrico",
                restSeconds = 30,
                advancedTechnique = "Soltura",
                intensityRPE = 5,
                notes = "Alivie a pressão nos braços respirando com calma.",
                trainingPhase = "Alongamento"
            ))

        } else {
            // Mixed / Default Split (Full Body)
            heavyCompounds.add(buildExercise(
                id = "agachamento_guiado",
                name = "Agachamento Livre com Barra",
                muscle = "Pernas",
                detail = "Quadríceps Geral e Glúteos",
                isCompound = true,
                beginnerAlternative = "Agachamento no Smith (Guiado)",
                advancedAlternative = "Agachamento Livre Pesado com Barra",
                advancedTech = "Rest-Pause",
                notes = "Agachamento profundo focado em segurança com postura ereta."
            ))
            heavyCompounds.add(buildExercise(
                id = "supino_reto",
                name = "Supino Reto com Barra",
                muscle = "Peito",
                detail = "Peitoral Maior Central",
                isCompound = true,
                beginnerAlternative = "Supino Vertical Máquina",
                advancedAlternative = "Supino Reto Pesado com Barra",
                notes = "Importante exercício multiarticular de peitoral."
            ))

            auxiliaryCompounds.add(buildExercise(
                id = "puxada_pulley",
                name = "Puxada Pulley Frente Aberta",
                muscle = "Dorso",
                detail = "Latíssimo do Dorso",
                isCompound = true,
                notes = "Mantenha o tronco estável, puxando a barra em direção ao peito superior."
            ))

            isolators.add(buildExercise(
                id = "elevacao_lateral",
                name = "Elevação Lateral com Halteres",
                muscle = "Ombro",
                detail = "Deltoide Lateral",
                isCompound = false,
                notes = "Foque na ativação isolada sem elevação excessiva de trapézios."
            ))
            isolators.add(buildExercise(
                id = "rosca_martelo",
                name = "Rosca Martelo com Halteres",
                muscle = "Braços",
                detail = "Braquiorradial",
                isCompound = false,
                notes = "Mantenha o cotovelo travado ao lado do corpo."
            ))
            isolators.add(buildExercise(
                id = "triceps_corda",
                name = "Tríceps Corda Pulley",
                muscle = "Braços",
                detail = "Tríceps Cabeça Lateral",
                isCompound = false,
                notes = "Puxe expandindo a corda no final da contração."
            ))

            abs.add(buildExercise(
                id = "abdominal_retosino",
                name = "Abdominal Reto Solo",
                muscle = "Abdômen",
                detail = "Reto Abdominal",
                isCompound = false,
                notes = "Trabalhe de forma concentrada sem velocidade excessiva."
            ))

            cardioOrStretch.add(WorkoutExercise(
                exerciseId = "alongamento_descompressivo_total",
                name = "Alongamento Restaurativo Corporal Completo",
                muscleGroup = "Geral",
                targetMuscleDetail = "Fáscia e articulações gerais",
                sets = 1,
                repsRange = "45 segundos",
                tempo = "Isométrico",
                restSeconds = 30,
                advancedTechnique = "Soltura Fascial",
                intensityRPE = 5,
                notes = "Respire de maneira calma profunda, relaxando o corpo totalmente.",
                trainingPhase = "Alongamento"
            ))
        }

        // 3. Construct final exact list adhering to strict sequence:
        // Ordem: Warmup -> Composto Pesado -> Compostos Auxiliares -> Isoladores -> Abdômen -> Cardio/Stretch
        // We will fill exactly 'limit' exercises
        val resultList = mutableListOf<WorkoutExercise>()

        // Position 1: Warmup
        resultList.add(warmupExercise)

        val chosenMidSections = mutableListOf<WorkoutExercise>()

        // Grab compounds and isolators to fill needed sections
        when (limit) {
            5 -> { // 1 Warmup + 3 mid-sections + 1 stretch = 5
                chosenMidSections.add(heavyCompounds.getOrNull(0) ?: heavyCompounds.first())
                chosenMidSections.add(isolators.getOrNull(0) ?: isolators.first())
                chosenMidSections.add(abs.getOrNull(0) ?: abs.first())
            }
            6 -> { // 1 Warmup + 4 mid-sections + 1 stretch = 6
                chosenMidSections.add(heavyCompounds.getOrNull(0) ?: heavyCompounds.first())
                chosenMidSections.add(auxiliaryCompounds.getOrNull(0) ?: auxiliaryCompounds.first())
                chosenMidSections.add(isolators.getOrNull(0) ?: isolators.first())
                chosenMidSections.add(abs.getOrNull(0) ?: abs.first())
            }
            7 -> { // 1 Warmup + 5 mid-sections + 1 stretch = 7
                chosenMidSections.add(heavyCompounds.getOrNull(0) ?: heavyCompounds.first())
                chosenMidSections.add(auxiliaryCompounds.getOrNull(0) ?: auxiliaryCompounds.first())
                chosenMidSections.add(isolators.getOrNull(0) ?: isolators.first())
                chosenMidSections.add(isolators.getOrNull(1) ?: isolators.first())
                chosenMidSections.add(abs.getOrNull(0) ?: abs.first())
            }
            8 -> { // 1 Warmup + 6 mid-sections + 1 stretch = 8
                chosenMidSections.add(heavyCompounds.getOrNull(0) ?: heavyCompounds.first())
                chosenMidSections.add(auxiliaryCompounds.getOrNull(0) ?: auxiliaryCompounds.first())
                chosenMidSections.add(auxiliaryCompounds.getOrNull(1) ?: auxiliaryCompounds.getOrNull(0) ?: auxiliaryCompounds.first())
                chosenMidSections.add(isolators.getOrNull(0) ?: isolators.first())
                chosenMidSections.add(isolators.getOrNull(1) ?: isolators.first())
                chosenMidSections.add(abs.getOrNull(0) ?: abs.first())
            }
            else -> { // Same as 7
                chosenMidSections.add(heavyCompounds.getOrNull(0) ?: heavyCompounds.first())
                chosenMidSections.add(auxiliaryCompounds.getOrNull(0) ?: auxiliaryCompounds.first())
                chosenMidSections.add(isolators.getOrNull(0) ?: isolators.first())
                chosenMidSections.add(isolators.getOrNull(1) ?: isolators.first())
                chosenMidSections.add(abs.getOrNull(0) ?: abs.first())
            }
        }

        resultList.addAll(chosenMidSections)

        // Position Last: Cardio or Stretch
        resultList.add(cardioOrStretch.getOrNull(0) ?: cardioOrStretch.first())

        // Ensure exact slice equal to limit
        val finalExercises = resultList.take(limit)

        return GeneratedWorkout(title, splitType, focus, finalExercises)
    }
}

sealed class GeneratedWorkoutResult {
    data class Success(val cycle: List<GeneratedWorkout>, val isLocalFallback: Boolean) : GeneratedWorkoutResult()
    data class Error(val message: String) : GeneratedWorkoutResult()
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GeneratedWorkout(
    val title: String,
    val splitType: String,
    val focus: String,
    val exercises: List<WorkoutExercise>
)
