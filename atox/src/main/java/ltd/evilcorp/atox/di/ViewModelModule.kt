package ltd.evilcorp.atox.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import ltd.evilcorp.atox.ui.addcontact.AddContactViewModel
import ltd.evilcorp.atox.ui.chat.ChatViewModel
import ltd.evilcorp.atox.ui.contact_profile.ContactProfileViewModel
import ltd.evilcorp.atox.ui.contactlist.ContactListViewModel
import ltd.evilcorp.atox.ui.profile.ProfileViewModel
import kotlin.reflect.KClass

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
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel(vm: ProfileViewModel): ViewModel
}
