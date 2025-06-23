package br.com.dmcard.contadigital.data_remote.utils.constants

import br.com.dmcard.contadigital.data.exceptions.*
import br.com.dmcard.contadigital.domain.exception.DataSourceException
import br.com.dmcard.contadigital.domain.model.message.Message

enum class ErrorMessageEnum(val value: String) {
    INTERNET_ERROR("Tivemos um problema de conexão, tente novamente mais tarde."),
    GENERIC_ERROR("Tivemos um problema ao conectar ao servidor. Tente novamente mais tarde."),
    INVALID_PAYLOAD_ERROR("Dados inválidos."),
    UNAUTHORIZED_ERROR("Para sua segurança, por favor, faça o login novamente. (E003)"),
    SERVICE_UNAVAILABLE_ERROR("Nosso serviço está temporariamente indisponível. Tente novamente mais tarde."),
    AUTHENTICATION_TIMEOUT_ERROR("Tempo da operação expirado"),
    INVALID_LOGIN_ERROR("Dados incorretos"),
    MAPPING_ERROR("Erro inesperado."),
    CERTIFICATE_PINNING_ERROR("Ocorreu um erro no certificado. Por favor, tente novamente mais tarde."),
    INVALID_CREDENTIALS("Senha inválida."),
    BLOCK_TAX_ID_ERROR("Estamos em manutenção, por favor aguarde."),
    INVALID_SMS_CODE_ERROR("Código de verificação inválido. \nVerifique e tente novamente."),
    LIMIT_REACHED_SMS_CODE_ERROR("Código de verificação inválido. \nTente um novo código."),
    BLOCKED_ACCOUNT_ERROR("Conta bloqueada."),
    INVALID_PROPOSAL_AGE_ERROR("Menor de idade"),
    PIX_REQUEST_PORTABILITY_ERROR("Key in use, request portability"),
    PIX_MAXIMUM_KEY_CREATION_ATTEMPTS_EXCEED_ERROR("Cadastro de chave pendente \n\nFalha de comunicação. \nTente novamente"),
    PIX_REQUEST_CLAIM_ERROR("Key in use, request claim"),
    PIX_KEY_DUPLICATED_ERROR("Essa chave já foi registrada por você, não é necessário a cadastrar novamente"),
    PIX_KEY_ALREADY_REGISTRED_BY_OTHER_USER_ERROR("Essa chave já foi registrada por outra pessoa."),
    PIX_KEY_NOT_FOUND_ERROR("Chave não encontrada."),
    BILL_ALREADY_SCHEDULED_ERROR("Esse pagamento já foi agendado."),
    SINGLE_DEVICE_ERROR("Novo dispositivo."),
    BLOCKED_DEVICE_ACCOUNT_ERROR("Por questões de segurança, sua conta foi bloqueada. Por favor, entre em contato com a DMCard"),
    MAXIMUM_TOKEN_VALIDATION_ATTEMPTS_EXCEEDED_ERROR("Você excedeu a quantidade máxima de tentativas. Por segurança, você deverá aguardar 24 horas."),
    VALIDATE_DEVICE_TOKEN_BLOCKED_ACCOUNT_ERROR("Por questões de segurança, sua conta foi bloqueada. Por favor, entre em contato com a DMCard"),
    PROPOSAL_UNAVAILABLE_TEMPORARILY_ERROR("Criação de nova proposta indisponível temporariamente"),
    NO_DATA_TO_QUIZ_ERROR("Não há dados relacionados ao CPF para que sejam geradas as perguntas para a proposta"),
    NO_MORE_QUESTIONS_FOR_PROPOSAL_ERROR("Não há mais perguntas para a proposta"),
    PAYMENT_NOT_COMPLETED_LINK_EXPIRED_ERROR("QR Code / Link vencido. O pagamento desse QR Code / Link não é permitido após o vencimento."),
    PAYMENT_NOT_COMPLETED_LINK_INVALID_ERROR("QR Code / Link inválido. Tente novamente com outro QR Code ou Link."),
    LIMIT_SEARCH_PIX_KEY_ERROR("Você excedeu o número de tentativas permitidas para a busca de chaves."),
    PROPOSAL_DECLINED_ERROR("Proposta recusada"),
    FAVORITE_CONTACT_ALREADY_SAVE_ERROR("Esse contato já foi salvo como favorito."),
    USER_NOT_SIGNED_CONTRACT_ERROR("Os Termos de uso da DMConta foram atualizados, para continuar, por favor, leia e aceite o termo."),
    CARD_INCREASE_LIMIT_NEED_TO_PAID_INVOICES_ERROR("Não foi possível atualizar seu limite, você poderá ter aumento de limite pagando 4 faturas seguidas."),
    CARD_INCREASE_LIMIT_RECENTLY_CREATED_CARD_ERROR("Não conseguimoas aumentar o limite do seu cartão nesse momento."),
    CARD_INCREASE_LIMIT_RECENTLY_REQUEST_ERROR("Você solicitou um aumento de limite recentemente."),
    CARD_INCREASE_LIMIT_UNABLE_NOW_ERROR("Não foi possivel aumentar o seu limite."),
    CARD_INCREASE_LIMIT_NOT_EVALUATION_ERROR("No momento, o cartão não está avaliando pedidos de aumento de limite aos seus clientes"),
    CARD_INCREASE_LIMIT_REACHED_MAX_VALUE_ERROR("Você já atingiu o limite máximo deste cartão."),
    ERROR_COMMUNICATING_TO_SERVER_ERROR("Tivemos um erro no sistema e não conseguimos fazer sua análise. Espere alguns minutos ou tente novamente mais tarde."),
    CARD_INCREASE_LIMIT_SOLICITATION_TIME_ERROR("O aumento de limite deve ser solicitado entre 6h e 23h. Tente novamente dentro deste horário."),
    CARD_UPDATE_EXPIRATION_DATE_ERROR("Parece que você já alterou o vencimento da sua fatura recentemente.\n\nVocê poderá mudá-la novamente no dia **/**/****."),
    CARD_UPDATE_EXPIRATION_TIME_ERROR("Desculpe! Não é possível realizar alterações neste horário. Por favor, tente novamente após as 06h."),
    CARD_NOT_FOUND_ERROR("Cartão não encontrado"),
    CARD_CONTACT_NOT_FOUND_ERROR("Você não possui e-mail ou celular cadastrados no seu cartão. Entre em contato com nosso SAC antes de redefinir a senha."),
    PASSWORD_BLOCKED_MANY_INVALID_ATT_ERROR("Por favor, entre em contato com nosso SAC."),
    INVALID_CURRENT_PASSWORD_TWO_ATT_ERROR("Você pode tentar novamente mais 2 vezes."),
    INVALID_CURRENT_PASSWORD_ONE_ATT_ERROR("Você pode tentar novamente mais 1 vez."),
    PASSWORD_CANNOT_BE_BIRTH_DATE_ERROR("A nova senha informada não pode ser igual a sua data de aniversário."),
    PASSWORD_CANNOT_BE_SAME_ERROR("A nova senha informada não pode ser igual a senha atual."),
    NEW_PASSWORD_INVALID_ERROR("A nova senha informada é inválida."),
    INVALID_PASSWORD_TWO_ATT_ERROR("Você pode tentar novamente mais 2 vezes."),
    INVALID_PASSWORD_ONE_ATT_ERROR("Você pode tentar novamente mais 1 vez."),
    UNBLOCK_PERMANENT_TOO_MANY_ATT_ERROR("Cartão de desbloqueio ultrapassado número de tentativas"),
    UNBLOCK_PERMANENT_TWO_ATT_ERROR("Dados inválidos, faltam duas tentativas"),
    UNBLOCK_PERMANENT_ONE_ATT_ERROR("Dados inválidos, uma tentativa restante"),
    UNBLOCK_PERMANENT_TOKEN_SMS_EXPIRED_ERROR("Token sms expirou"),
    UNBLOCK_TOKEN_TOO_MANY_ATT_ERROR("Cartão de desbloqueio ultrapassado número de tentativas"),
    UNBLOCK_CARD_DATA_NOT_FOUND_ERROR("Parece que uma das informações que você digitou não está correta."),
    PERSONAL_LOAN_REQUEST_LIMIT_ERROR("Estamos com um problema no sistema. Você pode tentar novamente ou voltar mais tarde."),
    DAILY_LIMIT_RAISE_VALUE_ERROR("Não é possível aumentar o limite acima do limite superior"),
    DAILY_LIMIT_REDUCE_VALUE_ERROR("Não é possível reduzir o limite abaixo do limite inferior"),
    NOT_ENOUGH_DAILY_LIMIT_ERROR("Valor acima do limite diário."),
    DMCRED_NO_DMCRED_WAS_FOUND_ERROR("Usuário não possui DMCred"),
    DMCRED_USER_HAVE_ACTIVE_LOAN_ERROR("Usuário possuí DMCred ativo"),
    DMCRED_OUT_OF_QUOTES_ERROR("Sem cotas disponíveis no momento");
}

class MappingErrorException(cause: Throwable? = null) :
    DataSourceException(ErrorMessageEnum.MAPPING_ERROR.value, cause)

open class RemoteDataSourceException(
    message: String = ErrorMessageEnum.GENERIC_ERROR.value,
    code: String,
    cause: Throwable? = null,
    messageList: List<Message>
) : DataSourceException(message = message, cause = cause, code = code, messageList = messageList)

class ServerError(
    message: String = ErrorMessageEnum.GENERIC_ERROR.value,
    cause: Throwable? = null
) : DataSourceException(message = message, cause = cause)

class ServiceUnavailableException(
    message: String = ErrorMessageEnum.SERVICE_UNAVAILABLE_ERROR.value,
    code: String
) : ServiceUnavailableException(message = message, code = code)

class UnauthorizedException(
    message: String = ErrorMessageEnum.UNAUTHORIZED_ERROR.value,
    code: String
) : UnauthorizedException(message, code)

class AuthenticationTimeoutException(
    message: String = ErrorMessageEnum.AUTHENTICATION_TIMEOUT_ERROR.value,
    code: String
) : UnauthorizedException(message, code)

class CertificateErrorException(
    message: String = ErrorMessageEnum.CERTIFICATE_PINNING_ERROR.value, cause: Throwable? = null
) : DataSourceException(message = message, cause = cause)

class InvalidCredentialsException(
    message: String = ErrorMessageEnum.INVALID_CREDENTIALS.value,
    code: String
) : InvalidCredentialsException(message = message, code = code)

class BlockTaxIdException(
    message: String = ErrorMessageEnum.BLOCK_TAX_ID_ERROR.value,
    code: String
) : BlockTaxIdException(message = message, code = code)

class LimitReachedException(
    attempts: Int,
    message: String = ErrorMessageEnum.LIMIT_REACHED_SMS_CODE_ERROR.value,
    code: String
) : LimitReachedException(attempts = attempts, message = message, code = code)

class BlockedAccountException(
    message: String = ErrorMessageEnum.BLOCKED_ACCOUNT_ERROR.value,
    code: String
) : BlockedAccountException(message = message, code = code)

class InvalidProposalAgeException(
    message: String = ErrorMessageEnum.INVALID_PROPOSAL_AGE_ERROR.value,
    code: String
) : InvalidProposalAgeException(message = message, code = code)

class SingleDeviceException(
    message: String = ErrorMessageEnum.SINGLE_DEVICE_ERROR.value,
    code: String
) : SingleDeviceException(message = message, code = code)

class AccountBlockedByValidateDeviceIdException(
    message: String = ErrorMessageEnum.BLOCKED_DEVICE_ACCOUNT_ERROR.value,
    code: String
) : AccountBlockedByValidateDeviceIdException(message = message, code = code)

class MaximumTokenValidationAttemptsExceededException(
    message: String = ErrorMessageEnum.MAXIMUM_TOKEN_VALIDATION_ATTEMPTS_EXCEEDED_ERROR.value,
    code: String
) : MaximumTokenValidationAttemptsExceededException(message = message, code = code)

class InvalidDeviceTokenException(
    message: String = ErrorMessageEnum.VALIDATE_DEVICE_TOKEN_BLOCKED_ACCOUNT_ERROR.value,
    code: String
) : InvalidDeviceTokenException(message = message, code = code)

class BillAlreadyScheduledException(
    message: String = ErrorMessageEnum.BILL_ALREADY_SCHEDULED_ERROR.value,
    code: String?
) : BillAlreadyScheduledException(message, code)

class PixRequestPortabilityException(
    message: String = ErrorMessageEnum.PIX_REQUEST_PORTABILITY_ERROR.value,
    code: String
) : PixRequestPortabilityException(message = message, code = code)

class PixRequestClaimException(
    message: String = ErrorMessageEnum.PIX_REQUEST_CLAIM_ERROR.value,
    code: String
) : PixRequestClaimException(message = message, code = code)

class PixDuplicatedKeyException(
    message: String = ErrorMessageEnum.PIX_KEY_DUPLICATED_ERROR.value,
    code: String
) : PixDuplicatedKeyException(message = message, code = code)

class PixKeyAlreadyRegistredException(
    message: String = ErrorMessageEnum.PIX_KEY_ALREADY_REGISTRED_BY_OTHER_USER_ERROR.value,
    code: String
) : PixKeyAlreadyRegistredException(message = message, code = code)

class PixKeyNotFoundException(
    message: String = ErrorMessageEnum.PIX_KEY_NOT_FOUND_ERROR.value,
    code: String
) : PixKeyNotFoundException(message = message, code = code)

class PixKeyCreationAttemptsExceededException(
    message: String = ErrorMessageEnum.PIX_MAXIMUM_KEY_CREATION_ATTEMPTS_EXCEED_ERROR.value,
    code: String
) : PixKeyCreationAttemptsExceededException(message = message, code = code)

class ProposalNotAvailableException(
    message: String = ErrorMessageEnum.PROPOSAL_UNAVAILABLE_TEMPORARILY_ERROR.value,
    code: String?
) : ProposalNotAvailableException(message, code)

class NoDataToQuizException(
    message: String = ErrorMessageEnum.NO_DATA_TO_QUIZ_ERROR.value,
    code: String?
) : NoDataToQuizException(message, code)

class NoMoreQuestionException(
    message: String = ErrorMessageEnum.NO_MORE_QUESTIONS_FOR_PROPOSAL_ERROR.value,
    code: String?
) : NoMoreQuestionException(message, code)

class LimitSearchPixKeyException(
    minutes: Int,
    message: String = ErrorMessageEnum.LIMIT_SEARCH_PIX_KEY_ERROR.value,
    code: String
) : LimitSearchPixKeyException(minutes, message, code)

class ProposalDeclinedException(
    message: String = ErrorMessageEnum.PROPOSAL_DECLINED_ERROR.value,
    code: String?,
    days: String
) : ProposalDeclinedException(message, code, days)

class UnknowErrorException(
    message: String = ErrorMessageEnum.GENERIC_ERROR.value,
    code: String?
) : UnknowErrorException(message, code)

class FavoriteContactAlreadySaveException(
    message: String = ErrorMessageEnum.FAVORITE_CONTACT_ALREADY_SAVE_ERROR.value,
    code: String?
) : FavoriteContactAlreadySaveException(message, code)

class InvalidPayloadException(
    message: String = ErrorMessageEnum.INVALID_PAYLOAD_ERROR.value,
    code: String?
) : InvalidPayloadException(message, code)

class UserNotSignedContractException(
    message: String = ErrorMessageEnum.USER_NOT_SIGNED_CONTRACT_ERROR.value,
    code: String?
) : UserNotSignedContractException(message, code)

class CardUpdateExpirationDateException(
    message: String = ErrorMessageEnum.CARD_UPDATE_EXPIRATION_DATE_ERROR.value,
    code: String?,
    date: String
) : CardUpdateExpirationDateException(message, code, date)

class CardUpdateExpirationTimeException(
    message: String = ErrorMessageEnum.CARD_UPDATE_EXPIRATION_TIME_ERROR.value,
    code: String?
) : CardUpdateExpirationTimeException(message, code)

class PaymentNotCompletedLinkExpiredException(
    message: String = ErrorMessageEnum.PAYMENT_NOT_COMPLETED_LINK_EXPIRED_ERROR.value,
    code: String?
) : PaymentNotCompletedLinkExpiredException(message, code)

class PaymentNotCompletedLinkInvalidException(
    message: String = ErrorMessageEnum.PAYMENT_NOT_COMPLETED_LINK_INVALID_ERROR.value,
    code: String?
) : PaymentNotCompletedLinkInvalidException(message, code)

class CardIncreaseLimitNeedToPaidInvoicesException(
    paid: String,
    message: String = ErrorMessageEnum.CARD_INCREASE_LIMIT_NEED_TO_PAID_INVOICES_ERROR.value,
    code: String?
) : CardIncreaseLimitNeedToPaidInvoicesException(paid, message, code)

class CardIncreaseLimitRecentlyCreatedException(
    days: String,
    message: String = ErrorMessageEnum.CARD_INCREASE_LIMIT_RECENTLY_CREATED_CARD_ERROR.value,
    code: String?
) : CardIncreaseLimitRecentlyCreatedException(days, message, code)

class CardIncreaseLimitRecentlyRequestException(
    days: String,
    message: String = ErrorMessageEnum.CARD_INCREASE_LIMIT_RECENTLY_REQUEST_ERROR.value,
    code: String?
) : CardIncreaseLimitRecentlyRequestException(days, message, code)

class CardIncreaseLimitReachedMaxValueException(
    message: String = ErrorMessageEnum.CARD_INCREASE_LIMIT_REACHED_MAX_VALUE_ERROR.value,
    code: String?
) : CardIncreaseLimitReachedMaxValueException(message, code)

class CardIncreaseLimitSolicitationTimeException(
    startTime: String, endTime: String,
    message: String = ErrorMessageEnum.CARD_INCREASE_LIMIT_SOLICITATION_TIME_ERROR.value,
    code: String?
) : CardIncreaseLimitSolicitationTimeException(startTime, endTime, message, code)

class CardIncreaseLimitNotEvaluatingException(
    message: String = ErrorMessageEnum.CARD_INCREASE_LIMIT_NOT_EVALUATION_ERROR.value,
    code: String?
) : CardIncreaseLimitNotEvaluatingException(message, code)

class CardIncreaseLimitUnableNowException(
    days: String,
    message: String = ErrorMessageEnum.CARD_INCREASE_LIMIT_UNABLE_NOW_ERROR.value,
    code: String?
) : CardIncreaseLimitUnableNowException(days, message, code)

class CardNotFoundException(
    message: String = ErrorMessageEnum.CARD_NOT_FOUND_ERROR.value,
    code: String?
) : CardNotFoundException(message, code)

class CardContactNotFoundException(
    message: String = ErrorMessageEnum.CARD_CONTACT_NOT_FOUND_ERROR.value,
    code: String?
) : CardContactNotFoundException(message, code)

class InvalidCurrentPasswordTwoAttemptsException(
    message: String = ErrorMessageEnum.INVALID_CURRENT_PASSWORD_TWO_ATT_ERROR.value,
    code: String?
) : InvalidCurrentPasswordTwoAttemptsException(message, code)

class InvalidCurrentPasswordOneAttemptsException(
    message: String = ErrorMessageEnum.INVALID_CURRENT_PASSWORD_ONE_ATT_ERROR.value,
    code: String?
) : InvalidCurrentPasswordOneAttemptsException(message, code)

class PasswordBlockedManyAttemptsException(
    message: String = ErrorMessageEnum.PASSWORD_BLOCKED_MANY_INVALID_ATT_ERROR.value,
    code: String?
) : PasswordBlockedManyAttemptsException(message, code)

class PasswordCannotBeBirthDateException(
    message: String = ErrorMessageEnum.PASSWORD_CANNOT_BE_BIRTH_DATE_ERROR.value,
    code: String?
) : PasswordCannotBeBirthDateException(message, code)

class PasswordCannotBeSameException(
    message: String = ErrorMessageEnum.PASSWORD_CANNOT_BE_SAME_ERROR.value,
    code: String?
) : PasswordCannotBeSameException(message, code)

class NewPasswordInvalidException(
    message: String = ErrorMessageEnum.NEW_PASSWORD_INVALID_ERROR.value,
    code: String?
) : NewPasswordInvalidException(message, code)

class InvalidPasswordTwoAttemptsException(
    message: String = ErrorMessageEnum.INVALID_PASSWORD_TWO_ATT_ERROR.value,
    code: String?
) : InvalidPasswordTwoAttemptsException(message, code)

class InvalidPasswordOneAttemptsException(
    message: String = ErrorMessageEnum.INVALID_PASSWORD_ONE_ATT_ERROR.value,
    code: String?
) : InvalidPasswordOneAttemptsException(message, code)

class UnblockPermanentTooManyAttemptsException(
    message: String = ErrorMessageEnum.UNBLOCK_PERMANENT_TOO_MANY_ATT_ERROR.value,
    code: String?
) : UnblockPermanentTooManyAttemptsException(message, code)

class UnblockPermanentTwoAttemptsException(
    message: String = ErrorMessageEnum.UNBLOCK_PERMANENT_TOO_MANY_ATT_ERROR.value,
    code: String?
) : UnblockPermanentTwoAttemptsException(message, code)

class UnblockPermanentOneAttemptException(
    message: String = ErrorMessageEnum.UNBLOCK_PERMANENT_TOO_MANY_ATT_ERROR.value,
    code: String?
) : UnblockPermanentOneAttemptException(message, code)

class UnblockPermanentTokenExpiredException(
    message: String = ErrorMessageEnum.UNBLOCK_PERMANENT_TOO_MANY_ATT_ERROR.value,
    code: String?
) : UnblockPermanentTokenExpiredException(message, code)

class UnblockTokenTooManyAttemptsException(
    message: String = ErrorMessageEnum.UNBLOCK_TOKEN_TOO_MANY_ATT_ERROR.value,
    code: String?
) : UnblockTokenTooManyAttemptsException(message, code)

class UnblockCardDataNotFoundException(
    message: String = ErrorMessageEnum.UNBLOCK_CARD_DATA_NOT_FOUND_ERROR.value,
    code: String?
) : UnblockCardDataNotFoundException(message, code)

class CommunicatingToServerException(
    message: String = ErrorMessageEnum.ERROR_COMMUNICATING_TO_SERVER_ERROR.value,
    code: String?
) : CommunicatingToServerException(message, code)

class PersonalLoanRequestLimitException(
    message: String = ErrorMessageEnum.PERSONAL_LOAN_REQUEST_LIMIT_ERROR.value,
    code: String?
) : PersonalLoanRequestLimitException(message, code)

class ValueAboveDailyLimitException(
    message: String = ErrorMessageEnum.NOT_ENOUGH_DAILY_LIMIT_ERROR.value,
    code: String?
) : ValueAboveDailyLimitException(message, code)

class DailyLimitRaiseValueRequestException(
    message: String = ErrorMessageEnum.DAILY_LIMIT_RAISE_VALUE_ERROR.value,
    code: String?,
    maxLimit: String
) : DailyLimitValueRequestException(message, code, maxLimit)

class DailyLimitReduceValueRequestException(
    message: String = ErrorMessageEnum.DAILY_LIMIT_REDUCE_VALUE_ERROR.value,
    code: String?,
    minLimit: String
) : DailyLimitValueRequestException(message, code, minLimit)

class NoDMCredWasFoundException(
    message: String = ErrorMessageEnum.DMCRED_NO_DMCRED_WAS_FOUND_ERROR.value,
    code: String?
) : NoDMCredWasFoundException(message, code)

class UserHaveDMCredActiveException(
    message: String = ErrorMessageEnum.DMCRED_USER_HAVE_ACTIVE_LOAN_ERROR.value,
    code: String?
) : UserHaveDMCredActiveException(message, code)

class DMCredOutOfQuotesException(
    message: String = ErrorMessageEnum.DMCRED_USER_HAVE_ACTIVE_LOAN_ERROR.value,
    code: String?
) : DMCredOutOfQuotesException(message, code)
