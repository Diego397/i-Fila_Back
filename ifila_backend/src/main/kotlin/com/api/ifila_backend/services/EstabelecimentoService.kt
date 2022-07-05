package com.api.ifila_backend.services

import com.api.ifila_backend.models.EstabelecimentoModel
import com.api.ifila_backend.repositories.EstabelecimentoRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class EstabelecimentoService(val estabelecimentoRepository: EstabelecimentoRepository) {

    @Transactional
    fun save(estabelecimentoModel: EstabelecimentoModel): Any? {
        return estabelecimentoRepository.save(estabelecimentoModel)
    }

    fun findAll(): List<EstabelecimentoModel> {
        return estabelecimentoRepository.findAll()
    }

    fun findById(id: UUID): Optional<EstabelecimentoModel> {
        return estabelecimentoRepository.findById(id)
    }

    @Transactional
    fun delete(estabelecimentoModel: EstabelecimentoModel) {
        return estabelecimentoRepository.delete(estabelecimentoModel)
    }

    fun findByCode(codigoEstab: String): Optional<EstabelecimentoModel> {
        return estabelecimentoRepository.findByCodigo(codigoEstab)
    }

    fun existsByCnpj(cnpj: String): Boolean {
        return estabelecimentoRepository.existsByCnpj(cnpj)
    }

    fun existsByCodigo(codigo: String): Boolean {
        return estabelecimentoRepository.existsByCodigo(codigo)
    }
}