package ltd.evilcorp.atox.di

import im.tox.tox4j.core.options.SaveDataOptions
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.repository.FriendRequestRepository
import ltd.evilcorp.atox.repository.UserRepository
import ltd.evilcorp.atox.tox.ToxThread
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToxThreadFactory @Inject constructor(
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val userRepository: UserRepository,
    private val toxFactory: ToxFactory
) {
    var instance: ToxThread? = null

    fun create(saveOption: SaveDataOptions): ToxThread {
        if (instance == null) {
            instance = ToxThread(
                saveOption,
                toxFactory,
                contactRepository,
                friendRequestRepository,
                userRepository
            )
        }

        return instance!!
    }
}
