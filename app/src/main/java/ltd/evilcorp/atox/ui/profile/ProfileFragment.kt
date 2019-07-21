package ltd.evilcorp.atox.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.profile_fragment.view.*
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.di.ViewModelFactory
import ltd.evilcorp.atox.ui.contactlist.USER_PUBLIC_KEY
import javax.inject.Inject

private const val IMPORT = 42

class ProfileFragment : Fragment() {
    @Inject
    lateinit var vmFactory: ViewModelFactory
    private val viewModel: ProfileViewModel by lazy {
        ViewModelProviders.of(this, vmFactory).get(ProfileViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.profile_fragment, container, false).apply {
        btnCreate.setOnClickListener {
            btnCreate.isEnabled = false

            viewModel.startToxThread()
            viewModel.createUser(
                App.toxThread.publicKey,
                if (username.text.isNotEmpty()) username.text.toString() else "aTox user",
                if (password.text.isNotEmpty()) password.text.toString() else ""
            )

            navigateToContactList()
        }

        toolbar.inflateMenu(R.menu.profile_options_menu)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.import_tox_save -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }

                    startActivityForResult(intent, IMPORT)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode != IMPORT || resultCode != Activity.RESULT_OK) {
            return
        }

        resultData?.data?.let { uri ->
            Log.e("ProfileFragment", "Importing file $uri")
            viewModel.tryImportToxSave(uri)?.also { save ->
                if (viewModel.startToxThread(save)) {
                    viewModel.verifyUserExists(App.toxThread.publicKey)
                    navigateToContactList()
                } else {
                    Toast.makeText(requireContext(), R.string.import_tox_save_failed, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.tryLoadToxSave()?.also { save ->
            viewModel.startToxThread(save)
            viewModel.verifyUserExists(App.toxThread.publicKey)
            navigateToContactList()
        }
    }

    private fun navigateToContactList() = findNavController().navigate(
        R.id.action_profileFragment_to_contactListFragment,
        Bundle().apply { putString(USER_PUBLIC_KEY, App.toxThread.publicKey) }
    )
}
