package br.com.dmcard.contadigital.data_remote.utils

import br.com.dmcard.contadigital.data_remote.model.generic.DmCardGenericResponse

interface RequestWrapper {

    suspend fun <T> wrapperGenericResponse(
        call: suspend () -> DmCardGenericResponse<T>
    ): DmCardGenericResponse<T>

    suspend fun <D> wrapper(
        retryCount: Int = 0,
        call: suspend () -> D
    ): D
}