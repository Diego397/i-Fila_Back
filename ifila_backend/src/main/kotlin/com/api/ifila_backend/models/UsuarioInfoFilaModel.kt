package com.api.ifila_backend.models

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "TB_USUARIO_INFO_FILA")
class UsuarioInfoFilaModel(
    tipoFila: String,
    codigoFila: String
) {
    @Id
    @Column(name = "infoFilaId")
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null

    @Column(nullable = false)
    var horarioEntrada: LocalDateTime = LocalDateTime.now(ZoneId.of("America/Fortaleza"))

    @Column(nullable = false)
    var tipoFila: String = tipoFila

    @Column(nullable = false)
    var presencaSolicitada: Boolean = false

    @Column(nullable = false)
    var codigoFila: String = codigoFila
}