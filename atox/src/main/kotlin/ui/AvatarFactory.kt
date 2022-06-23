// SPDX-FileCopyrightText: 2019-2022 aTox contributors
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
import ltd.evilcorp.atox.R
import kotlin.math.abs

internal object AvatarFactory {

    private fun getInitials(name: String): String {
        val segments = name.split(" ")
        if (segments.size == 1) return segments.first().take(1)
        return segments.first().take(1) + segments[1].take(1)
    }

    // Method will create an avatar based on the initials of a name and a public key for the background color.
    fun create(
        resources: Resources,
        name: String,
        publicKey: String,
        size: Px = Px(resources.getDimension(R.dimen.default_avatar_size).toInt()),
    ): Bitmap {
        val defaultAvatarSize = resources.getDimension(R.dimen.default_avatar_size)
        val textScale = size.px / defaultAvatarSize

        val bitmap = Bitmap.createBitmap(size.px, size.px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val colors = resources.getIntArray(R.array.contactBackgrounds)
        val backgroundPaint = Paint().apply { color = colors[abs(publicKey.hashCode()).rem(colors.size)] }

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = resources.getDimension(R.dimen.contact_avatar_placeholder_text) * textScale
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }

        val textBounds = Rect()
        val initials = getInitials(name)
        textPaint.getTextBounds(initials, 0, initials.length, textBounds)
        canvas.drawRoundRect(rect, rect.bottom, rect.right, backgroundPaint)
        canvas.drawText(initials, rect.centerX(), rect.centerY() - textBounds.exactCenterY(), textPaint)

        return bitmap
    }
}
