package com.api.ifila_backend.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalTime

@ApiModel
data class InfoFilaEstabelecimentoDTO (
    val tamanhoFilaPrincipal: Int,
    val tamanhoFilaPrioridade: Int,
    val maximoPessoasFila: Int,
    val clienteConfirmouPresenca: Boolean,
    val chamarCliente: Boolean,

    @JsonFormat(pattern = "HH:mm")
    @ApiModelProperty(example = "00:10")
    val tempoMedio: LocalTime,

    @JsonFormat(pattern = "HH:mm")
    @ApiModelProperty(example = "22:10")
    val horarioMaximoEntrada: LocalTime
)