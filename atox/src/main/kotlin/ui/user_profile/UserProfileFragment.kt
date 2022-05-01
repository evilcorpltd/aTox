// SPDX-FileCopyrightText: 2020-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.user_profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.graphics.scale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import androidx.lifecycle.viewModelScope
import io.nayuki.qrcodegen.QrCode
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ltd.evilcorp.atox.BuildConfig
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentUserProfileBinding
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.Dp
import ltd.evilcorp.atox.ui.Px
import ltd.evilcorp.atox.ui.StatusDialog
import ltd.evilcorp.atox.ui.colorFromStatus
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.tox.ToxID
import org.kodein.di.android.x.viewmodel.viewModel

private const val TOX_MAX_NAME_LENGTH = 128
private const val TOX_MAX_STATUS_MESSAGE_LENGTH = 1007

private const val QR_CODE_TO_SCREEN_RATIO = 0.5f
private val qrCodePadding = Dp(16f)
private val qrCodeSharedImageSize = Px(1024)
private val qrCodeSharedImagePadding = Px(200)

class UserProfileFragment : BaseFragment<FragmentUserProfileBinding>(FragmentUserProfileBinding::inflate) {
    private val vm: UserProfileViewModel by viewModel()
    private lateinit var currentStatus: UserStatus

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            profileCollapsingToolbar.updatePadding(left = insets.left, right = insets.right)
            profileToolbar.updatePadding(top = insets.top)
            mainSection.updatePadding(left = insets.left, right = insets.right)
            compat
        }

        profileToolbar.apply {
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }

        vm.user.observe(viewLifecycleOwner) { user ->
            currentStatus = user.status

            userName.text = user.name
            userStatusMessage.text = user.statusMessage
            userStatus.setColorFilter(colorFromStatus(requireContext(), user.status))
        }

        userToxId.text = vm.toxId.string()

        // TODO(robinlinden): This should open a nice dialog where you show the QR and have both share and copy buttons.
        profileShareId.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, vm.toxId.string())
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.tox_id_share)))
        }
        registerForContextMenu(profileShareId)

        profileOptions.profileChangeNickname.setOnClickListener {
            val nameEdit = EditText(requireContext()).apply {
                text.append(binding.userName.text)
                filters = arrayOf(InputFilter.LengthFilter(TOX_MAX_NAME_LENGTH))
                setSingleLine()
            }
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.name)
                .setView(nameEdit)
                .setPositiveButton(R.string.update) { _, _ ->
                    vm.setName(nameEdit.text.toString())
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        profileOptions.profileChangeStatusText.setOnClickListener {
            val statusMessageEdit =
                EditText(requireContext()).apply {
                    text.append(binding.userStatusMessage.text)
                    filters = arrayOf(InputFilter.LengthFilter(TOX_MAX_STATUS_MESSAGE_LENGTH))
                }
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.status_message)
                .setView(statusMessageEdit)
                .setPositiveButton(R.string.update) { _, _ ->
                    vm.setStatusMessage(statusMessageEdit.text.toString())
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        profileOptions.profileChangeStatus.setOnClickListener {
            StatusDialog(requireContext(), currentStatus) { status -> vm.setStatus(status) }.show()
        }

        // TODO(robinlinden): Remove hack. It's used to make sure we can scroll to the settings
        //  further down when in landscape orientation. This is only needed if the view is recreated
        //  while we're on this screen as Android changes the size of the contents of the NestedScrollView
        //  when that happens.
        if (savedInstanceState != null) {
            needsHacks.updatePadding(bottom = (150 * resources.displayMetrics.density).toInt())
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) = binding.run {
        super.onCreateContextMenu(menu, v, menuInfo)
        when (v.id) {
            R.id.profile_share_id -> requireActivity().menuInflater.inflate(
                R.menu.user_profile_share_id_context_menu,
                menu
            )
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = binding.run {
        return when (item.itemId) {
            R.id.copy -> {
                val clipboard = requireActivity().getSystemService<ClipboardManager>()!!
                clipboard.setPrimaryClip(ClipData.newPlainText(getText(R.string.tox_id), vm.toxId.string()))
                Toast.makeText(requireContext(), getText(R.string.copied), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.qr -> {
                createQrCodeDialog().show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun createQrCodeDialog(): AlertDialog {
        val qrSize =
            min(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels) * QR_CODE_TO_SCREEN_RATIO
        val bmp = asQr(vm.toxId, Px(qrSize.toInt()), qrCodePadding.asPx(resources))
        val qrCode = ImageView(requireContext()).apply {
            setPadding(qrCodePadding.asPx(resources).px)
            setImageBitmap(bmp)
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.tox_id)
            .setView(qrCode)
            .setPositiveButton(getString(R.string.share)) { _, _ ->
                vm.viewModelScope.launch {
                    val qrImageUri = getQrForSharing()
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        clipData = ClipData.newRawUri(null, qrImageUri)
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, qrImageUri)
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.tox_id_share)))
                }
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .create()
    }

    private fun saveQrForSharing(qrBmp: Bitmap): Uri {
        val imagesFolder = File(requireContext().cacheDir, "shared_images").apply { mkdirs() }
        val file = File(imagesFolder, "tox_id_qr_code.png")
        FileOutputStream(file).use { stream ->
            qrBmp.compress(Bitmap.CompressFormat.PNG, 90, stream)
        }

        return FileProvider.getUriForFile(requireContext(), "${BuildConfig.APPLICATION_ID}.fileprovider", file)
    }

    private fun asQr(id: ToxID, qrSize: Px, padding: Px): Bitmap {
        val qrData = QrCode.encodeText("tox:%s".format(id.string()), QrCode.Ecc.LOW)
        var bmpQr: Bitmap = Bitmap.createBitmap(qrData.size, qrData.size, Bitmap.Config.RGB_565)
        for (x in 0 until qrData.size) {
            for (y in 0 until qrData.size) {
                bmpQr.setPixel(x, y, if (qrData.getModule(x, y)) Color.BLACK else Color.WHITE)
            }
        }

        bmpQr = bmpQr.scale(qrSize.px, qrSize.px, false)

        val bmpQrWithPadding =
            Bitmap.createBitmap(
                bmpQr.width + 2 * padding.px,
                bmpQr.height + 2 * padding.px,
                Bitmap.Config.RGB_565
            )
        val canvas = Canvas(bmpQrWithPadding)
        canvas.drawPaint(
            Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
        )
        canvas.drawBitmap(bmpQr, padding.px.toFloat(), padding.px.toFloat(), null)

        return bmpQrWithPadding
    }

    private suspend fun getQrForSharing(): Uri =
        withContext(Dispatchers.IO) {
            val bmp = asQr(vm.toxId, qrCodeSharedImageSize, qrCodeSharedImagePadding)
            saveQrForSharing(bmp)
        }
}
