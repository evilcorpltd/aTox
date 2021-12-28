// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.core.content.ContextCompat
import ltd.evilcorp.atox.R
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.UserStatus

internal fun colorByStatus(context: Context, contact: Contact): Int {
    if (contact.connectionStatus == ConnectionStatus.None) return ContextCompat.getColor(
        context,
        R.color.statusOffline,
    )
    return when (contact.status) {
        UserStatus.None -> ContextCompat.getColor(context, R.color.statusAvailable)
        UserStatus.Away -> ContextCompat.getColor(context, R.color.statusAway)
        UserStatus.Busy -> ContextCompat.getColor(context, R.color.statusBusy)
    }
}

internal fun dpToPx(dp: Int, res: Resources): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), res.displayMetrics).toInt()
