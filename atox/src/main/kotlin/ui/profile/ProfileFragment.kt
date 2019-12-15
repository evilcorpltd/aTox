package ltd.evilcorp.atox.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.profile_fragment.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.vmFactory

private const val IMPORT = 42

class ProfileFragment : Fragment() {
    private val viewModel: ProfileViewModel by viewModels { vmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.profile_fragment, container, false).apply {
        btnCreate.setOnClickListener {
            btnCreate.isEnabled = false

            viewModel.startTox()
            viewModel.createUser(
                viewModel.publicKey,
                if (username.text.isNotEmpty()) username.text.toString() else "aTox user",
                if (password.text.isNotEmpty()) password.text.toString() else ""
            )

            findNavController().popBackStack()
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
                if (viewModel.startTox(save)) {
                    viewModel.verifyUserExists(viewModel.publicKey)
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), R.string.import_tox_save_failed, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
