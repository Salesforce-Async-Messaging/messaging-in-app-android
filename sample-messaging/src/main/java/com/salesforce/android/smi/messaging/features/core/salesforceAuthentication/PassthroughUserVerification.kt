package com.salesforce.android.smi.messaging.features.core.salesforceAuthentication

import com.salesforce.android.smi.core.UserVerificationToken
import com.salesforce.android.smi.messaging.features.core.UserVerification

class PassthroughUserVerification(private val sampleSalesforceSDKManager: SampleSalesforceSDKManager) : UserVerification() {
    override suspend fun authenticate(): UserVerificationToken = sampleSalesforceSDKManager.fetchMessagingToken()

    override suspend fun renewAuthentication(): UserVerificationToken = authenticate()
}
