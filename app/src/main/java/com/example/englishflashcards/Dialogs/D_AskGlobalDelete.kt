package com.example.englishflashcards.Dialogs

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import com.example.englishflashcards.Objects.DataBaseServices
import com.example.englishflashcards.Objects.Settings
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.AskDeleteByHide
import com.example.englishflashcards.Utilities.AskDeleteGlobally
import com.example.englishflashcards.Utilities.AskDeleteLocally
import com.example.englishflashcards.Utilities.deleteOption

class D_AskGlobalDelete(context : Context, val event : (Int) -> Unit) : MyDialogBuilder(context, R.layout.d_ask_global_delete){

    override fun initView(builderView: View) {
        val approve : ImageView = builderView.findViewById(R.id.d_add)
        val dismiss : ImageView = builderView.findViewById(R.id.d_dismiss)
        val text : TextView = builderView.findViewById(R.id.infoText)
        val deleteGlobally : RadioButton = builderView.findViewById(R.id.deleteGlobally)
        val deleteLocally : RadioButton = builderView.findViewById(R.id.deleteLocally)
        val deleteByHide : RadioButton = builderView.findViewById(R.id.deleteByHide)
        var selected = Settings.deleteOption

        dialog.setOnShowListener {
            when(selected){
                AskDeleteGlobally->deleteGlobally.isChecked = true
                AskDeleteLocally->deleteLocally.isChecked = true
                AskDeleteByHide->deleteByHide.isChecked = true
            }


            approve.setOnClickListener {
                DataBaseServices.updateVar(deleteOption, selected.toString())
                Settings.deleteOption = selected
                event(selected)
                dismiss()
            }

            dismiss.setOnClickListener {
                dismiss()
            }

            deleteGlobally.setOnClickListener {
                text.text = context.getString(R.string.ask_delete_op_1_info)
                selected = AskDeleteGlobally
            }

            deleteLocally.setOnClickListener {
                text.text = context.getString(R.string.ask_delete_op_2_info)
                selected = AskDeleteLocally
            }

            deleteByHide.setOnClickListener {
                text.text = context.getString(R.string.ask_delete_op_3_info)
                selected = AskDeleteByHide
            }
        }
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

    }
}