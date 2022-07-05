package com.api.ifila_backend.utils

import org.springframework.stereotype.Component
import java.time.LocalTime

@Component
class FilaUtils {
    fun calcularTempoMedio (tempoMedio : Long) : LocalTime {
        var tempoMedioHoras = tempoMedio / 3600
        var tempoMedioMinutos = (tempoMedio / 60) % 3600
        var tempoMedioSegundos = tempoMedio % 60

        return LocalTime.parse(String.format("%02d:%02d:%02d", tempoMedioHoras ,tempoMedioMinutos, tempoMedioSegundos))
    }
}