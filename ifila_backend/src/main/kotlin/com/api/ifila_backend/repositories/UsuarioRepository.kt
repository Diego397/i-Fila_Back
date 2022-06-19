package com.api.ifila_backend.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.api.ifila_backend.models.UsuarioModel
import java.util.UUID

@Repository
interface UsuarioRepository  : JpaRepository<UsuarioModel, UUID>{
    fun existsByEmail(email: String): Boolean
    fun existsByCpf(cpf: String): Boolean
    fun findByEmail(email: String): UsuarioModel?
}