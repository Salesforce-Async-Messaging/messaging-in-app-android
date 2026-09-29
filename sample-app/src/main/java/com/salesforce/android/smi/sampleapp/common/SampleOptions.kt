package com.salesforce.android.smi.sampleapp.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * NOTE: sample-app scaffolding, not part of the core In-App Messaging sample. It persists the demo
 * toggles across navigation and restarts so the sample reopens where you left it. Nothing here is
 * needed to integrate the SDK.
 *
 * The toggles that configure how the sample presents the messaging UI.
 */
data class SampleOptions(
    val fullScreen: Boolean = false,
    val openOnSelection: Boolean = true,
    val showWidget: Boolean = false,
    val showConversationList: Boolean = true,
    val showIcon: Boolean = true
)

/**
 * A [SampleOptions] snapshot plus a setter per toggle. [options] reflects what's stored; each setter
 * persists the change, so state survives navigation, config changes and process death.
 */
class SampleOptionsState internal constructor(
    val options: SampleOptions,
    private val store: DataStore<Preferences>,
    private val scope: CoroutineScope
) {
    fun setFullScreen(value: Boolean) = write(Keys.FULL_SCREEN, value)
    fun setOpenOnSelection(value: Boolean) = write(Keys.OPEN_ON_SELECTION, value)
    fun setShowWidget(value: Boolean) = write(Keys.SHOW_WIDGET, value)
    fun setShowConversationList(value: Boolean) = write(Keys.SHOW_CONVERSATION_LIST, value)
    fun setShowIcon(value: Boolean) = write(Keys.SHOW_ICON, value)

    private fun write(key: Preferences.Key<Boolean>, value: Boolean) {
        scope.launch { store.edit { it[key] = value } }
    }
}

/**
 * Remembers the persisted sample options, using defaults until the first read completes.
 *
 * @param context used to reach the app-scoped DataStore.
 * @return state whose [SampleOptionsState.options] tracks disk and whose setters persist changes.
 */
@Composable
fun rememberSampleOptions(context: Context): SampleOptionsState {
    val store = context.applicationContext.sampleOptionsDataStore
    val scope = rememberCoroutineScope()
    val options by remember(store) { store.data.map(::toOptions) }
        .collectAsStateWithLifecycle(initialValue = SampleOptions())
    return remember(options, store, scope) { SampleOptionsState(options, store, scope) }
}

private val Context.sampleOptionsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sample_options"
)

private object Keys {
    val FULL_SCREEN = booleanPreferencesKey("full_screen")
    val OPEN_ON_SELECTION = booleanPreferencesKey("open_on_selection")
    val SHOW_WIDGET = booleanPreferencesKey("show_widget")
    val SHOW_CONVERSATION_LIST = booleanPreferencesKey("show_conversation_list")
    val SHOW_ICON = booleanPreferencesKey("show_icon")
}

private fun toOptions(prefs: Preferences): SampleOptions {
    val defaults = SampleOptions()
    return SampleOptions(
        fullScreen = prefs[Keys.FULL_SCREEN] ?: defaults.fullScreen,
        openOnSelection = prefs[Keys.OPEN_ON_SELECTION] ?: defaults.openOnSelection,
        showWidget = prefs[Keys.SHOW_WIDGET] ?: defaults.showWidget,
        showConversationList = prefs[Keys.SHOW_CONVERSATION_LIST] ?: defaults.showConversationList,
        showIcon = prefs[Keys.SHOW_ICON] ?: defaults.showIcon
    )
}
