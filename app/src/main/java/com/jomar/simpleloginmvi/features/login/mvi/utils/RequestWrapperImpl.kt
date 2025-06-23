@file:Suppress("MemberVisibilityCanBePrivate")

package br.com.dmcard.contadigital.data_remote.utils

import br.com.dmcard.contadigital.data.datasource.local.SessionLocalDataSource
import br.com.dmcard.contadigital.data.datasource.local.UserLocalDataSource
import br.com.dmcard.contadigital.data.datasource.remote.SessionRemoteDataSource
import br.com.dmcard.contadigital.data_remote.mapper.cep.MessageMapper
import br.com.dmcard.contadigital.data_remote.model.error.WSO2ErrorResponse
import br.com.dmcard.contadigital.data_remote.model.generic.DmCardGenericResponse
import br.com.dmcard.contadigital.data_remote.model.generic.DmCardGenericTokenResponse
import br.com.dmcard.contadigital.data_remote.model.generic.DmCardMessageResponse
import br.com.dmcard.contadigital.data_remote.utils.constants.*
import br.com.dmcard.contadigital.data_remote.utils.constants.Const.CODES_FOR_UPDATING_TOKEN
import br.com.dmcard.contadigital.data_remote.utils.constants.ErrorMessageEnum.*
import br.com.dmcard.contadigital.data_remote.utils.extensions.*
import br.com.dmcard.contadigital.domain.exception.DataSourceException
import br.com.dmcard.contadigital.domain.model.message.Message
import br.com.dmcard.contadigital.domain.repository.SystemMessageRepository
import br.com.dmcard.contadigital.domain.utils.constants.*
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.single
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.HttpException
import java.io.IOException
import javax.net.ssl.SSLHandshakeException

class RequestWrapperImpl : RequestWrapper, KoinComponent {

    private val sessionLocalDataSource: SessionLocalDataSource by inject()
    private val userLocalDataSource: UserLocalDataSource by inject()
    private val sessionRemoteDataSource: SessionRemoteDataSource by inject()
    private val systemMessageRepository: SystemMessageRepository by inject()
    private val errorRequestRegex = """(\d+)|((?i)[wec].*)""".toRegex()

    @Synchronized
    override suspend fun <T> wrapperGenericResponse(
        call: suspend () -> DmCardGenericResponse<T>
    ): DmCardGenericResponse<T> {
        val result = wrapper(call = call)
        val code = result.getCode()

        return when {
            errorRequestRegex.matches(code ?: "") -> throw handleExceptionByCode(result)
            else -> result
        }
    }

    @Synchronized
    override suspend fun <D> wrapper(retryCount: Int, call: suspend () -> D): D {
        return try {
            call().also { result ->
                if (result is DmCardGenericResponse<*>) {
                    val code = result.getCode()
                    when {
                        errorRequestRegex.matches(code ?: "") -> throw handleExceptionByCode(result)
                        else -> {
                            translateSuccessMessage(result)
                            checkIfShouldSaveDMCardToken(result.token)
                        }
                    }
                }
            }
        } catch (e: SSLHandshakeException) {
            throw CertificateErrorException(cause = e)
        } catch (httpException: HttpException) {
            return handleHttpException(httpException, call, retryCount)
        } catch (ioException: IOException) {
            throw ServerError(cause = ioException)
        } catch (stateException: IllegalStateException) {
            throw ServerError(cause = stateException)
        }
    }

    private fun verifyBlockTaxIdCode(code: String) = if (BLOCK_TAX_ID.contains(code)) code else ""

    private fun verifyProposalDeclinedCode(code: String) =
        if (PROPOSAL_DECLINED_CODE.contains(code)) code else ""

    private suspend fun handleExceptionByCode(
        result: DmCardGenericResponse<*>, json: String? = null
    ): Exception {
        val code = result.getCode()
        val messageList = result.messageList
        val mapMessageList = mapMessageList(messageList, json = json)
        val message = mapMessageList.getOrNull(0)?.text

        return when (code) {
            FAVORITE_CONTACT_ALREADY_SAVE_CODE -> {
                FavoriteContactAlreadySaveException(
                    message = message ?: FAVORITE_CONTACT_ALREADY_SAVE_ERROR.value,
                    code = FAVORITE_CONTACT_ALREADY_SAVE_CODE
                )
            }

            NO_DATA_TO_QUIZ_CODE -> {
                NoDataToQuizException(
                    message = message ?: NO_DATA_TO_QUIZ_ERROR.value,
                    code = NO_DATA_TO_QUIZ_CODE
                )
            }

            NO_MORE_QUESTIONS_FOR_PROPOSAL_CODE -> {
                NoMoreQuestionException(
                    message = message ?: NO_MORE_QUESTIONS_FOR_PROPOSAL_ERROR.value,
                    code = NO_MORE_QUESTIONS_FOR_PROPOSAL_CODE
                )
            }

            INVALID_CREDENTIALS_CODE -> {
                InvalidCredentialsException(
                    message = message ?: INVALID_CREDENTIALS.value,
                    code = INVALID_CREDENTIALS_CODE
                )
            }

            LIMIT_REACHED_SMS_CODE -> {
                LimitReachedException(
                    attempts = 5,
                    message = message ?: LIMIT_REACHED_SMS_CODE_ERROR.value,
                    code = LIMIT_REACHED_SMS_CODE
                )
            }

            INVALID_SMS_CODE -> {
                LimitReachedException(
                    attempts = 1,
                    message = message ?: INVALID_SMS_CODE_ERROR.value,
                    code = INVALID_SMS_CODE
                )
            }

            PIX_REQUEST_PORTABILITY_CODE -> {
                PixRequestPortabilityException(
                    message = message ?: PIX_REQUEST_PORTABILITY_ERROR.value,
                    code = PIX_REQUEST_PORTABILITY_CODE
                )
            }

            PIX_MAXIMUM_KEY_CREATION_ATTEMPTS_EXCEEDED_CODE -> {
                PixKeyCreationAttemptsExceededException(
                    message = PIX_MAXIMUM_KEY_CREATION_ATTEMPTS_EXCEED_ERROR.value,
                    code = PIX_MAXIMUM_KEY_CREATION_ATTEMPTS_EXCEEDED_CODE
                )
            }

            PIX_REQUEST_CLAIM_CODE -> {
                PixRequestClaimException(
                    message = message ?: PIX_REQUEST_CLAIM_ERROR.value,
                    code = PIX_REQUEST_CLAIM_CODE
                )
            }

            PIX_KEY_DUPLICATED_CODE -> {
                PixDuplicatedKeyException(
                    message = message ?: PIX_KEY_DUPLICATED_ERROR.value,
                    code = PIX_KEY_DUPLICATED_CODE
                )
            }

            PIX_KEY_ALREADY_REGISTRED_BY_OTHER_USER_CODE -> {
                PixKeyAlreadyRegistredException(
                    message = message ?: PIX_KEY_ALREADY_REGISTRED_BY_OTHER_USER_ERROR.value,
                    code = PIX_KEY_ALREADY_REGISTRED_BY_OTHER_USER_CODE
                )
            }

            PIX_KEY_NOT_FOUND_CODE -> {
                PixKeyNotFoundException(
                    message = message ?: PIX_KEY_NOT_FOUND_ERROR.value,
                    code = PIX_KEY_NOT_FOUND_CODE
                )
            }

            DEVICE_NOT_ALLOWED -> {
                SingleDeviceException(
                    message = message ?: SINGLE_DEVICE_ERROR.value,
                    code = DEVICE_NOT_ALLOWED
                )
            }

            BLOCKED_DEVICE_ACCOUNT_CODE -> {
                AccountBlockedByValidateDeviceIdException(
                    message = message ?: BLOCKED_DEVICE_ACCOUNT_ERROR.value,
                    code = BLOCKED_DEVICE_ACCOUNT_CODE
                )
            }

            MAXIMUM_TOKEN_VALIDATION_ATTEMPTS_EXCEEDED_CODE -> {
                MaximumTokenValidationAttemptsExceededException(
                    message = MAXIMUM_TOKEN_VALIDATION_ATTEMPTS_EXCEEDED_ERROR.value,
                    code = MAXIMUM_TOKEN_VALIDATION_ATTEMPTS_EXCEEDED_CODE
                )
            }

            VALIDATE_DEVICE_TOKEN_BLOCKED_ACCOUNT_CODE -> {
                InvalidDeviceTokenException(
                    message = message ?: VALIDATE_DEVICE_TOKEN_BLOCKED_ACCOUNT_ERROR.value,
                    code = VALIDATE_DEVICE_TOKEN_BLOCKED_ACCOUNT_CODE
                )
            }

            INVALID_PROPOSAL_AGE_CODE -> {
                InvalidProposalAgeException(
                    message = message ?: INVALID_PROPOSAL_AGE_ERROR.value,
                    code = INVALID_PROPOSAL_AGE_CODE
                )
            }

            BILL_ALREADY_SCHEDULED_CODE -> {
                BillAlreadyScheduledException(
                    message = message ?: BILL_ALREADY_SCHEDULED_ERROR.value,
                    code = BILL_ALREADY_SCHEDULED_CODE
                )
            }

            PROPOSAL_UNAVAILABLE_TEMPORARILY_CODE -> {
                ProposalNotAvailableException(
                    message = message ?: PROPOSAL_UNAVAILABLE_TEMPORARILY_ERROR.value,
                    code = PROPOSAL_UNAVAILABLE_TEMPORARILY_CODE
                )
            }

            CARD_UPDATE_EXPIRATION_DATE_CODE -> {
                CardUpdateExpirationDateException(
                    message = message ?: CARD_UPDATE_EXPIRATION_DATE_ERROR.value,
                    code = CARD_UPDATE_EXPIRATION_DATE_CODE,
                    date = result.getExpirationDate()
                )
            }

            CARD_UPDATE_EXPIRATION_TIME_CODE -> {
                CardUpdateExpirationTimeException(
                    message = message ?: CARD_UPDATE_EXPIRATION_TIME_ERROR.value,
                    code = CARD_UPDATE_EXPIRATION_TIME_CODE
                )
            }

            verifyProposalDeclinedCode(code ?: "") -> {
                ProposalDeclinedException(
                    message = message ?: PROPOSAL_DECLINED_ERROR.value,
                    code = code,
                    days = result.getDaysTreeMapValue()
                )
            }

            INVALID_PAYLOAD_CODE -> {
                InvalidPayloadException(
                    message = INVALID_PAYLOAD_ERROR.value.plus(" ($INVALID_PAYLOAD_CODE)"),
                    code = INVALID_PAYLOAD_CODE
                )
            }

            INVALID_QR_CODE_CODE -> {
                PaymentNotCompletedLinkInvalidException(
                    message = message ?: PAYMENT_NOT_COMPLETED_LINK_INVALID_ERROR.value,
                    code = INVALID_QR_CODE_CODE
                )
            }

            EXPIRED_QR_CODE_CODE -> {
                PaymentNotCompletedLinkExpiredException(
                    message = message ?: PAYMENT_NOT_COMPLETED_LINK_EXPIRED_ERROR.value,
                    code = EXPIRED_QR_CODE_CODE
                )
            }

            LIMIT_SEARCH_PIX_KEY_CODE -> {
                LimitSearchPixKeyException(
                    minutes = result.getMinutes(),
                    message = message ?: LIMIT_SEARCH_PIX_KEY_ERROR.value,
                    code = LIMIT_SEARCH_PIX_KEY_CODE
                )
            }

            PASSWORD_BLOCKED_MANY_INVALID_ATT_CODE -> {
                PasswordBlockedManyAttemptsException(
                    message = message ?: PASSWORD_BLOCKED_MANY_INVALID_ATT_ERROR.value,
                    code = PASSWORD_BLOCKED_MANY_INVALID_ATT_CODE
                )
            }

            INVALID_CURRENT_PASSWORD_TWO_ATT_CODE -> {
                InvalidCurrentPasswordTwoAttemptsException(
                    message = message ?: INVALID_CURRENT_PASSWORD_TWO_ATT_ERROR.value,
                    code = INVALID_CURRENT_PASSWORD_TWO_ATT_CODE
                )
            }

            INVALID_CURRENT_PASSWORD_ONE_ATT_CODE -> {
                InvalidCurrentPasswordOneAttemptsException(
                    message = message ?: INVALID_CURRENT_PASSWORD_ONE_ATT_ERROR.value,
                    code = INVALID_CURRENT_PASSWORD_ONE_ATT_CODE
                )
            }

            PASSWORD_CANNOT_BE_BIRTH_DATE_CODE -> {
                PasswordCannotBeBirthDateException(
                    message = message ?: PASSWORD_CANNOT_BE_BIRTH_DATE_ERROR.value,
                    code = PASSWORD_CANNOT_BE_BIRTH_DATE_CODE
                )
            }

            PASSWORD_CANNOT_BE_SAME_CODE -> {
                PasswordCannotBeSameException(
                    message = message ?: PASSWORD_CANNOT_BE_SAME_ERROR.value,
                    code = PASSWORD_CANNOT_BE_SAME_CODE
                )
            }

            NEW_PASSWORD_INVALID_CODE -> {
                NewPasswordInvalidException(
                    message = message ?: NEW_PASSWORD_INVALID_ERROR.value,
                    code = NEW_PASSWORD_INVALID_CODE
                )
            }

            INVALID_PASSWORD_TWO_ATT_CODE -> {
                InvalidPasswordTwoAttemptsException(
                    message = message ?: INVALID_PASSWORD_TWO_ATT_ERROR.value,
                    code = INVALID_PASSWORD_TWO_ATT_CODE
                )
            }

            INVALID_PASSWORD_ONE_ATT_CODE -> {
                InvalidPasswordOneAttemptsException(
                    message = message ?: INVALID_PASSWORD_ONE_ATT_ERROR.value,
                    code = INVALID_PASSWORD_ONE_ATT_CODE
                )
            }

            UNBLOCK_PERMANENT_TOO_MANY_ATT_CODE -> {
                UnblockPermanentTooManyAttemptsException(
                    message = message ?: UNBLOCK_PERMANENT_TOO_MANY_ATT_ERROR.value,
                    code = UNBLOCK_PERMANENT_TOO_MANY_ATT_CODE
                )
            }

            UNBLOCK_PERMANENT_TWO_ATT_CODE -> {
                UnblockPermanentTwoAttemptsException(
                    message = message ?: UNBLOCK_PERMANENT_TWO_ATT_ERROR.value,
                    code = UNBLOCK_PERMANENT_TWO_ATT_CODE
                )
            }

            UNBLOCK_PERMANENT_ONE_ATT_CODE -> {
                UnblockPermanentOneAttemptException(
                    message = message ?: UNBLOCK_PERMANENT_ONE_ATT_ERROR.value,
                    code = UNBLOCK_PERMANENT_ONE_ATT_CODE
                )
            }

            UNBLOCK_PERMANENT_TOKEN_SMS_EXPIRED_CODE -> {
                UnblockPermanentTokenExpiredException(
                    message = message ?: UNBLOCK_PERMANENT_TOKEN_SMS_EXPIRED_ERROR.value,
                    code = UNBLOCK_PERMANENT_TOKEN_SMS_EXPIRED_CODE
                )
            }

            UNBLOCK_TOKEN_TOO_MANY_ATT_CODE -> {
                UnblockTokenTooManyAttemptsException(
                    message = message ?: UNBLOCK_TOKEN_TOO_MANY_ATT_ERROR.value,
                    code = UNBLOCK_TOKEN_TOO_MANY_ATT_CODE
                )
            }

            UNBLOCK_CARD_DATA_NOT_FOUND_CODE -> {
                UnblockCardDataNotFoundException(
                    message = message ?: UNBLOCK_CARD_DATA_NOT_FOUND_ERROR.value,
                    code = UNBLOCK_CARD_DATA_NOT_FOUND_CODE
                )
            }

            CARD_NOT_FOUND_CODE -> {
                CardNotFoundException(
                    message = CARD_NOT_FOUND_ERROR.value,
                    code = code
                )
            }

            CARD_CONTACT_NOT_FOUND_CODE -> {
                CardContactNotFoundException(
                    message = message ?: CARD_CONTACT_NOT_FOUND_ERROR.value,
                    code = CARD_CONTACT_NOT_FOUND_CODE
                )
            }

            CARD_INCREASE_LIMIT_NEED_TO_PAID_INVOICES_CODE -> {
                CardIncreaseLimitNeedToPaidInvoicesException(
                    message = message ?: CARD_INCREASE_LIMIT_NEED_TO_PAID_INVOICES_ERROR.value,
                    code = CARD_INCREASE_LIMIT_NEED_TO_PAID_INVOICES_CODE,
                    paid = result.getInvoicesTreeMapValue()
                )
            }

            CARD_INCREASE_LIMIT_RECENTLY_CREATED_CARD_CODE -> {
                CardIncreaseLimitRecentlyCreatedException(
                    message = message ?: CARD_INCREASE_LIMIT_RECENTLY_CREATED_CARD_ERROR.value,
                    code = CARD_INCREASE_LIMIT_RECENTLY_CREATED_CARD_CODE,
                    days = result.getDaysTreeMapValue()
                )
            }

            CARD_INCREASE_LIMIT_RECENTLY_REQUEST_CODE -> {
                CardIncreaseLimitRecentlyRequestException(
                    message = message ?: CARD_INCREASE_LIMIT_RECENTLY_REQUEST_ERROR.value,
                    code = CARD_INCREASE_LIMIT_RECENTLY_REQUEST_CODE,
                    days = result.getDaysTreeMapValue()
                )
            }

            CARD_INCREASE_LIMIT_UNABLE_NOW_CODE -> {
                CardIncreaseLimitUnableNowException(
                    message = message ?: CARD_INCREASE_LIMIT_UNABLE_NOW_ERROR.value,
                    code = CARD_INCREASE_LIMIT_UNABLE_NOW_CODE,
                    days = result.getDaysTreeMapValue()
                )
            }

            CARD_INCREASE_LIMIT_NOT_EVALUATION_CODE -> {
                CardIncreaseLimitNotEvaluatingException(
                    message = message ?: CARD_INCREASE_LIMIT_NOT_EVALUATION_ERROR.value,
                    code = CARD_INCREASE_LIMIT_NOT_EVALUATION_CODE,
                )
            }

            CARD_INCREASE_LIMIT_REACHED_MAX_VALUE_CODE -> {
                CardIncreaseLimitReachedMaxValueException(
                    message = message ?: CARD_INCREASE_LIMIT_REACHED_MAX_VALUE_ERROR.value,
                    code = CARD_INCREASE_LIMIT_REACHED_MAX_VALUE_CODE,
                )
            }

            ERROR_COMMUNICATING_TO_SERVER_CODE -> {
                CommunicatingToServerException(
                    message = message ?: ERROR_COMMUNICATING_TO_SERVER_ERROR.value,
                    code = ERROR_COMMUNICATING_TO_SERVER_CODE,
                )
            }

            CARD_INCREASE_LIMIT_SOLICITATION_TIME_CODE -> {
                CardIncreaseLimitSolicitationTimeException(
                    message = message ?: CARD_INCREASE_LIMIT_SOLICITATION_TIME_ERROR.value,
                    code = CARD_INCREASE_LIMIT_SOLICITATION_TIME_CODE,
                    startTime = result.getStartTimeTreeMapValue(),
                    endTime = result.getEndTimeTreeMapValue()
                )
            }

            USER_NOT_SIGNED_CONTRACT_CODE -> {
                saveContractVoucherToken(json)
                UserNotSignedContractException(
                    message = message ?: USER_NOT_SIGNED_CONTRACT_ERROR.value,
                    code = USER_NOT_SIGNED_CONTRACT_CODE
                )
            }

            PERSONAL_LOAN_REQUEST_LIMIT_CODE -> {
                PersonalLoanRequestLimitException(
                    message = message ?: PERSONAL_LOAN_REQUEST_LIMIT_ERROR.value,
                    code = PERSONAL_LOAN_REQUEST_LIMIT_CODE
                )
            }

            DAILY_LIMIT_RAISE_VALUE_CODE -> {
                DailyLimitRaiseValueRequestException(
                    message = DAILY_LIMIT_RAISE_VALUE_ERROR.value,
                    code = DAILY_LIMIT_RAISE_VALUE_CODE,
                    maxLimit = result.getThresholdLimitMinMaxValue()
                )
            }

            DAILY_LIMIT_REDUCE_VALUE_CODE -> {
                DailyLimitReduceValueRequestException(
                    message = DAILY_LIMIT_REDUCE_VALUE_ERROR.value,
                    code = DAILY_LIMIT_REDUCE_VALUE_CODE,
                    minLimit = result.getThresholdLimitMinMaxValue()
                )
            }

            NOT_ENOUGH_DAILY_LIMIT_CODE -> {
                ValueAboveDailyLimitException(
                    message = message ?: NOT_ENOUGH_DAILY_LIMIT_ERROR.value,
                    code = NOT_ENOUGH_DAILY_LIMIT_CODE
                )
            }

            else -> {
                translateSuccessMessage(result)
                checkIfShouldSaveDMCardToken(result.token)
                RemoteDataSourceException(
                    message = message ?: GENERIC_ERROR.value,
                    code = code ?: "",
                    messageList = mapMessageList
                )
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Synchronized
    private suspend fun <D> handleHttpException(
        httpException: HttpException, call: suspend () -> D, retryCount: Int
    ): D {
        val json = httpException.response()?.errorBody()?.string() ?: ""

        return if (httpException.is4xxError() && isWSO2Code(json) && retryCount < 1) {
            refreshToken(retryCount, call, httpException, json)
        } else {
            throw httpException.parseError(json)
        }
    }

    private suspend fun <D> refreshToken(
        retryCount: Int, call: suspend () -> D, httpException: HttpException, json: String
    ): D {
        sessionLocalDataSource.saveWso2Token(null)
        val token = sessionRemoteDataSource.getWso2Token()
        return if (token.isSuccess) {
            sessionLocalDataSource.saveWso2Token(token.getOrDefault(""))
            wrapper(retryCount + 1, call)
        } else {
            throw httpException.parseError(json)
        }
    }

    private fun isWSO2Code(jsonString: String): Boolean {
        return CODES_FOR_UPDATING_TOKEN.contains(getWSO2Code(jsonString))
    }

    private fun getWSO2Code(jsonString: String?): Long {
        val error = jsonString?.fromJson<WSO2ErrorResponse>()
        return error?.fault?.code ?: 0
    }

    private suspend fun HttpException.parseError(json: String): Exception {
        var token: String? = null
        val error = json.fromJson<DmCardGenericResponse<*>>() ?: let {
            token = json.fromJson<DmCardGenericTokenResponse>()?.token
            null
        }

        checkIfShouldSaveDMCardToken(token ?: error?.token)
        return mapException(error, json)
    }

    private suspend fun HttpException.mapException(
        error: DmCardGenericResponse<*>?, json: String
    ): DataSourceException {
        val messageList = mapMessageList(error?.messageList, json = json)
        val message = messageList.getOrNull(0)?.text
        val code = error?.getCode()
        val isCodeOfWrongPassword =
            listOf(WRONG_PASSWORD_CODE, CHANGED_PASSWORD_CODE).contains(code)
        val isInvalidLogin = listOf(
            MAXIMUM_LOGIN_ATTEMPTS,
            BLOCKED_ACCOUNT,
            INCORRECT_PASSWORD_FOUR_ATTEMPTS_LEFT,
            INCORRECT_PASSWORD_THREE_ATTEMPTS_LEFT,
            INCORRECT_PASSWORD_TWO_ATTEMPTS_LEFT,
            INCORRECT_PASSWORD_ONE_ATTEMPTS_LEFT
        ).contains(code)

        return when {
            code == WRONG_PASSWORD_CODE -> {
                InvalidCredentialsException(
                    message = message ?: INVALID_CREDENTIALS.value,
                    code = WRONG_PASSWORD_CODE
                )
            }

            code == verifyBlockTaxIdCode(code ?: "") -> {
                handleWithUnauthorized()
                BlockTaxIdException(
                    message = message ?: BLOCK_TAX_ID_ERROR.value,
                    code = code
                )
            }

            code == BLOCKED_ACCOUNT_CODE -> {
                handleWithUnauthorized()
                BlockedAccountException(
                    message = message ?: BLOCKED_ACCOUNT_ERROR.value,
                    code = BLOCKED_ACCOUNT_CODE
                )
            }

            code == UNKNOWN_ERROR_CODE -> {
                UnknowErrorException(
                    message = GENERIC_ERROR.value,
                    code = code
                )
            }

            code == SERVICE_UNAVAILABLE_ERROR_CODE -> {
                ServiceUnavailableException(
                    message = message ?: SERVICE_UNAVAILABLE_ERROR.value,
                    code = SERVICE_UNAVAILABLE_ERROR_CODE
                )
            }

            code == INVALID_TOKEN_CODE || code() == HTTP_FORBBIDEN || code() == HTTP_UNAUTHORIZED && !isCodeOfWrongPassword && !isInvalidLogin -> {
                handleWithUnauthorized()
                UnauthorizedException(
                    message = message ?: UNAUTHORIZED_ERROR.value,
                    code = code ?: ""
                )
            }

            code() == HTTP_AUTHENTICATION_TIMEOUT && !isCodeOfWrongPassword && !isInvalidLogin -> {
                handleWithUnauthorized()
                AuthenticationTimeoutException(
                    message = message ?: AUTHENTICATION_TIMEOUT_ERROR.value,
                    code = code ?: ""
                )
            }

            code == NO_DMCRED_WAS_FOUND_CODE -> {
                NoDMCredWasFoundException(
                    message = message ?: DMCRED_NO_DMCRED_WAS_FOUND_ERROR.value,
                    code = NO_DMCRED_WAS_FOUND_CODE
                )
            }

            code == USER_HAVE_DMCRED_ACTIVE_CODE -> {
                UserHaveDMCredActiveException(
                    message = message ?: DMCRED_USER_HAVE_ACTIVE_LOAN_ERROR.value,
                    code = USER_HAVE_DMCRED_ACTIVE_CODE
                )
            }

            code == DMCRED_OUT_OF_QUOTES_CODE -> {
                DMCredOutOfQuotesException(
                    message = message ?: DMCRED_OUT_OF_QUOTES_ERROR.value,
                    code = DMCRED_OUT_OF_QUOTES_CODE
                )
            }

            errorRequestRegex.matches(code ?: "") -> throw handleExceptionByCode(error!!)
            else -> DataSourceException(
                message = message ?: GENERIC_ERROR.value,
                code = code ?: "",
                cause = this,
                messageList = messageList
            )
        }
    }

    private fun handleWithUnauthorized() {
        sessionLocalDataSource.saveDMCardToken(null)
        userLocalDataSource.saveUserId(null)
    }

    suspend fun mapMessageList(
        response: List<DmCardMessageResponse>?,
        json: String? = null
    ) = response?.mapNotNull {
        when (!it.message.isNullOrBlank() && !it.code.isNullOrBlank()) {
            true -> {
                val code = getFormattedCode(it.code)
                val formattedMessage = try {
                    replaceMessageWithJsonKeys(
                        json = json,
                        message = systemMessageRepository.getMessage(it.code).single()
                    )
                } catch (e: java.lang.Exception) {
                    replaceMessageWithJsonKeys(
                        json = json,
                        message = it.message
                    )
                }
                Message(code = it.code, text = "${formattedMessage ?: it.message}$code")
            }

            else -> null
        }
    } ?: listOf()

    fun replaceMessageWithJsonKeys(json: String?, message: String?): String? {
        var formattedMessage = message
        val jsonObject = json?.fromJson<JsonObject>()?.get("data")?.asJsonObjectOrNull
            ?: return message

        jsonObject.keySet().forEach { key ->
            jsonObject[key].asJsonPrimitiveOrNull?.also { jsonValue ->
                val number = jsonValue.asNumberOrNull
                val value = when {
                    number is Double && number.truncateDecimal() == number -> {
                        "${number.toInt()}"
                    }

                    else -> jsonValue.asString
                }

                formattedMessage = formattedMessage?.replace("{${key}}", value.replace(".0", ""))
            }
        }
        return formattedMessage
    }

    fun saveContractVoucherToken(json: String?) {
        val jsonObject = json?.fromJson<JsonObject>()?.get("data")?.asJsonObjectOrNull
        jsonObject?.keySet()?.forEach { key ->
            if (key == CONTRACT_VOUCHER) {
                jsonObject[key].asJsonPrimitiveOrNull?.also { jsonValue ->
                    sessionLocalDataSource.saveDMCardToken(jsonValue.asString)
                }
            }
        }
    }

    private fun checkIfShouldSaveDMCardToken(token: String?) {
        if (!token.isNullOrBlank()) {
            sessionLocalDataSource.saveDMCardToken(token)
        }
    }

    private fun getFormattedCode(code: String?) = when (code) {
        PROPOSAL_CARD_BLOCKED_TIME -> ""
        else -> " ($code)"
    }

    private suspend fun translateSuccessMessage(result: DmCardGenericResponse<*>) {
        val messageList = mapMessageList(result.messageList)
        result.messageList = messageList.map { MessageMapper.fromDomain(it) }
    }
}