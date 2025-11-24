package com.salesforce.android.smi.messaging.features.components

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.lifecycleScope
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.messaging.SalesforceMessaging
import com.salesforce.android.smi.ui.MessagingInAppUI

/**
 * Maintains the messaging event stream. Should be used when the [MessagingInAppUI] is not active.
 */
@Composable
fun LifecycleResumeMessagingStreamEffect(salesforceMessaging: SalesforceMessaging) {
    LifecycleResumeMessagingStreamEffect(salesforceMessaging.coreClient)
}

/**
 * Maintains the messaging event stream. Should be used when the [MessagingInAppUI] is not active.
 */
@Composable
fun LifecycleResumeMessagingStreamEffect(coreClient: CoreClient) {
    // It's important to start/stop the client events based on these lifecycle events
    LifecycleResumeEffect(Unit) {
        coreClient.start(lifecycleScope)
        onPauseOrDispose { coreClient.stop() }
    }
}
