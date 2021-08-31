package com.example.englishflashcards.Dialogs

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.children
import com.example.englishflashcards.Objects.Settings
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.*

class D_Collections_filter(context : Context, val event : () -> Unit)
    : MyDialogBuilder(context, R.layout.d_collections_filter) {

    var sortType = 0
    var orderType = 0
    var hideItemsInTop = false

    override fun initView(builderView: View) {
        val deny = builderView.findViewById<TextView>(R.id.deny)
        val approve = builderView.findViewById<TextView>(R.id.approve)
        val sortTypesRadioLayout = builderView.findViewById<LinearLayout>(R.id.sortTypesLayout)
        val ordersLayout = builderView.findViewById<LinearLayout>(R.id.ordersLayout)
        val categoryLayout = builderView.findViewById<LinearLayout>(R.id.categoryLayout)
        val sortTypesRadio = getAllRadioBtn(sortTypesRadioLayout)
        val ordersRadio = getAllRadioBtn(ordersLayout)
        val categoryRadio = getAllCheckBtn(categoryLayout)

        radioSortClick(sortTypesRadio)
        radioOrderClick(ordersRadio)
        checkCategoryClick(categoryRadio)

        initSortRadio(builderView)
        initOrderRadio(builderView)
        initCategoryCheck(builderView)
        dialog.setOnShowListener {
            approve.setOnClickListener {
                Settings.collectionSort.changeSortTypeCollection(sortType)
                Settings.collectionSort.changeOrderCollection(orderType)
                Settings.collectionSort.changeHideItemsOnTop(hideItemsInTop)
                event()
                dismiss()
            }
            deny.setOnClickListener {
                dismiss()
            }
        }
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )


    }


    //------------------------------------------------------------------------------

    private fun radioSortClick(radios : ArrayList<RadioButton>){
        for(item in radios){
            item.setOnClickListener {
                deactivateRadio(radios)
                item.isChecked = true
                when(item.id){
                    R.id.sortAlpha->{
                        sortType = Alphabetic
                    }
                    R.id.sortCreated->{
                        sortType = CreatedTime
                    }
                    R.id.sortModified->{
                        sortType = LastModified
                    }
                    R.id.sortLastViewTime->{
                        sortType = LastView
                    }
                }
            }
        }
    }

    private fun radioOrderClick(radios : ArrayList<RadioButton>){
        for(item in radios){
            item.setOnClickListener {
                deactivateRadio(radios)
                item.isChecked = true
                when(item.id){
                    R.id.Ascending->{
                        orderType = Ascending
                    }
                    R.id.Descending->{
                        orderType = Descending
                    }
                }
            }
        }
    }

    private fun checkCategoryClick(radios : ArrayList<CheckBox>){
        var old : Boolean
        for(item in radios){
            item.setOnClickListener {
                old = item.isChecked
                deactivateCheckBox(radios)
                item.isChecked = old
                when(item.id){
                    R.id.categoryHideItem->{
                        hideItemsInTop = item.isChecked
                    }
                }
            }
        }
    }


    //------------------------------------------------------------------------------

    private fun initSortRadio(view : View){
        val id = when(Settings.collectionSort.sortTypeCollection){
            Alphabetic->{
                R.id.sortAlpha
            }
            CreatedTime->{
                R.id.sortCreated
            }
            LastModified->{
                R.id.sortModified
            }
            LastView->{
                R.id.sortLastViewTime
            }
            else ->{
                R.id.sortCreated
            }
        }
        val radio = view.findViewById<RadioButton>(id)
        radio.callOnClick()
    }

    private fun initOrderRadio(view : View){
        val id = when(Settings.collectionSort.orderCollection){
            Ascending->{
                R.id.Ascending
            }
            Descending->{
                R.id.Descending
            }
            else ->{
                R.id.Ascending
            }
        }
        val radio = view.findViewById<RadioButton>(id)
        radio.callOnClick()
    }

    private fun initCategoryCheck(view : View){
        val checkBox = view.findViewById<CheckBox>(R.id.categoryHideItem)
        hideItemsInTop = Settings.collectionSort.hideItemsOnTop
        checkBox.isChecked = hideItemsInTop
    }


    //------------------------------------------------------------------------------
    private fun getAllRadioBtn(view : LinearLayout) : ArrayList<RadioButton>{
        val result  = ArrayList<RadioButton>()
        for(item in view.children){
            if(item is RadioButton){
                result.add(item)
            }
        }
        return result
    }

    private fun getAllCheckBtn(view : LinearLayout) : ArrayList<CheckBox>{
        val result  = ArrayList<CheckBox>()
        for(item in view.children){
            if(item is CheckBox){
                result.add(item)
            }
        }
        return result
    }

    private fun deactivateRadio(radios : ArrayList<RadioButton>){
        for(item in radios){
            item.isChecked = false
        }
    }

    private fun deactivateCheckBox(radios : ArrayList<CheckBox>){
        for(item in radios){
            item.isChecked = false
        }
    }
}