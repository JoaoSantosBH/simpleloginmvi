package br.com.dmcard.contadigital.data_remote.utils

abstract class DataRemoteMapper<in R, out D> {
    abstract fun toDomain(data: R): D

    fun toDomain(data: List<R>) = data.map { toDomain(it) }
}