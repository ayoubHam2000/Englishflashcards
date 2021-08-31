package com.example.englishflashcards.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Classes.Collection
import com.example.englishflashcards.Dialogs.D_collectionInfo
import com.example.englishflashcards.Objects.*
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.*
import java.nio.channels.SelectableChannel

class A_collection(val context : Context, val event : (String, Collection?) -> Unit):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region init and changeList vars


    var list = ArrayList<Collection>()
    var selectFirst = true
    private val layout = R.layout.a_collection_item

    fun changeList(){
        selectFirst = true
        list = SetManagement.getListOfCollection()
        when(list.isNotEmpty()){
            true -> event(NotEmpty, null)
            false -> event(Empty, null)
        }
        notifyDataSetChanged()
    }

    //endregion

    inner class ViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView!!){

        //region vars
        //var
        private lateinit var popUpMenu : PopupMenu

        //view
        private val collectionBackground = itemView?.findViewById<RelativeLayout>(R.id.collectionBackground)
        private val collectionName = itemView?.findViewById<TextView>(R.id.collectionName)
        private val collectionDetail = itemView?.findViewById<TextView>(R.id.collectionDetail)
        private val practiceButton = itemView?.findViewById<Button>(R.id.practiceButton)
        private val collectionMenu = itemView?.findViewById<ImageView>(R.id.collectionMenu)
        private val itemClick = itemView?.findViewById<ImageView>(R.id.itemClick)
        private val collectionTagColor = itemView?.findViewById<ImageView>(R.id.collectionTagColor)
        private val checkCollection = itemView?.findViewById<CheckBox>(R.id.checkCollection)

        //endregion

        fun bindView(position : Int){
            collectionName?.text = list[position].name
            collectionDetail?.text = getCollectionDetail(list[position])
            checkCollection?.isChecked = list[position].isChecked
            popUpMenu = Lib.initPopupMenu(context, collectionMenu!!, R.menu.m_collection_menu)

            //buttons
            itemClick?.setOnClickListener { itemClick(position) }
            practiceButton?.setOnClickListener { practiceClick(position) }

            selectFirstIfMerge()
            changeColorTag(position)
            menuClickItems(position)
            actionForCollection(position)
        }

        //region functions
        //region buttons
        private fun itemClick(position: Int){
            if(!SetManagement.isCheckCollection){
                event(OpenItem, list[position])
            }else{
                mergeInto(position)
            }
        }

        private fun practiceClick(position: Int){
            event(Practice, list[position])
        }

        //endregion

        private fun changeColorTag(position: Int){
            val isHide = list[position].isHide
            val backgroundColor = if(isHide) R.color.hideElement else R.color.white
            val setColor = DataBaseServices.getSetColor(list[position].father)
            Lib.changeBackgroundTint(context, backgroundColor, collectionBackground)
            Lib.changeBackgroundTint(setColor, collectionTagColor)
            if(list[position].isSelected){
                Lib.changeBackgroundTint(context, R.color.co_selectedElement, collectionBackground)
            }
        }

        private fun getCollectionDetail(collection: Collection) : String{
            val tt = DataBaseServices.getCollectionWordsCount(collection.tableName)
            return if(tt != 0){
                "Total Words : $tt"
            }else{
                "Empty"
            }
        }

        private fun menuClickItems(position: Int){
            popUpMenu.menu.findItem(R.id.hide).title = if(list[position].isHide) "UnHide" else "Hide"

            popUpMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.edit -> event(Edit, list[position])
                    R.id.importt -> event(Import, list[position])
                    R.id.hide -> event(Hide, list[position])
                    R.id.delete -> event(Delete, list[position])
                    R.id.importDirectly -> event(ImportDirectly, list[position])
                    R.id.addWord -> event(AddWord, list[position])
                }
                true
            }
        }

        //endregion

        //region action
        private fun selectFirstIfMerge(){
            if(SetManagement.isCheckCollection
                && SetManagement.collectionActionType == MergeCollection
                && selectFirst
            ){
                selectItem(0)
                selectFirst = false
            }

        }

        private fun mergeInto(position: Int){
            if(SetManagement.collectionActionType == MergeCollection){
                selectItem(position)
                notifyDataSetChanged()
            }
        }

        private fun actionForCollection(position: Int){
            //make check box appear and hide the menu button
            if(SetManagement.isCheckCollection){
                collectionMenu?.visibility = View.INVISIBLE
                checkCollection?.visibility = View.VISIBLE
            }else{
                checkCollection?.visibility = View.INVISIBLE
                collectionMenu?.visibility = View.VISIBLE
            }

            checkCollection?.setOnClickListener {
                list[position].isChecked = checkCollection.isChecked
            }
        }

        private fun selectItem(pos : Int){
            for((i, item) in list.withIndex()){
                item.isSelected = pos == i
            }
        }

        //endregion



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