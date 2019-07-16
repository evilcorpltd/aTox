package ltd.evilcorp.atox.ui.contactlist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.contact_list_fragment.*
import kotlinx.android.synthetic.main.contact_list_fragment.view.*
import kotlinx.android.synthetic.main.contact_list_view_item.view.*
import kotlinx.android.synthetic.main.nav_header_contact_list.view.*
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.activity.AddContactActivity
import ltd.evilcorp.atox.activity.ChatActivity
import ltd.evilcorp.atox.di.ViewModelFactory
import ltd.evilcorp.atox.ui.ContactAdapter
import ltd.evilcorp.atox.ui.FriendRequestAdapter
import ltd.evilcorp.atox.vo.ConnectionStatus
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.vo.FriendRequest
import javax.inject.Inject

class ContactListFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        fun newInstance(userPublicKey: String) = ContactListFragment().apply {
            arguments = Bundle().apply {
                putString("userPublicKey", userPublicKey)
            }
        }
    }

    @Inject
    lateinit var vmFactory: ViewModelFactory
    private lateinit var viewModel: ContactListViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProviders.of(this, vmFactory).get(ContactListViewModel::class.java)
        viewModel.publicKey = arguments!!.getString("userPublicKey")!!

        return inflater.inflate(R.layout.contact_list_fragment, container, false).apply {
            toolbar.title = getText(R.string.app_name)

            viewModel.user.observe(this@ContactListFragment, Observer { user ->
                navView.getHeaderView(0).apply {
                    profileName.text = user.name
                }
            })

            navView.setNavigationItemSelectedListener(this@ContactListFragment)

            val friendRequestAdapter = FriendRequestAdapter(inflater)
            friendRequests.adapter = friendRequestAdapter
            registerForContextMenu(friendRequests)
            viewModel.friendRequests.observe(this@ContactListFragment, Observer { friendRequests ->
                friendRequestAdapter.friendRequests = friendRequests
                friendRequestAdapter.notifyDataSetChanged()
            })

            val contactAdapter = ContactAdapter(inflater, resources)
            contactList.adapter = contactAdapter
            registerForContextMenu(contactList)
            viewModel.contacts.observe(this@ContactListFragment, Observer { contacts ->
                contactAdapter.contacts = contacts.sortedWith(
                    compareBy(
                        { contact -> contact.connectionStatus == ConnectionStatus.NONE },
                        Contact::lastMessage,
                        Contact::status
                    )
                )
                contactAdapter.notifyDataSetChanged()
            })
            contactList.setOnItemClickListener { _, _, position, _ ->
                openChat(contactList.getItemAtPosition(position) as Contact)
            }

            val toggle = ActionBarDrawerToggle(
                requireActivity(),
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val inflater: MenuInflater = requireActivity().menuInflater
        val info = menuInfo as AdapterView.AdapterContextMenuInfo

        when (v.id) {
            R.id.contactList -> {
                menu.setHeaderTitle(info.targetView.name.text)
                inflater.inflate(R.menu.contact_list_context_menu, menu)
            }
            R.id.friendRequests -> {
                menu.setHeaderTitle(info.targetView.publicKey.text)
                inflater.inflate(R.menu.friend_request_context_menu, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo

        return when (info.targetView.id) {
            R.id.friendRequestItem -> {
                val friendRequest = friendRequests.adapter.getItem(info.position) as FriendRequest
                when (item.itemId) {
                    R.id.accept -> {
                        viewModel.acceptFriendRequest(friendRequest)
                    }
                    R.id.reject -> {
                        viewModel.rejectFriendRequest(friendRequest)
                    }
                }
                true
            }
            R.id.contactListItem -> {
                when (item.itemId) {
                    R.id.delete -> {
                        val contact = contactList.adapter.getItem(info.position) as Contact
                        viewModel.deleteContact(contact)
                    }
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share_tox_id -> {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, App.toxThread.toxId)
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.tox_id_share)))
            }
            R.id.copy_tox_id -> {
                val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.primaryClip = ClipData.newPlainText(getText(R.string.tox_id), App.toxThread.toxId)

                Toast.makeText(requireContext(), getText(R.string.tox_id_copied), Toast.LENGTH_SHORT).show()
            }
            R.id.add_contact -> {
                startActivity(Intent(requireContext(), AddContactActivity::class.java))
            }
            R.id.settings -> {
                // TODO(robinlinden): Settings activity
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            requireActivity().finishAfterTransition()
        }
    }

    private fun openChat(contact: Contact) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("publicKey", contact.publicKey)
        startActivity(intent)
    }
}
