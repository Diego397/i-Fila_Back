package com.api.ifila_backend.models

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "TB_FILA")
class FilaModel {
    @Id
    @Column(name = "filaId")
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null

    @Column(name = "capacidadeMaxima")
    var capacidadeMaxima: Int? = null

    @OneToOne(mappedBy = "fila")
    @JsonManagedReference
    lateinit var estabelecimento: EstabelecimentoModel

    @Column(name = "filaPrincipal")
    @ElementCollection
    var filaPrincipal: MutableList<UUID> = mutableListOf<UUID>()

    @Column(name = "filaPrioridade")
    @ElementCollection
    var filaPrioridade: MutableList<UUID> = mutableListOf<UUID>()
}