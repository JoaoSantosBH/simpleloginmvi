package br.com.dmcard.contadigital.data.repository

import br.com.dmcard.contadigital.data.datasource.local.UserLocalDataSource
import br.com.dmcard.contadigital.data.datasource.remote.AccountRemoteDataSource
import br.com.dmcard.contadigital.domain.model.useraccount.UserAccountInformation
import br.com.dmcard.contadigital.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class AccountRepositoryImpl(
    private val accountRemoteDataSource: AccountRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : AccountRepository {

    override fun getUserAccountInformation(): Flow<UserAccountInformation> =
        accountRemoteDataSource.getAccountInformation(
            accountId = userLocalDataSource.getUserId() ?: ""
        )

    override fun saveProfilePicture(file: File) = accountRemoteDataSource.saveProfilePicture(
        accountId = userLocalDataSource.getUserId() ?: "",
        file = file
    ).map {
        it.image ?: ""
    }

    override fun savePassword(passwordVoucher: String, currentPassword: String, password: String) =
        accountRemoteDataSource.savePassword(
            accountId = userLocalDataSource.getUserId() ?: "",
            passwordVoucher = passwordVoucher,
            currentPassword = currentPassword,
            password = password
        )

    override fun updateUserPreferredName(preferredName: String) =
        accountRemoteDataSource.updateUserPreferredName(
            preferredName = preferredName,
            accountId = userLocalDataSource.getUserId() ?: ""
        )

    override fun savePhone(number: String, stateCode: String) =
        accountRemoteDataSource.savePhone(
            accountId = userLocalDataSource.getUserId() ?: "",
            number = number,
            stateCode = stateCode
        )

    override fun saveEmail(email: String) =
        accountRemoteDataSource.saveEmail(
            email = email,
            accountId = userLocalDataSource.getUserId() ?: ""
        )
}