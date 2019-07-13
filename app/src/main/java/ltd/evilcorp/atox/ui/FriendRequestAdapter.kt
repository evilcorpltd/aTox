package ltd.evilcorp.atox.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.contact_list_view_item.view.publicKey
import kotlinx.android.synthetic.main.friend_request_item.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.tox.byteArrayToHex
import ltd.evilcorp.atox.vo.FriendRequest

class FriendRequestAdapter(private val inflater: LayoutInflater) : BaseAdapter() {
    var friendRequests: List<FriendRequest> = listOf()

    override fun getCount(): Int = friendRequests.size
    override fun getItem(position: Int): Any = friendRequests[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val vh: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.friend_request_item, parent, false)
            vh = ViewHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }

        val friendRequest = friendRequests[position]
        vh.publicKey.text = friendRequest.publicKey
        vh.message.text = friendRequest.message

        return view
    }

    private class ViewHolder(row: View) {
        val publicKey: TextView = row.publicKey
        val message: TextView = row.message
    }
}
