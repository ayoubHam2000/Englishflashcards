package com.example.englishflashcards.Dialogs

import android.content.Context
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.example.englishflashcards.R


class D_ask_fast(context: Context, val event: () -> Unit) : MyDialogBuilder(context, R.layout.d_ask_fast
) {

    var text = ""
    var time = 2000L

    override fun initView(builderView: View) {
        val approve = builderView.findViewById<TextView>(R.id.approve)
        val message = builderView.findViewById<TextView>(R.id.message)

        message.text = text
        dialog.setOnShowListener {
            approve.setOnClickListener {
                event()
                dismiss()
            }
        }

        val dialogWindow = dialog.window

        dialogWindow?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            ,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )
        dialogWindow?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                    or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        dialogWindow?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        builderView.invalidate()
        dialogWindow?.attributes?.gravity = Gravity.TOP
        dismissAfter()
    }

    private fun dismissAfter(){
        Handler(context.mainLooper).postDelayed(Runnable{
            dismiss()
        }, time)
    }



}