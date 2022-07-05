package com.api.ifila_backend.repositories

import com.api.ifila_backend.models.EstabelecimentoModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EstabelecimentoRepository : JpaRepository<EstabelecimentoModel, UUID> {
    fun findByCodigo(codigoEstab: String): Optional<EstabelecimentoModel>
    fun existsByCnpj(cnpj: String): Boolean
    fun existsByCodigo(codigo: String): Boolean
}