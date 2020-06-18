package ltd.evilcorp.atox.ui.create_profile

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox
import javax.inject.Inject

class CreateProfileViewModel @Inject constructor(
    private val resolver: ContentResolver,
    private val userManager: UserManager,
    private val tox: Tox,
    private val toxStarter: ToxStarter
) : ViewModel() {
    val publicKey: PublicKey by lazy { tox.publicKey }

    fun startTox(save: ByteArray? = null): Boolean = toxStarter.startTox(save)
    fun tryImportToxSave(uri: Uri): ByteArray? = resolver.openInputStream(uri)?.readBytes()
    fun create(user: User) = userManager.create(user)
    fun verifyUserExists(publicKey: PublicKey) = userManager.verifyExists(publicKey)
}
