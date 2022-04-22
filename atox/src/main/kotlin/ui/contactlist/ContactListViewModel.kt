// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.contactlist

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.settings.Settings
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.domain.feature.CallManager
import ltd.evilcorp.domain.feature.ChatManager
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.feature.FileTransferManager
import ltd.evilcorp.domain.feature.FriendRequestManager
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.ProxyType
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.SaveOptions
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxSaveStatus
import ltd.evilcorp.domain.tox.testToxSave
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class ContactListViewModel(app: App) : AndroidViewModel(app), DIAware {
    override val di by closestDI()

    private val scope: CoroutineScope by instance()
    private val context: Context by instance()
    private val resolver: ContentResolver by instance()
    private val callManager: CallManager by instance()
    private val chatManager: ChatManager by instance()
    private val contactManager: ContactManager by instance()
    private val fileTransferManager: FileTransferManager by instance()
    private val friendRequestManager: FriendRequestManager by instance()
    private val userManager: UserManager by instance()
    private val notificationHelper: NotificationHelper by instance()
    private val tox: Tox by instance()
    private val toxStarter: ToxStarter by instance()
    private val settings: Settings by instance()

    val publicKey by lazy { tox.publicKey }

    val user: LiveData<User> by lazy { userManager.get(publicKey).asLiveData() }
    val contacts: LiveData<List<Contact>> = contactManager.getAll().asLiveData()
    val friendRequests: LiveData<List<FriendRequest>> = friendRequestManager.getAll().asLiveData()

    fun isToxRunning() = tox.started
    fun tryLoadTox(password: String?): ToxSaveStatus = toxStarter.tryLoadTox(password)
    fun quitTox() = toxStarter.stopTox()

    fun quittingNeedsConfirmation(): Boolean = settings.confirmQuitting

    fun acceptFriendRequest(friendRequest: FriendRequest) = friendRequestManager.accept(friendRequest)
    fun rejectFriendRequest(friendRequest: FriendRequest) = friendRequestManager.reject(friendRequest)
    fun deleteContact(publicKey: PublicKey) {
        callManager.endCall(publicKey)
        notificationHelper.dismissNotifications(publicKey)
        notificationHelper.dismissCallNotification(publicKey)
        contactManager.delete(publicKey)
        chatManager.clearHistory(publicKey)
        scope.launch {
            fileTransferManager.deleteAll(publicKey)
        }
    }

    fun saveToxBackupTo(uri: Uri) = scope.launch(Dispatchers.IO) {
        // Export the save.
        resolver.openFileDescriptor(uri, "w")!!.use { fd ->
            FileOutputStream(fd.fileDescriptor).use { out ->
                out.write(tox.getSaveData())
            }
        }

        // Verify that the exported save can be imported.
        resolver.openFileDescriptor(uri, "r")!!.use { fd ->
            FileInputStream(fd.fileDescriptor).use { ios ->
                val saveData = ios.readBytes()
                val save = SaveOptions(saveData, true, ProxyType.None, "", 0)
                val toast = when (val status = testToxSave(save, tox.password)) {
                    ToxSaveStatus.Ok -> context.getText(R.string.tox_save_exported)
                    else -> context.getString(R.string.tox_save_export_failure, status.name)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, toast, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun onShareText(what: String, to: Contact) =
        chatManager.sendMessage(PublicKey(to.publicKey), what)
}
