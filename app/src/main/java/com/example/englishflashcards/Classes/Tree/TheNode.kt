package com.example.englishflashcards.Classes.Tree

import com.example.englishflashcards.Classes.Word

class TheNode {
    var exist = false
    var data : String? = null
    val next = Array<TheNode?>(65){null}
}