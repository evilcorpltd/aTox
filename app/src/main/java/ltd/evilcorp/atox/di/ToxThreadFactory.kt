package ltd.evilcorp.atox.di

import ltd.evilcorp.atox.tox.SaveOptions
import ltd.evilcorp.atox.tox.ToxThread
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FriendRequestRepository
import ltd.evilcorp.core.repository.UserRepository
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

    fun create(saveOption: SaveOptions): ToxThread {
        if (instance == null) {
            instance = ToxThread(
                toxFactory.create(saveOption),
                contactRepository,
                friendRequestRepository,
                userRepository
            )
        }

        return instance!!
    }
}
