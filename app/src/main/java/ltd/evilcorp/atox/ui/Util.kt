package ltd.evilcorp.atox.ui

import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.vo.ConnectionStatus
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.vo.UserStatus

internal fun colorByStatus(resources: Resources, contact: Contact): Int {
    if (contact.connectionStatus == ConnectionStatus.NONE) return ResourcesCompat.getColor(
        resources,
        R.color.statusOffline,
        null
    )
    return when (contact.status) {
        UserStatus.NONE -> ResourcesCompat.getColor(resources, R.color.statusAvailable, null)
        UserStatus.AWAY -> ResourcesCompat.getColor(resources, R.color.statusAway, null)
        UserStatus.BUSY -> ResourcesCompat.getColor(resources, R.color.statusBusy, null)
    }
}
