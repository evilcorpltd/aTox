package ltd.evilcorp.atox

import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import ltd.evilcorp.atox.di.ViewModelFactory

fun Context.getPreferences() =
    PreferenceManager.getDefaultSharedPreferences(this)

val Fragment.vmFactory: ViewModelFactory
    get() = (requireActivity() as MainActivity).vmFactory

fun Fragment.requireStringArg(key: String) =
    arguments?.getString(key) ?: throw Exception("Missing argument $key")

fun View.setUpFullScreenUi(listener: (v: View, insets: WindowInsets) -> WindowInsets) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        setOnApplyWindowInsetsListener(listener)
    }
}
