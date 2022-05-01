// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.di

import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.ui.addcontact.AddContactViewModel
import ltd.evilcorp.atox.ui.call.CallViewModel
import ltd.evilcorp.atox.ui.chat.ChatViewModel
import ltd.evilcorp.atox.ui.contact_profile.ContactProfileViewModel
import ltd.evilcorp.atox.ui.contactlist.ContactListViewModel
import ltd.evilcorp.atox.ui.create_profile.CreateProfileViewModel
import ltd.evilcorp.atox.ui.friend_request.FriendRequestViewModel
import ltd.evilcorp.atox.ui.settings.SettingsViewModel
import ltd.evilcorp.atox.ui.user_profile.UserProfileViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.diContext
import org.kodein.di.provider

fun viewModelModule(app: App) = DI.Module(name = "ViewModelModule") {
    importOnce(appModule(diContext(app)))
    importOnce(domainModule(diContext(app)))
    importOnce(coreModule())

    bind { provider { AddContactViewModel(app) } }
    bind { provider { CallViewModel(app) } }
    bind { provider { ChatViewModel(app) } }
    bind { provider { ContactListViewModel(app) } }
    bind { provider { ContactProfileViewModel(app) } }
    bind { provider { CreateProfileViewModel(app) } }
    bind { provider { FriendRequestViewModel(app) } }
    bind { provider { SettingsViewModel(app) } }
    bind { provider { UserProfileViewModel(app) } }
}
