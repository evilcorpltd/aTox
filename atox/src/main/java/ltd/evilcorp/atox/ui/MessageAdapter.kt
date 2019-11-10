package ltd.evilcorp.atox.ui

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ltd.evilcorp.atox.R
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.Sender
import java.text.DateFormat

private fun inflateView(type: Sender, inflater: LayoutInflater): View =
    inflater.inflate(
        if (type == Sender.Sent) R.layout.message_sent else R.layout.message_received,
        null,
        true
    )

class MessagesAdapter(
    private val inflater: LayoutInflater,
    private val resources: Resources
) : BaseAdapter() {
    var messages: List<Message> = ArrayList()

    override fun getCount(): Int = messages.size
    override fun getItem(position: Int): Any = messages[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val message = messages[position]

        val view: View
        val vh: ViewHolder

        if (convertView != null && (convertView.tag as? ViewHolder)?.sender == message.sender) {
            view = convertView
            vh = view.tag as ViewHolder
        } else {
            view = inflateView(message.sender, inflater)
            vh = ViewHolder(view, message.sender)
            view.tag = vh
        }

        vh.message.text = message.message
        vh.timestamp.text = if (message.timestamp != 0L) {
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(message.timestamp)
        } else {
            resources.getText(R.string.sending)
        }

        return view
    }

    private class ViewHolder(row: View, val sender: Sender) {
        val message = row.findViewById(R.id.message) as TextView
        val timestamp = row.findViewById(R.id.timestamp) as TextView
    }
}
