package com.api.ifila_backend.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ApiModel
class UsuarioDTO (

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