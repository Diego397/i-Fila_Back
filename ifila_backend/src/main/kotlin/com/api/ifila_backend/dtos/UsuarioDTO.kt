package com.api.ifila_backend.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ApiModel
data class UsuarioDTO (

    @field:NotBlank
    @Size(max=70)
    @ApiModelProperty(position = 1)
    val nome:String,

    @field:NotBlank
    @field:Size(max=100)
    @Email(regexp = ".+@.+\\..+")
    @ApiModelProperty(position = 2, example = "exemplo@gmail.com")
    val email:String,

    @field:NotBlank
    @ApiModelProperty(position = 3)
    var senha: String,

    @field:NotBlank
    @field:Size(max=20, message="cpf inválido")
    @ApiModelProperty(position = 4, example = "111.111.111-11")
    val cpf: String,

    @field:NotBlank
    @field:Size(max=20)
    @ApiModelProperty(position = 5, example = "(85)99999-9999")
    val numeroCelular:String,

    @NotNull
    @JsonFormat(pattern = "dd/MM/yyyy")
    @ApiModelProperty(position = 6, example = "22/10/2001")
    val dataDeNascimento: LocalDate,
)

@ApiModel
data class GetUsuarioDTO (

    @NotNull
    @ApiModelProperty(position = 1)
    val nome:String,

    @NotNull
    @ApiModelProperty(position = 2, example = "exemplo@gmail.com")
    val email:String,

    @NotNull
    @ApiModelProperty(position = 3, example = "111.111.111-11")
    val cpf: String,

    @NotNull
    @ApiModelProperty(position = 4, example = "(85)99999-9999")
    val numeroCelular:String,

    @NotNull
    @ApiModelProperty(position = 5, example = "22/10/2001")
    val dataDeNascimento: LocalDate,

    @NotNull
    @ApiModelProperty(position = 6, example = "false")
    val emFila: Boolean,

    @NotNull
    @ApiModelProperty(position = 7, example = "Nome do Estabelecimento")
    val nomeEstabelecimento:String,

)