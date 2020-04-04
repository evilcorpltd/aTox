package ltd.evilcorp.atox.ui.addcontact

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_add_contact.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.setUpFullScreenUi
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.domain.tox.ToxID
import ltd.evilcorp.domain.tox.ToxIdValidator

class AddContactFragment : Fragment() {
    private val viewModel: AddContactViewModel by viewModels { vmFactory }

    private var toxIdValid: Boolean = false
    private var messageValid: Boolean = true

    private fun isAddAllowed(): Boolean = toxIdValid && messageValid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!viewModel.isToxRunning() && !viewModel.tryLoadTox()) findNavController().navigateUp()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_contact, container, false).apply {
        setUpFullScreenUi { _, insets ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@setUpFullScreenUi insets
            toolbar.updatePadding(
                left = insets.systemWindowInsetLeft,
                top = insets.systemWindowInsetTop
            )
            content.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            insets
        }

        toolbar.setNavigationIcon(R.drawable.back)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        toxId.doAfterTextChanged { s ->
            toxId.error = when (ToxIdValidator.validate(ToxID(s?.toString() ?: ""))) {
                ToxIdValidator.Result.INCORRECT_LENGTH -> getString(
                    R.string.tox_id_error_length,
                    s?.toString()?.length ?: 0
                )
                ToxIdValidator.Result.INVALID_CHECKSUM -> getString(R.string.tox_id_error_checksum)
                ToxIdValidator.Result.NOT_HEX -> getString(R.string.tox_id_error_hex)
                ToxIdValidator.Result.NO_ERROR -> null
            }

            toxIdValid = toxId.error == null
            add.isEnabled = isAddAllowed()
        }

        message.doAfterTextChanged { s ->
            val content = s?.toString() ?: ""
            message.error = if (content.isNotEmpty())
                null
            else
                getString(R.string.add_contact_message_error_empty)

            messageValid = message.error == null
            add.isEnabled = isAddAllowed()
        }

        add.setOnClickListener {
            viewModel.addContact(ToxID(toxId.text.toString()), message.text.toString())
            findNavController().navigateUp()
        }

        if (requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            read_qr.setOnClickListener {
                IntentIntegrator.forSupportFragment(this@AddContactFragment).apply {
                    setOrientationLocked(false)
                    setBeepEnabled(false)
                }.initiateScan(listOf(IntentIntegrator.QR_CODE))
            }
        } else {
            read_qr.visibility = View.GONE
        }

        add.isEnabled = false

        toxId.setText(arguments?.getString("toxId"), TextView.BufferType.EDITABLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.contents?.let {
            view?.toxId?.setText(it.removePrefix("tox:"))
        } ?: super.onActivityResult(requestCode, resultCode, data)
}
