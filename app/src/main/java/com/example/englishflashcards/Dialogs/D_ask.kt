package com.example.englishflashcards.Dialogs

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.example.englishflashcards.R

class D_ask(context : Context, private val theMessage : String, val event : (Boolean)->Unit) : MyDialogBuilder(context, R.layout.d_ask_layout) {

    override fun initView(builderView: View) {
        val dismiss = builderView.findViewById<ImageView>(R.id.d_dismiss)
        val message = builderView.findViewById<TextView>(R.id.message)
        val approve = builderView.findViewById<TextView>(R.id.approve)
        val deny = builderView.findViewById<TextView>(R.id.deny)

        dialog.setOnShowListener {
            message.text = theMessage
            dismiss.setOnClickListener {
                event(false)
                dismiss()
            }
            approve.setOnClickListener {
                event(true)
                dismiss()
            }
            deny.setOnClickListener {
                event(false)
                dismiss()
            }
        }
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

    }
}