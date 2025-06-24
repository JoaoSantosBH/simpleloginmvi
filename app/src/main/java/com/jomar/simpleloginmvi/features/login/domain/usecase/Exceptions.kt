package com.jomar.simpleloginmvi.features.login.domain.usecase

import java.util.Date

open class DomainException(message: String, title: String? = null) :
    RuntimeException(message, RuntimeException(title))


class MissingParamsException : ParamException("Params must not be null.")


sealed class ParamException(message: String, title: String? = null) :
    DomainException(message, title)


data class SystemMessages(
    val createdAt: Date,
    val messageList: List<Message>
)

data class Message(
    val code: String,
    val text: String
)