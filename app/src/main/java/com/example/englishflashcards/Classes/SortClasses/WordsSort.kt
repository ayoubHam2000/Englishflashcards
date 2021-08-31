package com.example.englishflashcards.Classes.SortClasses

import com.example.englishflashcards.Objects.DataBaseServices

class WordsSort {
    var sortTypeWords = 1
    var orderWords = 0
    var favoriteOnTop = false
    var rememberedOnTop = false

    fun changeSortTypeWords(type : Int){
        DataBaseServices.updateVar("sortTypeWords", type.toString())
        sortTypeWords = type
    }

    fun changeOrderWords(type : Int){
        DataBaseServices.updateVar("orderWords", type.toString())
        orderWords = type
    }

    fun changeFavoriteOnTop(type : Boolean){
        DataBaseServices.updateVar("favoriteOnTop", type.toString())
        if(type) rememberedOnTop = false
        favoriteOnTop = type
    }

    fun changeRememberedOnTop(type : Boolean){
        DataBaseServices.updateVar("rememberedOnTop", type.toString())
        if(type) favoriteOnTop = false
        rememberedOnTop = type
    }

    fun restoreValues(){
        sortTypeWords = insertOrGet("sortTypeWords", sortTypeWords.toString()).toInt()
        orderWords = insertOrGet("orderWords", sortTypeWords.toString()).toInt()
        favoriteOnTop = insertOrGet("favoriteOnTop", sortTypeWords.toString()).toBoolean()
        rememberedOnTop = insertOrGet("rememberedOnTop", sortTypeWords.toString()).toBoolean()
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