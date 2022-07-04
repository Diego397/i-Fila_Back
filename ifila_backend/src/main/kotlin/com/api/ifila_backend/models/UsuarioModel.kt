package com.api.ifila_backend.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "TB_USUARIO")
class   UsuarioModel {
    @Id
    @Column(name = "usuarioId")
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id:UUID? = null

    @Column(nullable = false, length = 70)
    lateinit var nome:String

    @Column(nullable = false)
    @JsonFormat(pattern = "dd/MM/yyyy")
    lateinit var dataDeNascimento:LocalDate

    @Column(nullable = false, unique = true, length = 100)
    lateinit var email:String

    @Column(nullable = false, length = 20)
    lateinit var numeroCelular:String

    @Column(nullable = false, unique = true, length = 20)
    lateinit var cpf:String

    @Column(nullable = false)
    var dataDeCriacao: LocalDateTime = LocalDateTime.now(ZoneId.of("America/Fortaleza"))

    @Column(nullable = false, length = 100)
    lateinit var role: String

    @Column(nullable = false)
    var emFila: Boolean = false

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "infoFilaId")
//    @JsonIgnore
    var infoFila: UsuarioInfoFilaModel? = null

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "estabelecimentoId")
//    @JsonIgnore
    var estabelecimento: EstabelecimentoModel? = null

    @Column(nullable = false, length = 100)
    @JsonIgnore
    var senha: String = ""
        get() = field
        set(value) {
            val senhaEncoder = BCryptPasswordEncoder()
            field = senhaEncoder.encode(value)
        }

    fun checarSenha(senha: String): Boolean {
        return BCryptPasswordEncoder().matches(senha, this.senha)
    }
}