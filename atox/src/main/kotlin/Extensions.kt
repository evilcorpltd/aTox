package ltd.evilcorp.atox

import androidx.fragment.app.Fragment
import ltd.evilcorp.atox.di.ViewModelFactory

val Fragment.vmFactory: ViewModelFactory
    get() = (requireActivity() as MainActivity).vmFactory

fun Fragment.requireStringArg(key: String) = arguments?.getString(key) ?: throw Exception("Missing argument $key")
