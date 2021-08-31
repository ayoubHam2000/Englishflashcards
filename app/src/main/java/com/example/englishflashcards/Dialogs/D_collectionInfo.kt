package com.example.englishflashcards.Dialogs

import android.content.Context
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.example.englishflashcards.Classes.Collection
import com.example.englishflashcards.Objects.WordsManagement
import com.example.englishflashcards.R

class D_collectionInfo(context : Context, layout : Int) : MyDialogBuilder(context, layout) {

    lateinit var collection : Collection

    override fun initView(builderView: View) {
        val collectionInfo = builderView.findViewById<TextView>(R.id.collectionInfo)
        val collectionInfoNbr = builderView.findViewById<TextView>(R.id.collectionInfoNbr)
        val dismiss = builderView.findViewById<ImageView>(R.id.d_dismiss)

        dialog.setOnShowListener {
            dismiss.setOnClickListener { dismiss() }
            /*if(collection.words.count() != 0) {
                collectionInfo.text = WordsManagement.getCollectionInfoDetail(collection)[0]
                collectionInfoNbr.text = WordsManagement.getCollectionInfoDetail(collection)[1]
            }*/
        }
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }
}