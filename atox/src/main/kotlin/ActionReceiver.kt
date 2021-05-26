package ltd.evilcorp.atox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import javax.inject.Inject
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.feature.CallManager
import ltd.evilcorp.domain.feature.ChatManager
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox

const val KEY_TEXT_REPLY = "text_reply"
const val KEY_CALL = "accept_or_reject_call"
const val KEY_CONTACT_PK = "contact_pk"

class ActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var callManager: CallManager

    @Inject
    lateinit var chatManager: ChatManager

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var tox: Tox

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as App).component.inject(this)

        RemoteInput.getResultsFromIntent(intent)?.let { results ->
            results.getCharSequence(KEY_TEXT_REPLY)?.toString()?.let { input ->
                val pk = intent.getStringExtra(KEY_CONTACT_PK) ?: return
                chatManager.sendMessage(PublicKey(pk), input)
                notificationHelper.showMessageNotification(Contact(pk, tox.getName()), input, outgoing = true)
            }
        }

        intent.getStringExtra(KEY_CALL)?.let { callChoice ->
            val pk = intent.getStringExtra(KEY_CONTACT_PK) ?: return
            if (callChoice == "accept") {
                callManager.answerCall(PublicKey(pk))
                notificationHelper.showOngoingCallNotification(Contact(pk, tox.getName()))
            } else if (callChoice == "reject") {
                callManager.endCall(PublicKey(pk))
                notificationHelper.dismissCallNotification(Contact(pk))
            }
        }
    }
}
