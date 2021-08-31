package com.example.englishflashcards.Classes.Costum

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText

class MyEditText : TextInputEditText {
    //To clear focus when keyboard gone
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    override fun onKeyPreIme(key_code: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) clearFocus()
        return super.onKeyPreIme(key_code, event)
    }
}