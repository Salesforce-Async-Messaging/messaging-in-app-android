package com.salesforce.android.smi.messaging.features.core.salesforceAuthentication

import android.app.Activity
import android.os.Build
import android.widget.Toast
import com.salesforce.android.smi.common.api.Result
import com.salesforce.android.smi.common.api.data
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.core.UserVerificationToken
import com.salesforce.android.smi.messaging.features.core.UserVerification
import com.salesforce.androidsdk.app.SalesforceSDKManager
import com.salesforce.androidsdk.rest.RestClient
import com.salesforce.androidsdk.rest.RestRequest
import com.salesforce.androidsdk.rest.RestRequest.RestMethod
import com.salesforce.androidsdk.rest.RestResponse
import com.salesforce.androidsdk.ui.LoginActivity
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SampleSalesforceSDKManager(
    private val activityProvider: () -> Activity,
    private val coreClient: CoreClient,
    loginActivity: Class<out Activity?> = LoginActivity::class.java
) : SalesforceSDKManager(activityProvider().applicationContext, activityProvider()::class.java, loginActivity) {
    private val logger = Logger.getLogger(TAG)

    init {
        customTabBrowser = null
    }

    private val messagingTokenPath: String = coreClient.salesforceAuthenticationRequestPath

    fun login() = gateToAPI28 { restClient() }

    fun logout() = gateToAPI28 { logout(activityProvider(), false) }

    suspend fun fetchVersions() = makeRequest(RestRequest.getRequestForVersions())

    suspend fun fetchResources() = makeRequest(RestRequest.getRequestForResources(API_VERSION))

    suspend fun fetchMessagingToken(): UserVerificationToken =
        makeRequest(RestRequest(RestMethod.GET, messagingTokenPath)).data?.asJSONObject()?.getJSONObject("data")?.let { json ->
            UserVerificationToken.passthroughToken(json.getString("accessToken"), json.getString("lastEventId"))
        } ?: throw Exception("Failed to fetch messaging token.")

    private suspend fun makeRequest(request: RestRequest): Result<RestResponse?> =
        gateToAPI28<Result<RestResponse?>> {
            restClient()?.let { restClient ->
                suspendCoroutine { continuation ->
                    val callback = object : RestClient.AsyncRequestCallback {
                        override fun onSuccess(
                            request: RestRequest?,
                            response: RestResponse?
                        ) {
                            response?.consume()
                            continuation.resume(Result.Success(response))
                        }

                        override fun onError(exception: Exception?) {
                            continuation.resume(Result.Error(exception ?: REST_UNKNOWN_ERROR))
                        }
                    }

                    restClient.sendAsync(request, callback)
                }
            } ?: Result.Error(REST_UNAVAILABLE_ERROR)
        } ?: Result.Error(API_28_ERROR)

    private fun restClient(): RestClient? {
        var restClient: RestClient? = null
        clientManager.getRestClient(activityProvider()) {
            restClient = it
        }

        return restClient
    }

    private inline fun <T> gateToAPI28(block: () -> T): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            block()
        } else {
            logger.log(Level.WARNING, API_28_ERROR.message)
            Toast.makeText(activityProvider(), API_28_ERROR.message, Toast.LENGTH_SHORT).show()
            null
        }

    companion object {
        fun getInstance(
            coreClient: CoreClient,
            activityProvider: () -> Activity
        ): SampleSalesforceSDKManager {
            if (!hasInstance() || activityProvider()::class.java != getInstance().mainActivityClass) {
                setInstance(SampleSalesforceSDKManager(activityProvider, coreClient))
            }
            initInternal(activityProvider().applicationContext)
            return SalesforceSDKManager.Companion.getInstance() as SampleSalesforceSDKManager
        }

        private val REST_UNAVAILABLE_ERROR = Exception("Rest client not yet available.")
        private val REST_UNKNOWN_ERROR = Exception("Rest client not yet available.")
        private val API_28_ERROR = Exception("API 28+ only")
        private const val TAG = "SampleSalesforceSDKManager"
        private const val API_VERSION = "v65.0"
    }
}
