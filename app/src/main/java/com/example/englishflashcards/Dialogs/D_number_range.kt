package com.example.englishflashcards.Dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.UserDictionary
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.englishflashcards.Objects.Lib
import com.example.englishflashcards.R
import java.util.*
import kotlin.collections.ArrayList


class D_number_range(
    context: Context,
    val event: (ArrayList<Int>) -> Unit
) : MyDialogBuilder(context, R.layout.d_pdf_pages_limit) {

    var minRange = 0
    var maxRange = 100
    var text = ""
    private val message2 = "something wrong"

    @SuppressLint("SetTextI18n")
    override fun initView(builderView: View) {
        val accept = builderView.findViewById<ImageView>(R.id.d_add)
        val dismissBtn = builderView.findViewById<ImageView>(R.id.d_dismiss)
        val pagesInput = builderView.findViewById<EditText>(R.id.pages)
        val pdfMaxPageInfo = builderView.findViewById<TextView>(R.id.pdfMaxPageInfo)
        val comaChar : LinearLayout = builderView.findViewById(R.id.commaChar)
        val tirChar : LinearLayout = builderView.findViewById(R.id.minusChar)


        dialog.setOnShowListener {

            pdfMaxPageInfo.text = text
            Lib.showKeyboardToDialog(dialog)
            pagesInput.requestFocus()


            accept.setOnClickListener {
                val pages = formatInput(pagesInput.text.toString()).split(",")
                val result = ArrayList<Int>()
                for(item in pages){
                    val p = item.split("-")
                    if(p.count() == 2){
                        val p1 = p[0].toIntOrNull()
                        val p2 = p[1].toIntOrNull()
                        if(p1 != null && p2 != null){
                            if(p1 < p2){
                                addFrom(result, p1, p2)
                            }else{
                                addFrom(result, p2, p1)
                            }
                        }
                    }else if(p.count() == 1){
                        val p1 = p[0].toIntOrNull()
                        if(p1 != null){
                            addFrom(result, p1, p1)
                        }
                    }
                }
                val finalResult = removeDuplicate(result)
                if(finalResult.count() != 0){
                    event(finalResult)
                    dismiss()
                }else{
                    Lib.showMessage(context, message2)
                }
            }

            //region dismissBtn comaChar tirChar
            dismissBtn.setOnClickListener {
                dismiss()
            }

            comaChar.setOnClickListener {
                pagesInput.setText(pagesInput.text.toString() + ",")
                pagesInput.setSelection(pagesInput.text.count())
            }

            tirChar.setOnClickListener {
                pagesInput.setText(pagesInput.text.toString() + "-")
                pagesInput.setSelection(pagesInput.text.count())
            }

            //endregion

        }

        dialog.setCanceledOnTouchOutside(false)

        val window = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun addFrom(list: ArrayList<Int>, a: Int, b: Int){
        var i = a
        while(i <= b && i <= maxRange){
            if(i >= minRange){
                list.add(i)
            }
            i++
        }
    }

    private fun removeDuplicate(list: ArrayList<Int>) : ArrayList<Int>{
        val b = ArrayList<Int>()
        for(item in list){
            if(item !in b){
                b.add(item)
            }
        }
        return b
    }

    private fun formatInput(input: String) : String{
        return input.replace(" ", "")
            .replace("-+".toRegex(), "-")
            .replace(",+".toRegex(), ",")
    }

}