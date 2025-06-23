package br.com.dmcard.contadigital.data_remote.utils.constants


var BASE_URL_GATEWAY = "https://gateway.dmcardapi.com.br/"
var BASE_URL_APP = "https://app.dmcardapi.com.br/prod/"

const val COOPERATIVE_BASE_URL = "corporativo/v1/"
const val ACCOUNT_BASE_URL = "accounts/v2/"
const val TRANSACTION_BASE_URL = "transactions/v1/"
const val PROPOSAL_BASE_URL = "proposals/v2/"
const val AUTH_BASE_URL = "auth/v1/"
const val CARD_BASE_URL = "cards/v1/"
const val PAYMENT_BASE_URL = "payments/v1/"
const val DEPOSIT_BASE_URL = "deposits/v1/"
const val TRANSFERS_BASE_URL = "transfers/v1/"
const val RECHARGE_BASE_URL = "topups/v1/"
const val WSO2_BASE_URL = "https://gateway.api.cloud.wso2.com/"
const val ACCOUNT_MESSAGES_BASE_URL = "https://d19rfnnn0weuyo.cloudfront.net/"
const val CONFIG_BASE_URL = "configs/v1/"
const val HOME_BASE_URL = "mobile/v1/"
const val LOAN_BASE_URL = "loans/v1/"
const val PIX_BASE_URL = "pkm/v1/"
const val SCHEDULE_BASE_URL = "schedules/v1/"
const val NEW_TRANSFER_BASE_URL = "favorites/v1/"
const val FAVORITES_BASE_URL = "favorites/v1/"
const val CONTRACTS_BASE_URL = "contracts/v1/"
const val PIX_PAYMENT_BASE_URL = "pixpayment/v1/"
const val PKM_BASE_URL = "pkm/v1/"
const val HEADER_WSO2_TOKEN = "Authorization"
const val HEADER_API_TOKEN = "X-Authorization"
const val DMCRED_BASE_URL = "dmcred/v1/"

const val CONTRACT_VOUCHER = "contract_voucher"

const val TIMEOUT_DURATION_SECONDS = 30L
const val DEVICE_TYPE = "android"
const val SHA_512 = "SHA-512"

const val COUNTRY_CODE = "55"

object Const {
    val CODES_FOR_UPDATING_TOKEN: List<Long> = listOf(900901, 900902, 900908)
}

const val HTTP_AUTHENTICATION_TIMEOUT = 419
const val HTTP_UNAUTHORIZED = 401
const val HTTP_FORBBIDEN = 403

const val DEBUG_MODE = "debug"

