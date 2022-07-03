package com.api.ifila_backend.services

import com.api.ifila_backend.models.UsuarioInfoFilaModel
import com.api.ifila_backend.repositories.UsuarioInfoFilaRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UsuarioInfoFilaService (val usuarioInfoFilaRepository: UsuarioInfoFilaRepository) {

    @Transactional
    fun delete(usuarioInfoFilaModel: UsuarioInfoFilaModel) {
        return usuarioInfoFilaRepository.delete(usuarioInfoFilaModel)
    }
}