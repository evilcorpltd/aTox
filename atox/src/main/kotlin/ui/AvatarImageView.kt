// SPDX-FileCopyrightText: 2022 aTox contributors
// SPDX-FileCopyrightText: 2022 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.doOnPreDraw
import com.google.android.material.imageview.ShapeableImageView
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import ltd.evilcorp.atox.R
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.UserStatus

private const val STATUS_INDICATOR_SIZE_RATIO_WITH_AVATAR = 12f / 50

class AvatarImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ShapeableImageView(context, attrs, defStyleAttr) {

    private val statusIndicatorPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = colorFromStatus(context, UserStatus.None)
    }

    private val statusIndicatorVisible: Boolean = context.theme.obtainStyledAttributes(
        attrs,
        R.styleable.AvatarImageView,
        0,
        0,
    ).use { it.getBoolean(R.styleable.AvatarImageView_statusIndicatorVisible, true) }

    private fun colorByContactStatus(context: Context, contact: Contact) =
        if (contact.connectionStatus == ConnectionStatus.None) {
            ContextCompat.getColor(context, R.color.statusOffline)
        } else {
            colorFromStatus(context, contact.status)
        }

    private var name = ""
    private var publicKey = ""
    private var avatarUri = ""

    fun setFrom(contact: Contact) {
        statusIndicatorPaint.color = colorByContactStatus(context, contact)
        // Assigning to the object's properties as capturing those values in the doOnPreDraw's lambda
        // either directly or using temporary `val` objects decreases performance of scrolling SIGNIFICANTLY.
        name = contact.name
        publicKey = contact.publicKey
        avatarUri = contact.avatarUri

        doOnPreDraw {
            if (avatarUri.isNotEmpty()) {
                setImageURI(Uri.parse(avatarUri))
            } else {
                setImageBitmap(AvatarFactory.create(resources, name, publicKey, Px(min(width, height))))
            }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null || !statusIndicatorVisible) return

        val size = min(width, height).toFloat()

        // Calculating status indicator size and margin in relation to the overall size
        val avatarImageRadius = size / 2
        val avatarImageDiagonal = sqrt(size.pow(2) + size.pow(2)) // Pythagorean theorem
        val avatarImageDistanceFromCorner = avatarImageDiagonal / 2 - avatarImageRadius
        val statusIndicatorSize = size * STATUS_INDICATOR_SIZE_RATIO_WITH_AVATAR
        val statusIndicatorDiagonal =
            sqrt(statusIndicatorSize.pow(2) + statusIndicatorSize.pow(2)) // Pythagorean theorem
        val statusIndicatorMarginDiagonal = avatarImageDistanceFromCorner - statusIndicatorDiagonal / 2
        val statusIndicatorMargin =
            sqrt(statusIndicatorMarginDiagonal.pow(2) / 2).toInt() // Pythagorean theorem

        val radius = statusIndicatorSize / 2
        val x = width - (width - size) / 2 - statusIndicatorMargin - radius
        val y = height - (height - size) / 2 - statusIndicatorMargin - radius

        canvas.drawCircle(x, y, radius, statusIndicatorPaint)
    }
}
