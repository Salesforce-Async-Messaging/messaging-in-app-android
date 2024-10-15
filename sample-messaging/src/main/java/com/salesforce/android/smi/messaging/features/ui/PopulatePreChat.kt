package com.salesforce.android.smi.messaging.features.ui

import com.salesforce.android.smi.network.data.domain.prechat.PreChatField
import java.util.logging.Level
import java.util.logging.Logger

open class PopulatePreChat {
    protected val logger: Logger = Logger.getLogger(this::class.java.name)
    open val defaultValue = "%S-Placeholder"

    open fun populate(preChatFields: List<PreChatField>): List<PreChatField> = preChatFields.onEach {
        it.userInput = defaultValue.format(it.name)
        logResult(it)
    }

    fun logResult(preChatField: PreChatField): PreChatField = preChatField.also {
        logger.log(Level.INFO, "Populate ${preChatField.name} = ${preChatField.userInput}")
    }
}
