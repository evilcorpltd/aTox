package ltd.evilcorp.atox

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ContactAdapter(private val context: Context, private val contacts: ArrayList<ContactModel>) :
    BaseAdapter() {

    override fun getCount(): Int = contacts.size
    override fun getItem(position: Int): Any = contacts[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        val vh: ViewHolder

        if (convertView == null) {
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.contact_list_view_item, null, true)
            vh = ViewHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }

        vh.name.text = contacts[position].name
        vh.toxId.text = contacts[position].toxId

        return view!!
    }

    private class ViewHolder(row: View) {
        val name: TextView = row.findViewById(R.id.name) as TextView
        val toxId: TextView = row.findViewById(R.id.toxId) as TextView
    }
}
