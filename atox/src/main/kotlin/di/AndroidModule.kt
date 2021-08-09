// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AndroidModule {
    @Provides
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver
}
