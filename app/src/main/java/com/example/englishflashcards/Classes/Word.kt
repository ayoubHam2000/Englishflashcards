package com.example.englishflashcards.Classes

class Word(
    var name : String
){
    val definition = ArrayList<String>()
    val examples = ArrayList<String>()
    var createdTime = 0L
    var frequency = 0
    var isFavorite : Boolean = false
    var isHide : Boolean = false
    var isRemembered : Boolean = false
    var repeatedTime : Int = 0//how much time you see this word
    var level : Int = 1 //the level of the word
    var origin : String = "" //word origin like played ---> play
    var type : String = "" //word type like play --> verb
    var lastViewedTime : Long = 0
    var lastModifiedTime = 0L
    var isShowed = false
    var levelAdded = 0
    var isDeleted = false
}