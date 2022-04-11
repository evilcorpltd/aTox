// SPDX-FileCopyrightText: 2020-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.chat

import android.content.res.Resources
import android.text.format.Formatter
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.squareup.picasso.Picasso
import java.net.URLConnection
import java.text.DateFormat
import java.util.Locale
import kotlin.math.roundToInt
import ltd.evilcorp.atox.R
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.Sender
import ltd.evilcorp.core.vo.isComplete
import ltd.evilcorp.core.vo.isRejected
import ltd.evilcorp.core.vo.isStarted

private const val TAG = "ChatAdapter"

private fun FileTransfer.isImage() = try {
    URLConnection.guessContentTypeFromName(fileName).startsWith("image/")
} catch (e: Exception) {
    Log.e(TAG, e.toString())
    false
}

private fun inflateView(type: ChatItemType, inflater: LayoutInflater): View =
    inflater.inflate(
        when (type) {
            ChatItemType.SentMessage -> R.layout.chat_message_sent
            ChatItemType.ReceivedMessage -> R.layout.chat_message_received
            ChatItemType.SentAction -> R.layout.chat_action_sent
            ChatItemType.ReceivedAction -> R.layout.chat_action_received
            ChatItemType.SentFileTransfer, ChatItemType.ReceivedFileTransfer -> R.layout.chat_filetransfer
        },
        null,
        true
    )

private enum class ChatItemType {
    ReceivedMessage,
    SentMessage,
    ReceivedAction,
    SentAction,
    ReceivedFileTransfer,
    SentFileTransfer,
}

private val types = ChatItemType.values()
private val timeFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

private class MessageViewHolder(row: View) {
    val message = row.findViewById(R.id.message) as TextView
    val timestamp = row.findViewById(R.id.timestamp) as TextView
}

private class FileTransferViewHolder(row: View) {
    val container = row.findViewById(R.id.fileTransfer) as RelativeLayout
    val fileName = row.findViewById(R.id.fileName) as TextView
    val fileSize = row.findViewById(R.id.fileSize) as TextView
    val progress = row.findViewById(R.id.progress) as ProgressBar
    val state = row.findViewById(R.id.state) as TextView
    val timestamp = row.findViewById(R.id.timestamp) as TextView
    val acceptLayout = row.findViewById(R.id.acceptLayout) as View
    val accept = row.findViewById(R.id.accept) as Button
    val reject = row.findViewById(R.id.reject) as Button
    val cancelLayout = row.findViewById(R.id.cancelLayout) as View
    val cancel = row.findViewById(R.id.cancel) as Button
    val completedLayout = row.findViewById(R.id.completedLayout) as View
    val imagePreview = row.findViewById(R.id.imagePreview) as ImageView
}

class ChatAdapter(
    private val inflater: LayoutInflater,
    private val resources: Resources
) : BaseAdapter() {
    var messages: List<Message> = listOf()
    var fileTransfers: List<FileTransfer> = listOf()

    override fun getCount(): Int = messages.size
    override fun getItem(position: Int): Any = messages[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getViewTypeCount(): Int = types.size
    override fun getItemViewType(position: Int): Int = with(messages[position]) {
        when (type) {
            MessageType.Normal -> when (sender) {
                Sender.Sent -> ChatItemType.SentMessage.ordinal
                Sender.Received -> ChatItemType.ReceivedMessage.ordinal
            }
            MessageType.Action -> when (sender) {
                Sender.Sent -> ChatItemType.SentAction.ordinal
                Sender.Received -> ChatItemType.ReceivedAction.ordinal
            }
            MessageType.FileTransfer -> when (sender) {
                Sender.Sent -> ChatItemType.SentFileTransfer.ordinal
                Sender.Received -> ChatItemType.ReceivedFileTransfer.ordinal
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        when (val type = types[getItemViewType(position)]) {
            ChatItemType.ReceivedMessage, ChatItemType.SentMessage,
            ChatItemType.ReceivedAction, ChatItemType.SentAction -> {
                val message = messages[position]
                val view: View
                val vh: MessageViewHolder

                if (convertView != null) {
                    view = convertView
                    vh = view.tag as MessageViewHolder
                } else {
                    view = inflateView(type, inflater)
                    vh = MessageViewHolder(view)
                    view.tag = vh
                }

                val unsent = message.timestamp == 0L
                vh.message.text = message.message
                vh.timestamp.text = if (!unsent) {
                    timeFormatter.format(message.timestamp)
                } else {
                    resources.getText(R.string.sending)
                }

                vh.timestamp.visibility = if (position == messages.lastIndex || unsent) {
                    View.VISIBLE
                } else {
                    val next = messages[position + 1]
                    if (next.timestamp != 0L &&
                        next.sender == message.sender &&
                        next.timestamp - message.timestamp < 60_000
                    ) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }

                view
            }
            ChatItemType.ReceivedFileTransfer, ChatItemType.SentFileTransfer -> {
                val message = messages[position]
                val fileTransfer = fileTransfers.find { it.id == message.correlationId } ?: run {
                    Log.e(TAG, "Unable to find ft ${message.correlationId} for ${message.publicKey} required for view")
                    FileTransfer("", 0, 0, 0, "", message.sender == Sender.Sent)
                }

                val view: View
                val vh: FileTransferViewHolder

                if (convertView != null) {
                    view = convertView
                    vh = view.tag as FileTransferViewHolder
                } else {
                    view = inflateView(type, inflater)
                    vh = FileTransferViewHolder(view)
                    view.tag = vh
                }

                // TODO(robinlinden)
                // Updating the file transfer progress refreshes this so often that onClick-listeners never trigger
                // for some reason. Will revisit this once I've replaced the ListView with a RecyclerView.
                val touchListener = View.OnTouchListener { v, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        (parent as ListView).performItemClick(v, position, position.toLong())
                    }
                    false
                }
                vh.accept.setOnTouchListener(touchListener)
                vh.reject.setOnTouchListener(touchListener)
                vh.cancel.setOnTouchListener(touchListener)

                if (fileTransfer.isImage() && (fileTransfer.isComplete() || fileTransfer.outgoing)) {
                    vh.completedLayout.visibility = View.VISIBLE
                    Picasso.get().load(fileTransfer.destination)
                        .resize((Resources.getSystem().displayMetrics.widthPixels * 0.75).roundToInt(), 0)
                        .onlyScaleDown()
                        .into(vh.imagePreview)
                } else {
                    vh.completedLayout.visibility = View.GONE
                }

                vh.state.visibility = View.GONE
                if (fileTransfer.isRejected() || fileTransfer.isComplete()) {
                    vh.acceptLayout.visibility = View.GONE
                    vh.cancelLayout.visibility = View.GONE
                    vh.progress.visibility = View.GONE
                    vh.state.visibility =
                        if (fileTransfer.isImage() && fileTransfer.isComplete()) View.GONE else View.VISIBLE
                } else if (!fileTransfer.isStarted()) {
                    if (fileTransfer.outgoing) {
                        vh.acceptLayout.visibility = View.GONE
                        vh.cancelLayout.visibility = View.VISIBLE
                        vh.progress.visibility = View.VISIBLE
                    } else {
                        vh.acceptLayout.visibility = View.VISIBLE
                        vh.cancelLayout.visibility = View.GONE
                        vh.progress.visibility = View.GONE
                    }
                } else {
                    vh.acceptLayout.visibility = View.GONE
                    vh.cancelLayout.visibility = View.VISIBLE
                    vh.progress.visibility = View.VISIBLE
                }

                vh.fileName.text = fileTransfer.fileName
                vh.fileSize.text = Formatter.formatFileSize(inflater.context, fileTransfer.fileSize)
                vh.progress.max = fileTransfer.fileSize.toInt()
                vh.progress.progress = fileTransfer.progress.toInt()
                // TODO(robinlinden): paused, but that requires a database update and a release is overdue.
                val stateId = if (fileTransfer.isRejected()) R.string.cancelled else R.string.completed
                vh.state.text = resources.getString(stateId).lowercase(Locale.getDefault())
                vh.timestamp.text = timeFormatter.format(message.timestamp)

                vh.timestamp.visibility = if (position == messages.lastIndex) {
                    View.VISIBLE
                } else {
                    val next = messages[position + 1]
                    if (next.sender == message.sender && next.timestamp - message.timestamp < 60_000) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }

                vh.container.gravity = if (fileTransfer.outgoing) {
                    Gravity.END
                } else {
                    Gravity.START
                }

                view
            }
        }
}
