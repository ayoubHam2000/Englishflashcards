package com.example.englishflashcards.Objects

import com.example.englishflashcards.Classes.Word
import com.example.englishflashcards.Utilities.D_createdTime
import com.example.englishflashcards.Utilities.D_name

object PracticeManager {

    var practiceWords = ArrayList<String>()
    var onEditFromPractice = false

    fun getSortedPracticeWordList() : ArrayList<String>{

        val t = SetManagement.selectedC.tableName
        val formatList = DataBaseServices.fromListStringToString(practiceWords, format = true, toBase64 = true)
        val q = "SELECT $D_name FROM $t WHERE $D_name IN $formatList ORDER BY $D_createdTime"

        return DataBaseServices.getListWordNames(q)
    }




}