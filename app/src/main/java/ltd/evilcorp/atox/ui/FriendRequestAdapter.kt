package ltd.evilcorp.atox.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.contact_list_view_item.view.publicKey
import kotlinx.android.synthetic.main.friend_request_item.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.repository.FriendRequestRepository
import ltd.evilcorp.atox.tox.byteArrayToHex
import ltd.evilcorp.atox.vo.FriendRequest

class FriendRequestAdapter(
    private val context: Context,
    lifecycleOwner: LifecycleOwner,
    friendRequestRepository: FriendRequestRepository
) : BaseAdapter() {
    private var friendRequests: List<FriendRequest> = listOf()

    init {
        friendRequestRepository.getAll().observe(lifecycleOwner, Observer {
            friendRequests = it
            notifyDataSetChanged()
        })
    }

    override fun getCount(): Int = friendRequests.size
    override fun getItem(position: Int): Any = friendRequests[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val vh: ViewHolder

        if (convertView == null) {
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.friend_request_item, parent, false)
            vh = ViewHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }

        val friendRequest = friendRequests[position]
        vh.publicKey.text = friendRequest.publicKey.byteArrayToHex()
        vh.message.text = friendRequest.message

        return view
    }

    private class ViewHolder(row: View) {
        val publicKey: TextView = row.publicKey
        val message: TextView = row.message
    }
}
