package com.example.englishflashcards.Objects

import android.app.Activity
import android.util.DisplayMetrics
import com.example.englishflashcards.Classes.SortClasses.CollectionSort
import com.example.englishflashcards.Classes.SortClasses.WordsSort
import com.example.englishflashcards.Utilities.AskDeleteLocally

object Settings {
    val dailyWords = 100 //how mach words you can receive per section
    val maxLevel = 240
    val rememberedValue = 2
    val improveValue = 1
    val normalAdd = 1
    val addedTime = 3 * 3600 * 1000
    val memorizedCardsLimitLevel = 56 //7 day
    val wordPracticeChunck = 50
    val wordPracticeChunk = 50

    var screenWith = 0
    var startTimeOfApplication = 0L
    var passedDays = 0L
    var displayHideCollection = false
    var displayHideDeletedItems = false
    var deleteOption = AskDeleteLocally

    //sort
    val wordSort = WordsSort()
    val collectionSort = CollectionSort()

    fun initSort(){
        wordSort.restoreValues()
        collectionSort.restoreValues()
    }

    fun setScreenDimension(activity : Activity){
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWith = displayMetrics.widthPixels
    }

}