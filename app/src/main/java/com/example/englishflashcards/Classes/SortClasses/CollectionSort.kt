package com.example.englishflashcards.Classes.SortClasses

import com.example.englishflashcards.Objects.DataBaseServices

class CollectionSort{
    var sortTypeCollection = 1
    var orderCollection = 0
    var hideItemsOnTop = false

    fun changeSortTypeCollection(type : Int){
        DataBaseServices.updateVar("sortTypeCollection", type.toString())
        sortTypeCollection = type
    }

    fun changeOrderCollection(type : Int){
        DataBaseServices.updateVar("orderCollection", type.toString())
        orderCollection = type
    }

    fun changeHideItemsOnTop(type : Boolean){
        DataBaseServices.updateVar("hideItemsOnTop", type.toString())
        hideItemsOnTop = type
    }

    fun restoreValues(){
        sortTypeCollection = insertOrGet("sortTypeCollection", sortTypeCollection.toString()).toInt()
        orderCollection = insertOrGet("orderCollection", orderCollection.toString()).toInt()
        hideItemsOnTop = insertOrGet("hideItemsOnTop", hideItemsOnTop.toString()).toBoolean()
    }

    private fun insertOrGet(n : String, value : String) : String{
        val a = DataBaseServices.getVar(n)
        if(a.isEmpty()){
            DataBaseServices.insertVar(n, value)
        }else{
            return a
        }
        return value
    }
}