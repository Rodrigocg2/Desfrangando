package com.example.data

import com.example.model.ExerciseExecutionReference

val EXERCISES_DATABASE = listOf(
    // PEITO
    // Supino
    ExerciseExecutionReference(
        id = "peito_supino_reto_barra", name = "Supino Reto com Barra", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "Tríceps, Deltoide Anterior", equipment = "Barra", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Deite com as costas planas no banco", "Segure a barra um pouco mais largo que os ombros", "Desça a barra até a barriga/peito inferior", "Empurre de volta à posição inicial"), highPerformanceTips = listOf("Mantenha os cotovelos a 45 graus", "Use o leg drive"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_supino_reto_halter", name = "Supino Reto com Halteres", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "Tríceps, Deltoide Anterior", equipment = "Halter", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Deite no banco e posicione os halteres ao lado do peito", "Empurre os halteres para cima juntando levemente no topo", "Retorne de forma controlada"), highPerformanceTips = listOf("Alongamento máximo na descida"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_supino_reto_smith", name = "Supino Reto no Smith", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "Tríceps", equipment = "Smith", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Ajuste o banco no Smith", "Realize o movimento de empurrar guiado"), highPerformanceTips = listOf("Foque na contração pura"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_supino_reto_maq", name = "Supino Reto na Máquina", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "Tríceps", equipment = "Máquina", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Sente-se na máquina", "Empurre as alavancas à frente"), highPerformanceTips = listOf("Apenas foque em apertar o peito"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_supino_incl_barra", name = "Supino Inclinado com Barra", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Superior", secondaryMuscleName = "Tríceps, Ombro", equipment = "Barra", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Ajuste o banco a 30-45 graus", "Desça a barra no peito superior"), highPerformanceTips = listOf("Não exagere na carga para proteger os ombros"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_supino_incl_halter", name = "Supino Inclinado com Halteres", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Superior", secondaryMuscleName = "Tríceps", equipment = "Halter", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Banco inclinado", "Movimento com amplitude máxima"), highPerformanceTips = listOf("Rotação leve para maior conforto articular"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_supino_incl_smith", name = "Supino Inclinado no Smith", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Superior", secondaryMuscleName = "Tríceps", equipment = "Smith", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("No smith", "Foco no peitoral superior"), highPerformanceTips = listOf("Movimento controlado"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_supino_decl_barra", name = "Supino Declinado com Barra", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Inferior", secondaryMuscleName = "Tríceps", equipment = "Barra", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Banco declinado", "Barra na altura do baixo peito"), highPerformanceTips = listOf("Bom para desenvolver a linha inferior do peito"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_supino_decl_halter", name = "Supino Declinado com Halteres", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Inferior", secondaryMuscleName = "Tríceps", equipment = "Halter", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Banco declinado", "Controle a estabilidade"), highPerformanceTips = listOf("Maior amplitude que a barra"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_supino_articulado", name = "Supino Articulado", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "Tríceps", equipment = "Máquina", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Sentado na máquina articulada", "Empurre ativando o peito"), highPerformanceTips = listOf("Permite grandes cargas com segurança"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_supino_convergente", name = "Supino Convergente", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Superior", secondaryMuscleName = "Tríceps", equipment = "Máquina", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Empurre unindo as mãos no topo"), highPerformanceTips = listOf("Máximo de adução"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    
    // Crucifixo
    ExerciseExecutionReference(
        id = "peito_crucifixo_reto_halter", name = "Crucifixo Reto com Halteres", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "Deltóide Anterior", equipment = "Halter", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Braços semiflexionados", "Abertura até sentir alongar", "Feche ativando o peito"), highPerformanceTips = listOf("Não transforme em um supino", "Abrace a árvore"), biomechanicalTempo = "3-1-2-1", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_crucifixo_incl_halter", name = "Crucifixo Inclinado com Halteres", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Superior", secondaryMuscleName = "Deltóide Anterior", equipment = "Halter", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Banco inclinado", "Mesmo processo do crucifixo reto"), highPerformanceTips = listOf("Estiramento poderoso"), biomechanicalTempo = "3-1-2-1", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_crucifixo_maquina", name = "Crucifixo na Máquina", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Sentado e braços nos suportes", "Aperte os braços ao centro"), highPerformanceTips = listOf("Tensão constante"), biomechanicalTempo = "3-1-2-1", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_crucifixo_cabo", name = "Crucifixo no Cabo", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "", equipment = "Cabo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Cabos na polia", "Contração isométrica no meio"), highPerformanceTips = listOf("Pico de contração fenomenal"), biomechanicalTempo = "3-1-2-1", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_peck_deck", name = "Peck Deck", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Apoie bem os cotovelos", "Feche o peitoral"), highPerformanceTips = listOf("Ótimo para finalização de treino"), biomechanicalTempo = "3-1-2-1", jointPathType = "CHEST_PRESS"
    ),
    
    // Cross Over
    ExerciseExecutionReference(
        id = "peito_cross_alta", name = "Cross Over Polia Alta", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Inferior", secondaryMuscleName = "", equipment = "Cabo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Polias no alto", "Puxe para baixo e para o centro"), highPerformanceTips = listOf("Cruze os braços no fim para mais adução"), biomechanicalTempo = "3-1-2-1", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_cross_baixa", name = "Cross Over Polia Baixa", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Superior", secondaryMuscleName = "", equipment = "Cabo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Polias embaixo", "Eleve os puxadores unidos em cima do peito"), highPerformanceTips = listOf("Foco esmagar a porção superior"), biomechanicalTempo = "3-1-2-1", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_cross_media", name = "Cross Over Polia Média", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "", equipment = "Cabo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Polias alinhadas aos ombros", "Adução horizontal clássica"), highPerformanceTips = listOf("Tensão mecânica brutal no pico"), biomechanicalTempo = "3-1-2-1", jointPathType = "CHEST_PRESS"
    ),
    
    // Flexões
    ExerciseExecutionReference(
        id = "peito_flexao_tradic", name = "Flexão Tradicional", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "Tríceps, Core", equipment = "Peso do Corpo", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("No solo", "Mãos na largura dos ombros", "Corpo em prancha"), highPerformanceTips = listOf("Trave o abdômen", "Não deixe o quadril cair"), biomechanicalTempo = "2-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_flexao_inclinada", name = "Flexão Inclinada", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Inferior", secondaryMuscleName = "Tríceps", equipment = "Peso do Corpo", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Mãos em apoio elevado", "Execução padrão form prancha"), highPerformanceTips = listOf("Mais fácil, foca na base do peito"), biomechanicalTempo = "2-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_flexao_declinada", name = "Flexão Declinada", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Superior", secondaryMuscleName = "Ombro, Tríceps", equipment = "Peso do Corpo", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Pés em banco e mãos no solo", "Desça controlado"), highPerformanceTips = listOf("Mais peso nos ombros e região clavicular"), biomechanicalTempo = "2-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_flexao_fechada", name = "Flexão Fechada (Diamante)", primaryMuscleCode = "CHEST", primaryMuscleName = "Tríceps / Peitoral Miolo", secondaryMuscleName = "Deltóide", equipment = "Peso do Corpo", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Mãos unidas formando um diamante"), highPerformanceTips = listOf("Cotovelos rentes ao corpo"), biomechanicalTempo = "2-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "peito_flexao_explosiva", name = "Flexão Explosiva (Bater Palmas)", primaryMuscleCode = "CHEST", primaryMuscleName = "Peitoral Maior", secondaryMuscleName = "Fibras de contração rápida", equipment = "Peso do Corpo", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Desça e exploda empurrando o chão", "Bata palmas no ar"), highPerformanceTips = listOf("Foco em potência máxima"), biomechanicalTempo = "1-0-X-0", jointPathType = "CHEST_PRESS"
    ),

    // COSTAS
    // Remadas
    ExerciseExecutionReference(
        id = "costas_remada_curvada_barra", name = "Remada Curvada com Barra", primaryMuscleCode = "LATS", primaryMuscleName = "Costas e Dorsal", secondaryMuscleName = "Bíceps, Lombar", equipment = "Barra", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Tronco inclinado à frente", "Puxe a barra em direção ao umbigo"), highPerformanceTips = listOf("Costas neutras, sem arredondar a lombar"), biomechanicalTempo = "2-1-1-1", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "costas_remada_curvada_pronada", name = "Remada Curvada Pegada Pronada", primaryMuscleCode = "LATS", primaryMuscleName = "Dorsal e Romboides", secondaryMuscleName = "Bíceps", equipment = "Barra", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Pegada com as palmas para baixo", "Maior foco em superior de costas"), highPerformanceTips = listOf("Cotovelos mais abertos"), biomechanicalTempo = "2-1-1-1", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "costas_remada_curvada_supinada", name = "Remada Curvada Pegada Supinada", primaryMuscleCode = "LATS", primaryMuscleName = "Latíssimo Inferior", secondaryMuscleName = "Bíceps", equipment = "Barra", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Palmas para cima", "Cotovelos colados ao corpo"), highPerformanceTips = listOf("Ativa mais o bíceps durante a remada"), biomechanicalTempo = "2-1-1-1", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "costas_remada_cavalinho", name = "Remada Cavalinho (T-Bar row)", primaryMuscleCode = "LATS", primaryMuscleName = "Miolos do Dorso", secondaryMuscleName = "Bíceps", equipment = "Máquina", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Use um puxador em triângulo", "Puxe o peso em direção ao peito"), highPerformanceTips = listOf("Sinta o alongamento da dorsal na descida"), biomechanicalTempo = "3-1-1-1", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "costas_remada_unilateral_halter", name = "Remada Unilateral com Halter (Serrote)", primaryMuscleCode = "LATS", primaryMuscleName = "Latíssimo do Dorso", secondaryMuscleName = "Bíceps", equipment = "Halter", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Apoie joelho e mão no banco", "Puxe o halter trazendo o cotovelo para o teto"), highPerformanceTips = listOf("Não rotacione exageradamente a coluna"), biomechanicalTempo = "2-1-1-1", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "costas_remada_baixa_triangulo", name = "Remada Baixa com Triângulo", primaryMuscleCode = "LATS", primaryMuscleName = "Costas", secondaryMuscleName = "Bíceps", equipment = "Cabo", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Sentado na polia baixa", "Puxe o triângulo no abdômen"), highPerformanceTips = listOf("Peito estufado, não projete os ombros a frente"), biomechanicalTempo = "2-1-1-1", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "costas_remada_baixa_aberta", name = "Remada Baixa Aberta", primaryMuscleCode = "LATS", primaryMuscleName = "Romboides e Trapézio", secondaryMuscleName = "Bíceps", equipment = "Cabo", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Barra reta grande na polia", "Pegada aberta, puxando no peito"), highPerformanceTips = listOf("Foque em juntar as escápulas"), biomechanicalTempo = "2-1-1-1", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "costas_remada_articulada", name = "Remada Articulada (Máquina)", primaryMuscleCode = "LATS", primaryMuscleName = "Dorsal", secondaryMuscleName = "Bíceps", equipment = "Máquina", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Apoie o peito", "Puxe as alavancas sentindo a contração das costas"), highPerformanceTips = listOf("Opeção excelente para falhar com segurança"), biomechanicalTempo = "2-1-1-1", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "costas_remada_sentado_cabo", name = "Remada Sentado Unilateral no Cabo", primaryMuscleCode = "LATS", primaryMuscleName = "Dorsal inferior", secondaryMuscleName = "Bíceps", equipment = "Cabo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Puxe a polia com um braço apenas", "Torça levemente"), highPerformanceTips = listOf("Isolação máxima com rotação contínua"), biomechanicalTempo = "2-1-1-1", jointPathType = "DEAD_LIFT"
    ),

    // Pulldown
    ExerciseExecutionReference(
        id = "costas_pulldown_frente", name = "Pulldown Frente Aberto", primaryMuscleCode = "LATS", primaryMuscleName = "Grande Dorsal", secondaryMuscleName = "Bíceps", equipment = "Máquina", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Puxe a barra no peito superior", "Alargue as dorsais"), highPerformanceTips = listOf("Nunca puxe para trás do pescoço"), biomechanicalTempo = "3-1-1-1", jointPathType = "LAT_PULLDOWN"
    ),
    ExerciseExecutionReference(
        id = "costas_pulldown_supinado", name = "Pulldown Pegada Supinada", primaryMuscleCode = "LATS", primaryMuscleName = "Dorsal (foco central)", secondaryMuscleName = "Bíceps", equipment = "Máquina", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Pegada onde as palmas olham para seu rosto", "Puxe fechado"), highPerformanceTips = listOf("Maior alongamento da dorsal na subida"), biomechanicalTempo = "3-1-1-1", jointPathType = "LAT_PULLDOWN"
    ),
    ExerciseExecutionReference(
        id = "costas_pulldown_triangulo", name = "Pulldown com Triângulo", primaryMuscleCode = "LATS", primaryMuscleName = "Grande Dorsal (alongamento focal)", secondaryMuscleName = "Bíceps", equipment = "Máquina", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Puxe o pegador neutro no esterno", "Projete o peito estourando em direção ao triângulo"), highPerformanceTips = listOf("Trave o core"), biomechanicalTempo = "3-1-1-1", jointPathType = "LAT_PULLDOWN"
    ),
    ExerciseExecutionReference(
        id = "costas_pulldown_articulado", name = "Pulldown Articulado", primaryMuscleCode = "LATS", primaryMuscleName = "Costas Isoladas", secondaryMuscleName = "Bíceps", equipment = "Máquina", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("No equipamento mecânico articulado"), highPerformanceTips = listOf("Aproveite o eixo da máquina para colocar peso"), biomechanicalTempo = "3-1-1-1", jointPathType = "LAT_PULLDOWN"
    ),

    // Barra fixa
    ExerciseExecutionReference(
        id = "costas_barra_pronada", name = "Barra Fixa Pegada Pronada", primaryMuscleCode = "LATS", primaryMuscleName = "Costas Completas", secondaryMuscleName = "Bíceps, Core", equipment = "Peso do Corpo", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Puxe-se para cima até o peitoral tocar a barra"), highPerformanceTips = listOf("Abaixe e feche os ombros no início para não forçar cervicais"), biomechanicalTempo = "2-0-1-0", jointPathType = "LAT_PULLDOWN"
    ),
    ExerciseExecutionReference(
        id = "costas_barra_supinada", name = "Barra Fixa Pegada Supinada (Chin Up)", primaryMuscleCode = "LATS", primaryMuscleName = "Costas e Bíceps Profundo", secondaryMuscleName = "Core", equipment = "Peso do Corpo", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Pegada fechada no eixo dos ombros"), highPerformanceTips = listOf("Potencializa recutamento dos braços"), biomechanicalTempo = "2-0-1-0", jointPathType = "LAT_PULLDOWN"
    ),
    ExerciseExecutionReference(
        id = "costas_barra_neutra", name = "Barra Fixa Pegada Neutra", primaryMuscleCode = "LATS", primaryMuscleName = "Dorsal Lateral", secondaryMuscleName = "Bíceps Braquial", equipment = "Peso do Corpo", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Palmas voltadas uma pra outra"), highPerformanceTips = listOf("Posição mais forte do ombro"), biomechanicalTempo = "2-0-1-0", jointPathType = "LAT_PULLDOWN"
    ),

    // Levantamentos
    ExerciseExecutionReference(
        id = "costas_terra_conv", name = "Levantamento Terra Convencional", primaryMuscleCode = "LOWER_BACK", primaryMuscleName = "Cadeia Posterior Completa", secondaryMuscleName = "Glúteos, Quadríceps", equipment = "Barra", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Posição de base. Suba deslizando"), highPerformanceTips = listOf("Puxe a folga da barra antes de tirar do chão"), biomechanicalTempo = "1-1-1-0", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "costas_terra_sumo", name = "Levantamento Terra Sumô", primaryMuscleCode = "LOWER_BACK", primaryMuscleName = "Adutores, Glúteos e Posterior", secondaryMuscleName = "Quadríceps", equipment = "Barra", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Base ultralarga, mãos por dentro dos joelhos"), highPerformanceTips = listOf("Eixo mais curto e mecânica melhor pra dorsais"), biomechanicalTempo = "1-1-1-0", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "costas_rack_pull", name = "Rack Pull", primaryMuscleCode = "LOWER_BACK", primaryMuscleName = "Costas Superiores, Lombar", secondaryMuscleName = "Trapézio", equipment = "Barra", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Base alta em suportes. Apenas fim do movimento do Terra"), highPerformanceTips = listOf("Permite levantar muito mais garga segura no trapézio"), biomechanicalTempo = "2-1-1-0", jointPathType = "DEAD_LIFT"
    ),

    // OMBROS
    // Desenvolvimento
    ExerciseExecutionReference(
        id = "ombro_desenvolvimento_barra", name = "Desenvolvimento Militar com Barra", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Anterior e Médio", secondaryMuscleName = "Tríceps, Core", equipment = "Barra", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Em pé ou sentado, empurre a barra do peito ao teto"), highPerformanceTips = listOf("Não flexione os joelhos. Trave pernas e glúteos"), biomechanicalTempo = "2-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "ombro_desenvolvimento_halter", name = "Desenvolvimento Sentado com Halteres", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Anterior", secondaryMuscleName = "Tríceps", equipment = "Halter", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Costas calçadas no banco", "Levante halteres sem fechar em cima"), highPerformanceTips = listOf("Amplitude profunda na descida ativa mais ombro"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "ombro_desenvolvimento_smith", name = "Desenvolvimento no Smith", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóides", secondaryMuscleName = "Tríceps", equipment = "Smith", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Banco no Smith", "Empurre alinhado ao rosto"), highPerformanceTips = listOf("Movimento travado possibilita intensidade sem balanço"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "ombro_desenvolvimento_maq", name = "Desenvolvimento em Máquina", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóides", secondaryMuscleName = "Tríceps", equipment = "Máquina", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Sentado e empurrando aparelho isolado"), highPerformanceTips = listOf("Pratique falha muscular com segurança"), biomechanicalTempo = "3-1-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "ombro_arnold_press", name = "Arnold Press", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Anterior e Lateral", secondaryMuscleName = "Tríceps", equipment = "Halter", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Comece com as mãos na frente do queixo supinadas, expire rotacionando até abrir ao topo"), highPerformanceTips = listOf("Maior tempo sob tensão para os deltoides frontais"), biomechanicalTempo = "3-1-2-0", jointPathType = "CHEST_PRESS"
    ),

    // Elevação Lateral
    ExerciseExecutionReference(
        id = "ombro_elev_lateral_halter", name = "Elevação Lateral com Halteres", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Lateral", secondaryMuscleName = "Trapézio", equipment = "Halter", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Braços semiflexionados, erga pelo lado"), highPerformanceTips = listOf("O cotovelo sempre sobe primeiro das mãos"), biomechanicalTempo = "3-0-1-1", jointPathType = "LATERAL_RAISE"
    ),
    ExerciseExecutionReference(
        id = "ombro_elev_lateral_cabo", name = "Elevação Lateral no Cabo", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Lateral", secondaryMuscleName = "Trapézio", equipment = "Cabo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Puxe pelo cabo do lado oposto"), highPerformanceTips = listOf("Tensão perpétua desde o repouso"), biomechanicalTempo = "3-0-1-1", jointPathType = "LATERAL_RAISE"
    ),
    ExerciseExecutionReference(
        id = "ombro_elev_lateral_maq", name = "Elevação Lateral em Máquina", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Lateral", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Levantar os pad de ombro forçando p/ os lados"), highPerformanceTips = listOf("Concentre-se em isolar apenas o ombro sem trapacear"), biomechanicalTempo = "3-0-1-1", jointPathType = "LATERAL_RAISE"
    ),
    ExerciseExecutionReference(
        id = "ombro_elev_lateral_uni", name = "Elevação Lateral Unilateral Banco", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Lateral Isolado", secondaryMuscleName = "", equipment = "Halter", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Segure um suporte e faça lateralmente inclinado"), highPerformanceTips = listOf("Muda o arco de alavanca pra esmagar deltoide"), biomechanicalTempo = "3-0-1-1", jointPathType = "LATERAL_RAISE"
    ),

    // Elevação frontal
    ExerciseExecutionReference(
        id = "ombro_elev_frontal_barra", name = "Elevação Frontal com Barra", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Anterior", secondaryMuscleName = "Peitoral SUp", equipment = "Barra", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Suba a barra na frente até rosto"), highPerformanceTips = listOf("Cuidado para não usar a lombar como impulso"), biomechanicalTempo = "2-0-1-0", jointPathType = "LATERAL_RAISE"
    ),
    ExerciseExecutionReference(
        id = "ombro_elev_frontal_halter", name = "Elevação Frontal com Halteres", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Anterior", secondaryMuscleName = "", equipment = "Halter", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Alternado ou junto, erga a frente"), highPerformanceTips = listOf("Controlar giro"), biomechanicalTempo = "2-0-1-0", jointPathType = "LATERAL_RAISE"
    ),
    ExerciseExecutionReference(
        id = "ombro_elev_frontal_corda", name = "Elevação Frontal com Corda (Polia)", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Anterior Diferenciado", secondaryMuscleName = "", equipment = "Cabo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("No cross polia baixa puxando entere as pernas"), highPerformanceTips = listOf("Permite subir os punhos em angulo neutro"), biomechanicalTempo = "2-0-1-0", jointPathType = "LATERAL_RAISE"
    ),

    // Posterior ombro
    ExerciseExecutionReference(
        id = "ombro_crucifixo_inv", name = "Crucifixo Invertido com Halteres", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Posterior", secondaryMuscleName = "Romboides", equipment = "Halter", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Peito inclinado 90º", "Abra braços pra trás"), highPerformanceTips = listOf("Feche escápulas"), biomechanicalTempo = "2-1-1-1", jointPathType = "LATERAL_RAISE"
    ),
    ExerciseExecutionReference(
        id = "ombro_face_pull", name = "Face Pull", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Posterior e Rombóides", secondaryMuscleName = "Trapézio", equipment = "Cabo", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Corda no rosto na altura dos olhos"), highPerformanceTips = listOf("Rotação externa dos manguitos = ombro saudável"), biomechanicalTempo = "2-1-1-1", jointPathType = "LAT_PULLDOWN"
    ),
    ExerciseExecutionReference(
        id = "ombro_reverse_peck", name = "Crucifixo Invertido na Máquina (Reverse Peck Deck)", primaryMuscleCode = "SHOULDER", primaryMuscleName = "Deltóide Posterior Puro", secondaryMuscleName = "Trapézio", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Sentado ao contrário no peck deck"), highPerformanceTips = listOf("A melhor opção para isolar e gerar pump e sangue no posterior"), biomechanicalTempo = "2-1-1-1", jointPathType = "LATERAL_RAISE"
    ),

    // BÍCEPS
    ExerciseExecutionReference(
        id = "biceps_rosca_direta_reta", name = "Rosca Direta com Barra Reta", primaryMuscleCode = "BICEPS", primaryMuscleName = "Bíceps Braquial", secondaryMuscleName = "Antebraço", equipment = "Barra", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Flexiona cotovelo levantando a barra"), highPerformanceTips = listOf("Punho cerrado rígido", "sem empurrar corpo"), biomechanicalTempo = "3-0-1-1", jointPathType = "BICEPS_CURL"
    ),
    ExerciseExecutionReference(
        id = "biceps_rosca_direta_w", name = "Rosca Direta com Barra W", primaryMuscleCode = "BICEPS", primaryMuscleName = "Bíceps Braquial", secondaryMuscleName = "Antebraço", equipment = "Barra", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Com pegada angulada da barra W"), highPerformanceTips = listOf("Protege o punho em altas cargas"), biomechanicalTempo = "3-0-1-1", jointPathType = "BICEPS_CURL"
    ),
    ExerciseExecutionReference(
        id = "biceps_rosca_alternada", name = "Rosca Alternada com Halteres", primaryMuscleCode = "BICEPS", primaryMuscleName = "Bíceps (Ambas porções)", secondaryMuscleName = "Antebraço", equipment = "Halter", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Alternar braços com supinação extra"), highPerformanceTips = listOf("Sempre gire o peso esmagando o pico do múcsuolo"), biomechanicalTempo = "3-0-1-1", jointPathType = "BICEPS_CURL"
    ),
    ExerciseExecutionReference(
        id = "biceps_rosca_martelo", name = "Rosca Martelo", primaryMuscleCode = "BICEPS", primaryMuscleName = "Braquio-radial e Bíceps longo", secondaryMuscleName = "Antebraço Severo", equipment = "Halter", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Costelas fechadas, pegada neutra"), highPerformanceTips = listOf("Gera muito aspecot de largura no braço"), biomechanicalTempo = "3-0-1-0", jointPathType = "BICEPS_CURL"
    ),
    ExerciseExecutionReference(
        id = "biceps_rosca_concentrada", name = "Rosca Concentrada", primaryMuscleCode = "BICEPS", primaryMuscleName = "Pico do Bíceps", secondaryMuscleName = "", equipment = "Halter", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Cotovelo ancorado na coxa interna, puxe só o biceps"), highPerformanceTips = listOf("Zero momento (embalagem)"), biomechanicalTempo = "3-1-1-1", jointPathType = "BICEPS_CURL"
    ),
    ExerciseExecutionReference(
        id = "biceps_rosca_scott_maq", name = "Rosca Scott em Máquina", primaryMuscleCode = "BICEPS", primaryMuscleName = "Bíceps Ventre Inferior", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Apoiado no banco acolchoado da máquina"), highPerformanceTips = listOf("Controle excêntrico para não rasgar o bíceps na extensão inteira"), biomechanicalTempo = "3-1-1-1", jointPathType = "BICEPS_CURL"
    ),
    ExerciseExecutionReference(
        id = "biceps_rosca_scott_barra", name = "Rosca Scott com Barra EZ", primaryMuscleCode = "BICEPS", primaryMuscleName = "Bíceps Isolamento Grito", secondaryMuscleName = "", equipment = "Barra", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Livre num suporte scott"), highPerformanceTips = listOf("Foco absoluto no stretch sob carga"), biomechanicalTempo = "3-1-1-1", jointPathType = "BICEPS_CURL"
    ),
    ExerciseExecutionReference(
        id = "biceps_rosca_cabo", name = "Rosca no Cabo (Polia Baixa)", primaryMuscleCode = "BICEPS", primaryMuscleName = "Bíceps Tensão Contante", secondaryMuscleName = "", equipment = "Cabo", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Puxe da máquina com barra contínua"), highPerformanceTips = listOf("Tensão nunca desaparece"), biomechanicalTempo = "3-0-1-1", jointPathType = "BICEPS_CURL"
    ),
    ExerciseExecutionReference(
        id = "biceps_rosca_spider", name = "Rosca Spider (Aranha)", primaryMuscleCode = "BICEPS", primaryMuscleName = "Pico Focado", secondaryMuscleName = "", equipment = "Halter", difficulty = "Avançado", type = "Isolador",
        executionDetails = listOf("Deitado de barriga pra abixo, braços livres pro chão"), highPerformanceTips = listOf("Gravity work contra o pico"), biomechanicalTempo = "3-1-1-1", jointPathType = "BICEPS_CURL"
    ),

    // TRÍCEPS
    ExerciseExecutionReference(
        id = "triceps_corda", name = "Tríceps Pulldown com Corda", primaryMuscleCode = "TRICEPS", primaryMuscleName = "Tríceps Cabeça Lateral", secondaryMuscleName = "Antebraço", equipment = "Cabo", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Puxe pra baixo abrindo ao final"), highPerformanceTips = listOf("Abra a corda em V pra ativar externo"), biomechanicalTempo = "2-0-1-1", jointPathType = "TRICEPS_EXTENSION"
    ),
    ExerciseExecutionReference(
        id = "triceps_barra_reta", name = "Tríceps Pushdown com Barra Reta", primaryMuscleCode = "TRICEPS", primaryMuscleName = "Tríceps Massivo", secondaryMuscleName = "", equipment = "Cabo", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Extensão do cotovelo para o centro"), highPerformanceTips = listOf("Permite grande sobrecarga"), biomechanicalTempo = "2-0-1-1", jointPathType = "TRICEPS_EXTENSION"
    ),
    ExerciseExecutionReference(
        id = "triceps_barra_w", name = "Tríceps com Barra W (V-Bar)", primaryMuscleCode = "TRICEPS", primaryMuscleName = "Tríceps Lateral/Medial", secondaryMuscleName = "", equipment = "Cabo", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Pega em formato V polegar fechado"), highPerformanceTips = listOf("Melhor para quem sente punho dololrido"), biomechanicalTempo = "2-0-1-1", jointPathType = "TRICEPS_EXTENSION"
    ),
    ExerciseExecutionReference(
        id = "triceps_frances_halter", name = "Tríceps Francês com Halter (Overhead)", primaryMuscleCode = "TRICEPS", primaryMuscleName = "Cabeça Longa do Tríceps", secondaryMuscleName = "", equipment = "Halter", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Segure um halter atras d acabeça e puxe pra cima"), highPerformanceTips = listOf("Essencial pro tamanho global do braço. Cotugelo pro teto."), biomechanicalTempo = "3-1-1-0", jointPathType = "TRICEPS_EXTENSION"
    ),
    ExerciseExecutionReference(
        id = "triceps_testa_barra", name = "Tríceps Testa com Barra", primaryMuscleCode = "TRICEPS", primaryMuscleName = "Tríceps Integral", secondaryMuscleName = "", equipment = "Barra", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Deitado abaixe a barra livre W na testa"), highPerformanceTips = listOf("Cotovelos levemente pra tras, fura o padrao do solo"), biomechanicalTempo = "3-1-1-0", jointPathType = "TRICEPS_EXTENSION"
    ),
    ExerciseExecutionReference(
        id = "triceps_testa_halter", name = "Tríceps Testa Neutro Halteres", primaryMuscleCode = "TRICEPS", primaryMuscleName = "Tríceps Cabeça Lateral e Longa", secondaryMuscleName = "", equipment = "Halter", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Deitado na lateral da chabuça neutro descde os haltere"), highPerformanceTips = listOf("Mais confortavel q barra em volume"), biomechanicalTempo = "3-1-1-0", jointPathType = "TRICEPS_EXTENSION"
    ),
    ExerciseExecutionReference(
        id = "triceps_paralelas", name = "Mergulho em Paralelas (Dips)", primaryMuscleCode = "TRICEPS", primaryMuscleName = "Tríceps, Peito inferior", secondaryMuscleName = "Ombro Anterior", equipment = "Peso do Corpo", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Desça corpo em barras parelelas 90 graus"), highPerformanceTips = listOf("Corpo ereto para tríceps. Pra peito inclina pra frente."), biomechanicalTempo = "2-0-1-0", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "triceps_banco", name = "Tríceps no Banco (Dips apoiado)", primaryMuscleCode = "TRICEPS", primaryMuscleName = "Tríceps Geral", secondaryMuscleName = "Ombro da frente", equipment = "Peso do Corpo", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Amão tras apoiadas, desça rasgo"), highPerformanceTips = listOf("Cuidado se ter ombro rígido machuca acrômio"), biomechanicalTempo = "2-0-1-0", jointPathType = "TRICEPS_EXTENSION"
    ),
    ExerciseExecutionReference(
        id = "triceps_maquina", name = "Tríceps Extensora Máquina", primaryMuscleCode = "TRICEPS", primaryMuscleName = "Tríceps Puro Isometrizado", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Na maquina empurre as barras almofadadas pra bx"), highPerformanceTips = listOf("Sente bumbum atrás sem imposto lombar"), biomechanicalTempo = "2-0-1-1", jointPathType = "TRICEPS_EXTENSION"
    ),

    // PERNAS
    // Agachamentos
    ExerciseExecutionReference(
        id = "perna_agachamento_livre", name = "Agachamento Livre", primaryMuscleCode = "QUAD", primaryMuscleName = "Quadríceps, Glúteos", secondaryMuscleName = "Posterior, Core", equipment = "Barra", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Nas costas agache abaixo do parelalo"), highPerformanceTips = listOf("Barra alto ou baixo muda ativac"), biomechanicalTempo = "3-0-1-0", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_agachamento_frontal", name = "Agachamento Frontal com Barra", primaryMuscleCode = "QUAD", primaryMuscleName = "Foco Massivo Quadríceps", secondaryMuscleName = "Core intenso", equipment = "Barra", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Barra nos deltoides na frente do pescoço"), highPerformanceTips = listOf("Costas muito verticais forçao o abdomem e quadriceps extremos"), biomechanicalTempo = "3-0-1-0", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_agachamento_smith", name = "Agachamento no Smith", primaryMuscleCode = "QUAD", primaryMuscleName = "Perna Integral Fixa", secondaryMuscleName = "Glúteo", equipment = "Smith", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Faça agachamento guiado"), highPerformanceTips = listOf("Pés masi a frente recrutam glúteo total"), biomechanicalTempo = "3-0-1-0", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_hack_squat", name = "Hack Squat", primaryMuscleCode = "QUAD", primaryMuscleName = "Quadríceps Exclusivo Isolado Composto", secondaryMuscleName = "Gluteo", equipment = "Máquina", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Desça a maquiuna suportada nos ombros inclinado"), highPerformanceTips = listOf("Calcanhar fixo o tempo todo!"), biomechanicalTempo = "3-0-1-0", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_goblet_squat", name = "Agachamento Taça (Goblet Squat)", primaryMuscleCode = "QUAD", primaryMuscleName = "Pernas completas iniciante", secondaryMuscleName = "Core", equipment = "Halter", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Segure Halter grande no peito e agache"), highPerformanceTips = listOf("Base para progredir pro livre pesado"), biomechanicalTempo = "3-1-1-0", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_agachamento_sumo", name = "Agachamento Sumô (Halter ou Kettlebell)", primaryMuscleCode = "QUAD", primaryMuscleName = "Glúteos e Adutores (Miolo Perna)", secondaryMuscleName = "Quadríceps", equipment = "Halter", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Pernas esptamente abertas. Pé 45graus. Agaxe"), highPerformanceTips = listOf("Jolhos abertos esmaga gluteo"), biomechanicalTempo = "3-1-1-0", jointPathType = "SQUAT"
    ),

    // Leg Press
    ExerciseExecutionReference(
        id = "perna_leg_press_45", name = "Leg Press 45º", primaryMuscleCode = "QUAD", primaryMuscleName = "Perna Global", secondaryMuscleName = "Gluteos, Posteriores", equipment = "Máquina", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Pés meio, desça pressa e espurra. O joelho nunca tranca"), highPerformanceTips = listOf("Pés baixos: Quad. Pés alto: Glteo"), biomechanicalTempo = "3-0-1-0", jointPathType = "LEG_PRESS"
    ),
    ExerciseExecutionReference(
        id = "perna_leg_press_horiz", name = "Leg Press Horizontal", primaryMuscleCode = "QUAD", primaryMuscleName = "Pernas Máquina Sentada", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Empurre treno em plano horizontal"), highPerformanceTips = listOf("Foco metabóçoc de bom uso"), biomechanicalTempo = "2-0-1-0", jointPathType = "LEG_PRESS"
    ),
    ExerciseExecutionReference(
        id = "perna_leg_press_uni", name = "Leg Press Unilateral", primaryMuscleCode = "QUAD", primaryMuscleName = "Perna Simetria Corretiva", secondaryMuscleName = "Gluteo forte", equipment = "Máquina", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Faz 1 perna só nio leg"), highPerformanceTips = listOf("Corrige instabilidades e forcas duais"), biomechanicalTempo = "2-0-1-0", jointPathType = "LEG_PRESS"
    ),
    ExerciseExecutionReference(
        id = "perna_leg_press_vertical", name = "Leg Press Vertical", primaryMuscleCode = "QUAD", primaryMuscleName = "Perna Clássica Golden Era", secondaryMuscleName = "Dorsiflexor", equipment = "Máquina", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Deitado chão, purra pro teto reto"), highPerformanceTips = listOf("Exige moblidade pelvica gigante"), biomechanicalTempo = "3-0-1-0", jointPathType = "LEG_PRESS"
    ),

    // Extensora
    ExerciseExecutionReference(
        id = "perna_extensora_bi", name = "Cadeira Extensora", primaryMuscleCode = "QUAD", primaryMuscleName = "Reto Femoral / Vasto Lateral e Medial", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Senta amarra canela emurru pra cima esticando jjoelho"), highPerformanceTips = listOf("Susta em cima 2 seg pro vasto medial chorar"), biomechanicalTempo = "3-0-1-2", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_extensora_uni", name = "Extensora Unilateral", primaryMuscleCode = "QUAD", primaryMuscleName = "Quad Isolado Focado", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Igual mas 1 perna"), highPerformanceTips = listOf("Foco máximo neuro-muscular na deficiencia"), biomechanicalTempo = "3-0-1-2", jointPathType = "SQUAT"
    ),

    // Flexora
    ExerciseExecutionReference(
        id = "perna_mesa_flexora", name = "Mesa Flexora (Deitado)", primaryMuscleCode = "HAMSTRING", primaryMuscleName = "Isquiotibiais (Posterior)", secondaryMuscleName = "Panturrilhas", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Deia, rola almofada pro gluteo tras"), highPerformanceTips = listOf("Não tire o quadril fo banco (sem roubar a lombar)"), biomechanicalTempo = "3-0-1-1", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "perna_flexora_sentada", name = "Cadeira Flexora", primaryMuscleCode = "HAMSTRING", primaryMuscleName = "Posterior Extra Alongado", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Sentado trava coxa puxa pc pra trsa/baixo"), highPerformanceTips = listOf("Mais efetivo para tamanho q  a deitrada (alongamento super)"), biomechanicalTempo = "3-0-1-1", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "perna_flexora_uni", name = "Flexora Unilateral", primaryMuscleCode = "HAMSTRING", primaryMuscleName = "Posterior Simetria", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Pé por pe"), highPerformanceTips = listOf("Menos espasmos de perna dominante"), biomechanicalTempo = "3-0-1-1", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "perna_flexora_empe", name = "Flexora em Pé", primaryMuscleCode = "HAMSTRING", primaryMuscleName = "Isquiotibial Isolado Total", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Pé na base sobe calcanaha rs gluteo individual"), highPerformanceTips = listOf("Pump gigante pra volume posterior de gota"), biomechanicalTempo = "3-0-1-1", jointPathType = "DEAD_LIFT"
    ),

    // Glúteos
    ExerciseExecutionReference(
        id = "perna_elev_pelvica_barra", name = "Elevação Pélvica com Barra (Hip Thrust)", primaryMuscleCode = "GLUTES", primaryMuscleName = "Glúteo Máximo", secondaryMuscleName = "Posterior, Lombar", equipment = "Barra", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Costas no bancom abarra quadril joga pro teto"), highPerformanceTips = listOf("Espreme o bumbum por maximo de 2s"), biomechanicalTempo = "2-1-1-2", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_elev_pelvica_maq", name = "Elevação Pélvica na Máquina", primaryMuscleCode = "GLUTES", primaryMuscleName = "Glúteo Máximo", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Igual mas na maq guiada"), highPerformanceTips = listOf("Facil de socar muito peso sem does de osso pelvico!"), biomechanicalTempo = "2-1-1-2", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_coice_cabo", name = "Glúteo Coice no Cabo (Kickback)", primaryMuscleCode = "GLUTES", primaryMuscleName = "Glúteo Médio/Máximo superior", secondaryMuscleName = "Oblíquo", equipment = "Cabo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Prende caneleira de cabo chuta patras"), highPerformanceTips = listOf("Coluna neutra pra nao ferrar lombar! Perna ligeiramente diagonal = mais gluteo medio"), biomechanicalTempo = "2-1-1-1", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_coice_maq", name = "Glúteo Coice na Máquina", primaryMuscleCode = "GLUTES", primaryMuscleName = "Glutueo empurrao", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Maquina isolada recuada peita atras"), highPerformanceTips = listOf("Forca no calcanhar!"), biomechanicalTempo = "2-0-1-1", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_gluteo_smith", name = "Agachamento Búlgaro Smith Foco Glúteo / Gluteo 4 Apoios (Não faça se possivel)", primaryMuscleCode = "GLUTES", primaryMuscleName = "Gluteos em Maq", secondaryMuscleName = "", equipment = "Smith", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Levanta smith calcanhar"), highPerformanceTips = listOf("Cuidado perigozso para lesao, use outras opcoes"), biomechanicalTempo = "2-0-1-0", jointPathType = "SQUAT"
    ),

    // Abdutora
    ExerciseExecutionReference(
        id = "perna_abdutora_maq", name = "Cadeira Abdutora", primaryMuscleCode = "GLUTES", primaryMuscleName = "Glúteo Médio, Tensor Fascia Lata", secondaryMuscleName = "Abdutores da Coxa", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Abra perna na máquia contra forca pra fora"), highPerformanceTips = listOf("Incline pra gfrente pa ativar maximo o medio (formato abelha!)"), biomechanicalTempo = "2-0-1-1", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_abdutora_cabo", name = "Abdução no Cabo", primaryMuscleCode = "GLUTES", primaryMuscleName = "Glúteo Médio Unilateral", secondaryMuscleName = "", equipment = "Cabo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Perneit em pé abvre lateralmente arrastnaod fio"), highPerformanceTips = listOf("Foco lateral abosluto"), biomechanicalTempo = "2-0-1-0", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_caminhada_lateral", name = "Caminhada Lateral com Elástico (Monster Walk)", primaryMuscleCode = "GLUTES", primaryMuscleName = "Aquecimento Glúteo", secondaryMuscleName = "Condiconamento Ativador", equipment = "Livre", difficulty = "Iniciante", type = "Composto",
        executionDetails = listOf("Cinta no joleho anda de lado"), highPerformanceTips = listOf("Otimo pra ligar os nervso da bvunba!"), biomechanicalTempo = "1-0-1-0", jointPathType = "SQUAT"
    ),

    // Adutora
    ExerciseExecutionReference(
        id = "perna_adutora_maq", name = "Cadeira Adutora", primaryMuscleCode = "QUAD", primaryMuscleName = "Adutores (Parte Interna da Coxa)", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Aperte pernas feehando-as cntra resistencia"), highPerformanceTips = listOf("Não alongue alem do limite pelvico"), biomechanicalTempo = "2-0-1-1", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_adutora_cabo", name = "Adução no Cabo", primaryMuscleCode = "QUAD", primaryMuscleName = "Internos Unilateral Cabo", secondaryMuscleName = "", equipment = "Cabo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Amarrado de lado force perna pra crzuar frent e da otura"), highPerformanceTips = listOf("Bom prehab"), biomechanicalTempo = "2-0-1-0", jointPathType = "SQUAT"
    ),

    // Posterior
    ExerciseExecutionReference(
        id = "perna_stiff_barra", name = "Stiff com Barra", primaryMuscleCode = "HAMSTRING", primaryMuscleName = "Isquiotibiais Profundos", secondaryMuscleName = "Lombar, Glúteos", equipment = "Barra", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Joelho levemetnte semi, jogue quadril la pa tras alonga tudo e volta"), highPerformanceTips = listOf("Barra arrastando na canela, se afastar vc qybebra as costas!"), biomechanicalTempo = "3-1-1-0", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "perna_stiff_halter", name = "Stiff com Halteres", primaryMuscleCode = "HAMSTRING", primaryMuscleName = "Posterior Isometria e Flex", secondaryMuscleName = "Lombar, Glúteos", equipment = "Halter", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Como a varra mas os gakteres esticam mais ldo lado"), highPerformanceTips = listOf("Bom alognamentio"), biomechanicalTempo = "3-1-1-0", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "perna_rdl", name = "RDL (Romanian Deadlift)", primaryMuscleCode = "GLUTES", primaryMuscleName = "Gluteios, Pouco poseterior", secondaryMuscleName = "Lombar", equipment = "Barra", difficulty = "Intermediário", type = "Composto",
        executionDetails = listOf("Flexiona BEM mais joelhos q stiff porem so decs eate o joelho"), highPerformanceTips = listOf("Empurre cu pa tras no limite sem descer peito a mais"), biomechanicalTempo = "3-0-1-0", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "perna_good_morning", name = "Good Morning (Bom Dia)", primaryMuscleCode = "LOWER_BACK", primaryMuscleName = "Lombar de Pedra", secondaryMuscleName = "Posteriro Raiz", equipment = "Barra", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Barra costa tipoagacha curva-se pra renfrennte domiando o peos com quadtil"), highPerformanceTips = listOf("So fazer se tiver abodomen trincado de forte"), biomechanicalTempo = "2-1-1-0", jointPathType = "DEAD_LIFT"
    ),

    // Panturrilha
    ExerciseExecutionReference(
        id = "perna_pantu_sentado", name = "Panturrilha Sentado na Máquina", primaryMuscleCode = "CALVES", primaryMuscleName = "Sóleo (Panturrilha Inferior)", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Eleve pes om jpelos flexao na maq"), highPerformanceTips = listOf("O musculo soleo tem muita fibra lenta, faza mty repeticaso alongada!"), biomechanicalTempo = "2-2-1-1", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_pantu_em_pe", name = "Panturrilha em Pé (Gastrocnêmio)", primaryMuscleCode = "CALVES", primaryMuscleName = "Gastrocnêmio (Panturrilhas Coração)", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Estique pés em pe. Joelho reto"), highPerformanceTips = listOf("Contração maxima de 1s, se balançar nao cresce!"), biomechanicalTempo = "2-1-1-2", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_pantu_leg", name = "Panturrilha no Leg Press", primaryMuscleCode = "CALVES", primaryMuscleName = "Gemeos Grossos", secondaryMuscleName = "", equipment = "Máquina", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Ponta do pe no abdaixa fozna flexao palnrar"), highPerformanceTips = listOf("Maior alongamento posivel"), biomechanicalTempo = "2-2-1-2", jointPathType = "SQUAT"
    ),
    ExerciseExecutionReference(
        id = "perna_pantu_uni", name = "Panturrilha Unilateral (Degrau Livre)", primaryMuscleCode = "CALVES", primaryMuscleName = "Isolador Fibras Pantu", secondaryMuscleName = "", equipment = "Livre", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Degrau seguarnado halter noo maoa mdo esmmo peso"), highPerformanceTips = listOf("Simetria e balanco"), biomechanicalTempo = "2-1-1-1", jointPathType = "SQUAT"
    ),

    // ABDÔMEN
    ExerciseExecutionReference(
        id = "abs_crunch", name = "Crunch (Abdominal Clássico)", primaryMuscleCode = "ABS", primaryMuscleName = "Reto Abdominal", secondaryMuscleName = "Oblíquos", equipment = "Peso do Corpo", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Suba ombros flexionando cranios"), highPerformanceTips = listOf("Costas lombar travadas no max!"), biomechanicalTempo = "2-0-1-1", jointPathType = "CHEST_PRESS" // arbitrary joint
    ),
    ExerciseExecutionReference(
        id = "abs_crunch_maq", name = "Crunch na Máquina", primaryMuscleCode = "ABS", primaryMuscleName = "Reto Abdominal Pesado", secondaryMuscleName = "Core Interno", equipment = "Máquina", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Prenda perna e dobre columa contra carga"), highPerformanceTips = listOf("Abdômen tbm precidsa progressao de carga igual bicep!"), biomechanicalTempo = "2-0-1-1", jointPathType = "CHEST_PRESS"
    ),
    ExerciseExecutionReference(
        id = "abs_elev_pernas", name = "Elevação de Pernas (Hanging Leg Raise)", primaryMuscleCode = "ABS", primaryMuscleName = "Abdômen Inferior Máster", secondaryMuscleName = "Core, Flexores de Quadril", equipment = "Peso do Corpo", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("Pendurado na barra srga pena esticadas"), highPerformanceTips = listOf("Enrole a pelve! Nao sobe so qadri!"), biomechanicalTempo = "2-0-1-0", jointPathType = "DEAD_LIFT"
    ),
    ExerciseExecutionReference(
        id = "abs_infra_banco", name = "Infra no Banco", primaryMuscleCode = "ABS", primaryMuscleName = "Abdômen Infra base", secondaryMuscleName = "Flexor quadril", equipment = "Peso do Corpo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Deitado erga as pernams no baço incliando"), highPerformanceTips = listOf("Tira bdunda fa maça"), biomechanicalTempo = "2-0-1-0", jointPathType = "LOWER_BACK"
    ),
    ExerciseExecutionReference(
        id = "abs_prancha", name = "Prancha Abdominal (Plank)", primaryMuscleCode = "ABS", primaryMuscleName = "Estabilizadores Transversos", secondaryMuscleName = "Core 360", equipment = "Peso do Corpo", difficulty = "Iniciante", type = "Isolador",
        executionDetails = listOf("Isometria forental no cotevelo"), highPerformanceTips = listOf("Puxe o umbigo pra dentro sugande pra lombar! (Stommac vaccum)"), biomechanicalTempo = "Isometria", jointPathType = "STATIC"
    ),
    ExerciseExecutionReference(
        id = "abs_prancha_lat", name = "Prancha Lateral", primaryMuscleCode = "ABS", primaryMuscleName = "Oblíquos de Aço", secondaryMuscleName = "Transverso", equipment = "Peso do Corpo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Lado e suspenda quadrial"), highPerformanceTips = listOf("Respiração fira controladfa!"), biomechanicalTempo = "Isometria", jointPathType = "STATIC"
    ),
    ExerciseExecutionReference(
        id = "abs_ab_wheel", name = "Roda Abdominal (Ab Wheel)", primaryMuscleCode = "ABS", primaryMuscleName = "Core Absoluto Focado", secondaryMuscleName = "Tríceps, Costas", equipment = "Livre", difficulty = "Avançado", type = "Composto",
        executionDetails = listOf("De joelhlso desliza role rolete"), highPerformanceTips = listOf("Nunca deixa lomar cair formando U arcado pra abixo!!"), biomechanicalTempo = "3-1-2-0", jointPathType = "STATIC"
    ),
    ExerciseExecutionReference(
        id = "abs_cable_crunch", name = "Abdominal no Cabo (Cable Crunch)", primaryMuscleCode = "ABS", primaryMuscleName = "Reto Abdominal Sobrecarga", secondaryMuscleName = "", equipment = "Cabo", difficulty = "Intermediário", type = "Isolador",
        executionDetails = listOf("Joelho e puxa polia al ta porr tras cabcça enrotnlando ate chao"), highPerformanceTips = listOf("Imagine ssua ccbeça emcantdado no saco crotal"), biomechanicalTempo = "2-0-1-1", jointPathType = "CHEST_PRESS"
    ),

    // CARDIO
    ExerciseExecutionReference(
        id = "cardio_hiit", name = "HIIT (Treino Intervalado de Alta Intensidade)", primaryMuscleCode = "CARDIO", primaryMuscleName = "Sistema Cardiovascular", secondaryMuscleName = "Metabolismo Lipídico", equipment = "Cardio", difficulty = "Avançado", type = "Cardio",
        executionDetails = listOf("Tiros de all-out misturado cm dessano ativo"), highPerformanceTips = listOf("Sua vida dpende de 4 minuteos!!"), biomechanicalTempo = "-", jointPathType = "CARDIO"
    ),
    ExerciseExecutionReference(
        id = "cardio_esteira", name = "Esteira (Corrida/Caminhada)", primaryMuscleCode = "CARDIO", primaryMuscleName = "Cardiovascular Padrão", secondaryMuscleName = "Pernas", equipment = "Cardio", difficulty = "Iniciante", type = "Cardio",
        executionDetails = listOf("Zonas aerobiocvas contatesns"), highPerformanceTips = listOf("Evita segurar os corirmaors apra mairo gastoa caloruiroc!"), biomechanicalTempo = "-", jointPathType = "CARDIO"
    ),
    ExerciseExecutionReference(
        id = "cardio_escada", name = "Simulador de Escada", primaryMuscleCode = "CARDIO", primaryMuscleName = "Quadril, Gluteos Cardio", secondaryMuscleName = "Endurance", equipment = "Cardio", difficulty = "Intermediário", type = "Cardio",
        executionDetails = listOf("Sube e não paaar"), highPerformanceTips = listOf("Sem apoiar o tronaco no monitor!!!! Isso rtura tosdos beneicios!!!"), biomechanicalTempo = "-", jointPathType = "CARDIO"
    ),
    ExerciseExecutionReference(
        id = "cardio_bike", name = "Bicicleta Ergométrica (Spinning)", primaryMuscleCode = "CARDIO", primaryMuscleName = "Cardio Sem Impacto Quad", secondaryMuscleName = "Endurance", equipment = "Cardio", difficulty = "Iniciante", type = "Cardio",
        executionDetails = listOf("Pedalar e swett"), highPerformanceTips = listOf("Carga pesadsa intervalada melohora formca da pern a msmntesnto acriido"), biomechanicalTempo = "-", jointPathType = "CARDIO"
    ),
    ExerciseExecutionReference(
        id = "cardio_eliptico", name = "Crosstrainer (Elíptico)", primaryMuscleCode = "CARDIO", primaryMuscleName = "Full Body Cardio sem imapcto", secondaryMuscleName = "", equipment = "Cardio", difficulty = "Iniciante", type = "Cardio",
        executionDetails = listOf("Bracos e preas moenbtno conrjuonot"), highPerformanceTips = listOf("Forca no claancnahr, nmas nias ponta doe spr"), biomechanicalTempo = "-", jointPathType = "CARDIO"
    )
)
