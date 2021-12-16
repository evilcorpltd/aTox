package ltd.evilcorp.atox.ui.edit_text_value_dialog

import android.text.InputFilter
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class EditTextValueDialogViewModel @Inject constructor() : ViewModel() {
    var title: String? = null
    var hint: String? = null
    var defaultValue: String? = null
    var singleLine: Boolean = true
    var filters: Array<InputFilter>? = null
    lateinit var setTextValueBlock: (String) -> Unit

    var selectionStart: Int? = null
    var selectionEnd: Int? = null
}
