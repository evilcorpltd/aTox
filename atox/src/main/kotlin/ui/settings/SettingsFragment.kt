package ltd.evilcorp.atox.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.settings_fragment.*
import kotlinx.android.synthetic.main.settings_fragment.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.vmFactory

class SettingsFragment : Fragment() {
    private val vm: SettingsViewModel by viewModels { vmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.settings_fragment, container, false).apply {
        toolbar.apply {
            setNavigationIcon(R.drawable.back)
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }

        vm.user.observe(viewLifecycleOwner, Observer { user ->
            name.setText(user.name)
            statusMessage.setText(user.statusMessage)
        })
    }

    override fun onPause() {
        super.onPause()
        vm.setName(name.text.toString())
        vm.setStatusMessage(statusMessage.text.toString())
    }
}
