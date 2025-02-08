// SPDX-FileCopyrightText: 2021-2025 Robin Lindén <dev@robinlinden.eu>
// SPDX-FileCopyrightText: 2021-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.RemoteInput
import androidx.core.content.IntentCompat
import im.tox.tox4j.av.exceptions.ToxavAnswerException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.feature.CallManager
import ltd.evilcorp.domain.feature.CallState
import ltd.evilcorp.domain.feature.ChatManager
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.Tox

const val KEY_TEXT_REPLY = "key_text_reply"
const val KEY_CONTACT_PK = "key_contact_pk"
const val KEY_ACTION = "key_action"

enum class Action {
    CallAccept,
    CallEnd,
    CallIgnore,
    CallReject,
    MarkAsRead,
}

private const val TAG = "ActionReceiver"

class ActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var callManager: CallManager

    @Inject
    lateinit var chatManager: ChatManager

    @Inject
    lateinit var contactManager: ContactManager

    @Inject
    lateinit var contactRepository: ContactRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var tox: Tox

    @Inject
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as App).component.inject(this)

        scope.launch {
            val pk = intent.getStringExtra(KEY_CONTACT_PK)?.let { PublicKey(it) }
            if (pk == null) {
                Log.e(TAG, "Got intent without required key $KEY_CONTACT_PK $intent")
                return@launch
            }
            if (!contactRepository.exists(pk.string())) {
                notificationHelper.dismissNotifications(pk)
                notificationHelper.dismissCallNotification(pk)
                return@launch
            }

            RemoteInput.getResultsFromIntent(intent)?.let { results ->
                results.getCharSequence(KEY_TEXT_REPLY)?.toString()?.let { input ->
                    contactRepository.setHasUnreadMessages(pk.string(), false)
                    chatManager.sendMessage(pk, input)
                    notificationHelper.showMessageNotification(
                        Contact(pk.string(), tox.getName()),
                        input,
                        outgoing = true,
                    )
                    return@launch
                }
            }

            when (IntentCompat.getSerializableExtra(intent, KEY_ACTION, Action::class.java)) {
                Action.CallAccept -> acceptCall(context, pk)
                Action.CallEnd, Action.CallReject -> {
                    callManager.endCall(pk)
                    notificationHelper.dismissCallNotification(pk)
                }
                Action.CallIgnore -> callManager.removePendingCall(pk)
                Action.MarkAsRead -> {
                    contactRepository.setHasUnreadMessages(pk.string(), false)
                    notificationHelper.dismissNotifications(pk)
                }
                null -> Log.e(TAG, "Missing action in intent $intent")
            }
        }
    }

    private suspend fun acceptCall(context: Context, pk: PublicKey) {
        val contact = contactManager.get(pk).firstOrNull().let {
            if (it != null) {
                it
            } else {
                Log.e(TAG, "Unable to get contact ${pk.fingerprint()} for call notification")
                Contact(publicKey = pk.string(), name = pk.fingerprint())
            }
        }

        if (callManager.inCall.value is CallState.InCall) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.error_simultaneous_calls), Toast.LENGTH_LONG).show()
                notificationHelper.showPendingCallNotification(UserStatus.Busy, contact)
            }
            return
        }

        try {
            callManager.startCall(pk)
            notificationHelper.showOngoingCallNotification(contact)
        } catch (e: ToxavAnswerException) {
            Log.e(TAG, e.toString())
            return
        }

        val isSendingAudio = context.hasPermission(Manifest.permission.RECORD_AUDIO) && callManager.startSendingAudio()
        if (!isSendingAudio) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, R.string.call_mic_permission_needed, Toast.LENGTH_LONG).show()
            }
        }
    }
}
