package com.api.ifila_backend.services

import com.api.ifila_backend.repositories.FilaRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class FilaService (val filaRespository: FilaRepository) {
    fun existsByUUID(filaId: UUID): Any {
        return filaRespository.findById(filaId)
    }
}