package com.api.ifila_backend.dtos

import java.time.LocalDate
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

class UsuarioDTO (

    @field:NotBlank
    @Size(max=70)
    val nome:String,

    @NotNull
    val dataDeNascimento: LocalDate,

    @field:NotBlank
    @field:Size(max=100)
    @Email(regexp = ".+@.+\\..+")
    val email:String,

    @field:NotBlank
    @field:Size(max=20)
    val numeroCelular:String,

    @field:NotBlank
    @field:Size(max=20, message="cpf inválido")
    val cpf: String
)