package com.example.englishflashcards.Dialogs

import android.content.Context
import android.os.Handler
import android.text.Html
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import com.example.englishflashcards.Classes.Word
import com.example.englishflashcards.Objects.DataBaseServices
import com.example.englishflashcards.Objects.Lib
import com.example.englishflashcards.Objects.SetManagement
import com.example.englishflashcards.R
import kotlin.concurrent.thread
import kotlin.math.min

class D_WordInfo(context : Context, layout : Int, val word : Word, val text : String) : MyDialogBuilder(context, layout) {

    var exampleLimit = 3

    override fun initView(builderView: View) {

        val wordNameTitle = builderView.findViewById<TextView>(R.id.wordNameTitle)
        val yourDefinitions = builderView.findViewById<TextView>(R.id.yourDefinitions)
        val yourExamples = builderView.findViewById<TextView>(R.id.yourExamples)
        val definitions = builderView.findViewById<TextView>(R.id.definitions)
        val origin = builderView.findViewById<TextView>(R.id.origin)

        val examplesTextView = builderView.findViewById<TextView>(R.id.examples)
        val addMoreExamples = builderView.findViewById<TextView>(R.id.addMoreExamples)
        val exampleProgress = builderView.findViewById<ProgressBar>(R.id.exampleProgress)

        dialog.setOnShowListener {
            wordNameTitle.text = word.name
            yourDefinitions.text = getYourInfo(word.definition)
            yourExamples.text = getYourInfo(word.examples)
            setDefinition(word.name, definitions)
            setExample(word.name, examplesTextView, exampleProgress)
            origin.text = word.origin

            addMoreExamples.setOnClickListener {
                if(exampleProgress.visibility == View.INVISIBLE){
                    exampleLimit += 3
                    setExample(word.name, examplesTextView, exampleProgress)
                    println(">>| add more examples")
                }else{
                    Lib.showMessage(context, "Working ...")
                }
            }
        }
        dialog.window?.setBackgroundDrawableResource(R.color.transparentForWordInfo)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

    }

    private fun getYourInfo(info : ArrayList<String>) : String{
        var count = 1
        var result = ""
        for(item in info){
            result += "$count. $item\n"
            count++
        }
        return result
    }

    private fun setDefinition(theWord: String, textView: TextView){
        thread {
            val definitions = getDefinition(theWord)

            val myHandler = Handler(context.mainLooper)
            val myRunnable = Runnable {
                textView.text = definitions
            }
            myHandler.post(myRunnable)
        }
    }

    private fun getDefinition(theWord: String) : String{
        val definition = DataBaseServices.findDefinition(theWord)
        val regex = ".*?\\d.*?".toRegex()
        val matches = regex.findAll(definition)
        var result = ""

        var i = 1
        for(item in matches){
            result += "$i. ${item.value.replace("\\d$".toRegex(), "")}\n"
            i++
        }

        return result
    }

    private fun setExample(theWord: String, textView : TextView, progress : ProgressBar){
        progress.visibility = View.VISIBLE
        thread {
            val examples = getExamples(theWord)

            val mainHandler = Handler(context.mainLooper)
            val myRunnable = Runnable {
                textView.setText(Html.fromHtml(examples, Html.FROM_HTML_MODE_COMPACT), TextView.BufferType.SPANNABLE)
                progress.visibility = View.INVISIBLE
            }
            mainHandler.post(myRunnable)
        }
    }

    private fun getExamples(theWord: String) : String{
        val result = ArrayList<String>()
        val maxL = 15
        val maxR = 15
        val frequency = DataBaseServices.getWordFrequency(SetManagement.selectedC.tableName, theWord)
        val maxExample = min(exampleLimit, frequency) - 1
        val regex = "(|.*?\\W)${theWord}(\\W.*|)".toRegex()


        val items = text.split(" ")
        var i = 0
        var counter = 1
        while(i < items.count()){
            if(items[i].contains(theWord) && regex.matches(items[i])){
                var a = "$counter. "
                var j = if(i > maxL) i - maxL else 0
                while(j < i + maxR && j < items.count()){
                    if(i == j){
                        a += " <font color='red'>${items[j]}</font>"
                    }else{
                        a += " " + items[j]
                    }
                    j++
                }
                a+= " ..."
                result.add(a)
                counter++
                if(result.count() > maxExample){
                    break
                }
            }
            i++
        }
        return result.joinToString("<br>").replace("[^\\x20-\\x7e]".toRegex(), "")
    }

}