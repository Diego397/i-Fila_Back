package com.api.ifila_backend.dtos

import java.time.LocalDate
import java.time.ZonedDateTime
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

class UsuarioDTO (

    @NotBlank
    @Size(max=70)
    var nome:String,

    @NotNull
    var dataDeNascimento: LocalDate,

    @Size(max=350)
    var endereco:String,

    @NotBlank
    @Size(max=100)
    @Email
    var email:String,

    @NotBlank
    @Size(max=20)
    var numeroCelular:String,

    @NotBlank
    @Size(max=20)
    var cpf: String
)