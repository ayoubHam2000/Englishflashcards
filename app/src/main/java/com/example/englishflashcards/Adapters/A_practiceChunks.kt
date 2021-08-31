package com.example.englishflashcards.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Classes.WordInfoChunk
import com.example.englishflashcards.Objects.DataBaseServices
import com.example.englishflashcards.Objects.Lib
import com.example.englishflashcards.Objects.SetManagement
import com.example.englishflashcards.Objects.Settings
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.Complete
import com.example.englishflashcards.Utilities.Practice
import com.example.englishflashcards.Utilities.Reset
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class A_practiceChunks(val context : Context, val event : (String, ArrayList<String>, Int)-> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region vars and changeList
    private val layout = R.layout.a_practice_chunk_item
    private val message1 = "Not Open Yet"
    private val chunk = Settings.wordPracticeChunk
    private var list = ArrayList<WordInfoChunk>()


    fun changeList(){
        thread{
            val t = SetManagement.selectedC.tableName
            list = DataBaseServices.getWordPracticeInfo(t, chunk)
            val mainHandler = Handler(context.mainLooper)
            val myRunnable = Runnable {
                notifyDataSetChanged()
                event(Complete, arrayListOf(), 0)
            }
            mainHandler.post(myRunnable)
        }
    }

    //endregion

    inner class ViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView!!){

        private val theBox = itemView?.findViewById<RelativeLayout>(R.id.theBox)
        private val itemIndex = itemView?.findViewById<TextView>(R.id.itemIndex)
        private val lastViewTime = itemView?.findViewById<TextView>(R.id.lastViewTime)
        private val completedWords = itemView?.findViewById<TextView>(R.id.completedWords)
        private val completeProgress = itemView?.findViewById<ProgressBar>(R.id.completeProgress)

        @SuppressLint("SetTextI18n")
        fun bindView(position: Int){
            val cWord = list[position].completedWord.toInt()
            val lastView = list[position].lastView
            val count = list[position].words.count()


            val p = position + 1
            itemIndex?.text = p.toString()
            completedWords?.text = "$cWord / $count"
            completeProgress?.max = count
            completeProgress?.progress = cWord
            lastViewTime?.text = getLastViewTime(lastView)

            theBox?.setOnClickListener { event(Practice, list[position].words, position) }
            theBox?.setOnLongClickListener { event(Reset, list[position].words, position  )
                true}
        }

        private fun getLastViewTime(theSmallTime: Long) : String{
            return if(theSmallTime == 0L){
                message1
            }else{
                Lib.fromSecondsToDate(System.currentTimeMillis() - theSmallTime)
            }
        }



    }




    //region utilities

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(position)
    }

    override fun getItemCount(): Int {
        return  list.count()
    }

    //endregion


}