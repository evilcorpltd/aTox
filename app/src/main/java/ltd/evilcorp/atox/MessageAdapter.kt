package ltd.evilcorp.atox

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class MessagesAdapter(private val context: Context, private val messages: MutableList<MessageModel>) : BaseAdapter() {
    override fun getCount(): Int = messages.size
    override fun getItem(position: Int): Any = messages[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        val vh: ViewHolder

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(
                if (messages[position].sender == Sender.Sent) R.layout.message_sent else R.layout.message_received,
                null,
                true
            )
            vh = ViewHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }

        vh.message.text = messages[position].message

        return view!!
    }

    private class ViewHolder(row: View) {
        val message: TextView = row.findViewById(R.id.message) as TextView
    }
}
