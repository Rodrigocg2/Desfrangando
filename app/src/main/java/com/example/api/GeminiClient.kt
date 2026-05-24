package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.model.WorkoutExercise
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

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
                getLocalFallbackWorkout(splitType, focus, specialNotes, experienceLevel, workoutsPerDay, workoutsPerWeek),
                isLocalFallback = true
            )
        }

        val systemInstruction = """
            Você é um Treinador Científico de Musculação de Alto Rendimento com PhD em Cinesiologia e treinador de atletas classe Elite.
            Você gera rotinas impecáveis, que parecem prescritas por um personal trainer premium de altíssimo nível.
            Regra Fundamental: Você DEVE responder EXCLUSIVAMENTE com o objeto JSON válido, sem tags markdown (como ```json ou ```) e sem qualquer texto explicativo fora do JSON.
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

            Retorne no seguinte formato estrito JSON (Substitua as variáveis pelo treino gerado para o dia em questão):
            {
               "title": "Nome imponente do Treino e o Dia Corrido (ex: Treino A: Dorsal Force & Bíceps)",
               "splitType": "$splitType",
               "focus": "$focus",
               "exercises": [
                  {
                     "exerciseId": "identificador_unico_minusculo_com_sublinhado (ex: supino_reto, agachamento_livre, levantamento_terra, puxada_polia_alta, elevacao_lateral, rosca_polia, triceps_testa, leg_press_45, ou um id customizado relevante)",
                     "name": "Nome do exercício em Português",
                     "muscleGroup": "Grupo muscular principal (Peito, Dorso, Pernas, Ombros, Braços, Cardio, Abdômen)",
                     "targetMuscleDetail": "Qual porção muscular foca especificamente (ex: Fibras superiores do peitoral, Vasto lateral do quadríceps)",
                     "sets": 4,
                     "repsRange": "Faixa de repetições (ex: 4 séries de 8-12 reps, ou Pirâmide 12-10-8-6)",
                     "tempo": "Tempo sob tensão em 4 dígitos (ex: 3-1-1-0 para 3s excêntrica, 1s isométrica, 1s isométrica de contração)",
                     "restSeconds": 90,
                     "advancedTechnique": "Técnica avançada usada (ex: Drop-set, Rest-Pause, Bi-set, Nenhuma)",
                     "intensityRPE": 8,
                     "notes": "Dica executiva do personal trainer premium para controle de movimento e integridade física (ex: Controlar subida, não curvar a lombar)",
                     "trainingPhase": "Aquecimento" (ou "Principal", "Acessório", "Alongamento")
                  }
               ]
            }
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
                val generatedWorkout = parseWorkoutJson(cleanedJson)
                if (generatedWorkout != null) {
                    GeneratedWorkoutResult.Success(generatedWorkout, isLocalFallback = false)
                } else {
                    GeneratedWorkoutResult.Error("Erro ao analisar a resposta gerada. Gerando localmente.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini flow: ", e)
            GeneratedWorkoutResult.Success(
                getLocalFallbackWorkout(splitType, focus, specialNotes, experienceLevel, workoutsPerDay, workoutsPerWeek),
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
        workoutsPerWeek: Int
    ): GeneratedWorkout {
        val title = if (workoutsPerDay > 1) {
            "PowerHigh Elite AM/PM - $splitType ($focus)"
        } else {
            "PowerHigh Elite - $splitType ($focus)"
        }

        // 1. Determine size (total exercises) based on duration in specialNotes or default
        val limit = when {
            specialNotes.contains("30 min") -> 5
            specialNotes.contains("45 min") -> 6
            specialNotes.contains("60 min") -> 7
            specialNotes.contains("90 min") -> 8
            else -> 7 // Default duration
        }

        // 2. Determine rest seconds based on Focus
        val rest = when {
            focus.contains("Hipertrofia", ignoreCase = true) || focus.contains("Recomposição", ignoreCase = true) -> 90
            focus.contains("Força", ignoreCase = true) -> 150
            focus.contains("Emagrecimento", ignoreCase = true) || focus.contains("Definição", ignoreCase = true) -> 45
            else -> 35 // Condicionamento / Resistência
        }

        // 3. Level adjustments (Iniciante, Intermediário, Avançado)
        val level = experienceLevel.trim()
        val isBeginner = level.startsWith("Iniciante", ignoreCase = true)
        val isAdvanced = level.startsWith("Avançado", ignoreCase = true)
        val defaultSets = if (isBeginner) 3 else 4
        val defaultRPE = if (isBeginner) 7 else if (isAdvanced) 9 else 8
        val defaultTechnique = if (isAdvanced) "Rest-Pause" else "Nenhuma"

        // Build suitable list of exercises for the splitType
        val splitUpper = splitType.uppercase()
        
        // Define lists of exercises for phases
        val warmups: List<WorkoutExercise> = when {
            splitUpper.contains("AEROBICO") || splitUpper.contains("CARDIO") -> listOf(
                WorkoutExercise(
                    exerciseId = "esteira_aerobico",
                    name = "Aquecimento: Caminhada Progressiva",
                    muscleGroup = "Cardio",
                    targetMuscleDetail = "Sistema Cardiorrespiratório",
                    sets = 1,
                    repsRange = "5 min",
                    tempo = "Livre",
                    restSeconds = 30,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 5,
                    notes = "Ative dobras motoras e eleve gradualmente a frequência cardíaca.",
                    trainingPhase = "Aquecimento"
                )
            )
            splitUpper.contains("LEGS") || splitUpper.contains("PERNA") || splitUpper.contains("GLÚTEO") -> listOf(
                WorkoutExercise(
                    exerciseId = "mobilidade_quadril",
                    name = "Aquecimento: Mobilidade de Quadril",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Articulações coxofemoral e tornozelos",
                    sets = 2,
                    repsRange = "12 reps por lado",
                    tempo = "2-2-2-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 4,
                    notes = "Solte o quadril realizando rotações dinâmicas para proteger a lombar.",
                    trainingPhase = "Aquecimento"
                )
            )
            splitUpper.contains("PULL") || splitUpper.contains("PUXAR") || splitUpper.contains("DORSO") || splitUpper.contains("COSTAS") -> listOf(
                WorkoutExercise(
                    exerciseId = "mobilidade_coluna",
                    name = "Aquecimento: Gato-Camelo",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Eretores de espinha e lombar",
                    sets = 2,
                    repsRange = "15 reps conscientes",
                    tempo = "3-2-3-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 4,
                    notes = "Trabalhe a flexão e extensão vertebral sem pressa para hidratar os discos.",
                    trainingPhase = "Aquecimento"
                )
            )
            else -> listOf( // Push/Peito/Ombro/Braços/All others
                WorkoutExercise(
                    exerciseId = "manguito_rotador",
                    name = "Aquecimento: Mobilidade de Ombros & Manguito",
                    muscleGroup = "Ombro",
                    targetMuscleDetail = "Manguito rotador e cápsulas articulares",
                    sets = 2,
                    repsRange = "15-20 ativações",
                    tempo = "2-0-2-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 5,
                    notes = "Faça movimentos rotacionais finos preventivos de impacto no ombro.",
                    trainingPhase = "Aquecimento"
                )
            )
        }

        val compounds: List<WorkoutExercise> = when {
            splitUpper.contains("AEROBICO") || splitUpper.contains("CARDIO") -> listOf(
                WorkoutExercise(
                    exerciseId = "hiit_treadmill",
                    name = "Corrida HIIT Catalisadora na Esteira",
                    muscleGroup = "Cardio",
                    targetMuscleDetail = "Capacidade de VO2 Máx e déficit calórico",
                    sets = defaultSets + 2,
                    repsRange = "30s veloz / 30s caminhando",
                    tempo = "Intervalado",
                    restSeconds = 0,
                    advancedTechnique = "HIIT",
                    intensityRPE = defaultRPE + 1,
                    notes = "Dê o máximo de si nos tiros de 30 segundos mantendo postura ereta.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "cardio_moderado",
                    name = "Elíptico ou Bicicleta de Alto Gasto",
                    muscleGroup = "Cardio",
                    targetMuscleDetail = "Zona de Fat-burn (Frequência média)",
                    sets = 1,
                    repsRange = "20-30 min contínuos",
                    tempo = "Estável",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Mantenha ritmo constante na faixa aeróbica de queima lipídica.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "abdominal_crunch",
                    name = "Abdominal infra pendurado",
                    muscleGroup = "Abdômen",
                    targetMuscleDetail = "Reto Abdominal Geral",
                    sets = defaultSets,
                    repsRange = "15-20 reps",
                    tempo = "2-1-2-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Flexione o quadril jogando os joelhos em direção ao peito com controle.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "abdominal_prancha",
                    name = "Prancha Isométrica Ativa",
                    muscleGroup = "Abdômen",
                    targetMuscleDetail = "Core e Transverso Abdominal",
                    sets = 3,
                    repsRange = "45s isometria",
                    tempo = "Estático",
                    restSeconds = rest,
                    advancedTechnique = "Isometria",
                    intensityRPE = defaultRPE,
                    notes = "Mantenha o glúteo contraído e abdômen cravado sem despencar o quadril.",
                    trainingPhase = "Principal"
                )
            )
            splitUpper.contains("LEGS") || splitUpper.contains("PERNA") || splitUpper.contains("GLÚTEO") -> listOf(
                WorkoutExercise(
                    exerciseId = "agachamento_livre",
                    name = if (isBeginner) "Agachamento no Smith (Guiado)" else "Agachamento Livre com Barra",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Quadríceps Geral e Glúteos",
                    sets = defaultSets,
                    repsRange = "8-12 reps controladas",
                    tempo = "4-1-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Aperte o abdômen (bracing) e desça mandando o quadril para trás.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "leg_press_45",
                    name = "Leg Press 45º Linear",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Quadríceps, Vasto Lateral, Glúteos",
                    sets = defaultSets,
                    repsRange = "10-15 reps",
                    tempo = "3-0-1-0",
                    restSeconds = rest,
                    advancedTechnique = defaultTechnique,
                    intensityRPE = defaultRPE,
                    notes = "Mantenha o quadril pressionado fortemente sobre o assentamento.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "levantamento_terra_stiff",
                    name = "Stiff com Halteres",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Posteriores de Coxa e Glúteo",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "4-0-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Alongue os posteriores mantendo a coluna reta e os joelhos semi-flexionados.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "afundo_halteres",
                    name = "Passada Caminhando com Halteres",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Quadríceps e Glúteo Médio",
                    sets = defaultSets,
                    repsRange = "10 passos por perna",
                    tempo = "2-0-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Mantenha o joelho alinhado com a ponta do pé na descida.",
                    trainingPhase = "Principal"
                )
            )
            splitUpper.contains("PULL") || splitUpper.contains("PUXAR") || splitUpper.contains("DORSO") || splitUpper.contains("COSTAS") -> listOf(
                WorkoutExercise(
                    exerciseId = "puxada_polia_alta",
                    name = "Puxada Pulley Aberta Frente",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Latíssimo do Dorso externo",
                    sets = defaultSets,
                    repsRange = "8-12 reps",
                    tempo = "3-0-1-1",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Direcione a barra no peito superior mantendo o tronco levemente inclinado.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "levantamento_terra",
                    name = if (isBeginner) "Dorsal Pulley Triangular" else "Levantamento Terra Clássico",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Cadeia posterior completa e eretores",
                    sets = defaultSets,
                    repsRange = "6-10 reps",
                    tempo = "2-1-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Saia empurrando o chão com as solas dos pés.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "remada_baixa_triangulo",
                    name = "Remada Baixa Polia com Triângulo",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Porção média do Dorso (Trapézio e Romboides)",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "3-0-1-1",
                    restSeconds = rest,
                    advancedTechnique = defaultTechnique,
                    intensityRPE = defaultRPE,
                    notes = "Esmague as escápulas no final e alongue completamente na ida.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "pull_down_corda",
                    name = "Pulldown com Corda no Cabo",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Latíssimo do Dorso Inferior",
                    sets = defaultSets,
                    repsRange = "12 reps",
                    tempo = "3-0-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Mantenha o braço semi-estendido e puxe levando as mãos ao quadril.",
                    trainingPhase = "Principal"
                )
            )
            splitUpper.contains("PUSH") || splitUpper.contains("EMPURRAR") || splitUpper.contains("PEITO") -> listOf(
                WorkoutExercise(
                    exerciseId = "supino_reto",
                    name = if (isBeginner) "Supino Vertical Máquina" else "Supino Reto com Barra",
                    muscleGroup = "Peito",
                    targetMuscleDetail = "Peitoral Maior Central",
                    sets = defaultSets,
                    repsRange = "8-12 reps",
                    tempo = "3-1-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Escápulas em adução no banco para proteger os ombros.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "supino_inclinado_halteres",
                    name = "Supino Inclinado com Halteres",
                    muscleGroup = "Peito",
                    targetMuscleDetail = "Fibras Claviculares Superiores",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "3-1-1-0",
                    restSeconds = rest,
                    advancedTechnique = defaultTechnique,
                    intensityRPE = defaultRPE,
                    notes = "Incline a 30 graus para concentrar no peito superior.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "desenvolvimento_halteres",
                    name = "Desenvolvimento de Ombros com Halteres",
                    muscleGroup = "Ombro",
                    targetMuscleDetail = "Deltoide Anterior",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "3-0-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Suba verticalmente de forma controlada.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "crucifixo_maquina",
                    name = "Voador Peitoral (Pec Deck)",
                    muscleGroup = "Peito",
                    targetMuscleDetail = "Fibras Internas do Peito",
                    sets = defaultSets,
                    repsRange = "12 reps",
                    tempo = "3-1-1-1",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Mantenha os cotovelos levemente flexionados e esmague na contração.",
                    trainingPhase = "Principal"
                )
            )
            splitUpper.contains("PONTO_FRACO") || splitUpper.contains("BRAÇO") || splitUpper.contains("OMBRO") -> listOf(
                WorkoutExercise(
                    exerciseId = "rosca_polia",
                    name = "Rosca Direta no Cabo Polia",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Bíceps Braquial",
                    sets = defaultSets,
                    repsRange = "8-12 reps",
                    tempo = "3-0-1-1",
                    restSeconds = rest,
                    advancedTechnique = defaultTechnique,
                    intensityRPE = defaultRPE,
                    notes = "Esmague o bíceps no topo por 1 segundo.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "triceps_testa",
                    name = "Tríceps Testa com Barra W",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Tríceps Cabeça Longa",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "3-1-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Mantenha os cotovelos paralelos apontados para cima.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "desenvolvimento_halteres",
                    name = "Desenvolvimento de Ombros Sentado",
                    muscleGroup = "Ombro",
                    targetMuscleDetail = "Deltoide Anterior",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "3-0-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Suba com força controlada.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "rosca_martelo",
                    name = "Rosca Martelo com Halteres",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Braquiorradial e Braquial",
                    sets = defaultSets,
                    repsRange = "12 reps",
                    tempo = "3-0-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Pegada neutra constante controlando a descida.",
                    trainingPhase = "Principal"
                )
            )
            else -> listOf( // Default / Misto / Generalized (combines peito, costas, pernas)
                WorkoutExercise(
                    exerciseId = "agachamento_livre",
                    name = if (isBeginner) "Agachamento na Barra Guiada" else "Agachamento Livre com Barra",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Quadríceps e Glúteos",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "4-1-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Desça com controle estabilizando o tronco.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "supino_reto",
                    name = if (isBeginner) "Supino Vertical Máquina" else "Supino Reto com Barra",
                    muscleGroup = "Peito",
                    targetMuscleDetail = "Peitoral Maior Central",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "3-1-1-0",
                    restSeconds = rest,
                    advancedTechnique = defaultTechnique,
                    intensityRPE = defaultRPE,
                    notes = "Ative adutores escapulares no banco constante.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "puxada_polia_alta",
                    name = "Puxada Pulley Frente",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Latíssimo do Dorso",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "3-0-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Direcione os cotovelos para baixo esmagando as costas.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "leg_press_45",
                    name = "Leg Press 45º Linear",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Quadríceps e Panturrilhas",
                    sets = defaultSets,
                    repsRange = "12 reps",
                    tempo = "3-0-1-0",
                    restSeconds = rest,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Mantenha calcanhares cravados na plataforma.",
                    trainingPhase = "Principal"
                )
            )
        }

        val accessories: List<WorkoutExercise> = when {
            splitUpper.contains("AEROBICO") || splitUpper.contains("CARDIO") -> listOf(
                WorkoutExercise(
                    exerciseId = "cardio_escada",
                    name = "Subida de Escadaria Cardio",
                    muscleGroup = "Cardio",
                    targetMuscleDetail = "Resistência cardiovascular",
                    sets = 1,
                    repsRange = "15 min",
                    tempo = "Livre",
                    restSeconds = 30,
                    advancedTechnique = "Aumento metabólico",
                    intensityRPE = defaultRPE,
                    notes = "Simule subida constante mantendo calcanhar apoiado.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "cardio_polichinelos",
                    name = "Polichinelos ou Saltos Ativos",
                    muscleGroup = "Cardio",
                    targetMuscleDetail = "Padrão pliométrico aeróbico",
                    sets = 3,
                    repsRange = "45 segundos",
                    tempo = "Rápido",
                    restSeconds = 30,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE - 1,
                    notes = "Mantenha o amortecimento dos joelhos em cada aterrissagem.",
                    trainingPhase = "Acessório"
                )
            )
            splitUpper.contains("LEGS") || splitUpper.contains("PERNA") || splitUpper.contains("GLÚTEO") -> listOf(
                WorkoutExercise(
                    exerciseId = "cadeira_extensora",
                    name = "Cadeira Extensora de Quadríceps",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Reto femoral e vastos anteriorizados",
                    sets = defaultSets,
                    repsRange = "12-15 reps + drop-set",
                    tempo = "3-1-1-1",
                    restSeconds = rest - 20,
                    advancedTechnique = if (isAdvanced) "Drop-set na última" else "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Ponto de pico no topo de contração por 1 segundo.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "mesa_flexora",
                    name = "Mesa Flexora Horizontal",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Bíceps femoral e isquiotibiais",
                    sets = defaultSets,
                    repsRange = "12 reps",
                    tempo = "3-0-1-1",
                    restSeconds = rest - 20,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Faça contração completa sem levantar o quadril da mesa.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "panturrilha_pe",
                    name = "Gêmeos em Pé (Panturrilhas)",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Gastrocnêmio",
                    sets = 4,
                    repsRange = "15-20 reps",
                    tempo = "3-1-1-1",
                    restSeconds = 45,
                    advancedTechnique = "Alongamento estático no final",
                    intensityRPE = defaultRPE,
                    notes = "Alongamento máximo embaixo e contração máxima em cima.",
                    trainingPhase = "Acessório"
                )
            )
            splitUpper.contains("PULL") || splitUpper.contains("PUXAR") || splitUpper.contains("DORSO") || splitUpper.contains("COSTAS") -> listOf(
                WorkoutExercise(
                    exerciseId = "rosca_polia",
                    name = "Rosca Direta Cabo Polia",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Bíceps Braquial cabeça curta",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "3-0-1-1",
                    restSeconds = 60,
                    advancedTechnique = if (isAdvanced) "Drop-set" else "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Evite roubar projetando os cotovelos para frente.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "rosca_martelo_alternada",
                    name = "Rosca Martelo Alternada",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Braquiorradial e antebraço",
                    sets = defaultSets,
                    repsRange = "12 reps",
                    tempo = "3-0-1-0",
                    restSeconds = 60,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Aperte forte os halteres ativando o antebraço.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "elevacao_escapular",
                    name = "Encolhimento de Ombros com Halteres",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Trapézio Superior",
                    sets = 3,
                    repsRange = "15 reps",
                    tempo = "2-0-1-1",
                    restSeconds = 60,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Suba os ombros em direção às orelhas sem girá-los.",
                    trainingPhase = "Acessório"
                )
            )
            splitUpper.contains("PUSH") || splitUpper.contains("EMPURRAR") || splitUpper.contains("PEITO") -> listOf(
                WorkoutExercise(
                    exerciseId = "elevacao_lateral",
                    name = "Elevação Lateral com Halteres",
                    muscleGroup = "Ombro",
                    targetMuscleDetail = "Deltoide Lateral fibra média",
                    sets = defaultSets,
                    repsRange = "12-15 reps",
                    tempo = "3-1-1-1",
                    restSeconds = 60,
                    advancedTechnique = if (isAdvanced) "Drop-set triplo" else "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Projete no plano escapular de forma constante.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "triceps_testa",
                    name = "Tríceps Testa no Cabo",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Tríceps medial e lateral",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "3-1-1-0",
                    restSeconds = 60,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Mantenha os cotovelos estáticos.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "triceps_pulley_barra",
                    name = "Tríceps Pulley com Barra Reta",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Tríceps Cabeça Lateral",
                    sets = defaultSets,
                    repsRange = "12 reps",
                    tempo = "3-0-1-0",
                    restSeconds = 60,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Esmague embaixo isolando perfeitamente o tríceps.",
                    trainingPhase = "Acessório"
                )
            )
            splitUpper.contains("PONTO_FRACO") || splitUpper.contains("BRAÇO") || splitUpper.contains("OMBRO") -> listOf(
                WorkoutExercise(
                    exerciseId = "triceps_pulley_barra",
                    name = "Tríceps Corda Pulley",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Tríceps Cabeça Lateral",
                    sets = defaultSets,
                    repsRange = "12 reps",
                    tempo = "3-0-1-1",
                    restSeconds = 60,
                    advancedTechnique = if (isAdvanced) "Drop-set" else "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Abra a corda no final da descida para contrair ao máximo.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "rosca_concentrada",
                    name = "Rosca Concentrada Sentado",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Bíceps Pico e Isolamento",
                    sets = defaultSets,
                    repsRange = "12 reps",
                    tempo = "3-1-1-1",
                    restSeconds = 60,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Apoie o tríceps na coxa e suba isolando o braço.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "elevacao_lateral",
                    name = "Elevação Lateral com Halteres",
                    muscleGroup = "Ombro",
                    targetMuscleDetail = "Deltoide Lateral",
                    sets = defaultSets,
                    repsRange = "15 reps",
                    tempo = "3-1-1-1",
                    restSeconds = 60,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Projete levemente os braços para frente na elevação.",
                    trainingPhase = "Acessório"
                )
            )
            else -> listOf( // Default / Misto
                WorkoutExercise(
                    exerciseId = "elevacao_lateral",
                    name = "Elevação Lateral com Halteres",
                    muscleGroup = "Ombro",
                    targetMuscleDetail = "Deltoide Lateral",
                    sets = defaultSets,
                    repsRange = "12-15 reps",
                    tempo = "3-1-1-1",
                    restSeconds = 60,
                    advancedTechnique = if (isAdvanced) "Drop-set" else "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Execute no plano das escápulas.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "rosca_polia",
                    name = "Rosca Direta Polia Alta",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Bíceps Geral",
                    sets = defaultSets,
                    repsRange = "10 reps",
                    tempo = "3-0-1-1",
                    restSeconds = 60,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Pegada firme controlando a volta excêntrica lenta.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "triceps_testa",
                    name = "Tríceps Testa Pulley",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Tríceps Cabeça Longa",
                    sets = defaultSets,
                    repsRange = "12 reps",
                    tempo = "3-0-1-0",
                    restSeconds = 60,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = defaultRPE,
                    notes = "Estenda os braços completamente mantendo os cotovelos travados.",
                    trainingPhase = "Acessório"
                )
            )
        }

        val stretchings: List<WorkoutExercise> = listOf(
            WorkoutExercise(
                exerciseId = "alongamento_articular",
                name = "Alongamento Soltura Descompressiva",
                muscleGroup = "Geral",
                targetMuscleDetail = "Fáscia muscular e articulações recrutadas",
                sets = 1,
                repsRange = "45s estático",
                tempo = "Estático",
                restSeconds = 30,
                advancedTechnique = "Fascial Stretch",
                intensityRPE = 5,
                notes = "Respire de maneira calma e profunda, gerando liberação ativa na grande fáscia e restaurando comprimento.",
                trainingPhase = "Alongamento"
            )
        )

        // 4. Assemble exact requested size based on 'limit' (5, 6, 7, 8)
        val resultList = mutableListOf<WorkoutExercise>()
        
        // 1 warmup (Aquecimento)
        resultList.add(warmups.first())

        // Calculate how many compound (Principal) & accessories (Acessório) to grab
        val principalCount = when (limit) {
            5 -> 2
            6 -> 3
            7 -> 3
            8 -> 4
            else -> 3
        }
        val acessorioCount = when (limit) {
            5 -> 1
            6 -> 1
            7 -> 2
            8 -> 2
            else -> 2
        }

        // Compounds
        for (i in 0 until principalCount) {
            val c = compounds.getOrNull(i) ?: compounds.first()
            resultList.add(c)
        }

        // Accessories
        for (i in 0 until acessorioCount) {
            val a = accessories.getOrNull(i) ?: accessories.first()
            resultList.add(a)
        }

        // 1 stretching (Alongamento)
        resultList.add(stretchings.first())

        // Safety slice/ensure exactly equals limit
        val finalExercises = resultList.take(limit)

        return GeneratedWorkout(title, splitType, focus, finalExercises)
    }
}

sealed class GeneratedWorkoutResult {
    data class Success(val workout: GeneratedWorkout, val isLocalFallback: Boolean) : GeneratedWorkoutResult()
    data class Error(val message: String) : GeneratedWorkoutResult()
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GeneratedWorkout(
    val title: String,
    val splitType: String,
    val focus: String,
    val exercises: List<WorkoutExercise>
)
