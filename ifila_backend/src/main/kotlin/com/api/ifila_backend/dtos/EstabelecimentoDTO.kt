package com.api.ifila_backend.dtos

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class EstabelecimentoDTO (

    @field:NotBlank
    @Size(max=70)
    val nome:String,

    @field:NotBlank
    @Size(max=70)
    val endereco:String,

    @field:NotBlank
    @Size(max=20)
    val telefone:String
)