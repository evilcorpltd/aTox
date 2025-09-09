// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
// SPDX-FileCopyrightText: 2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.createprofile

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.runBlocking
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentProfileBinding
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.domain.tox.ToxSaveStatus

class CreateProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {
    private val viewModel: CreateProfileViewModel by viewModels { vmFactory }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult

        Log.i("ProfileFragment", "Importing file $uri")
        viewModel.tryImportToxSave(uri)?.also { save ->
            when (val startStatus = viewModel.startTox(save)) {
                ToxSaveStatus.Ok -> {
                    viewModel.verifyUserExists(viewModel.publicKey)
                    findNavController().popBackStack()
                }
                ToxSaveStatus.Encrypted -> {
                    val passwordEdit = EditText(requireContext()).apply {
                        hint = getString(R.string.password)
                        inputType = EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                        setSingleLine()
                        transformationMethod = PasswordTransformationMethod()
                    }
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.unlock_profile)
                        .setView(passwordEdit)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            val password = passwordEdit.text.toString()
                            if (viewModel.startTox(save, password) == ToxSaveStatus.Ok) {
                                viewModel.verifyUserExists(viewModel.publicKey)
                                findNavController().popBackStack()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.incorrect_password),
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
                else -> Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.import_tox_save_failed, startStatus.name),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            toolbar.updatePadding(left = insets.left, top = insets.top, right = insets.right)
            content.updatePadding(left = insets.left, right = insets.right)
            compat
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activity?.finish()
        }

        btnCreate.setOnClickListener {
            btnCreate.isEnabled = false

            viewModel.startTox()
            val user = User(
                publicKey = viewModel.publicKey.string(),
                name = if (username.text.isNotEmpty()) username.text.toString() else getString(R.string.name_default),
                statusMessage = getString(R.string.status_message_default),
            )

            runBlocking {
                viewModel.create(user).join()
            }

            findNavController().popBackStack()
        }

        btnImport.setOnClickListener {
            importLauncher.launch(arrayOf("*/*"))
        }
    }
}
