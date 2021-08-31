package com.example.englishflashcards.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Classes.Collection
import com.example.englishflashcards.Objects.DataBaseServices
import com.example.englishflashcards.Objects.Lib
import com.example.englishflashcards.Objects.SetManagement
import com.example.englishflashcards.R
import java.util.zip.Inflater

class A_CollectionTarget(val context : Context, val event : (Collection?) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region vars init changeList
    //theText the id of the textView

    var list = ArrayList<Collection>()
    private val layout = R.layout.a_ask_collection

    fun changeList(){
        list = getNewList()
        if(list.isEmpty()) event(null)
        notifyDataSetChanged()
    }

    private fun getNewList() : ArrayList<Collection>{
        val result = ArrayList<Collection>()
        val lc = SetManagement.getListOfCollection()
        for(item in lc)
            if(item.name != SetManagement.selectedC.name || item.father != SetManagement.selectedC.father)
                result.add(item)
        return result
    }

    //endregion



    inner class ViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView!!){

        private val theText = itemView?.findViewById<TextView>(R.id.collectionName)
        private val colorTag = itemView?.findViewById<ImageView>(R.id.colorTag)
        private val fatherName = itemView?.findViewById<TextView>(R.id.fatherName)
        private val itemClick = itemView?.findViewById<RelativeLayout>(R.id.itemClick)

        @SuppressLint("SetTextI18n")
        fun bindView(position: Int){
            val theColor = DataBaseServices.getSetColor(list[position].father)
            theText?.text = list[position].name
            fatherName?.text = "(${list[position].father})"
            itemClick?.setOnClickListener {
                event(list[position])
            }
            Lib.changeBackgroundTint(theColor, colorTag)
        }

    }

    //region about recyclerView override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(position)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    //endregion

}