package com.api.ifila_backend.services

import com.api.ifila_backend.models.EstabelecimentoModel
import com.api.ifila_backend.repositories.EstabelecimentoRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class EstabelecimentoService(val estabelecimentoRepository: EstabelecimentoRepository) {

    @Transactional
    fun save(estabelecimentoModel: EstabelecimentoModel): Any? {
        return estabelecimentoRepository.save(estabelecimentoModel)
    }


}