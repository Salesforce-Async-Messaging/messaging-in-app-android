package com.salesforce.android.smi.messaging.features.core

import com.salesforce.android.smi.core.ChallengeReason
import com.salesforce.android.smi.core.UserVerificationProvider
import com.salesforce.android.smi.core.UserVerificationToken
import com.salesforce.android.smi.core.UserVerificationType
import kotlinx.coroutines.delay

open class UserVerification : UserVerificationProvider {
    override suspend fun userVerificationChallenge(reason: ChallengeReason): UserVerificationToken {
        val token: String = when (reason) {
            ChallengeReason.INITIAL -> authenticate()
            ChallengeReason.RENEW -> renewAuthentication()
            ChallengeReason.EXPIRED -> renewAuthentication()
            ChallengeReason.MALFORMED -> {
                // log issue and provide a fresh token
                authenticate()
            }
        }

        return UserVerificationToken(UserVerificationType.JWT, token)
    }

    open suspend fun authenticate(): String {
        delay(1000)
        return "fakeToken"
    }

    open suspend fun renewAuthentication(): String {
        delay(1000)
        return "renewedToken"
    }
}
