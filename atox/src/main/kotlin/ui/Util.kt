// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import android.content.res.Resources
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import ltd.evilcorp.atox.R
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.UserStatus

internal fun colorByStatus(resources: Resources, contact: Contact): Int {
    if (contact.connectionStatus == ConnectionStatus.None) return ResourcesCompat.getColor(
        resources,
        R.color.statusOffline,
        null
    )
    return when (contact.status) {
        UserStatus.None -> ResourcesCompat.getColor(resources, R.color.statusAvailable, null)
        UserStatus.Away -> ResourcesCompat.getColor(resources, R.color.statusAway, null)
        UserStatus.Busy -> ResourcesCompat.getColor(resources, R.color.statusBusy, null)
    }
}

internal fun dpToPx(dp: Int, res: Resources): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), res.displayMetrics).toInt()
