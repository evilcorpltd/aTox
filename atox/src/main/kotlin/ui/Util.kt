// SPDX-FileCopyrightText: 2019-2022 Robin Lindén <dev@robinlinden.eu>
// SPDX-FileCopyrightText: 2021-2022 aTox contributors
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

internal fun colorFromStatus(context: Context, status: UserStatus) = when (status) {
    UserStatus.None -> ContextCompat.getColor(context, R.color.statusAvailable)
    UserStatus.Away -> ContextCompat.getColor(context, R.color.statusAway)
    UserStatus.Busy -> ContextCompat.getColor(context, R.color.statusBusy)
}

internal sealed interface Size

@JvmInline
internal value class Px(val px: Int) : Size

@JvmInline
internal value class Dp(val dp: Float) : Size {
    fun asPx(res: Resources): Px =
        Px(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.displayMetrics).toInt())
}

internal fun contactListSorter(contact: Contact) = when {
    contact.lastMessage != 0L -> contact.lastMessage
    contact.connectionStatus == ConnectionStatus.None -> -1000L
    else -> -contact.status.ordinal.toLong()
}
