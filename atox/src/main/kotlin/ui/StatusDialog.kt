package ltd.evilcorp.atox.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.Window
import javax.inject.Inject
import kotlinx.android.synthetic.main.dialog_status.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.feature.UserManager

private const val TRANSITION_TIME = 250

class StatusDialog(
    ctx: Context,
    private var activeStatus: UserStatus,
    private val setStatusFunc: (UserStatus) -> Unit
) : Dialog(ctx, R.style.DialogSlideAnimation) {
    @Inject
    lateinit var userManager: UserManager

    private fun viewByStatus(status: UserStatus): TransitionDrawable = when (status) {
        UserStatus.None -> status_available.background as TransitionDrawable
        UserStatus.Away -> status_away.background as TransitionDrawable
        UserStatus.Busy -> status_busy.background as TransitionDrawable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_status)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        selectStatus(activeStatus)
        status_available.setOnClickListener { selectStatus(UserStatus.None) }
        status_away.setOnClickListener { selectStatus(UserStatus.Away) }
        status_busy.setOnClickListener { selectStatus(UserStatus.Busy) }

        cancel.setOnClickListener { dismiss() }
        confirm.setOnClickListener {
            setStatusFunc(activeStatus)
            dismiss()
        }
    }

    private fun selectStatus(status: UserStatus) {
        viewByStatus(activeStatus).reverseTransition(TRANSITION_TIME)
        activeStatus = status
        viewByStatus(activeStatus).startTransition(TRANSITION_TIME)
    }
}
