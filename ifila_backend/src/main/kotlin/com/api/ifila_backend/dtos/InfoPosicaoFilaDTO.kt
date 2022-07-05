package com.api.ifila_backend.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.Duration

@ApiModel
data class InfoPosicaoFilaDTO(
    val posicao: Int,
    val deveConfirmarPresenca: Boolean,

    @JsonFormat(pattern = "HH:mm")
    @ApiModelProperty(example = "00:10")
    val tempoMedioPrincipal: kotlin.time.Duration,

    @JsonFormat(pattern = "HH:mm")
    @ApiModelProperty(example = "00:10")
    val tempoMedioPrioridade: kotlin.time.Duration
)