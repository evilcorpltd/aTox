// SPDX-FileCopyrightText: 2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.edit_text_value_dialog

import android.os.*
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ltd.evilcorp.atox.databinding.EditTextValueDialogBinding
import ltd.evilcorp.atox.vmFactory


class EditTextValueDialog() : BottomSheetDialogFragment() {

    private val vm: EditTextValueDialogViewModel by viewModels { vmFactory }
    private var _binding: EditTextValueDialogBinding? = null
    private val binding get() = _binding!!

    private var title: String? = null
    private var hint: String? = null
    private var defaultValue: String? = null
    private var singleLine: Boolean = true
    private var filters: Array<InputFilter>? = null
    private lateinit var setTextValueBlock: (String) -> Unit

    constructor(
        title: String,
        hint: String,
        defaultValue: String? = null,
        singleLine: Boolean = true,
        filters: Array<InputFilter>? = null,
        setTextValueBlock: (String) -> Unit
    ) : this() {
        this.title = title
        this.hint = hint
        this.defaultValue = defaultValue
        this.singleLine = singleLine
        this.filters = filters
        this.setTextValueBlock = setTextValueBlock
    }

    companion object {
        const val TAG = "EditTextValueDialog"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = EditTextValueDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        super.onViewCreated(view, savedInstanceState)

        val editText: EditText? = textField.editText

        // Assigning values from constructor to view model
        title?.run {
            vm.title = this
            hint?.run { vm.hint = this }
            defaultValue?.run {
                vm.defaultValue = this
                vm.selectionStart = length
                vm.selectionEnd = length
            }
            vm.singleLine = singleLine
            filters?.run { vm.filters = this }
            setTextValueBlock.run { vm.setTextValueBlock = this }
        }

        // Assigning values to the views according to the given parameters
        titleTextView.text = vm.title
        textField.hint = vm.hint
        vm.defaultValue?.run {
            editText?.setText(this)
        }
        editText?.isSingleLine = vm.singleLine
        vm.filters?.run {
            editText?.filters = this

            // Displaying the max length as a counter
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    (this.first { it is InputFilter.LengthFilter } as InputFilter.LengthFilter).run {
                        textField.isCounterEnabled = true
                        textField.counterMaxLength = max
                    }
                } catch (e: NoSuchElementException) {
                }
            }
        }
        vm.selectionStart?.let { selectionStart ->
            vm.selectionEnd?.let { selectionEnd ->
                editText?.setSelection(selectionStart, selectionEnd)
            }
        }

        cancel.setOnClickListener { dismiss() }
        save.setOnClickListener {
            vm.setTextValueBlock(textField.editText?.text.toString())
            dismiss()
        }
    }

    override fun onResume() = binding.run {
        textField.editText?.requestFocus()
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) = binding.run {
        val editText: EditText? = textField.editText

        vm.defaultValue = editText?.text.toString()
        vm.selectionStart = editText?.selectionStart
        vm.selectionEnd = editText?.selectionEnd
        super.onSaveInstanceState(outState)
    }
}
