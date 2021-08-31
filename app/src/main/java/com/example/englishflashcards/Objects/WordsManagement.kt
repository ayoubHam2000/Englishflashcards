package com.example.englishflashcards.Objects

import com.example.englishflashcards.Classes.SimpleWord
import com.example.englishflashcards.Classes.Word
import com.example.englishflashcards.Utilities.*
import kotlin.collections.ArrayList

object WordsManagement {

    var word : Word? = null
    var tableType = NormalWords
    var isCheckWords = false

    //region Map words, favorite, remember, deleted

    fun getListMapWords() : HashMap<String, SimpleWord>{
        val t = SetManagement.selectedC.tableName
        val q1 = "SELECT $D_name, $D_frequency, $D_createdTime FROM $t"
        return DataBaseServices.getCollectionSimpleWords(q1)
    }

    fun getListMapWordsFavorite() : HashMap<String, Boolean>{
        val t = SetManagement.selectedC.tableName
        val q1 = if(tableType == NormalWords){
            "SELECT $D_name, $D_isFavorite FROM $Words WHERE  $D_isFavorite = 1 AND $D_name IN (SELECT $D_name FROM $t);"
        }else{
            "SELECT $D_name, $D_isFavorite FROM $Words WHERE $D_isFavorite = 1;"
        }
        return DataBaseServices.getCollectionWordsInfo(q1)
    }

    fun getListMapWordsRemembered() : HashMap<String, Boolean>{
        val t = SetManagement.selectedC.tableName
        val q1 = if(tableType == NormalWords){
            "SELECT $D_name, $D_isRemember FROM $Words WHERE  $D_isRemember = 1 AND $D_name IN (SELECT $D_name FROM $t);"
        }else{
            "SELECT $D_name, $D_isRemember FROM $Words WHERE $D_isRemember = 1;"
        }
        return DataBaseServices.getCollectionWordsInfo(q1)
    }

    fun getListMapWordsHideDeleted() : HashMap<String, Boolean>{
        val q1 = "SELECT $D_name, $D_isHide FROM $Words WHERE $D_isHide = 1;"
        return if(tableType == DeletedWords){
            DataBaseServices.getCollectionWordsInfo(q1)
        }else{
            hashMapOf()
        }
    }

    //endregion

    //region sort and group

    fun getSortedWordList() : ArrayList<String>{
        val result = ArrayList<String>()
        val q = getQueryForListWords()

        //sort
        val sorted = if(Settings.wordSort.sortTypeWords == Alphabetic){
            DataBaseServices.getListWordNames(q).sorted()
        }else{
            DataBaseServices.getListWordNames(q)
        }

        //reverse or normal
        if(Settings.wordSort.orderWords == Ascending){
            result.addAll(sorted)
        }else{
            result.addAll(sorted.reversed())
        }

        //category on top
        return if(tableType == NormalWords || tableType == AllWords){
            getCategoryOnTop(result)
        }else{
            result
        }
    }

    private fun getCategoryOnTop(sortedList : ArrayList<String>) : ArrayList<String>{
        val t = SetManagement.selectedC.tableName
        //val p = "SELECT $D_name FROM $t EXCEPT SELECT $D_name FROM $Words WHERE"
        val p = "SELECT $D_name FROM $t WHERE $D_name IN (SELECT $D_name FROM $Words WHERE"

        val q1 = when {
            Settings.wordSort.favoriteOnTop -> {
                //"$p isFavorite = 0"
                "$p $D_isFavorite = 1)"
            }
            Settings.wordSort.rememberedOnTop -> {
                //"$p isRemember = 0"
                "$p $D_isRemember = 1)"
            }
            else -> {
                ""
            }
        }

        return if(q1.isNotEmpty()) {
            val categoryList = DataBaseServices.getListWordNames(q1)
            getFinalSortedList(sortedList, categoryList)
        } else {
            sortedList
        }
    }

    private fun getFinalSortedList(a : ArrayList<String>, b : ArrayList<String>) : ArrayList<String>{
        //a is sorted by type || b is sorted by category (favorite or remembered)
        val result = ArrayList<String>()
        val notIn = ArrayList<String>()
        for(item in a)
            if (item in b)
                result.add(item)
            else
                notIn.add(item)
        result.addAll(notIn)
        return result
    }

    private fun getQueryForListWords() : String{
        val t = SetManagement.selectedC.tableName
        val s1 = "SELECT $D_name FROM $Words WHERE $D_isDeleted = 0 AND $D_name IN (SELECT $D_name FROM $t)"
        val condition = when(tableType){
            AllWords->"$D_isDeleted = 0"
            FavoriteWords-> "$D_isFavorite = 1 AND $D_isDeleted = 0"
            RememberedWords-> "$D_isRemember = 1 AND $D_isDeleted = 0"
            DeletedWords-> {
                if(Settings.displayHideDeletedItems) "$D_isDeleted = 1" else "$D_isDeleted = 1 AND $D_isHide = 0"
            }
            else-> ""
        }
        val selectType = when(Settings.wordSort.sortTypeWords){
            Alphabetic , CreatedTime , Frequency ->{
                when(condition){
                    "" -> ""
                    else -> "WHERE $D_name IN (SELECT $D_name FROM $Words WHERE $condition)"
                }
            }

            LastModified, LastView, Level ->{
                when(condition){
                    "" -> ""
                    else -> "WHERE $condition"
                }
            }
            else-> ""
        }

        val normalWords = tableType == NormalWords

        return if(normalWords){
            when(Settings.wordSort.sortTypeWords) {
                Alphabetic -> {
                    "SELECT $D_name FROM $t"
                }
                CreatedTime -> {
                    "SELECT $D_name FROM $t ORDER BY $D_createdTime"
                }
                Frequency -> {
                    "SELECT $D_name FROM $t ORDER BY $D_frequency DESC"
                }
                LastModified -> {
                    "$s1 ORDER BY $D_lastModifiedTime DESC"
                }
                LastView -> {
                    "$s1 ORDER BY $D_lastViewTime DESC"
                }
                Level -> {
                    "$s1 ORDER BY $D_level DESC"
                }
                else -> {
                    "SELECT $D_name FROM $t ORDER BY $D_createdTime"
                }
            }
        }else {
            when (Settings.wordSort.sortTypeWords) {
                Alphabetic -> {
                    "SELECT $D_name FROM $CWords $selectType"
                }
                CreatedTime -> {
                    "SELECT $D_name FROM $CWords $selectType ORDER BY $D_createdTime"
                }
                Frequency -> {
                    "SELECT $D_name FROM $CWords $selectType ORDER BY $D_frequency DESC"
                }
                LastModified -> {
                    "SELECT $D_name FROM $Words $selectType ORDER BY $D_lastModifiedTime DESC"
                }
                LastView -> {
                    "SELECT $D_name FROM $Words $selectType ORDER BY $D_lastViewTime DESC"
                }
                Level -> {
                    "SELECT $D_name FROM $Words $selectType ORDER BY $D_level DESC"
                }
                else -> {
                    "SELECT $D_name FROM $CWords $selectType ORDER BY $D_createdTime"
                }
            }
        }
    }

    //endregion

}