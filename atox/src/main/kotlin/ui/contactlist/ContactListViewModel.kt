package ltd.evilcorp.atox.ui.contactlist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.ToxService
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.feature.FriendRequestManager
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox
import java.io.FileOutputStream
import javax.inject.Inject

class ContactListViewModel @Inject constructor(
    private val context: Context,
    private val contactManager: ContactManager,
    private val friendRequestManager: FriendRequestManager,
    private val userManager: UserManager,
    private val tox: Tox,
    private val toxStarter: ToxStarter
) : ViewModel(), CoroutineScope by GlobalScope {
    val publicKey by lazy { tox.publicKey }
    val toxId by lazy { tox.toxId }

    val user: LiveData<User> by lazy { userManager.get(publicKey) }
    val contacts = contactManager.getAll()
    val friendRequests = friendRequestManager.getAll()

    fun isToxRunning() = tox.started
    fun tryLoadTox(): Boolean = toxStarter.tryLoadTox()
    fun quitTox() = toxStarter.stopTox()

    fun acceptFriendRequest(friendRequest: FriendRequest) = friendRequestManager.accept(friendRequest)
    fun rejectFriendRequest(friendRequest: FriendRequest) = friendRequestManager.reject(friendRequest)
    fun deleteContact(publicKey: PublicKey) = contactManager.delete(publicKey)

    fun saveToxBackupTo(uri: Uri) = launch(Dispatchers.IO) {
        context.contentResolver.openFileDescriptor(uri, "w")!!.let { fd ->
            FileOutputStream(fd.fileDescriptor).let { out ->
                val saveData = tox.getSaveData().await()
                out.write(saveData)
                out.close()
            }
            fd.close()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getText(R.string.tox_save_exported), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
