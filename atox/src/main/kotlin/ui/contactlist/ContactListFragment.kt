package ltd.evilcorp.atox.ui.contactlist

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.contact_list_view_item.view.*
import kotlinx.android.synthetic.main.fragment_contact_list.*
import kotlinx.android.synthetic.main.fragment_contact_list.view.*
import kotlinx.android.synthetic.main.nav_header_contact_list.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.setUpFullScreenUi
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.tox.PublicKey

private const val REQUEST_CODE_BACKUP_TOX = 9202

class ContactListFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private val viewModel: ContactListViewModel by viewModels { vmFactory }
    private var backupFileNameHint = "something_is_broken.tox"
    private lateinit var currentStatus: UserStatus

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_contact_list, container, false).apply {
        if (!viewModel.isToxRunning()) return@apply

        setUpFullScreenUi { v, insets ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@setUpFullScreenUi insets
            v.updatePadding(left = insets.systemWindowInsetLeft)
            toolbar.updatePadding(left = insets.systemWindowInsetLeft)
            navView.updatePadding(left = insets.systemWindowInsetLeft)
            contactList.updatePadding(bottom = insets.systemWindowInsetBottom)
            insets
        }

        toolbar.title = getText(R.string.app_name)

        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            if (user == null) return@Observer

            backupFileNameHint = user.name + ".tox"
            currentStatus = user.status

            navView.getHeaderView(0).apply {
                profileName.text = user.name
                profileStatusMessage.text = user.statusMessage

                val statusColor = when (user.status) {
                    UserStatus.None -> ResourcesCompat.getColor(
                        resources,
                        R.color.statusAvailable,
                        null
                    )
                    UserStatus.Away -> ResourcesCompat.getColor(resources, R.color.statusAway, null)
                    UserStatus.Busy -> ResourcesCompat.getColor(resources, R.color.statusBusy, null)
                }
                statusSwitcher.setColorFilter(statusColor)
            }

            val connectionString = if (user.connectionStatus != ConnectionStatus.None) {
                getText(R.string.connected)
            } else {
                getText(R.string.connecting)
            }
            toolbar.subtitle = getString(
                R.string.connection_and_status,
                connectionString,
                resources.getStringArray(R.array.user_statuses)[user.status.ordinal]
            )
        })

        navView.setNavigationItemSelectedListener(this@ContactListFragment)
        navView.getHeaderView(0).apply {
            profileName.setOnClickListener {
                val nameEdit = EditText(requireContext()).apply {
                    text.append((it as TextView).text.toString())
                    setSingleLine()
                }
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.name)
                    .setView(nameEdit)
                    .setPositiveButton(R.string.update) { _, _ ->
                        viewModel.setName(nameEdit.text.toString())
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .show()
            }

            profileStatusMessage.setOnClickListener {
                val statusMessageEdit =
                    EditText(requireContext()).apply {
                        text.append((it as TextView).text.toString())
                        setSingleLine()
                    }
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.status_message)
                    .setView(statusMessageEdit)
                    .setPositiveButton(R.string.update) { _, _ ->
                        viewModel.setStatusMessage(statusMessageEdit.text.toString())
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .show()
            }

            statusSwitcher.setOnClickListener {
                val statuses = UserStatus.values()
                viewModel.setStatus(statuses[(currentStatus.ordinal + 1) % statuses.size])
            }
        }

        val contactAdapter = ContactAdapter(inflater, resources)
        contactList.adapter = contactAdapter
        registerForContextMenu(contactList)

        viewModel.friendRequests.observe(viewLifecycleOwner, Observer { friendRequests ->
            contactAdapter.friendRequests = friendRequests
            contactAdapter.notifyDataSetChanged()

            noContactsCallToAction.visibility = if (contactAdapter.isEmpty) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })

        viewModel.contacts.observe(viewLifecycleOwner, Observer { contacts ->
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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                activity?.finish()
            }
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

        when (contactList.adapter.getItemViewType(info.position)) {
            ContactListItemType.FriendRequest.ordinal -> {
                menu.setHeaderTitle(info.targetView.publicKey.text)
                inflater.inflate(R.menu.friend_request_context_menu, menu)
            }
            ContactListItemType.Contact.ordinal -> {
                menu.setHeaderTitle(info.targetView.name.text)
                inflater.inflate(R.menu.contact_list_context_menu, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo

        return when (info.targetView.id) {
            R.id.friendRequestItem -> {
                val friendRequest = contactList.adapter.getItem(info.position) as FriendRequest
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
            R.id.share_tox_id -> {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, viewModel.toxId.string())
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.tox_id_share)))
            }
            R.id.copy_tox_id -> {
                val clipboard = requireActivity().getSystemService<ClipboardManager>()!!
                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        getText(R.string.tox_id),
                        viewModel.toxId.string()
                    )
                )

                Toast.makeText(
                    requireContext(),
                    getText(R.string.tox_id_copied),
                    Toast.LENGTH_SHORT
                ).show()
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
                viewModel.quitTox()
                activity?.finishAffinity()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.getSystemService<InputMethodManager>().let { imm ->
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun openChat(contact: Contact) = findNavController().navigate(
        R.id.action_contactListFragment_to_chatFragment,
        Bundle().apply { putString(CONTACT_PUBLIC_KEY, contact.publicKey) }
    )
}
