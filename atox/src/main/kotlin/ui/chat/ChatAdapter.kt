package ltd.evilcorp.atox.ui.chat

import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import java.text.DateFormat
import ltd.evilcorp.atox.R
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.Sender
import ltd.evilcorp.core.vo.isRejected
import ltd.evilcorp.core.vo.isStarted

private fun inflateView(type: ChatItemType, inflater: LayoutInflater): View =
    inflater.inflate(
        when (type) {
            ChatItemType.SentMessage -> R.layout.chat_message_sent
            ChatItemType.ReceivedMessage -> R.layout.chat_message_received
            ChatItemType.SentAction -> R.layout.chat_action_sent
            ChatItemType.ReceivedAction -> R.layout.chat_action_received
            ChatItemType.ReceivedFileTransfer, ChatItemType.SentFileTransfer -> R.layout.chat_filetransfer
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
    val container = row.findViewById(R.id.fileTransferContainer) as LinearLayout
    val fileName = row.findViewById(R.id.fileName) as TextView
    val progress = row.findViewById(R.id.progress) as ProgressBar
    val timestamp = row.findViewById(R.id.timestamp) as TextView
    val acceptLayout = row.findViewById(R.id.acceptLayout) as View
    val accept = row.findViewById(R.id.accept) as Button
    val reject = row.findViewById(R.id.reject) as Button
}

private const val TAG = "ChatAdapter"

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
            ChatItemType.SentFileTransfer, ChatItemType.ReceivedFileTransfer -> {
                val message = messages[position]
                var fileTransfer = fileTransfers.find { it.id == message.correlationId }
                if (fileTransfer == null) {
                    Log.e(TAG, "Unable to find ft ${message.correlationId} for ${message.publicKey} required for view")
                    fileTransfer = FileTransfer("", 0, 0, 0, "", false)
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

                if (fileTransfer.isRejected()) {
                    vh.acceptLayout.visibility = View.GONE
                    vh.progress.visibility = View.GONE
                } else if (!fileTransfer.isStarted()) {
                    if (fileTransfer.outgoing) {
                        vh.acceptLayout.visibility = View.GONE
                        vh.progress.visibility = View.VISIBLE
                    } else {
                        vh.acceptLayout.visibility = View.VISIBLE
                        vh.progress.visibility = View.GONE
                    }
                    vh.accept.setOnClickListener {
                        (parent as ListView).performItemClick(it, position, position.toLong())
                    }
                    vh.reject.setOnClickListener {
                        (parent as ListView).performItemClick(it, position, position.toLong())
                    }
                } else {
                    vh.acceptLayout.visibility = View.GONE
                    vh.progress.visibility = View.VISIBLE
                }

                vh.fileName.text = fileTransfer.fileName
                vh.progress.max = fileTransfer.fileSize.toInt()
                vh.progress.progress = fileTransfer.progress.toInt()
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

                val layout = vh.container.layoutParams as RelativeLayout.LayoutParams
                val alignment = if (fileTransfer.outgoing) {
                    RelativeLayout.ALIGN_PARENT_END
                } else {
                    RelativeLayout.ALIGN_PARENT_START
                }
                layout.addRule(alignment)
                vh.container.layoutParams = layout

                view
            }
        }
}
