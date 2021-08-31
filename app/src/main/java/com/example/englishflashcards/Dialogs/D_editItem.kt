package com.example.englishflashcards.Dialogs

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.example.englishflashcards.Objects.Lib
import com.example.englishflashcards.R
import com.google.android.material.internal.ContextUtils.getActivity

class D_editItem(context: Context, val event: (String) -> Unit) : MyDialogBuilder(
    context,
    R.layout.d_add_item
) {

    var textInput = ""
    var maxChar = -1
    private val message1 = "input is empty"

    override fun initView(builderView: View) {
        val dismiss = builderView.findViewById<ImageView>(R.id.d_dismiss)
        val add = builderView.findViewById<ImageView>(R.id.d_add)
        val inputName = builderView.findViewById<EditText>(R.id.InputName)

        dialog.setOnShowListener {
            defineMaxChar(inputName)
            if(textInput.isNotEmpty()){ inputName.setText(textInput) }
            inputName.requestFocus()
            Lib.showKeyboardToDialog(dialog)

            inputName.setSelection(inputName.text.count())
            dismiss.setOnClickListener { dismiss() }
            add.setOnClickListener { addItem(inputName) }

            val test = D_ask_fast(context){}
            test.time = 100
            test.buildWithStyle(R.style.Bottom_Dialog)
            test.display()
        }
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setOnCancelListener {
            maxChar = -1
            inputName.setText("")
        }

    }

    private fun defineMaxChar(editText : EditText){
        if(maxChar != -1){
            editText.addTextChangedListener (
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable) {
                        if(s.count() > maxChar){
                            editText.error = "Max character is $maxChar"
                        }
                    }
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                }
            )
       }
    }

    private fun addItem(input: EditText){
        val text = input.text
        if(text.trim().isNotEmpty()){
            event(text.toString())
        }else{
            input.error = message1
        }
    }

}