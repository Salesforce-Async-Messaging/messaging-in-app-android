package com.example.messaginguiexample

import com.google.firebase.messaging.FirebaseMessagingService
import com.salesforce.android.smi.core.CoreClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
internal class FCMTokenService : FirebaseMessagingService() {
    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + supervisorJob)

    override fun onDestroy() {
        super.onDestroy()
        supervisorJob.cancel("Service destroyed")
    }
    override fun onNewToken(token: String) {
        scope.launch { CoreClient.provideDeviceToken(applicationContext, token) }
    }

}