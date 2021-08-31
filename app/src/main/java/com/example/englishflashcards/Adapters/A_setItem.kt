package com.example.englishflashcards.Adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Classes.Set
import com.example.englishflashcards.Objects.Lib
import com.example.englishflashcards.Objects.Lib.changeBackgroundTint
import com.example.englishflashcards.Objects.SetManagement
import com.example.englishflashcards.Objects.WordsManagement
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.*

class A_setItem(val context : Context, val event : (String, String) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    //region vars and changeList
    var list = ArrayList<Set>()
    private val layout = R.layout.a_set_item

    fun changeList(){
        list = SetManagement.getListOfSet()
        notifyDataSetChanged()
    }

    //endregion



    inner class ViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView!!) {
        //view
        private val setItem = itemView?.findViewById<RelativeLayout>(R.id.set_item)
        private val itemName = itemView?.findViewById<TextView>(R.id.item_name)
        private val itemColor = itemView?.findViewById<ImageView>(R.id.item_color)
        private val itemEdit = itemView?.findViewById<ImageView>(R.id.item_edit)

        private lateinit var menuItem : PopupMenu



        fun bindView(position : Int){
            itemName?.text = list[position].name
            menuItem = Lib.initPopupMenu(context, itemEdit!!, R.menu.m_set_detail)
            menuClickItems(position)

            setItem?.setOnClickListener { selectItem(position) }

            changeItemColor(position)
            whenMoveCollectionToAction(position)
        }

        private fun selectItem(position : Int){
            event(OpenItem, list[position].name)
            notifyDataSetChanged()
        }

        private fun whenMoveCollectionToAction(position: Int){
            if(SetManagement.isCheckCollection && SetManagement.collectionActionType == CollectionMoveTo){
                itemEdit?.visibility = View.INVISIBLE
                setItem?.setOnClickListener { event(MoveToAction, list[position].name) }
            }else{
                itemEdit?.visibility = View.VISIBLE
            }
        }

        private fun changeItemColor(position : Int){
            val theColor = list[position].tagColor
            changeBackgroundTint(theColor, itemColor)
            if(list[position].name == SetManagement.selectedSet && WordsManagement.tableType == NormalWords){
                itemName?.setTextColor(theColor)
                itemName?.setTypeface(null, Typeface.BOLD)
                itemName?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
                itemColor?.scaleX = 1.2f
                itemColor?.scaleY = 1.2f
            }else{
                itemName?.setTextColor(Color.rgb(0, 0, 0))
                itemName?.setTypeface(null, Typeface.NORMAL)
                itemName?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
                itemColor?.scaleX = 0.8f
                itemColor?.scaleY = 0.8f
            }
        }

        private fun menuClickItems(position: Int){
            if(list[position].name == AllSet){
                menuItem.menu.removeItem(R.id.edit)
                menuItem.menu.removeItem(R.id.delete)
            }
            menuItem.setOnMenuItemClickListener {

                when(it.itemId){
                    R.id.edit -> event(Edit, list[position].name)
                    R.id.delete -> event(Delete, list[position].name)
                    R.id.changeColor -> event(ChangeColor, list[position].name)
                }

                true
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
        return list.count()
    }
    //endregion
}