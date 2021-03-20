package ltd.evilcorp.atox.ui.contactlist

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.domain.feature.ChatManager
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.feature.FriendRequestManager
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.ProxyType
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.SaveOptions
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxSaveStatus
import ltd.evilcorp.domain.tox.testToxSave

class ContactListViewModel @Inject constructor(
    private val context: Context,
    private val resolver: ContentResolver,
    private val chatManager: ChatManager,
    private val contactManager: ContactManager,
    private val friendRequestManager: FriendRequestManager,
    private val tox: Tox,
    private val toxStarter: ToxStarter,
    userManager: UserManager
) : ViewModel(), CoroutineScope by GlobalScope {
    val publicKey by lazy { tox.publicKey }

    val user: LiveData<User> by lazy { userManager.get(publicKey).asLiveData() }
    val contacts: LiveData<List<Contact>> = contactManager.getAll().asLiveData()
    val friendRequests: LiveData<List<FriendRequest>> = friendRequestManager.getAll().asLiveData()

    fun isToxRunning() = tox.started
    fun tryLoadTox(): ToxSaveStatus = toxStarter.tryLoadTox()
    fun quitTox() = toxStarter.stopTox()

    fun acceptFriendRequest(friendRequest: FriendRequest) = friendRequestManager.accept(friendRequest)
    fun rejectFriendRequest(friendRequest: FriendRequest) = friendRequestManager.reject(friendRequest)
    fun deleteContact(publicKey: PublicKey) = contactManager.delete(publicKey)

    fun saveToxBackupTo(uri: Uri) = launch(Dispatchers.IO) {
        // Export the save.
        resolver.openFileDescriptor(uri, "w")!!.use { fd ->
            FileOutputStream(fd.fileDescriptor).use { out ->
                val saveData = tox.getSaveData().await()
                out.write(saveData)
            }
        }

        // Verify that the exported save can be imported.
        resolver.openFileDescriptor(uri, "r")!!.use { fd ->
            FileInputStream(fd.fileDescriptor).use { ios ->
                val saveData = ios.readBytes()
                val save = SaveOptions(saveData, true, ProxyType.None, "", 0)
                val toast = when (val status = testToxSave(save)) {
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
