package com.salesforce.android.smi.messaging.features.core

import com.salesforce.android.smi.core.PreChatValuesProvider
import com.salesforce.android.smi.messaging.features.ui.PopulatePreChat
import com.salesforce.android.smi.network.data.domain.prechat.PreChatField

open class HiddenPreChat : PreChatValuesProvider, PopulatePreChat() {
    override val defaultValue: String = "%S-HiddenPlaceholder"

    override suspend fun setValues(input: List<PreChatField>): List<PreChatField> = populate(input)
}
