package com.api.ifila_backend.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "TB_USUARIO")
class usuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id:UUID? = null

    @Column(nullable = false, length = 70)
    lateinit var nome:String

    @Column(nullable = false)
    lateinit var dataDeNascimento:LocalDate

    @Column(nullable = true, length = 350)
    lateinit var endereco:String

    @Column(nullable = false, length = 100)
    lateinit var email:String

    @Column(nullable = false, length = 20)
    lateinit var numeroCelular:String

    @Column(nullable = false, unique = true, length = 20)
    lateinit var cpf:String

}