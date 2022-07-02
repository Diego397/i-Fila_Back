package com.api.ifila_backend.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ApiModel
data class EstabelecimentoDTO (

    @field:NotBlank
    @Size(max=7)
    @ApiModelProperty(position = 1, example = "ABC#123")
    val codigo:String,

    @field:NotBlank
    @Size(max=70)
    @ApiModelProperty(position = 2)
    val nome:String,

    @field:NotBlank
    @Size(max=70)
    @ApiModelProperty(position = 3)
    val endereco:String,

    @field:NotBlank
    @Size(max=20)
    @ApiModelProperty(position = 4)
    val telefone:String,

    @Size(max=20)
    @ApiModelProperty(position = 5, example = "11.111.111/1111-11")
    var cnpj: String,

    @field:NotBlank
    @ApiModelProperty(position = 6)
    var descricao: String,

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    @ApiModelProperty(position = 7, example = "10:00")
    var horarioAbertura: LocalTime,

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    @ApiModelProperty(position = 8, example = "22:00")
    var horarioFechamento: LocalTime,

    @field:NotBlank
    @ApiModelProperty(position = 9, example = "1AB32")
    var codigoFila: String
)