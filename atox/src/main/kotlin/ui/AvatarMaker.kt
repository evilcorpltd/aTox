// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import android.graphics.*
import android.net.Uri
import android.widget.ImageView
import ltd.evilcorp.atox.R
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.User
import kotlin.math.abs


internal enum class SizeUnit {
    DP,
    PX,
}


/**
 * Class for creating an avatar for user or contact and setting it in the ImageView
 */
internal class AvatarMaker {

    companion object {
        private const val DEFAULT_AVATAR_SIZE_DP = 50
    }

    private var name: String = ""
    private var publicKey: String = ""
    private var avatarUri: String = ""
    private var initials: String = ""

    constructor(contact: Contact) {
        name = contact.name
        publicKey = contact.publicKey
        avatarUri = contact.avatarUri
        initials = getInitials()
    }
    constructor(user: User) {
        name = user.name
        publicKey = user.publicKey
        avatarUri = user.avatarUri
        initials = getInitials()
    }


    /**
     * Method will get the initial characters of the name
     * @return The initial characters of the name.
     */
    private fun getInitials(): String {
        val segments = name.split(" ")
        if (segments.size == 1) return segments.first().take(1)
        return segments.first().take(1) + segments[1][0]
    }


    /**
     * Method will set an avatar to an image view. If avatar image exists then it will be set to the image view,
     * otherwise a new avatar image will be created based on the initials of the name
     * and the public key for the background color.
     * @param imageView The image view for whom to set the avatar image.
     * @param size The size of the avatar image in the units specified in sizeUnit (default: DP units).
     * @param sizeUnit The size unit of size parameter.
     */
    fun setAvatar(imageView: ImageView, size: Int = DEFAULT_AVATAR_SIZE_DP, sizeUnit: SizeUnit = SizeUnit.DP) =
        if (avatarUri.isNotEmpty()) {
            imageView.setImageURI(Uri.parse(avatarUri))
        } else {
            val side: Int
            val textScale: Float

            if (sizeUnit == SizeUnit.DP) {
                side = (size * imageView.resources.displayMetrics.density).toInt()
                textScale = size.toFloat() / DEFAULT_AVATAR_SIZE_DP
            } else {
                side = size
                textScale = size.toFloat() / dpToPx(DEFAULT_AVATAR_SIZE_DP.toFloat(), imageView.resources)
            }

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
            textPaint.getTextBounds(initials, 0, initials.length, textBounds)
            canvas.drawRoundRect(rect, rect.bottom, rect.right, backgroundPaint)
            canvas.drawText(initials, rect.centerX(), rect.centerY() - textBounds.exactCenterY(), textPaint)
            imageView.setImageBitmap(bitmap)
        }

}
