package com.api.ifila_backend.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalTime

@ApiModel
data class InfoPosicaoFilaDTO(
    val posicao: Int,
    val deveConfirmarPresenca: Boolean,

    @JsonFormat(pattern = "HH-mm-ss")
    @ApiModelProperty(example = "00-10-00")
    val tempoMedio: LocalTime,
)