package ltd.evilcorp.atox.ui.contactlist

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.ContactListViewItemBinding
import ltd.evilcorp.atox.databinding.FragmentContactListBinding
import ltd.evilcorp.atox.databinding.FriendRequestItemBinding
import ltd.evilcorp.atox.databinding.NavHeaderContactListBinding
import ltd.evilcorp.atox.setUpFullScreenUi
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.ui.friend_request.FRIEND_REQUEST_PUBLIC_KEY
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.tox.PublicKey

private const val REQUEST_CODE_BACKUP_TOX = 9202

private fun User.online(): Boolean =
    connectionStatus != ConnectionStatus.None

class ContactListFragment :
    BaseFragment<FragmentContactListBinding>(FragmentContactListBinding::inflate),
    NavigationView.OnNavigationItemSelectedListener {

    private val viewModel: ContactListViewModel by viewModels { vmFactory }

    private var _navHeader: NavHeaderContactListBinding? = null
    private val navHeader get() = _navHeader!!

    private var backupFileNameHint = "something_is_broken.tox"

    private fun colorFromStatus(status: UserStatus) = when (status) {
        UserStatus.None -> ResourcesCompat.getColor(resources, R.color.statusAvailable, null)
        UserStatus.Away -> ResourcesCompat.getColor(resources, R.color.statusAway, null)
        UserStatus.Busy -> ResourcesCompat.getColor(resources, R.color.statusBusy, null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        _navHeader = NavHeaderContactListBinding.bind(binding.navView.getHeaderView(0))
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = binding.run {
        if (!viewModel.isToxRunning()) return@run

        view.setUpFullScreenUi { v, insets ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@setUpFullScreenUi insets
            v.updatePadding(left = insets.systemWindowInsetLeft)
            toolbar.updatePadding(left = insets.systemWindowInsetLeft)
            navView.updatePadding(left = insets.systemWindowInsetLeft)
            contactList.updatePadding(bottom = insets.systemWindowInsetBottom)
            insets
        }

        toolbar.title = getText(R.string.app_name)

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe

            backupFileNameHint = user.name + ".tox"

            navHeader.apply {
                profileName.text = user.name
                profileStatusMessage.text = user.statusMessage

                if (user.online()) {
                    statusIndicator.setColorFilter(colorFromStatus(user.status))
                } else {
                    statusIndicator.setColorFilter(R.color.statusOffline)
                }
            }

            toolbar.subtitle = if (user.online()) {
                resources.getStringArray(R.array.user_statuses)[user.status.ordinal]
            } else {
                getText(R.string.connecting)
            }
        }

        navView.setNavigationItemSelectedListener(this@ContactListFragment)

        val contactAdapter = ContactAdapter(layoutInflater, resources)
        contactList.adapter = contactAdapter
        registerForContextMenu(contactList)

        viewModel.friendRequests.observe(viewLifecycleOwner) { friendRequests ->
            contactAdapter.friendRequests = friendRequests
            contactAdapter.notifyDataSetChanged()

            noContactsCallToAction.visibility = if (contactAdapter.isEmpty) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            contactAdapter.contacts = contacts.sortedByDescending { contact ->
                when {
                    contact.lastMessage != 0L -> contact.lastMessage
                    contact.connectionStatus == ConnectionStatus.None -> -1000L
                    else -> -contact.status.ordinal.toLong()
                }
            }
            contactAdapter.notifyDataSetChanged()

            noContactsCallToAction.visibility = if (contactAdapter.isEmpty) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        contactList.setOnItemClickListener { _, _, position, _ ->
            when (contactList.adapter.getItemViewType(position)) {
                ContactListItemType.FriendRequest.ordinal -> {
                    openFriendRequest(contactList.getItemAtPosition(position) as FriendRequest)
                }
                ContactListItemType.Contact.ordinal -> {
                    openChat(contactList.getItemAtPosition(position) as Contact)
                }
            }
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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                activity?.finish()
            }
        }

        activity?.getSystemService<InputMethodManager>().let { imm ->
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        _navHeader = null
        super.onDestroyView()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val inflater: MenuInflater = requireActivity().menuInflater
        val info = menuInfo as AdapterView.AdapterContextMenuInfo

        when (binding.contactList.adapter.getItemViewType(info.position)) {
            ContactListItemType.FriendRequest.ordinal -> {
                val f = FriendRequestItemBinding.bind(info.targetView)
                menu.setHeaderTitle(f.publicKey.text)
                inflater.inflate(R.menu.friend_request_context_menu, menu)
            }
            ContactListItemType.Contact.ordinal -> {
                val c = ContactListViewItemBinding.bind(info.targetView)
                menu.setHeaderTitle(c.name.text)
                inflater.inflate(R.menu.contact_list_context_menu, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo

        return when (info.targetView.id) {
            R.id.friendRequestItem -> {
                val friendRequest = binding.contactList.adapter.getItem(info.position) as FriendRequest
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
                        val contact = binding.contactList.adapter.getItem(info.position) as Contact
                        viewModel.deleteContact(PublicKey(contact.publicKey))
                    }
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.drawer_profile -> {
                findNavController().navigate(R.id.action_contactListFragment_to_userProfileFragment)
            }
            R.id.add_contact -> findNavController().navigate(R.id.action_contactListFragment_to_addContactFragment)
            R.id.settings -> findNavController().navigate(R.id.action_contactListFragment_to_settingsFragment)
            R.id.export_tox_save -> {
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_TITLE, backupFileNameHint)
                }.also {
                    startActivityForResult(it, REQUEST_CODE_BACKUP_TOX)
                }
            }
            R.id.quit_tox -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.quit_confirm)
                    .setPositiveButton(R.string.quit) { _, _ ->
                        viewModel.quitTox()
                        activity?.finishAffinity()
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .show()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_BACKUP_TOX -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    viewModel.saveToxBackupTo(data.data as Uri)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!viewModel.isToxRunning()) viewModel.tryLoadTox()
    }

    override fun onStart() {
        super.onStart()
        if (!viewModel.isToxRunning()) {
            findNavController().navigate(R.id.action_contactListFragment_to_profileFragment)
        }
    }

    private fun openChat(contact: Contact) = findNavController().navigate(
        R.id.action_contactListFragment_to_chatFragment,
        bundleOf(CONTACT_PUBLIC_KEY to contact.publicKey)
    )

    private fun openFriendRequest(friendRequest: FriendRequest) = findNavController().navigate(
        R.id.action_contactListFragment_to_friendRequestFragment,
        bundleOf(FRIEND_REQUEST_PUBLIC_KEY to friendRequest.publicKey)
    )
}
