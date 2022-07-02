package com.api.ifila_backend.repositories

import com.api.ifila_backend.models.FilaModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface FilaRepository: JpaRepository<FilaModel, UUID> {
    fun existsByCodigoFila(codigoFila: String):Boolean
    fun findByCodigoFila(codigoFila: String):Optional<FilaModel>
}