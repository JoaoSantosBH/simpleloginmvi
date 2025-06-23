package br.com.dmcard.contadigital.presentation_login.login

import androidx.lifecycle.ViewModel
import br.com.dmcard.contadigital.basepresentation.core.isSuccess
import br.com.dmcard.contadigital.basepresentation.utils.extensions.*
import br.com.dmcard.contadigital.domain.exception.DataSourceException
import br.com.dmcard.contadigital.domain.exception.IncorrectPasswordException
import br.com.dmcard.contadigital.domain.exception.InvalidCpfException
import br.com.dmcard.contadigital.domain.interactor.authentication.Login
import br.com.dmcard.contadigital.domain.interactor.authentication.SaveLoginData
import br.com.dmcard.contadigital.domain.interactor.authentication.ValidateAppPassword
import br.com.dmcard.contadigital.domain.interactor.user.GetUserCpfLocal
import br.com.dmcard.contadigital.domain.interactor.user.GetUserPassword
import br.com.dmcard.contadigital.domain.interactor.user.HasBiometricRegistered
import br.com.dmcard.contadigital.domain.interactor.user.ValidateCpf
import br.com.dmcard.contadigital.domain.utils.constants.*
import br.com.dmcard.contadigital.domain.utils.extensions.isCpf
import br.com.dmcard.contadigital.domain.utils.extensions.removeNotNumbers
import org.koin.core.KoinComponent

class LoginViewModel : ViewModel(), KoinComponent {

    private var cpfLocal: String? = null

    private val validateLoginPassword: ValidateAppPassword by useCase()
    private val validateCpf: ValidateCpf by useCase()
    private val login: Login by useCase()
    private val getUserCpfLocal: GetUserCpfLocal by useCase()
    private val hasBiometricRegistered: HasBiometricRegistered by useCase()
    private val getUserPassword: GetUserPassword by useCase()
    private val saveLoginData: SaveLoginData by useCase()

    private val _loginState by viewState<Unit>()
    private val _cpfViewState by viewState<String>()
    private val _localCpfViewState by viewState<String>()
    private val _passwordState by viewState<String>()
    private val _fullFormState by viewState<Unit>()
    private val _hasBiometricState by viewState<Boolean>()
    private val _firebaseTokenState by viewState<String>()

    val loginViewState = _loginState.asLiveData()
    val cpfViewState = _cpfViewState.asLiveData()
    val localCpfViewState = _localCpfViewState.asLiveData()
    val passwordViewState = _passwordState.asLiveData()
    val fullFormViewState = _fullFormState.asLiveData()
    val hasBiometricState = _hasBiometricState.asLiveData()

    fun setupViewModel() {
        checkIfHasUserData()
        checkHasBiometricRegistered()
        getFirebaseInstanceId(
            afterGetToken = {
                _firebaseTokenState.postSuccess(it)
            }
        ) {
            _firebaseTokenState.postError(it)
        }
    }

    fun checkIfHasUserData() {
        getUserCpfLocal(
            onSuccess = { cpf ->
                val cpfNumbers = cpf.removeNotNumbers()
                if (cpf.isNotEmpty() && cpfNumbers.isCpf()) {
                    cpfLocal = cpfNumbers
                    _localCpfViewState.postSuccess(cpf.puttingHiddenCpfMask())
                }
            }
        )
    }

    fun checkHasBiometricRegistered() {
        hasBiometricRegistered { _hasBiometricState.postSuccess(it) }
    }

    fun setLoginCpf(cpf: String) {
        val cpfSaved = cpfLocal
        when {
            cpfSaved != null && cpf == cpfSaved.puttingHiddenCpfMask() -> {
                _cpfViewState.postSuccess(cpfSaved.removeNotNumbers())
                if (_passwordState.value?.isSuccess() == true) {
                    _fullFormState.postSuccess(Unit)
                }
                return
            }

            cpf.length == 13 -> _cpfViewState.postError(InvalidCpfException())
            cpf.length in 1..13 -> return
        }

        validateCpf(cpf)
    }

    fun validateCpf(cpf: String) {
        _fullFormState.postError("")

        validateCpf(
            params = ValidateCpf.Params(cpf.removeNotNumbers()),
            onSuccess = {
                if (_passwordState.value?.isSuccess() == true) {
                    _fullFormState.postSuccess(Unit)
                }
                _cpfViewState.postSuccess(cpf.removeNotNumbers())
            },
            onError = {
                _cpfViewState.postError(it)
            }
        )
    }

    fun setLoginPassword(password: String) {
        _fullFormState.postError("")

        validateLoginPassword(
            params = ValidateAppPassword.Params(password),
            onSuccess = {
                if (_cpfViewState.value?.isSuccess() == true) {
                    _fullFormState.postSuccess(Unit)
                }
                _passwordState.postSuccess(password)
            },
            onError = {
                _passwordState.postError(it)
            }
        )
    }

    fun loginBiometric() = getUserPassword { password ->
        _passwordState.postSuccess(password)
        login(password = password)
    }

    fun saveLoginData() {
        saveLoginData(
            params = SaveLoginData.Param(
                cpf = _cpfViewState.value?.data ?: "",
                password = _passwordState.value?.data ?: "",
                firebaseToken = _firebaseTokenState.value?.data
            )
        )
    }

    fun login(password: String = _passwordState.value?.data ?: "") {
        _loginState.postLoading()
        login(
            params = Login.Param(
                cpf = _cpfViewState.value?.data ?: "",
                password = password,
                deviceToken = _firebaseTokenState.value?.data
            ),
            onSuccess = {
                _loginState.postSuccess(it)
            },
            onError = {
                when (it) {
                    is DataSourceException -> validateLoginErrors(it)
                    else -> _loginState.postError(it)
                }
            }
        )
    }

    private fun validateLoginErrors(dataSourceException: DataSourceException) {
        val checkAlertMessage = checkAlertMessage(dataSourceException)
        val secondMessage = checkIncorrectPasswordAttempts(dataSourceException)
        val exception = when {
            checkAlertMessage.isBlank() && secondMessage.isNullOrBlank() -> dataSourceException
            else -> IncorrectPasswordException(
                message = checkAlertMessage,
                secondMessage = secondMessage
            )
        }
        _loginState.postError(exception)
    }

    private fun checkIncorrectPasswordAttempts(throwable: DataSourceException) =
        throwable.messageList.find {
            listOf(
                INCORRECT_PASSWORD_FOUR_ATTEMPTS_LEFT,
                INCORRECT_PASSWORD_THREE_ATTEMPTS_LEFT,
                INCORRECT_PASSWORD_TWO_ATTEMPTS_LEFT,
                INCORRECT_PASSWORD_ONE_ATTEMPTS_LEFT
            ).contains(it.code)
        }?.text


    private fun checkAlertMessage(throwable: DataSourceException) =
        throwable.messageList.find {
            listOf(MAXIMUM_LOGIN_ATTEMPTS, BLOCKED_ACCOUNT).contains(it.code)
        }?.text ?: throwable.messageList.find {
            it.code == CHANGED_PASSWORD_CODE
        }?.text ?: ""

    fun clearState() {
        _loginState.postNeutral()
    }
}
