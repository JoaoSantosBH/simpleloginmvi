package br.com.dmcard.contadigital.data_remote.utils

import br.com.dmcard.contadigital.data.utils.RecoverWso2Token

class RecoverWso2TokenImpl : RecoverWso2Token {

    override fun getHeaderXAuthorization(wasReleaseVersion: Boolean) =
        when (wasReleaseVersion) {
            true -> PRODUCTION_HEADER
            else -> DEVELOP_HEADER
        }

    companion object {
        private const val PRODUCTION_HEADER =
            "Basic RjdST1ZXTHBDaEFBU3I0WUJ3eG03blIweXl3YTo1Z2pkUFZUYUoyblNPRWkxRlJYNHVuQzB0MEFh"
        private const val DEVELOP_HEADER =
            "Basic Zjc3QU5mMlhuYjhKRVNVbTgwdWU4ckdFa1lrYTpobl9TZGVwckRKQ1gxNHRGUDNmZ1hqRWNIMjhh"
    }
}