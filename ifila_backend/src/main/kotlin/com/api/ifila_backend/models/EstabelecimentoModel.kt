package com.api.ifila_backend.models

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
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

    @Column(nullable = false, length = 70)
    lateinit var nome:String

    @Column(nullable = false, length = 70)
    lateinit var endereco:String

    @Column(nullable = false, length = 20)
    lateinit var telefone: String

    @Column(nullable = false)
    var dataDeCriacao: ZonedDateTime = ZonedDateTime.now()
}