// SPDX-FileCopyrightText: 2019-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.contactlist

import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.ContactListViewItemBinding
import ltd.evilcorp.atox.databinding.FragmentContactListBinding
import ltd.evilcorp.atox.databinding.FriendRequestItemBinding
import ltd.evilcorp.atox.databinding.NavHeaderContactListBinding
import ltd.evilcorp.atox.truncated
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.ReceiveShareDialog
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.ui.colorFromStatus
import ltd.evilcorp.atox.ui.friend_request.FRIEND_REQUEST_PUBLIC_KEY
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.ToxSaveStatus
import org.kodein.di.android.x.viewmodel.viewModel

const val ARG_SHARE = "share"
private const val MAX_CONFIRM_DELETE_STRING_LENGTH = 32

private fun User.online(): Boolean =
    connectionStatus != ConnectionStatus.None

class ContactListFragment :
    BaseFragment<FragmentContactListBinding>(FragmentContactListBinding::inflate),
    NavigationView.OnNavigationItemSelectedListener {

    private val viewModel: ContactListViewModel by viewModel()

    private var _navHeader: NavHeaderContactListBinding? = null
    private val navHeader get() = _navHeader!!

    private var backupFileNameHint = "something_is_broken.tox"

    private var shareDialog: ReceiveShareDialog? = null
    private var passwordDialog: AlertDialog? = null

    private val exportToxSaveLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument()) { dest ->
        if (dest == null) return@registerForActivityResult
        viewModel.saveToxBackupTo(dest)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        _navHeader = NavHeaderContactListBinding.bind(binding.navView.getHeaderView(0))
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = binding.run {
        if (!viewModel.isToxRunning()) return@run

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars())
            toolbar.updatePadding(left = insets.left)
            navView.updatePadding(left = insets.left)
            contactList.updatePadding(bottom = insets.bottom)
            compat
        }

        toolbar.title = getText(R.string.app_name)

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe

            backupFileNameHint = user.name + ".tox"

            navHeader.apply {
                profileName.text = user.name
                profileStatusMessage.text = user.statusMessage

                if (user.online()) {
                    statusIndicator.setColorFilter(colorFromStatus(requireContext(), user.status))
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

        val contactAdapter = ContactAdapter(layoutInflater, requireContext())
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

            shareDialog?.setContacts(contactAdapter.contacts)
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

        arguments?.getString(ARG_SHARE)?.let { share ->
            shareDialog = ReceiveShareDialog(
                requireContext(),
                (binding.contactList.adapter as ContactAdapter).contacts,
                share,
            ) {
                viewModel.onShareText(share, it)
                openChat(it)
            }
            shareDialog?.setOnDismissListener {
                shareDialog = null
                arguments?.remove(ARG_SHARE)
            }
            shareDialog?.show()
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
                    R.id.profile -> {
                        val contact = binding.contactList.adapter.getItem(info.position) as Contact
                        openProfile(contact)
                    }
                    R.id.delete -> {
                        val contact = binding.contactList.adapter.getItem(info.position) as Contact

                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.delete_contact)
                            .setMessage(
                                getString(
                                    R.string.contact_list_delete_contact_confirm,
                                    contact.name.truncated(MAX_CONFIRM_DELETE_STRING_LENGTH)
                                )
                            )
                            .setPositiveButton(R.string.delete) { _, _ ->
                                viewModel.deleteContact(PublicKey(contact.publicKey))
                            }
                            .setNegativeButton(android.R.string.cancel, null).show()
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
            R.id.export_tox_save -> exportToxSaveLauncher.launch(backupFileNameHint)
            R.id.quit_tox -> {
                if (!viewModel.quittingNeedsConfirmation()) {
                    viewModel.quitTox()
                    activity?.finishAffinity()
                    return false
                }

                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.quit_confirm)
                    .setPositiveButton(R.string.quit) { _, _ ->
                        viewModel.quitTox()
                        activity?.finishAffinity()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!viewModel.isToxRunning()) viewModel.tryLoadTox(null)
    }

    override fun onStart() {
        super.onStart()
        if (!viewModel.isToxRunning()) {
            when (val status = viewModel.tryLoadTox(null)) {
                ToxSaveStatus.BadProxyHost, ToxSaveStatus.BadProxyPort,
                ToxSaveStatus.BadProxyType, ToxSaveStatus.ProxyNotFound -> {
                    Toast.makeText(requireContext(), getString(R.string.warn_proxy_broken), Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_contactListFragment_to_settingsFragment)
                }
                ToxSaveStatus.SaveNotFound ->
                    findNavController().navigate(R.id.action_contactListFragment_to_profileFragment)
                ToxSaveStatus.Encrypted -> {
                    view?.visibility = View.INVISIBLE
                    if (passwordDialog != null) return
                    val passwordEdit = EditText(requireContext()).apply {
                        hint = getString(R.string.password)
                        inputType = EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                        setSingleLine()
                        transformationMethod = PasswordTransformationMethod()
                    }
                    val passwordLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        LinearLayout(requireContext()).apply {
                            val attr = context.obtainStyledAttributes(intArrayOf(android.R.attr.dialogPreferredPadding))
                            val padding = attr.getDimensionPixelSize(0, 0)
                            attr.recycle()
                            setPadding(padding, 0, padding, 0)
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                            )
                            addView(passwordEdit)
                        }
                    } else null
                    passwordDialog = AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.unlock_profile))
                        .setView(passwordLayout ?: passwordEdit)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            val password = passwordEdit.text.toString()
                            if (viewModel.tryLoadTox(password) == ToxSaveStatus.Ok) {
                                // Hack to reload fragment.
                                parentFragmentManager.beginTransaction().detach(this).commitAllowingStateLoss()
                                parentFragmentManager.beginTransaction().attach(this).commitAllowingStateLoss()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.incorrect_password),
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .setOnDismissListener {
                            passwordDialog = null
                            if (!viewModel.isToxRunning()) {
                                activity?.finishAffinity()
                            }
                        }
                        .show()
                }
                ToxSaveStatus.Ok -> {
                }
                else -> throw Exception("Unhandled tox save error $status")
            }
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

    private fun openProfile(contact: Contact) = findNavController().navigate(
        R.id.action_contactListFragment_to_contactProfileFragment,
        bundleOf(CONTACT_PUBLIC_KEY to contact.publicKey)
    )
}
