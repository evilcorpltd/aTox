// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.TypedValue
import android.widget.ImageButton
import androidx.core.content.res.ResourcesCompat
import ltd.evilcorp.atox.R
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.UserStatus


/**
 * Function will return the color of the status of the input contact
 * @param resources The resources of the app
 * @param contact The contact for whom to retrieve the status color.
 * @return The color int.
 */
internal fun colorByContactStatus(resources: Resources, contact: Contact) =
    if (contact.connectionStatus == ConnectionStatus.None)
        ResourcesCompat.getColor(
            resources,
            R.color.statusOffline,
            null
        )
    else colorFromStatus(resources, contact.status)


/**
 * Function will return the color of the status of the input user status
 * @param resources The resources of the app
 * @param status The user status for whom to return the corresponding color.
 * @return The color int.
 */
internal fun colorFromStatus(resources: Resources, status: UserStatus) = when (status) {
    UserStatus.None -> ResourcesCompat.getColor(resources, R.color.statusAvailable, null)
    UserStatus.Away -> ResourcesCompat.getColor(resources, R.color.statusAway, null)
    UserStatus.Busy -> ResourcesCompat.getColor(resources, R.color.statusBusy, null)
}


/**
 * Function will convert dp (Density Pixels) units to px (Pixels) units
 * @param dp The dp units.
 * @return The px units as Int.
 */
internal fun dpToPx(dp: Float, res: Resources): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.displayMetrics).toInt()


/**
 * Function will return whether or not night mode is set.
 * @param context The related context.
 * @return Boolean indicating whether or not night mode is set.
 */
internal fun isNightMode(context: Context) = context.resources.configuration.uiMode
    .and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES


/**
 * Function will set a transparent background to an image button with a ripple with color according
 * to whether or not night mode is set.
 * @param context The related context.
 * @param imageButton The ImageButton for whom to set the transparent background with ripple.
 */
internal fun setImageButtonRippleDayNight(context: Context, imageButton: ImageButton) =
    if (isNightMode(context))
        setImageButtonRipple(imageButton, Color.argb(51, 255, 255, 255))
    else setImageButtonRipple(imageButton, Color.argb(31, 0, 0, 0))


/**
 * Function will set a transparent background to an image button with a ripple with the input color.
 * @param imageButton The ImageButton for whom to set the transparent background with ripple.
 * @param colorInt The color of the ripple.
 */
internal fun setImageButtonRipple(imageButton: ImageButton, colorInt: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val background = GradientDrawable()
        background.shape = GradientDrawable.OVAL
        background.setColor(0x0FFFFFF)

        background.cornerRadius = 10f

        val mask = GradientDrawable()
        mask.shape = GradientDrawable.OVAL
        mask.setColor(-0x1000000)
        mask.cornerRadius = 5f

        val rippleColorLst = ColorStateList.valueOf(colorInt)
        val ripple = RippleDrawable(rippleColorLst, background, mask)
        imageButton.background = ripple
    }
}
