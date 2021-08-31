package com.example.englishflashcards.Dialogs

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.englishflashcards.Objects.Lib
import com.example.englishflashcards.R

class D_Insert_Word(context : Context, val event : (ArrayList<String>, Boolean) -> Unit) : MyDialogBuilder(context, R.layout.d_inset_a_word) {

    lateinit var inputName : EditText
    lateinit var definitionName : EditText
    lateinit var goToEditWord : CheckBox
    private val message1 = "Input Word Name is Empty"
    var maxChar = -1

    override fun initView(builderView: View) {
        val dismiss = builderView.findViewById<ImageView>(R.id.d_dismiss)
        val add = builderView.findViewById<ImageView>(R.id.d_add)
        inputName = builderView.findViewById(R.id.InputName)
        definitionName = builderView.findViewById(R.id.definitionName)
        goToEditWord = builderView.findViewById(R.id.goToEditWord)

        dialog.setOnShowListener {
            defineMaxChar(inputName)
            inputName.requestFocus()
            Lib.showKeyboardToDialog(dialog)

            dismiss.setOnClickListener { dismiss() }
            add.setOnClickListener { addItem() }
        }
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
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

    private fun addItem(){
        val text = inputName.text

        if(text.trim().isNotEmpty()){
            if(text.count() <= maxChar && maxChar != -1){
                event(arrayListOf(text.toString(),
                    definitionName.text.toString()
                ),
                    goToEditWord.isChecked
                )
            }else if(maxChar != -1){
                Lib.showMessage(context, "Max Char is $maxChar")
            }
        }else{
            Lib.showMessage(context, message1)
        }
    }
}