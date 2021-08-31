package com.example.englishflashcards.Classes

import com.example.englishflashcards.Objects.SetManagement
import com.example.englishflashcards.Objects.Settings
import com.example.englishflashcards.Objects.WordsManagement

class Collection(
    var name : String,
    val createdTime : Long,
    val father : String,
    val tableName : String
){
    //used in db
    var isHide = false
    var lastViewTime = 0L
    var lastModifiedTime = 0L

    var isChecked = false
    var isSelected = false
}