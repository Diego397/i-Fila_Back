package com.api.ifila_backend.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalTime

@ApiModel
data class FecharFilaReturnDTO(
    val qtdUsuariosAtendidiosPrincipal: Int,
    val qtdUsuariosAtendidiosPrioridade: Int,

    @JsonFormat(pattern = "HH:mm:ss")
    @ApiModelProperty(example = "00:00:10")
    val tempoMedioPrincipal: LocalTime,

    @JsonFormat(pattern = "HH:mm:ss")
    @ApiModelProperty(example = "00:00:10")
    val tempoMedioPrioridade: LocalTime
)

@ApiModel
data class GetFilaDTO(
    val statusFila: Boolean,
    val qtdUsuariosFilaPrincipal: Int,
    val qtdUsuariosFilaPrioridade: Int,

    @JsonFormat(pattern = "HH:mm:ss")
    @ApiModelProperty(example = "00:00:10")
    val tempoMedioPrincipal: LocalTime,

    @JsonFormat(pattern = "HH:mm:ss")
    @ApiModelProperty(example = "00:00:10")
    val tempoMedioPrioridade: LocalTime
)
