package com.salesforce.android.smi.messaging.features.voice.theme

import androidx.compose.ui.unit.dp

internal object SMIDimens {
    object Stroke {
        val dp1 = 1.dp
        val dp3 = 3.dp
        const val ARC_WIDTH = 8f
    }

    object Padding {
        val dp2 = 2.dp
        val dp4 = 4.dp
        val dp8 = 8.dp
        val dp12 = 12.dp
        val dp16 = 16.dp
        val dp32 = 32.dp
    }

    object Icon {
        val dp20 = 20.dp
        val dp24 = 24.dp
        val dp32 = 32.dp
        val dp72 = 72.dp
    }

    object Radius {
        val dp4 = 4.dp
        val dp8 = 8.dp
        val dp20 = 20.dp
    }

    object Elevation {
        val default = 4.dp
    }

    object Size {
        val dp12 = 12.dp
        val dp24 = 24.dp
        val dp32 = 32.dp
        val dp48 = 48.dp
        val dp64 = 64.dp
        val dp96 = 96.dp
        val dp128 = 128.dp
        val dp256 = 256.dp

        // carousel specific dimensions
        val dp240 = 240.dp
        val dp350 = 350.dp

        // Max markdown height before nested scrolling
        val dp1024 = 1024.dp
        const val SIXTY_PERCENT = 0.6F
    }

    object Alpha {
        const val DEFAULT = 1.0F
        const val TWENTY_PERCENT = 0.2F
    }

    val zero = 0.dp
}
