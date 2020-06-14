package ltd.evilcorp.atox.ui.create_profile

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_profile.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.setUpFullScreenUi
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.User

private const val IMPORT = 42

class CreateProfileFragment : Fragment() {
    private val viewModel: CreateProfileViewModel by viewModels { vmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false).apply {
        setUpFullScreenUi { _: View, insets: WindowInsets ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@setUpFullScreenUi insets
            toolbar.updatePadding(
                left = insets.systemWindowInsetLeft,
                top = insets.systemWindowInsetTop,
                right = insets.systemWindowInsetRight
            )
            content.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )

            insets
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activity?.finish()
        }

        btnCreate.setOnClickListener {
            btnCreate.isEnabled = false

            viewModel.startTox()
            val user = User(
                publicKey = viewModel.publicKey.string(),
                name = if (username.text.isNotEmpty()) username.text.toString() else "aTox user",
                password = if (password.text.isNotEmpty()) password.text.toString() else ""
            )
            viewModel.create(user)

            findNavController().popBackStack()
        }

        btnImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }

            startActivityForResult(intent, IMPORT)
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
                    Toast.makeText(
                        requireContext(),
                        R.string.import_tox_save_failed,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
