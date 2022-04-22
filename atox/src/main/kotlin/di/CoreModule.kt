package ltd.evilcorp.atox.di

import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FileTransferRepository
import ltd.evilcorp.core.repository.FriendRequestRepository
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.repository.UserRepository
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

fun coreModule() = DI.Module(name = "CoreModule") {
    importOnce(databaseModule())

    bind { singleton { ContactRepository(di) } }
    bind { singleton { FileTransferRepository(di) } }
    bind { singleton { FriendRequestRepository(di) } }
    bind { singleton { MessageRepository(di) } }
    bind { singleton { UserRepository(di) } }
}
