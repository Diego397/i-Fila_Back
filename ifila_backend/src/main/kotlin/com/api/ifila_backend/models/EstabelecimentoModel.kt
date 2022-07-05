package com.api.ifila_backend.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "TB_ESTABELECIMENTO")
class EstabelecimentoModel {
    @Id
    @Column(name = "estabelecimentoId")
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null

    @Column(nullable = false, length = 7)
    lateinit var codigo: String

    @Column(nullable = false, length = 70)
    lateinit var nome:String

    @Column(nullable = false, length = 70)
    lateinit var endereco:String

    @Column(nullable = false, length = 20)
    lateinit var telefone: String

    @Column(nullable = false, length = 20)
    lateinit var cnpj: String

    @Column(nullable = false)
    lateinit var descricao: String

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm")
    lateinit var horarioAbertura: LocalTime

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm")
    lateinit var horarioFechamento: LocalTime

    @Column(nullable = false)
    var dataDeCriacao: ZonedDateTime = ZonedDateTime.now()

    @Column(nullable = false)
    var statusFila: Boolean = false

    @Column(nullable = false)
    lateinit var categoria: String

    @Column(nullable = false)
    lateinit var linkImagem: String

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "filaId")
    @JsonIgnore
    var fila: FilaModel? = null
}