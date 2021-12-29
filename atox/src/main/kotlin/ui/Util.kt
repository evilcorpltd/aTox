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

internal fun colorByContactStatus(context: Context, contact: Contact) =
    if (contact.connectionStatus == ConnectionStatus.None)
        ContextCompat.getColor(context, R.color.statusOffline)
    else colorFromStatus(context, contact.status)

internal fun colorFromStatus(context: Context, status: UserStatus) = when (status) {
    UserStatus.None -> ContextCompat.getColor(context, R.color.statusAvailable)
    UserStatus.Away -> ContextCompat.getColor(context, R.color.statusAway)
    UserStatus.Busy -> ContextCompat.getColor(context, R.color.statusBusy)
}

internal fun dpToPx(dp: Float, res: Resources): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.displayMetrics).toInt()
