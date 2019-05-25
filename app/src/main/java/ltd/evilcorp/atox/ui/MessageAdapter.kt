package ltd.evilcorp.atox.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.vo.MessageModel
import ltd.evilcorp.atox.vo.Sender

class MessagesAdapter(private val context: Context, private val messages: MutableList<MessageModel>) : BaseAdapter() {
    override fun getCount(): Int = messages.size
    override fun getItem(position: Int): Any = messages[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // TODO(robinlinden): Optimise this.
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(
            if (messages[position].sender == Sender.Sent) R.layout.message_sent else R.layout.message_received,
            null,
            true
        )

        val vh = ViewHolder(view)
        view.tag = vh

        vh.message.text = messages[position].message

        return view!!
    }

    private class ViewHolder(row: View) {
        val message = row.findViewById(R.id.message) as TextView
    }
}
