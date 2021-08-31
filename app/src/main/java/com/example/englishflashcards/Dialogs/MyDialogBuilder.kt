package com.example.englishflashcards.Dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.R

abstract class MyDialogBuilder(val context : Context, val layout : Int){

    lateinit var dialog : Dialog
    lateinit var builderView : View

    fun build(){
        dialog = Dialog(context)
        builderView = LayoutInflater.from(context).inflate(layout, null, false)
        dialog.setContentView(builderView)


        initView(builderView)
    }

    fun buildWithStyle(style : Int){
        dialog = Dialog(context, style)
        builderView = LayoutInflater.from(context).inflate(layout, null, false)
        dialog.setContentView(builderView)


        initView(builderView)
    }

    fun display(){
        dialog.show()
    }

    fun dismiss(){
        dialog.dismiss()
    }

    //-------------------------------------------------------
    //set Up View

    abstract fun initView(builderView : View)
}