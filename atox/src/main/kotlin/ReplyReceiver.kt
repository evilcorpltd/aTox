package ltd.evilcorp.atox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import javax.inject.Inject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.feature.ChatManager
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox

const val KEY_TEXT_REPLY = "text_reply"
const val KEY_CONTACT_PK = "contact_pk"

class ReplyReceiver : BroadcastReceiver() {
    @Inject
    lateinit var chatManager: ChatManager

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var tox: Tox

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as App).component.inject(this)
        val results = RemoteInput.getResultsFromIntent(intent) ?: return
        val input = results.getCharSequence(KEY_TEXT_REPLY)?.toString() ?: return
        val pk = intent.getStringExtra(KEY_CONTACT_PK) ?: return
        GlobalScope.launch {
            chatManager.sendMessage(PublicKey(pk), input)
            notificationHelper.showMessageNotification(Contact(pk, tox.getName().await()), input, outgoing = true)
        }
    }
}
