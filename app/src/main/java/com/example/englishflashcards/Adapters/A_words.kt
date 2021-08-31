package com.example.englishflashcards.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Classes.SimpleWord
import com.example.englishflashcards.Classes.Word
import com.example.englishflashcards.Dialogs.D_number_range
import com.example.englishflashcards.Objects.*
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.*
import kotlin.concurrent.thread


class A_words(val context: Context, val event: (String, Word) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region vars and changeList

    private val layout = R.layout.a_words
    private val maxDefinitionLen = 30
    var listMap = HashMap<String, SimpleWord>()
    var sortedList = ArrayList<String>()
    var list = ArrayList<String>()
    var favoriteList = HashMap<String, Boolean>()
    var rememberedList = HashMap<String, Boolean>()
    var hideDeleted = HashMap<String, Boolean>()

    val checkedList = HashMap<String, Boolean>()
    var searchedText = ""
    var isSearchClick = false
    var typeCheck = -1

    fun changeList(){
        event(Prepare, Word(""))
        thread {
            val time = System.currentTimeMillis()
            println(">>| ------------------------------------")
            if(isSearchClick){
                searchFilter()
            }else{
                listMap = WordsManagement.getListMapWords()
                sortedList = WordsManagement.getSortedWordList()
                favoriteList = WordsManagement.getListMapWordsFavorite()
                rememberedList = WordsManagement.getListMapWordsRemembered()
                hideDeleted = WordsManagement.getListMapWordsHideDeleted() //empty if other tables
                searchFilter()
            }
            println(">>| Total time : ${System.currentTimeMillis() - time}")
            println(">>| list : (${sortedList.count()} -> ${list.count()})")
            println(">>| isSearching : $isSearchClick")
            println(">>| ------------------------------------")


            Handler(context.mainLooper).post {
                if(isSearchClick) Lib.showMessage(context, "${list.count()} Elements Found")
                isSearchClick = false
                notifyAdapter()
            }
        }
    }

    private fun searchFilter(){
        list.clear()
        val regex = "$searchedText.*".toRegex()
        for(item in  sortedList){
            if(regex.matches(item))
                list.add(item)
        }
    }

    private fun notifyAdapter(){
        notifyDataSetChanged()
        if(list.count() == 0){
            event(Empty, Word(""))
        }else{
            event(NotEmpty, Word(""))
        }
    }

    //endregion


    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        //region vars
        private val father = itemView?.findViewById<RelativeLayout>(R.id.fatherBack)
        private val wordName = itemView?.findViewById<TextView>(R.id.wordName)
        private val wordDefinition = itemView?.findViewById<TextView>(R.id.wordDefinitions)
        private val wordLevel = itemView?.findViewById<TextView>(R.id.wordLevel)
        private val wordMenu = itemView?.findViewById<ImageView>(R.id.wordMenu)
        private val favorite = itemView?.findViewById<ImageView>(R.id.favorite)
        private val itemClick = itemView?.findViewById<ImageView>(R.id.itemClick)
        private val wordNumber = itemView?.findViewById<TextView>(R.id.wordNumber)
        private val checkBox = itemView?.findViewById<CheckBox>(R.id.isChecked)
        private lateinit var menuItem : PopupMenu

        //endregion

        @SuppressLint("SetTextI18n")
        fun bindView(position: Int){
            val name = list[position]
            val size = getItemWith() * 0.6

            wordName?.width = (size).toInt()
            setDefinition(name) //wordDefinition
            wordLevel?.text = "Frequency : ${listMap[name]!!.frequency}"
            wordNumber?.text = "#${position + 1}"
            menuItem = Lib.initPopupMenu(context, wordMenu!!, R.menu.m_word_info)
            menuClickItems(position)
            makeItRemember(name)
            makeItCheck(name)
            makeDeleteHide(name)
            makeCheckVisible()
            changeFavoriteIcon(name) //favorite

            //region buttons
            favorite?.setOnClickListener {
                if(!WordsManagement.isCheckWords){
                    makeItFavorite(name)
                }
            }
            itemClick?.setOnClickListener {
                if(WordsManagement.isCheckWords){
                    checkBox!!.isChecked = !checkBox.isChecked
                    checkBox.callOnClick()
                }
            }
            itemClick?.setOnLongClickListener {
                WordsManagement.isCheckWords = false
                event(OpenItem, DataBaseServices.getWord(name)!!)
                true
            }
            checkBox?.setOnClickListener {
                checkBoxClick(name)
            }

            //endregion


        }

        //region function
        private fun getItemWith() : Int{
            val margin = father!!.marginStart + father.marginLeft
            return (Settings.screenWith / 2 - margin)
        }

        private fun makeItCheck(name: String){
            val isChecked = checkedList[name]
            checkBox?.isChecked = isChecked != null && isChecked
        }

        private fun makeCheckVisible(){
            if(WordsManagement.isCheckWords){
                checkBox?.visibility = View.VISIBLE
                wordMenu?.visibility = View.INVISIBLE
            }else{
                checkedList.clear()
                checkBox?.visibility = View.INVISIBLE
                wordMenu?.visibility = View.VISIBLE
            }
        }

        private fun checkBoxClick(name: String){
            if(checkBox!!.isChecked){
                checkedList[name] = checkBox.isChecked
            }else{
                checkedList.remove(name)
            }
        }

        private fun menuClickItems(position: Int){
            val name = list[position]
            if(WordsManagement.tableType != NormalWords && WordsManagement.tableType != DeletedWords){
                menuItem.menu.removeItem(R.id.delete)
            }
            menuItem.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.edit -> event(Edit, DataBaseServices.getWord(name)!!)
                    R.id.delete -> {
                        event(Delete, DataBaseServices.getWord(name)!!)
                    }
                }

                true
            }
        }

        @SuppressLint("SetTextI18n")
        private fun makeItRemember(name: String){
            val isRemember = rememberedList[name]
            if(isRemember != null && isRemember){
                wordName?.text = "$name ${10003.toChar()}"
            }else{
                wordName?.text = name
            }
        }

        private fun makeItFavorite(name: String){
            val isFavorite = favoriteList[name]
            if(isFavorite == null){
                DataBaseServices.updateWordIsFavorite(name, 1)
                favoriteList[name] = true
            }else{
                DataBaseServices.updateWordIsFavorite(name, (!isFavorite).toInteger())
                favoriteList[name] = !isFavorite
            }

            changeFavoriteIcon(name)
        }

        private fun makeDeleteHide(name : String){
            if(WordsManagement.tableType == DeletedWords){
                val isHide = hideDeleted[name]
                if(isHide != null && isHide){
                    Lib.changeBackgroundTint(context, R.color.hideElement, father)
                }else{
                    Lib.changeBackgroundTint(context, R.color.setBackgroundColorItem, father)
                }
            }
        }

        private fun changeFavoriteIcon(name: String){
            val isFavorite = favoriteList[name]
            if(isFavorite != null && isFavorite){
                favorite?.setBackgroundResource(R.drawable.ic_favorite_active)
            }else{
                favorite?.setBackgroundResource(R.drawable.ic_favorite)
            }
        }

        private fun setDefinition(name: String){
            wordDefinition?.text = ""
            thread {
                val d = DataBaseServices.getWordDefinitions(name)
                val definition = if(d.count() > 0){
                    if(d[0].count() > maxDefinitionLen){
                        d[0].substring(0, maxDefinitionLen) + " ..."
                    }else{
                        d[0]
                    }
                }else{ "" }
                Handler(context.mainLooper).post { wordDefinition?.text = definition }
            }
        }

        private fun Boolean.toInteger(): Int{
            return when(this){
                false -> 0
                true -> 1
            }
        }

        //endregion

    }

    //region deleteCheck MakeCheck
    fun deleteItems(){
        var i = 0
        while(i < list.count()){
            val item = list[i]
            val isExist = checkedList[item]
            if(isExist != null && isExist){
                list.remove(item)
                sortedList.remove(item)
                i--
            }
            i++
        }
    }

    fun makeItCheck(){
        when(typeCheck){
            SelectAllWords -> {
                for (item in list)
                    checkedList[item] = true
            }
            SelectFavoriteWords -> {
                val t = SetManagement.selectedC.tableName
                val q =
                    "SELECT $D_name FROM $Words WHERE $D_name IN (SELECT $D_name FROM $t) AND $D_isFavorite = 1"
                for (item in DataBaseServices.getListWordNames(q))
                    checkedList[item] = true
            }
            SelectRememberedWords -> {
                val t = SetManagement.selectedC.tableName
                val q =
                    "SELECT $D_name FROM $Words WHERE $D_name IN (SELECT $D_name FROM $t) AND $D_isRemember = 1"
                for (item in DataBaseServices.getListWordNames(q))
                    checkedList[item] = true
            }
            SelectByNumber->{
                val dialog = D_number_range(context){
                    for(item in it){
                        checkedList[list[item - 1]] = true
                    }
                    notifyDataSetChanged()
                }
                dialog.text = "Min Range is 1, Max Range is ${list.count()}"
                dialog.minRange = 1
                dialog.maxRange = list.count()
                dialog.build()
                dialog.display()
            }
        }
        notifyDataSetChanged()
    }

    //endregion

    //region override
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