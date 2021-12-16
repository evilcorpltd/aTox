// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass
import ltd.evilcorp.atox.ui.addcontact.AddContactViewModel
import ltd.evilcorp.atox.ui.call.CallViewModel
import ltd.evilcorp.atox.ui.chat.ChatViewModel
import ltd.evilcorp.atox.ui.contact_profile.ContactProfileViewModel
import ltd.evilcorp.atox.ui.contactlist.ContactListViewModel
import ltd.evilcorp.atox.ui.create_profile.CreateProfileViewModel
import ltd.evilcorp.atox.ui.edit_text_value_dialog.EditTextValueDialog
import ltd.evilcorp.atox.ui.edit_text_value_dialog.EditTextValueDialogViewModel
import ltd.evilcorp.atox.ui.edit_user_profile.EditUserProfileViewModel
import ltd.evilcorp.atox.ui.friend_request.FriendRequestViewModel
import ltd.evilcorp.atox.ui.settings.SettingsViewModel
import ltd.evilcorp.atox.ui.user_profile.UserProfileViewModel

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(AddContactViewModel::class)
    abstract fun bindAddContactViewModel(vm: AddContactViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CallViewModel::class)
    abstract fun bindCallViewModel(vm: CallViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    abstract fun bindChatViewModel(vm: ChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactListViewModel::class)
    abstract fun bindContactListViewModel(vm: ContactListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactProfileViewModel::class)
    abstract fun bindContactProfileViewModel(vm: ContactProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FriendRequestViewModel::class)
    abstract fun bindFriendRequestViewModel(vm: FriendRequestViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateProfileViewModel::class)
    abstract fun bindProfileViewModel(vm: CreateProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(vm: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserProfileViewModel::class)
    abstract fun bindUserProfileViewModel(vm: UserProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditUserProfileViewModel::class)
    abstract fun bindEditUserProfileViewModel(vm: EditUserProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditTextValueDialogViewModel::class)
    abstract fun bindEditTextValueDialogViewModel(vm: EditTextValueDialogViewModel): ViewModel
}
