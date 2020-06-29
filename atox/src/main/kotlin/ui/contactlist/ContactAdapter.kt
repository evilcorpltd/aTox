package ltd.evilcorp.atox.ui.contactlist

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.text.DateFormat
import kotlinx.android.synthetic.main.contact_list_view_item.view.*
import kotlinx.android.synthetic.main.contact_list_view_item.view.publicKey as contactPublicKey
import kotlinx.android.synthetic.main.friend_request_item.view.*
import kotlinx.android.synthetic.main.friend_request_item.view.publicKey as friendRequestPublicKey
import kotlinx.android.synthetic.main.profile_image_layout.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.ui.colorByStatus
import ltd.evilcorp.atox.ui.setAvatarFromContact
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest

enum class ContactListItemType {
    FriendRequest,
    Contact
}

private val types = ContactListItemType.values()

class ContactAdapter(
    private val inflater: LayoutInflater,
    private val resources: Resources
) : BaseAdapter() {
    var friendRequests: List<FriendRequest> = listOf()
    var contacts: List<Contact> = listOf()

    override fun getCount(): Int = friendRequests.size + contacts.size
    override fun getItem(position: Int): Any = when {
        position < friendRequests.size -> friendRequests[position]
        else -> contacts[position - friendRequests.size]
    }

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getViewTypeCount(): Int = 2
    override fun getItemViewType(position: Int): Int = when {
        position < friendRequests.size -> ContactListItemType.FriendRequest.ordinal
        else -> ContactListItemType.Contact.ordinal
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        when (types[getItemViewType(position)]) {
            ContactListItemType.FriendRequest -> {
                val view: View
                val vh: FriendRequestViewHolder

                if (convertView == null) {
                    view = inflater.inflate(R.layout.friend_request_item, parent, false)
                    vh = FriendRequestViewHolder(view)
                    view.tag = vh
                } else {
                    view = convertView
                    vh = view.tag as FriendRequestViewHolder
                }

                friendRequests[position].run {
                    vh.publicKey.text = publicKey
                    vh.message.text = message
                }

                view
            }
            ContactListItemType.Contact -> {
                val view: View
                val vh: ContactViewHolder

                if (convertView == null) {
                    view = inflater.inflate(R.layout.contact_list_view_item, parent, false)
                    vh = ContactViewHolder(view)
                    view.tag = vh
                } else {
                    view = convertView
                    vh = view.tag as ContactViewHolder
                }

                contacts[position - friendRequests.size].run {
                    val shortId = publicKey.take(8)
                    vh.publicKey.text = String.format("%s %s", shortId.take(4), shortId.takeLast(4))
                    vh.name.text = name
                    vh.lastMessage.text = if (lastMessage != 0L) {
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                            .format(lastMessage)
                    } else {
                        resources.getText(R.string.never)
                    }
                    vh.statusMessage.text = statusMessage
                    vh.status.setColorFilter(colorByStatus(resources, this))
                    setAvatarFromContact(vh.image, this)
                    vh.unreadIndicator.visibility = if (hasUnreadMessages) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }

                view
            }
        }

    private class FriendRequestViewHolder(row: View) {
        val publicKey: TextView = row.friendRequestPublicKey
        val message: TextView = row.message
    }

    private class ContactViewHolder(row: View) {
        val name: TextView = row.name
        val publicKey: TextView = row.contactPublicKey
        val statusMessage: TextView = row.statusMessage
        val lastMessage: TextView = row.lastMessage
        val status: ImageView = row.statusIndicator
        val image: ImageView = row.profileImage
        val unreadIndicator: ImageView = row.unreadIndicator
    }
}
