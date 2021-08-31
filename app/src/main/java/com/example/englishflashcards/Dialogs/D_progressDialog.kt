package com.example.englishflashcards.Dialogs

import android.content.Context
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import com.example.englishflashcards.R

class D_progressDialog(context : Context, layout : Int) : MyDialogBuilder(context, layout) {

    lateinit var progressBar: ProgressBar
    lateinit var progressText : TextView

    override fun initView(builderView: View) {
        progressBar = builderView.findViewById(R.id.progressBar)
        progressText = builderView.findViewById(R.id.progressText)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        
        dialog.setOnShowListener {
            progressBar.max = 100
        }
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    fun makeProgress(pro : Float){
        progressBar.progress = pro.toInt()
    }

    fun changeStep(step : String){
        val mainHandler =  Handler(context.mainLooper)
        val myRunnable =  Runnable {
            progressText.text = step
        }
        mainHandler.post(myRunnable)
    }


}