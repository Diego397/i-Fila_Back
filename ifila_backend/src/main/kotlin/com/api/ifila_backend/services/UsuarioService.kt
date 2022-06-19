package com.api.ifila_backend.services

import com.api.ifila_backend.models.UsuarioModel
import com.api.ifila_backend.repositories.UsuarioRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class UsuarioService (val usuarioRepository: UsuarioRepository){

    @Transactional
    fun save(usuarioModel: UsuarioModel): Any? {
        return usuarioRepository.save(usuarioModel)
    }

    fun findById(id: UUID): Optional<UsuarioModel> {
        return usuarioRepository.findById(id)
    }

    fun existsByEmail(email: String): Boolean {
        return usuarioRepository.existsByEmail(email)
    }

    fun existsByCpf(cpf: String): Boolean {
        return usuarioRepository.existsByCpf(cpf)
    }

    fun findAll(): List<UsuarioModel> {
        return usuarioRepository.findAll()
    }

    fun delete(usuarioModel: UsuarioModel) {
        return usuarioRepository.delete(usuarioModel)
    }

    fun findByEmail(email: String): UsuarioModel? {
        return usuarioRepository.findByEmail(email)
    }
}