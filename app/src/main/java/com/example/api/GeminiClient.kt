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
            Você é um Treinador Científico de Musculação de Alto Rendimento com PhD em Cinesiologia e treinador de atletas elite IFBB.
            Gere um programa de treinamento focado em alta intensidade mecânica, sobrecarga progressiva, controle de tempo (tempo sob tensão) e técnicas avançadas (como Rest-Pause, Drop-sets, Myo-reps, SST, Cluster Sets).
            Você DEVE responder EXCLUSIVAMENTE com um objeto JSON válido, sem tags markdown, sem comentários, contendo exatamente a estrutura descrita.
        """.trimIndent()

        val prompt = """
            Gere um treino de Alto Rendimento com os seguintes parâmetros:
            - Divisão / Grupo de Foco: $splitType (Exemplos: Tração/Empurrão/Pernas, Arnold Split, Ombros e Braços, Peito e Costas, Misto, Superior/Inferior)
            - Treinos ao dia: $workoutsPerDay vez(es) ao dia
            - Dias de treino na semana: $workoutsPerWeek dias por semana
            - Objetivo Principal: $focus (Exemplos: Hipertrofia Miofibrilar, Força Máxima RPE10, Densidade Sarcoplasmática)
            - Informações adicionais / Notas: $specialNotes
            - Nível de Experiência: $experienceLevel (Elite, Avançado, com tantos anos de experiência informados)

            DIRETRIZ DE DIVISÃO MUSCULAR:
            - Se a Divisão for ABC_DENSIDADE (Musculação: Foco na Densidade): Crie uma rotina com cargas pesadas, mantendo a execução correta para estimular a retenção/aumento de massa muscular enquanto reduz gordura. Use variações de 3 a 4 séries de 8 a 12 repetições. Adicione técnicas de intensidade como Drop-sets ou Supersets ao final para otimizar o gasto calórico.
            - Se a Divisão for ABC_AEROBICO (Aeróbico Catalisador): Crie uma rotina combinando exercícios cardiovasculares HIIT (treino intervalado de alta intensidade) e Cardio Moderado (caminhada rápida, transport, bicicleta de 30 a 40 minutos) focado em criar déficit calórico para maximizar queima de gordura e acelerar o metabolismo.
            - Se o usuário selecionar ou for treinar 3 vezes na semana (workoutsPerWeek = 3), crie um maravilhoso misto de membros Superiores e Inferiores (Superior/Inferior), ou uma estratégia de um dia para cada membro/grupo muscular em rotação (Push/Pull/Legs).
            - Se Divisão for MISTO_PERNA_BRACO_PEITO, misture de forma equilibrada exercícios focados em Pernas (quadriceps/posterior), Braços (biceps/triceps) e Peito (peitoral).
            - Se Divisão for MISTO_SUP_INF, misture exercícios de Membros Superiores (Dorso/Peito/Ombro) e Inferiores (Pernas).

            O treino gerado DEVE conter obrigatoriamente um ciclo completo e estruturado nas seguintes 4 fases organizadas em ordem cronológica de execução:
            1. No mínimo 1 exercício de "Aquecimento" focado em flexibilidade dinâmica, mobilidade articular ou ativação neural inicial.
            2. De 2 a 3 exercícios "Principal" multicompostos de sobrecarga progressiva pesada.
            3. De 1 a 2 exercícios "Acessório" de isolamento, foco de compressão e estresse metabólico.
            4. No mínimo 1 exercício de "Alongamento" focado em descompressão articular, flexibilidade passiva ou relaxamento miofascial.

            Retorne no seguinte formato JSON estrito:
            {
               "title": "Nome imponente do Treino de Alto Rendimento",
               "splitType": "$splitType",
               "focus": "$focus",
               "exercises": [
                  {
                     "exerciseId": "identificador_unico_minusculo (ex: supino_reto, agachamento_livre, levantamento_terra, puxada_polia_alta, elevacao_lateral, rosca_polia, triceps_testa, ou outro nome correspondente)",
                     "name": "Nome do exercício em Português",
                     "muscleGroup": "Grupo muscular principal (Peito, Dorso, Pernas, Ombros, Braços)",
                     "targetMuscleDetail": "Qual porção muscular específica foca (ex: Cabeça clavicular do peitoral, Cabeça lateral do tríceps)",
                     "sets": 4,
                     "repsRange": "Faixa de repetições e intensidade de alto nível (ex: 8-10 + 1 drop-set até a falha rpe 10, ou 4x Rest-pause 6+4+3)",
                     "tempo": "Tempo biomecânico sob tensão em 4 dígitos (ex: 4-0-1-0 para 4s descida excêntrica lenta, 1s contração explosiva)",
                     "restSeconds": 120,
                     "advancedTechnique": "Técnica avançada usada (ex: Rest-Pause, Drop-set, Myo-reps, Isometria no Pico, SST, Nenhuma)",
                     "intensityRPE": 9,
                     "notes": "Dica avançada de execução fisiológica para ativar mais fibras musculares e prevenir lesões",
                     "trainingPhase": "Aquecimento" (ou "Principal", "Acessório", "Alongamento")
                  }
               ]
            }
            Escreva o JSON limpo, sem quebras de linha defeituosas e sem bloco de código markdown (```json). Se o exercício não for um dos 8 conhecidos (supino_reto, agachamento_livre, levantamento_terra, puxada_polia_alta, elevacao_lateral, rosca_polia, triceps_testa, leg_press_45), você pode inventar seu id e nós geraremos uma animação cinesiológica elegante adaptada!
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
        val exercises = when {
            splitType.uppercase().contains("MISTO_PERNA_BRACO_PEITO") || (splitType.uppercase().contains("PERNA") && splitType.uppercase().contains("BRAÇO")) -> listOf(
                WorkoutExercise(
                    exerciseId = "manguito_rotador",
                    name = "Aquecimento: Ativação Articular Mista",
                    muscleGroup = "Ombro",
                    targetMuscleDetail = "Manguito e Quadril",
                    sets = 2,
                    repsRange = "15 reps",
                    tempo = "2-0-2-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 5,
                    notes = "Ativação geral dos ombros e quadris para o treino misto explosivo.",
                    trainingPhase = "Aquecimento"
                ),
                WorkoutExercise(
                    exerciseId = "supino_reto",
                    name = "Supino Reto com Barra",
                    muscleGroup = "Peito",
                    targetMuscleDetail = "Peitoral Maior",
                    sets = 4,
                    repsRange = "8-10 reps",
                    tempo = "3-1-1-0",
                    restSeconds = 90,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 8,
                    notes = "Mantenha a base cravada e execute a descida até o osso esterno.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "agachamento_livre",
                    name = "Agachamento com Barra Livre",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Quadríceps e Glúteos",
                    sets = 4,
                    repsRange = "10-12 reps",
                    tempo = "4-1-1-0",
                    restSeconds = 90,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 9,
                    notes = "Manobra de bracing ativa. Quadril neutro.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "rosca_polia",
                    name = "Rosca Direta no Cabo",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Bíceps Braquial",
                    sets = 3,
                    repsRange = "12 reps",
                    tempo = "3-0-1-1",
                    restSeconds = 60,
                    advancedTechnique = "Isometria 2s no topo",
                    intensityRPE = 9,
                    notes = "Aperte o bíceps ferozmente no pico da contração muscular.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "triceps_testa",
                    name = "Tríceps Testa na Polia",
                    muscleGroup = "Braços",
                    targetMuscleDetail = "Tríceps Cabeça Longa",
                    sets = 3,
                    repsRange = "12 reps",
                    tempo = "3-1-1-0",
                    restSeconds = 60,
                    advancedTechnique = "Drop-set na última",
                    intensityRPE = 8,
                    notes = "Cotovelos paralelos para isolar o tríceps.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "alongamento_peitoral",
                    name = "Alongamento: Soltura Geral",
                    muscleGroup = "Peito",
                    targetMuscleDetail = "Fáscia Peitoral",
                    sets = 1,
                    repsRange = "45s estático",
                    tempo = "Estático",
                    restSeconds = 30,
                    advancedTechnique = "Fascial Stretch",
                    intensityRPE = 5,
                    notes = "Respire de forma profunda destravando a fáscia do peito e braço.",
                    trainingPhase = "Alongamento"
                )
            )

            splitType.uppercase().contains("MISTO_SUP_INF") || splitType.uppercase().contains("UPPER_LOWER") || (workoutsPerWeek == 3) -> listOf(
                WorkoutExercise(
                    exerciseId = "mobilidade_quadril",
                    name = "Aquecimento: Mobilidade Escapular e Quadril",
                    muscleGroup = "Geral",
                    targetMuscleDetail = "Articulações de Ombros e Joelhos",
                    sets = 2,
                    repsRange = "12 reps",
                    tempo = "2-2-2-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 4,
                    notes = "Prepare articulações do corpo todo para uma sessão híbrida de superior e inferior.",
                    trainingPhase = "Aquecimento"
                ),
                WorkoutExercise(
                    exerciseId = "supino_reto",
                    name = "Supino Reto Convergente",
                    muscleGroup = "Peito",
                    targetMuscleDetail = "Fibras médias do peito",
                    sets = 4,
                    repsRange = "8, 8, 10, 10",
                    tempo = "3-0-1-0",
                    restSeconds = 90,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 8,
                    notes = "Empurre horizontalmente com força constante.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "puxada_polia_alta",
                    name = "Puxada Aberta na Polia",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Latíssimo do Dorso",
                    sets = 4,
                    repsRange = "8, 10, 10, 12",
                    tempo = "4-0-1-1",
                    restSeconds = 90,
                    advancedTechnique = "Isometria no final",
                    intensityRPE = 8,
                    notes = "Tracionar os cotovelos verticalmente ativando toda a asa dorsal.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "leg_press_45",
                    name = "Leg Press 45º Integrado",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Quadríceps e Glúteos",
                    sets = 4,
                    repsRange = "12, 12, 15, 15",
                    tempo = "4-0-1-0",
                    restSeconds = 90,
                    advancedTechnique = "Rest-Pause",
                    intensityRPE = 9,
                    notes = "Mantenha o glúteo cravado na base para proteger a região lombar.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "alongamento_costas",
                    name = "Alongamento: Descompressão Completa",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Grande Dorso e Posteriores",
                    sets = 2,
                    repsRange = "Isometria de 45s",
                    tempo = "Estático",
                    restSeconds = 30,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 5,
                    notes = "Segure na barra travando as escápulas e relaxando o quadril.",
                    trainingPhase = "Alongamento"
                )
            )

            splitType.uppercase().contains("ABC_DENSIDADE") -> listOf(
                WorkoutExercise(
                    exerciseId = "manguito_rotador",
                    name = "Aquecimento: Mobilidade Dinâmica Geral",
                    muscleGroup = "Ombro/Geral",
                    targetMuscleDetail = "Cápsula articular e manguito rotador",
                    sets = 2,
                    repsRange = "15-20 reps lentas",
                    tempo = "2-0-2-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 5,
                    notes = "Ative as articulações dos ombros e quadríceps de forma controlada.",
                    trainingPhase = "Aquecimento"
                ),
                WorkoutExercise(
                    exerciseId = "agachamento_livre",
                    name = "Agachamento Livre (Foco Densidade)",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Quadríceps e Glúteos",
                    sets = 4,
                    repsRange = "8-12 reps (Sobrecarga Progressiva)",
                    tempo = "3-1-1-0",
                    restSeconds = 90,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 9,
                    notes = "Use cargas pesadas mantendo a forma impecável para sinalizar a preservação da massa muscular.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "supino_reto",
                    name = "Supino Reto com Barra",
                    muscleGroup = "Peito",
                    targetMuscleDetail = "Peitoral Maior",
                    sets = 4,
                    repsRange = "8-12 reps",
                    tempo = "3-1-1-0",
                    restSeconds = 90,
                    advancedTechnique = "Supersérie com Crucifixo",
                    intensityRPE = 9,
                    notes = "Intensidade mecânica altíssima para estimular síntese de proteínas e queima calórica residual.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "puxada_polia_alta",
                    name = "Puxada Pulley Frente (Polia Alta)",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Latíssimo do Dorso",
                    sets = 4,
                    repsRange = "8-10 + 1 drop-set até a falha",
                    tempo = "3-0-1-1",
                    restSeconds = 90,
                    advancedTechnique = "Drop-set na última série",
                    intensityRPE = 10,
                    notes = "No final da última série, reduza a carga em 30% e faça o máximo de repetições possíveis até a exaustão.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "alongamento_peitoral",
                    name = "Alongamento e Liberação Escapular",
                    muscleGroup = "Peito/Dorso",
                    targetMuscleDetail = "Fáscia Peitorial e Grande Dorso",
                    sets = 1,
                    repsRange = "45s estático",
                    tempo = "Estático",
                    restSeconds = 30,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 5,
                    notes = "Alongue os grupos musculares recrutados para acelerar a regeneração miofibrilar.",
                    trainingPhase = "Alongamento"
                )
            )

            splitType.uppercase().contains("ABC_AEROBICO") -> listOf(
                WorkoutExercise(
                    exerciseId = "esteira_aerobico",
                    name = "Aquecimento: Caminhada Progressiva",
                    muscleGroup = "Cardio",
                    targetMuscleDetail = "Sistema Cardiorrespiratório",
                    sets = 1,
                    repsRange = "5 minutos",
                    tempo = "Velocidade 5.5",
                    restSeconds = 30,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 4,
                    notes = "Comece de forma moderada para elevar gradualmente a frequência cardíaca.",
                    trainingPhase = "Aquecimento"
                ),
                WorkoutExercise(
                    exerciseId = "hiit_treadmill",
                    name = "HIIT na Esteira (Catalisador)",
                    muscleGroup = "Cardio",
                    targetMuscleDetail = "Capacidade de VO2 Máx e Gasto Calórico",
                    sets = 10,
                    repsRange = "30s veloz (RPE 9-10) / 30s lento (RPE 4-5)",
                    tempo = "Intervalado",
                    restSeconds = 0,
                    advancedTechnique = "Intervalado de Alta Intensidade",
                    intensityRPE = 10,
                    notes = "Corra em velocidade elevada (ex: 14km/h) por 30s, e descanse andando por 30s. Repita por 10 ciclos para acelerar o metabolismo.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "cardio_moderado",
                    name = "Cardio Moderado Consistente",
                    muscleGroup = "Cardio",
                    targetMuscleDetail = "Bicicleta Estacionária ou Elíptico",
                    sets = 1,
                    repsRange = "30-40 minutos contínuos",
                    tempo = "Consistente",
                    restSeconds = 60,
                    advancedTechnique = "Foco em Queima de Gordura (Zona Fat-Burn)",
                    intensityRPE = 7,
                    notes = "Mantenha uma frequência cardíaca estável (entre 60% e 70% da FC máx) para maximizar o consumo de gordura.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "alongamento_articular",
                    name = "Alongamento de Membros Inferiores",
                    muscleGroup = "Geral",
                    targetMuscleDetail = "Isquiotibiais, Quadríceps e Panturrilhas",
                    sets = 2,
                    repsRange = "45s estático",
                    tempo = "Estático",
                    restSeconds = 30,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 5,
                    notes = "Acalme a respiração e relaxe os músculos do quadril e pernas.",
                    trainingPhase = "Alongamento"
                )
            )

            splitType.uppercase().contains("PPL_PUSH") || splitType.uppercase().contains("EMPURRAR") || splitType.uppercase().contains("PUSH") -> listOf(
                WorkoutExercise(
                    exerciseId = "manguito_rotador",
                    name = "Aquecimento: Mobilidade de Ombros e Manguito",
                    muscleGroup = "Ombro",
                    targetMuscleDetail = "Manguito Rotador e Cápsula Articular",
                    sets = 2,
                    repsRange = "15-20 (Ativação Leve)",
                    tempo = "2-0-2-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 5,
                    notes = "Movimentos controlados e sem carga excessiva para lubrificação articular e proteção dos ombros contra lesões.",
                    trainingPhase = "Aquecimento"
                ),
                WorkoutExercise(
                    exerciseId = "supino_reto",
                    name = "Supino Reto com Barra",
                    muscleGroup = "Peito",
                    targetMuscleDetail = "Peitoral Maior (Fibras médias e inferiores)",
                    sets = 4,
                    repsRange = "6, 8, 8, 10 (Séries de sobrecarga progressiva)",
                    tempo = "3-1-1-0",
                    restSeconds = 120,
                    advancedTechnique = "Rest-Pause na última série (falha + 15s + falha)",
                    intensityRPE = 9,
                    notes = "Com o 'leg drive' forte, mantenha os calcanhares cravados e o glúteo colado no banco para estabilidade total.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "elevacao_lateral",
                    name = "Elevação Lateral com Halteres",
                    muscleGroup = "Ombro",
                    targetMuscleDetail = "Fibras laterais do Deltoide e plano escapular",
                    sets = 4,
                    repsRange = "10, 12, 12, 15 (Séries tencionais)",
                    tempo = "3-1-1-1",
                    restSeconds = 90,
                    advancedTechnique = "Drop-set triplo na última série (reduzir 30% em duas etapas)",
                    intensityRPE = 10,
                    notes = "Arqueie as escápulas levemente para a frente e empurre o peso no plano escapular para evitar tendinite de impacto.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "alongamento_peitoral",
                    name = "Alongamento: Descompressão e Expansão Peitoral",
                    muscleGroup = "Peito",
                    targetMuscleDetail = "Fáscia do Peitoral Maior e Bíceps",
                    sets = 2,
                    repsRange = "Isometria de 30s a 45s",
                    tempo = "30s Estático",
                    restSeconds = 30,
                    advancedTechnique = "Fascial Stretch",
                    intensityRPE = 6,
                    notes = "Segure em uma coluna estável e projete o quadril/tronco para a frente de modo a alongar a fáscia e restaurar o comprimento do peito.",
                    trainingPhase = "Alongamento"
                )
            )
            splitType.uppercase().contains("PPL_PULL") || splitType.uppercase().contains("PUXAR") || splitType.uppercase().contains("PULL") -> listOf(
                WorkoutExercise(
                    exerciseId = "mobilidade_coluna",
                    name = "Aquecimento: Mobilidade Torácica e Gato-Camelo",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Eretores de espinha e mobilidade vertebral",
                    sets = 2,
                    repsRange = "12-15 reps lentas",
                    tempo = "3-2-3-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 4,
                    notes = "Arqueie e estenda a coluna de maneira suave para lubrificar as vértebras e preparar a lombar para as cargas.",
                    trainingPhase = "Aquecimento"
                ),
                WorkoutExercise(
                    exerciseId = "levantamento_terra",
                    name = "Levantamento Terra (Deadlift)",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Cadeia posterior completa, Lombar, Eretor de Espinha",
                    sets = 3,
                    repsRange = "5, 5, 8 (Foco força explosiva)",
                    tempo = "2-1-1-0",
                    restSeconds = 180,
                    advancedTechnique = "Isometria de 2s no topo na última repetição",
                    intensityRPE = 9,
                    notes = "Trave o abdômen e prepare o quadril. A força sai no chute das solas dos pés contra a terra firme.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "puxada_polia_alta",
                    name = "Puxada Aberta na Polia Alta",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Latíssimo do Dorso externo (Largura das Costas)",
                    sets = 4,
                    repsRange = "8, 10, 10, 12",
                    tempo = "4-0-1-1",
                    restSeconds = 90,
                    advancedTechnique = "Isometria de contração de 1s embaixo da polia em todas reps",
                    intensityRPE = 9,
                    notes = "Mentalize a contração da asa do dorso. Tracione os cotovelos no bolso de trás da calça.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "alongamento_costas",
                    name = "Alongamento: Descompressão da Coluna e Grande Dorso",
                    muscleGroup = "Dorso",
                    targetMuscleDetail = "Latíssimo do Dorso e fáscia toracolombar",
                    sets = 2,
                    repsRange = "Isometria de 40s",
                    tempo = "40s Estático",
                    restSeconds = 30,
                    advancedTechnique = "Descompressão Pendular",
                    intensityRPE = 5,
                    notes = "Segure em uma barra fixa alta, relaxe completamente as escápulas e o quadril, deixando a gravidade descomprimir a lombar e o dorso.",
                    trainingPhase = "Alongamento"
                )
            )
            else -> listOf( // Legs or Default Elite ABC
                WorkoutExercise(
                    exerciseId = "mobilidade_quadril",
                    name = "Aquecimento: Mobilidade de Quadril e Tornozelo",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Articulações Coxofemoral e Tibiotársica",
                    sets = 2,
                    repsRange = "12 reps por lado",
                    tempo = "2-2-2-0",
                    restSeconds = 45,
                    advancedTechnique = "Nenhuma",
                    intensityRPE = 4,
                    notes = "Faça agachamentos profundos sem peso segurando no suporte para soltar as articulações coxofemoral e tornozelo.",
                    trainingPhase = "Aquecimento"
                ),
                WorkoutExercise(
                    exerciseId = "agachamento_livre",
                    name = "Agachamento Livre com Barra",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Quadríceps, Vasto Lateral, Glúteo Maior",
                    sets = 4,
                    repsRange = "6, 8, 8, 10 (Pirâmide crescente de carga)",
                    tempo = "4-1-1-0",
                    restSeconds = 150,
                    advancedTechnique = "Nenhuma (Foco absoluto em sobrecarga progressiva)",
                    intensityRPE = 9,
                    notes = "Manobra de bracing impecável antes de iniciar a descida. Desça esticando bem as articulações.",
                    trainingPhase = "Principal"
                ),
                WorkoutExercise(
                    exerciseId = "leg_press_45",
                    name = "Leg Press 45 Graus",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Quadríceps e Glúteos (Volume tensional)",
                    sets = 4,
                    repsRange = "12, 12, 15, 20 (Alto volume de lactato)",
                    tempo = "4-0-1-0",
                    restSeconds = 120,
                    advancedTechnique = "Rest-Pause (20 reps + 15s descanso + 6 reps)",
                    intensityRPE = 10,
                    notes = "Mantenha o quadril pressionado fortemente sobre o assentamento amortecido para não retroverter a bacia.",
                    trainingPhase = "Acessório"
                ),
                WorkoutExercise(
                    exerciseId = "alongamento_pernas",
                    name = "Alongamento: Descompressão Cadeia Posterior e Glúteo",
                    muscleGroup = "Pernas",
                    targetMuscleDetail = "Hamstrings (Isquiotibiais) e Glúteos",
                    sets = 2,
                    repsRange = "Isometria de 45s",
                    tempo = "45s Estático",
                    restSeconds = 30,
                    advancedTechnique = "Estático Passivo",
                    intensityRPE = 5,
                    notes = "Incline as costas para frente em direção aos pés de forma controlada para relaxar os isquiotibiais sobrecarregados e aliviar os tendões.",
                    trainingPhase = "Alongamento"
                )
            )
        }

        return GeneratedWorkout(title, splitType, focus, exercises)
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
