package com.api.ifila_backend.dtos

import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class EntrarFilaDTO (
    val tipoFila: String,
    val codigoFila: String
)