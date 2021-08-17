// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs
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

private fun getInitials(contact: Contact): String {
    val segments = contact.name.split(" ")
    if (segments.size == 1) return segments.first().take(1)
    return segments.first().take(1) + segments[1][0]
}

private const val DEFAULT_AVATAR_SIZE_DP = 50
internal fun setAvatarFromContact(imageView: ImageView, contact: Contact, sizeDp: Int = DEFAULT_AVATAR_SIZE_DP) =
    if (contact.avatarUri.isNotEmpty()) {
        imageView.setImageURI(Uri.parse(contact.avatarUri))
    } else {
        val side = (sizeDp * imageView.resources.displayMetrics.density).toInt()
        val bitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val colors = imageView.resources.getIntArray(R.array.contactBackgrounds)
        val backgroundPaint = Paint().apply { color = colors[abs(contact.publicKey.hashCode()).rem(colors.size)] }

        val textScale = sizeDp.toFloat() / DEFAULT_AVATAR_SIZE_DP
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = imageView.resources.getDimension(R.dimen.contact_avatar_placeholder_text) * textScale
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }
        val initials = getInitials(contact)
        val textBounds = Rect()
        textPaint.getTextBounds(initials, 0, initials.length, textBounds)
        canvas.drawRoundRect(rect, rect.bottom, rect.right, backgroundPaint)
        canvas.drawText(initials, rect.centerX(), rect.centerY() - textBounds.exactCenterY(), textPaint)
        imageView.setImageBitmap(bitmap)
    }
