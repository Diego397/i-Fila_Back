package com.api.ifila_backend.models

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonManagedReference
import java.time.LocalTime
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

    @Column(name = "horarioMaximo")
    var horarioMaximo: LocalTime? = null

    @Column(name = "codigoFila" )
    var codigoFila: String? = null

    @Column(name = "clienteConfirmouPresenca" )
    var clienteConfirmouPresenca: Boolean = false

    @Column(name = "chamarCliente" )
    var chamarCliente: Boolean = false

    @Column(name = "filaPrincipal")
    @ElementCollection
    var filaPrincipal: MutableList<UUID> = mutableListOf<UUID>()

    @Column(name = "filaPrioridade")
    @ElementCollection
    var filaPrioridade: MutableList<UUID> = mutableListOf<UUID>()

    @OneToOne(mappedBy = "fila", cascade = [CascadeType.ALL])
    @JsonBackReference
    lateinit var estabelecimento: EstabelecimentoModel
}