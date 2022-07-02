package com.api.ifila_backend.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalTime

@ApiModel
data class AbrirFilaDTO (
    @ApiModelProperty(example = "50")
    var capacidadeMaxima: Int,

    @JsonFormat(pattern = "HH:mm")
    @ApiModelProperty(example = "22:00")
    var horarioMaximoEntrada: LocalTime
)