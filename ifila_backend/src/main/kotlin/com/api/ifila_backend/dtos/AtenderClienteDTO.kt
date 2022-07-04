package com.api.ifila_backend.dtos

import java.time.LocalDate
import java.util.*

data class AtenderClienteDTO(
    val id: UUID? = null,
    var nome:String,
    var dataDeNascimento: LocalDate,
    var email:String,
    var numeroCelular:String,
    var cpf:String,
    var mensagem:String
)