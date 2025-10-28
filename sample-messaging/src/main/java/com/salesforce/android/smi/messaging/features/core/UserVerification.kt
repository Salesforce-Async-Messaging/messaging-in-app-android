package com.salesforce.android.smi.messaging.features.core

import com.salesforce.android.smi.core.ChallengeReason
import com.salesforce.android.smi.core.UserVerificationProvider
import com.salesforce.android.smi.core.UserVerificationToken
import kotlinx.coroutines.delay

open class UserVerification : UserVerificationProvider {
    override suspend fun userVerificationChallenge(reason: ChallengeReason): UserVerificationToken =
        when (reason) {
            ChallengeReason.INITIAL -> authenticate()
            ChallengeReason.RENEW -> renewAuthentication()
            ChallengeReason.EXPIRED -> renewAuthentication()
            ChallengeReason.MALFORMED -> {
                // log issue and provide a fresh token
                authenticate()
            }
        }

    open suspend fun authenticate(): UserVerificationToken = delay(1000).let { UserVerificationToken.externalToken("fakeToken") }

    open suspend fun renewAuthentication(): UserVerificationToken = delay(1000).let { UserVerificationToken.externalToken("renewedToken") }
}
