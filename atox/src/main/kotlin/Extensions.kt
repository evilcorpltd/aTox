// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ltd.evilcorp.atox.di.ViewModelFactory

fun Context.hasPermission(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

val Fragment.vmFactory: ViewModelFactory
    get() = (requireActivity() as? MainActivity)?.vmFactory ?: (requireActivity() as NewMainActivity).vmFactory

class NoSuchArgumentException(arg: String) : Exception("No such argument: $arg")

fun Fragment.requireStringArg(key: String) =
    arguments?.getString(key) ?: throw NoSuchArgumentException(key)

fun String.truncated(length: Int): String =
    if (this.length > length) {
        this.take(length - 1) + "…"
    } else {
        this
    }
