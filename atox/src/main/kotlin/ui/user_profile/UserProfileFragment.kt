// SPDX-FileCopyrightText: 2020-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.user_profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.view.*
import androidx.fragment.app.viewModels
import io.nayuki.qrcodegen.QrCode
import kotlin.math.min
import kotlin.math.roundToInt
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentUserProfileBinding
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.UserStatus
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ltd.evilcorp.atox.BuildConfig
import ltd.evilcorp.atox.ui.*
import ltd.evilcorp.atox.ui.AvatarMaker
import ltd.evilcorp.atox.ui.colorFromStatus
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


private const val TOX_MAX_NAME_LENGTH = 128
private const val TOX_MAX_STATUS_MESSAGE_LENGTH = 1007

private const val QR_CODE_TO_SCREEN_RATIO = 0.5f
private const val QR_CODE_PADDING = 16f // in dp
private const val QR_CODE_SHARED_IMAGE_PADDING = 30f // in dp

private fun dpToPx(dp: Float, res: Resources): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.displayMetrics).toInt()

class UserProfileFragment : BaseFragment<FragmentUserProfileBinding>(FragmentUserProfileBinding::inflate) {
    private val vm: UserProfileViewModel by viewModels { vmFactory }
    private lateinit var currentStatus: UserStatus


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            toolbar.updatePadding(left = insets.left, top = insets.top)
            mainSection.updatePadding(left = insets.left, right = insets.right)
            compat
        }

        vm.user.observe(viewLifecycleOwner) { user ->
            currentStatus = user.status

            userName.text = user.name
            userStatusMessage.text = user.statusMessage
            profileImageLayout.statusIndicator.setColorFilter(colorFromStatus(resources, user.status))
            AvatarMaker(user).setAvatar(profileImageLayout.profileImage)
        }

        // Inflating views according to Day/Night theme
        if (isNightMode(requireContext())) {
            toolbar.setNavigationIcon(R.drawable.ic_back_white)
            icEditProfile.setImageResource(R.drawable.ic_edit_white)
            copyToxId.setImageResource(R.drawable.ic_copy_white)
            createQrCode(
                ResourcesCompat.getColor(resources, R.color.pleasantWhite, null),
                Color.TRANSPARENT,
                imageView = toxIdQr
            )
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_back_black)
            icEditProfile.setImageResource(R.drawable.ic_edit_black)
            copyToxId.setImageResource(R.drawable.ic_copy_black)
            createQrCode(Color.BLACK, Color.TRANSPARENT, imageView = toxIdQr)
        }
        setImageButtonRippleDayNight(requireContext(), copyToxId)

        toolbar.setNavigationOnClickListener {
            WindowInsetsControllerCompat(requireActivity().window, view)
                .hide(WindowInsetsCompat.Type.ime())
            activity?.onBackPressed()
        }

        userToxId.text = vm.toxId.string()
        userToxId.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, vm.toxId.string())
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.tox_id_share)))
        }

        copyToxId.setOnClickListener {
            val clipboard = requireActivity().getSystemService<ClipboardManager>()!!
            clipboard.setPrimaryClip(ClipData.newPlainText(getText(R.string.tox_id), vm.toxId.string()))
            Toast.makeText(requireContext(), getText(R.string.copied), Toast.LENGTH_SHORT).show()
        }

        toxIdQr.setOnClickListener {
            vm.viewModelScope.launch {
                val qrImageUri = getQrForSharing("tox_id_qr_code")
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

        /*profileOptions.profileChangeNickname.setOnClickListener {
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
                .setNegativeButton(R.string.cancel) { _, _ -> }
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
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
        }

        profileOptions.profileChangeStatus.setOnClickListener {
            StatusDialog(requireContext(), currentStatus) { status -> vm.setStatus(status) }.show()
        }*/
    }


    override fun onDestroyView() = binding.run {
        super.onDestroyView()
        unbindDrawables(icEditProfile)
        unbindDrawables(copyToxId)
    }


    /**
     * Function will create a QR code for the Tox ID and assign it to the image view if specified.
     * @param qrCodeColor The color of the QR code.
     * @param backgroundColor The color of the background.
     * @param paddingDp The padding to add after each side in the QR code bitmap. Will be painted with backgroundColor.
     * @param imageView The image view for whom to assign the QR code bitmap. Can be null.
     * @return The QR code bitmap with the specified padding.
     */
    private fun createQrCode(
        qrCodeColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
        paddingDp: Float = QR_CODE_PADDING,
        imageView: ImageView? = null,
    ) : Bitmap {
        // Creating the QR bitmap
        val qrData = QrCode.encodeText("tox:%s".format(vm.toxId.string()), QrCode.Ecc.LOW)
        var bmpQr: Bitmap = Bitmap.createBitmap(qrData.size, qrData.size, Bitmap.Config.ARGB_8888)
        bmpQr.setHasAlpha(true)
        for (x in 0 until qrData.size) {
            for (y in 0 until qrData.size) {
                bmpQr.setPixel(x, y, if (qrData.getModule(x, y)) qrCodeColor else backgroundColor)
            }
        }
        // Scaling the QR bitmap to be half of the screen's width
        val metrics = resources.displayMetrics
        val size = (min(metrics.widthPixels, metrics.heightPixels) * QR_CODE_TO_SCREEN_RATIO).roundToInt()
        bmpQr = bmpQr.scale(size, size, false)

        // Adding a padding to the QR bitmap
        val paddingPx = dpToPx(paddingDp, resources)
        val bmpQrWithPadding =
            Bitmap.createBitmap(bmpQr.width + 2 * paddingPx, bmpQr.height + 2 * paddingPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmpQrWithPadding)
        canvas.drawARGB(backgroundColor.alpha, backgroundColor.red, backgroundColor.green, backgroundColor.blue)
        canvas.drawBitmap(bmpQr, paddingPx.toFloat(), paddingPx.toFloat(), null)

        imageView?.apply {
            setPadding(paddingPx)
            setImageBitmap(bmpQr)
        }

        return bmpQrWithPadding
    }


    /**
     * Function will save the image with the input bitmap and name to the cache directory as a png format.
     * Then it will return the Uri of the image.
     * @param image The bitmap of the image.
     * @param name Image file name.
     * @return The Uri of the image or null.
     */
    private fun saveImageForSharing(image: Bitmap, name: String): Uri? {
        val imagesFolder = File(requireActivity().cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "$name.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(requireActivity(), "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        } catch (e: IOException) {
            Log.d("corp.atox.debug", "IOException while trying to write file for sharing: " + e.message)
        }
        return uri
    }


    /**
     * Function will run in a different thread, create a new QR code for sharing and return the Uri.
     * @param name The image name.
     * @return The Uri for the QR code.
     */
    private suspend fun getQrForSharing(name: String) : Uri? {
        return withContext(Dispatchers.IO) {
            val bmp = createQrCode(paddingDp = QR_CODE_SHARED_IMAGE_PADDING)
            saveImageForSharing(bmp, name)
        }
    }
}
