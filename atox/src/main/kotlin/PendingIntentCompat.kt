// SPDX-FileCopyrightText: 2021 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object PendingIntentCompat {
    fun getBroadcast(
        context: Context,
        requestCode: Int,
        intent: Intent,
        flags: Int,
        mutable: Boolean = false,
    ): PendingIntent = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        PendingIntent.getBroadcast(context, requestCode, intent, flags)
    } else {
        val mutabilityFlag =
            if (mutable) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            } else {
                PendingIntent.FLAG_IMMUTABLE
            }
        PendingIntent.getBroadcast(context, requestCode, intent, flags or mutabilityFlag)
    }

    fun getActivity(context: Context, requestCode: Int, intent: Intent, flags: Int): PendingIntent =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            PendingIntent.getActivity(context, requestCode, intent, flags)
        } else {
            PendingIntent.getActivity(context, requestCode, intent, flags or PendingIntent.FLAG_IMMUTABLE)
        }
}
