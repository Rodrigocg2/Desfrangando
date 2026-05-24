package com.example.data

import com.example.model.ExerciseExecutionReference
import com.example.model.SavedWorkout
import com.example.model.WorkoutHistory
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    val allSavedWorkouts: Flow<List<SavedWorkout>> = workoutDao.getAllSavedWorkouts()
    val workoutHistory: Flow<List<WorkoutHistory>> = workoutDao.getWorkoutHistory()
    val weightHistory: Flow<List<com.example.model.WeightHistory>> = workoutDao.getAllWeightHistory()

    suspend fun getSavedWorkoutById(id: String): SavedWorkout? {
        return workoutDao.getSavedWorkoutById(id)
    }

    suspend fun saveWorkout(workout: SavedWorkout) {
        workoutDao.insertSavedWorkout(workout)
    }

    suspend fun deleteWorkoutById(id: String) {
        workoutDao.deleteSavedWorkoutById(id)
    }

    suspend fun deleteAllWorkouts() {
        workoutDao.deleteAllSavedWorkouts()
    }

    suspend fun saveHistory(history: WorkoutHistory) {
         workoutDao.insertWorkoutHistory(history)
    }

    suspend fun deleteHistoryById(id: Long) {
         workoutDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
         workoutDao.clearAllHistory()
    }

    suspend fun saveWeight(weight: com.example.model.WeightHistory) {
         workoutDao.insertWeightHistory(weight)
    }

    suspend fun deleteWeightById(id: Long) {
         workoutDao.deleteWeightHistoryById(id)
    }

    // Static Pre-loaded high-intensity references (Videos are animated via Jetpack Compose canvases dynamically)
    val referenceExercises = listOf(
        ExerciseExecutionReference(
            id = "supino_reto",
            name = "Supino Reto com Barra",
            primaryMuscleCode = "CHEST",
            primaryMuscleName = "Peitoral Maior",
            executionDetails = listOf(
                "Deite no banco reto e posicione os olhos diretamente sob a barra armada.",
                "Retraia e deprima as escápulas fortemente contra o banco, mantendo os ombros encaixados.",
                "Segure a barra com uma largura ligeiramente maior que a dos ombros.",
                "Retire a barra do suporte e inicie a descida controlada em direção ao osso esterno (altura do mamilo).",
                "Empurre a barra verticalmente com foco em aproximar os bíceps na subida (adução), expandindo o peito no final."
            ),
            highPerformanceTips = listOf(
                "Mantenha os cotovelos rotacionados a 45-60 graus do corpo, nunca a 90 graus para preservar o manguito rotador.",
                "Aplique a técnica de 'leg drive': empurre os calcanhares no chão para estabilizar o quadril e gerar força cinética.",
                "Tempo Ideal: 3 segundos de descida controlada (fase excêntrica), pausa isométrica de 1 segundo embaixo antes de explodir para cima."
            ),
            biomechanicalTempo = "3-1-1-0",
            jointPathType = "CHEST_PRESS"
        ),
        ExerciseExecutionReference(
            id = "agachamento_livre",
            name = "Agachamento Livre com Barra",
            primaryMuscleCode = "QUAD",
            primaryMuscleName = "Quadríceps & Glúteos",
            executionDetails = listOf(
                "Posicione a barra confortavelmente sobre o trapézio, nunca nas vértebras cervicais.",
                "Remova a barra com os pés paralelos, dê um passo para trás e alinhe os pés apontados a 15-30° para fora.",
                "Contraia o abdômen profundamente (manobra de bracing) e inspire para inflar a cavidade abdominal.",
                "Agache jogando o quadril levemente para trás e flexionando os joelhos, direcionando-os para fora.",
                "Desça o máximo possível mantendo a coluna neutra e retorne empurrando o chão de forma explosiva."
            ),
            highPerformanceTips = listOf(
                "Não permita que os joelhos entrem para dentro durante a subida (valgo dinâmico); empurre-os ativamente para fora.",
                "Segure a barra com as mãos firmes e puxe-a contra as costas para ativar os dorsais e manter o tronco ereto.",
                "Sob carga extrema, não relaxe a musculatura abdominal em nenhum momento para proteger a lombar."
            ),
            biomechanicalTempo = "4-1-1-0",
            jointPathType = "SQUAT"
        ),
        ExerciseExecutionReference(
            id = "levantamento_terra",
            name = "Levantamento Terra (Deadlift)",
            primaryMuscleCode = "LOWER_BACK",
            primaryMuscleName = "Postertores, Lombar & Glúteos",
            executionDetails = listOf(
                "Aproxime-se da barra até que ela fique a cerca de 2-3 cm das suas canelas, dividindo o pé ao meio.",
                "Flexione os joelhos e o quadril, segurando a barra firme com costas retas.",
                "Force o peito para cima, ative as dorsais empurrando a barra contra suas canelas.",
                "Inicie a subida empurrando o chão com as pernas, mantendo a barra extremamente rente ao corpo.",
                "Estenda o quadril no topo contraindo fortemente os glúteos."
            ),
            highPerformanceTips = listOf(
                "O Levantamento Terra não é um movimento de braço; os braços servem apenas como cordas transferindo energia.",
                "Mantenha a barra colada na pele. Se a barra se afastar das canelas, a compressão lombar aumenta absurdamente.",
                "Tempo Ideal: Fase concêntrica explosiva, e descida controlada esticando os posteriores sob altíssima tensão."
            ),
            biomechanicalTempo = "2-1-1-0",
            jointPathType = "DEAD_LIFT"
        ),
        ExerciseExecutionReference(
            id = "puxada_polia_alta",
            name = "Puxada Aberta na Polia Alta",
            primaryMuscleCode = "LATS",
            primaryMuscleName = "Latíssimo do Dorso (Dorsal)",
            executionDetails = listOf(
                "Segure a barra com pegada pronada na curvatura externa e posicione as pernas sob o suporte limitador.",
                "Inicie puxando as escápulas para baixo (depressão escapular) antes de dobrar os braços.",
                "Puxe a barra em direção à parte superior do peito, jogando os cotovelos ativamente para baixo e para frente.",
                "Aperte as costas no ponto de esforço máximo, abrindo bem o tórax.",
                "Estenda os braços de maneira lenta e controlada, sentindo alongar a dorsal intensamente."
            ),
            highPerformanceTips = listOf(
                "Puxe com os cotovelos, não com os bíceps! Imagine que suas mãos são apenas ganchos ligados à barra.",
                "Incline ligeiramente o tronco para trás (no máximo 15 graus) apenas para dar passagem à barra e focar no miolo das costas.",
                "Segundos de excêntrica: alongue por no mínimo 3-4 segundos inteiros para maximizar a hipertrofia."
            ),
            biomechanicalTempo = "4-0-1-1",
            jointPathType = "LAT_PULLDOWN"
        ),
        ExerciseExecutionReference(
            id = "elevacao_lateral",
            name = "Elevação Lateral com Halteres",
            primaryMuscleCode = "SHOULDER",
            primaryMuscleName = "Deltoide Lateral",
            executionDetails = listOf(
                "Fique de pé com os halteres ao lado do corpo, segurando-os com pegada neutra.",
                "Incline levemente o tronco para a frente (cerca de 10 graus) para pré-estirar a fibra lateral.",
                "Eleve os braços para os lados em um arco, mantendo os cotovelos semidobrados na direção escapular (plano escapular).",
                "Suba as mãos até a altura dos ombros, garantindo que o cotovelo fique nivelado ou acima dos punhos.",
                "Mantenha a contração isométrica por 1 segundo e desça o peso lentamente segurando a gravidade."
            ),
            highPerformanceTips = listOf(
                "Não jogue o peso com balanço de tronco (roubo). Isole o deltoide focando em empurrar as mãos para os LADOS, não para cima.",
                "No topo do movimento, aponte levemente o polegar para baixo (sem rotacionar excessivamente) para máxima ativação da porção lateral.",
                "Tempo ideal de descida: 3 segundos mantendo tensão constante, nunca soltando o peso antes de encontrar as coxas."
            ),
            biomechanicalTempo = "3-1-1-1",
            jointPathType = "LATERAL_RAISE"
        ),
        ExerciseExecutionReference(
            id = "rosca_polia",
            name = "Rosca Direta no Cabo (Bíceps)",
            primaryMuscleCode = "BICEPS",
            primaryMuscleName = "Bíceps Braquial",
            executionDetails = listOf(
                "Ajuste a polia na altura mais baixa e utilize a barra reta ou barra W.",
                "Fique de pé, dê um passo para trás do cabo, mantenha os cotovelos ligeiramente à frente do corpo.",
                "Flexione os cotovelos puxando a barra em direção aos ombros enquanto expira.",
                "Pressione o bíceps ferozmente no topo do pico de contração.",
                "Retorne o cabo à posição inicial, estendendo quase por completo o cotovelo no ponto máximo de estiramento."
            ),
            highPerformanceTips = listOf(
                "A polia cria uma linha de tensão contínua que os halteres livres não fornecem. Aproveite o ponto neutro para esmagar as fibras no final.",
                "Não empurre os cotovelos para trás durante a contração; empurre ligeiramente para a frente para focar na cabeça longa do bíceps.",
                "Técnica de alto rendimento: aplique repetições parciais após atingir a falha concêntrica na amplitude completa."
            ),
            biomechanicalTempo = "3-0-1-1",
            jointPathType = "BICEPS_CURL"
        ),
        ExerciseExecutionReference(
            id = "triceps_testa",
            name = "Tríceps Testa na Polia ou com Halteres",
            primaryMuscleCode = "TRICEPS",
            primaryMuscleName = "Tríceps Braquial (Longa, Lateral & Medial)",
            executionDetails = listOf(
                "Deite em um banco plano segurando os halteres com os braços estendidos verticalmente.",
                "Incline os braços ligeiramente para trás (cerca de 15 a 20 graus) para criar pré-tensão na cabeça longa do tríceps.",
                "Mantendo os cotovelos apontados para o teto, flexione os braços descendo os halteres próximo à testa ou atrás da cabeça.",
                "Sinta o tríceps alongar em profundidade, mantendo os joelhos fixos.",
                "Estenda os braços empurrando os halteres de volta à inclinação de 15 graus inicial."
            ),
            highPerformanceTips = listOf(
                "Não abra os cotovelos para fora durante o movimento; mantenha-os apontando rigorosamente paralelos.",
                "A inclinação inicial de 15 graus mantém o tríceps sob tensão no ponto do topo, onde o supino normal relaxaria.",
                "Técnica avançada: ao falhar no testa, mude instantaneamente para supino fechado no mesmo banco para queimar com drop-sets mecânicos."
            ),
            biomechanicalTempo = "4-1-1-0",
            jointPathType = "TRICEPS_EXTENSION"
        ),
        ExerciseExecutionReference(
            id = "leg_press_45",
            name = "Leg Press 45 Graus",
            primaryMuscleCode = "QUAD",
            primaryMuscleName = "Quadríceps & Glúteos",
            executionDetails = listOf(
                "Sente no aparelho encostando totalmente o quadril e as costas contra o assento.",
                "Posicione os pés na largura dos ombros, a meia altura da plataforma móvel.",
                "Retire a trava de segurança e flexione os joelhos descendo a plataforma em direção ao peito com controle.",
                "Desça o máximo que conseguir sem permitir que sua região lombar ou glúteos se desprendam do encosto.",
                "Chute a plataforma de volta, aplicando força pelos calcanhares e mantendo os joelhos alinhados com a ponta dos pés."
            ),
            highPerformanceTips = listOf(
                "NUNCA estenda completamente os joelhos no final da subida (hiperextensão total). Pare a 95% do caminho para não carregar a articulação.",
                "Colocar os pés mais baixos na placa desloca a tensão para o quadríceps frontal; pés mais altosativam glúteos e posteriores.",
                "Para sobrecarga mecânica extrema, controle a descida lenta de 4 segundos inteiros para esgotar as reservas energéticas."
            ),
            biomechanicalTempo = "4-0-1-0",
            jointPathType = "LEG_PRESS"
        )
    )

    fun getExerciseReferenceById(id: String): ExerciseExecutionReference? {
        return referenceExercises.find { it.id == id }
    }
}
