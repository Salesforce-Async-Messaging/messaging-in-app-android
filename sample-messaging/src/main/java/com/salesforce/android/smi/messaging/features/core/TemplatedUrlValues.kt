package com.salesforce.android.smi.messaging.features.core

import com.salesforce.android.smi.core.TemplatedUrlValuesProvider
import com.salesforce.android.smi.network.data.domain.webview.TemplatedWebView
import java.util.logging.Logger

open class TemplatedUrlValues : TemplatedUrlValuesProvider {
    protected val logger: Logger = Logger.getLogger(this::class.java.name)
    open val defaultValue = "%S-Placeholder"

    override suspend fun setValues(input: TemplatedWebView): TemplatedWebView {
        return input.apply {
            (queryParams.keys + pathParams.keys).forEach {
                setParameterValue(it, defaultValue.format(it))
            }
        }
    }
}
