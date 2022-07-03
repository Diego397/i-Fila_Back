package com.api.ifila_backend.services

import com.api.ifila_backend.models.FilaModel
import com.api.ifila_backend.models.UsuarioModel
import com.api.ifila_backend.repositories.FilaRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class FilaService (val filaRepository: FilaRepository) {
    fun existsByUUID(filaId: UUID): Any {
        return filaRepository.findById(filaId)
    }

    fun existsByCode(codigoFila: String): Boolean {
        return filaRepository.existsByCodigoFila(codigoFila)
    }

/*    fun findAll(): List<> {
        return filaRepository.findAll()
    }*/

    fun findByCode(codigoFila: String): Optional<FilaModel> {
        return filaRepository.findByCodigoFila(codigoFila)
    }

    @Transactional
    fun save(filaModel: FilaModel): FilaModel {
        return filaRepository.save(filaModel)
    }

//    fun findByFilaPrincipal(): List<UUID>? {
//        return filaRepository.findByFilaPrincial()
//    }
}