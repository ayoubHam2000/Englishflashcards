package com.example.englishflashcards.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Classes.Word
import com.example.englishflashcards.Dialogs.D_editItem
import com.example.englishflashcards.Objects.DataBaseServices
import com.example.englishflashcards.R

class A_wordEdit(val context : Context, val theWord : Word)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region vars
    private val layout1 = R.layout.a_word_definition
    private val layout2 = R.layout.a_word_example_text
    private val layout3 = R.layout.a_wordedit_example_definition

    private val type1 = 0 //for definition text
    private val type2 = 1 //for examples text
    private val type3 = 2 //for items

    private val examples = 0
    private val definitions = 1
    private lateinit var dialog : D_editItem

    //endregion

    //region override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view1 = LayoutInflater.from(context).inflate(layout1, parent, false)
        val view2 = LayoutInflater.from(context).inflate(layout2, parent, false)
        val view3 = LayoutInflater.from(context).inflate(layout3, parent, false)
        return when(viewType){
            type1 -> ViewHolder1(view1)
            type2 -> ViewHolder2(view2)
            else -> ViewHolder3(view3)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            type1 -> (holder as ViewHolder1).bindView()
            type2 -> (holder as ViewHolder2).bindView()
            else -> {
                val index = theWord.definition.count() + 1
                if(position < index){
                    (holder as ViewHolder3).bindView(position - 1, definitions)
                }else{
                    (holder as ViewHolder3).bindView(position - index - 1, examples)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return theWord.examples.count() + theWord.definition.count() + 2
    }

    override fun getItemViewType(position: Int): Int {
        return when(position){
            0 -> type1
            theWord.definition.count() + 1 -> type2
            else -> type3
        }
    }

    //endregion

    inner class ViewHolder1(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        private val addDefinition = itemView?.findViewById<ImageView>(R.id.addDefinition)

        fun bindView() {
            dialog = D_editItem(context){
                DataBaseServices.insertDefinition(theWord.name, it)
                theWord.definition.add(it)
                dialog.dismiss()
                notifyDataSetChanged()
            }
            dialog.build()
            addDefinition?.setOnClickListener {
                dialog.display()
            }
        }
    }

    inner class ViewHolder2(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        private val addExample = itemView?.findViewById<ImageView>(R.id.addExample)



        fun bindView() {
            addExample?.setOnClickListener {
                dialog = D_editItem(context){
                    DataBaseServices.insertExamples(theWord.name, it)
                    theWord.examples.add(it)
                    dialog.dismiss()
                    notifyDataSetChanged()
                }
                dialog.build()
                dialog.display()
            }
        }
    }

    inner class ViewHolder3(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        private val wordInfo = itemView?.findViewById<TextView>(R.id.wordInfo)


        fun bindView(position: Int, type : Int) {
            when(type){
                definitions -> wordInfo?.text = theWord.definition[position]
                examples -> wordInfo?.text = theWord.examples[position]
            }
            wordInfo?.setOnClickListener {
                editItem(position, type)
            }
        }

        private fun editItem(position: Int, type: Int){
            dialog = D_editItem(context){
                when(type){
                    definitions -> {
                        DataBaseServices.updateWordDefinition(theWord.name, theWord.definition[position], it)
                        theWord.definition[position] = it
                    }
                    examples -> {
                        DataBaseServices.updateWordExample(theWord.name, theWord.examples[position], it)
                        theWord.examples[position] = it
                    }
                }
                dialog.dismiss()
                notifyDataSetChanged()
            }
            dialog.build()
            when(type){
                definitions -> dialog.textInput = theWord.definition[position]
                examples -> dialog.textInput = theWord.examples[position]
            }
            dialog.display()
        }
    }
}