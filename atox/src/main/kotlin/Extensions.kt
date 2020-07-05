package ltd.evilcorp.atox

import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import ltd.evilcorp.atox.di.ViewModelFactory

val Fragment.vmFactory: ViewModelFactory
    get() = (requireActivity() as MainActivity).vmFactory

class NoSuchArgumentException(arg: String) : Exception("No such argument: $arg")

fun Fragment.requireStringArg(key: String) =
    arguments?.getString(key) ?: throw NoSuchArgumentException(key)

fun View.setUpFullScreenUi(listener: (v: View, insets: WindowInsets) -> WindowInsets) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        setOnApplyWindowInsetsListener(listener)
    }
}
