package com.example.englishflashcards.Dialogs

import android.content.Context
import android.view.DragEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Adapters.A_CollectionTarget
import com.example.englishflashcards.Classes.Collection
import com.example.englishflashcards.Objects.Lib
import com.example.englishflashcards.R

class D_AskForTargetCollection(context : Context, val event: (Collection?) -> Unit) : MyDialogBuilder(context, R.layout.d_ask_for_taget_collection) {

    override fun initView(builderView: View) {
        val dismiss = builderView.findViewById<ImageView>(R.id.d_dismiss)
        val collectionRecyclerView = builderView.findViewById<RecyclerView>(R.id.collectionRecyclerView)

        dialog.setOnShowListener {
            dismiss.setOnClickListener { dismiss() }
            initRecyclerView(collectionRecyclerView)
        }
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun initRecyclerView(r : RecyclerView){
        val adapter = A_CollectionTarget(context){
            //if no list to copy to or to move to just dismiss (collection = null)
            event(it)
            dismiss()
        }
        adapter.changeList()

        val layoutManager = LinearLayoutManager(context)
        r.adapter= adapter
        r.layoutManager = layoutManager
    }


}