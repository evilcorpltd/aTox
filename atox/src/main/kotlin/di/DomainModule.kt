package ltd.evilcorp.atox.di

import ltd.evilcorp.domain.feature.CallManager
import ltd.evilcorp.domain.feature.ChatManager
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.feature.FileTransferManager
import ltd.evilcorp.domain.feature.FriendRequestManager
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.SaveManager
import ltd.evilcorp.domain.tox.SaveManagerImpl
import ltd.evilcorp.domain.tox.Tox
import org.kodein.di.DI
import org.kodein.di.DIContext
import org.kodein.di.bind
import org.kodein.di.singleton

fun domainModule(appContext: DIContext<*>) = DI.Module(name = "DomainModule") {
    importOnce(appModule(appContext))
    importOnce(coreModule())

    bind { singleton { CallManager(di, appContext) } }
    bind { singleton { ChatManager(di) } }
    bind { singleton { ContactManager(di) } }
    bind { singleton { FileTransferManager(di, appContext) } }
    bind { singleton { FriendRequestManager(di) } }
    bind<SaveManager> { singleton { SaveManagerImpl(di, appContext) } }
    bind { singleton { UserManager(di) } }
    bind { singleton { Tox(di) } }
}
