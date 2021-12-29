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
import kotlin.math.abs
import ltd.evilcorp.atox.R
import ltd.evilcorp.core.vo.Contact

sealed interface Size {
    fun asPx(res: Resources): Px
}

data class Px(val px: Int) : Size {
    override fun asPx(res: Resources) = this
}

data class Dp(val dp: Float) : Size {
    override fun asPx(res: Resources): Px = Px(dpToPx(dp, res))
}

// Class for creating an avatar for contact and assigning it into an ImageView
internal class AvatarFactory(contact: Contact) {

    companion object {
        const val DEFAULT_AVATAR_SIZE_DP = 50f
    }

    private val name: String = contact.name
    private val publicKey: String = contact.publicKey
    private val avatarUri: String = contact.avatarUri

    private fun getInitials(): String {
        val segments = name.split(" ")
        if (segments.size == 1) return segments.first().take(1)
        return segments.first().take(1) + segments[1][0]
    }

    /*
     * Method will assign an avatar to an image view. If avatar image has been set to the contact
     * then it will be set to the image view, otherwise a new avatar image will be created based
     * on the initials of the name and the public key for the background color.
     */
    fun assignInto(imageView: ImageView, size: Size = Dp(DEFAULT_AVATAR_SIZE_DP)) =
        if (avatarUri.isNotEmpty()) {
            imageView.setImageURI(Uri.parse(avatarUri))
        } else {
            val side = size.asPx(imageView.resources).px
            val textScale = side.toFloat() / Dp(DEFAULT_AVATAR_SIZE_DP).asPx(imageView.resources).px

            val bitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
            val colors = imageView.resources.getIntArray(R.array.contactBackgrounds)
            val backgroundPaint = Paint().apply { color = colors[abs(publicKey.hashCode()).rem(colors.size)] }

            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = imageView.resources.getDimension(R.dimen.contact_avatar_placeholder_text) * textScale
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            }

            val textBounds = Rect()
            val initials = getInitials()
            textPaint.getTextBounds(initials, 0, initials.length, textBounds)
            canvas.drawRoundRect(rect, rect.bottom, rect.right, backgroundPaint)
            canvas.drawText(initials, rect.centerX(), rect.centerY() - textBounds.exactCenterY(), textPaint)
            imageView.setImageBitmap(bitmap)
        }
}
