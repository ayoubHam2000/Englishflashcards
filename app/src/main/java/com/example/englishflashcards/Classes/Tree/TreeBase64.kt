package com.example.englishflashcards.Classes.Tree

class TreeBase64 {
    //regular expression for base64 regex is [^A-Za-z0-9+/=] so is 65 characters
    private var head = Array<TheNode?>(65){null}
    private var indexOf = HashMap<Char, Int>()

    init {
        initHashMap()
    }

    fun addUnique(data : String){
        if(data.count() > 0 && !isExist(data))
            addItem(data)
    }

    fun isExist(data : String) : Boolean{
        if(data.count() == 0)
            return false
        var temp = head
        var i = 0
        var index = 0
        while(i < data.count() - 1){
            index = indexOf[data[i]]!!
            if(temp[index] == null){
                return false
            }
            temp = temp[index]!!.next
            i++
        }
        if(indexOf[data[i]] == null)
            println(">>> ${data[i]} -> ${data[i].toInt()} -> $data")
        index = indexOf[data[i]]!!
        if(temp[index] == null){
            return false
        }
        return temp[index]!!.exist
    }

    private fun addItem(data : String){
        var temp = head
        var i = 0
        var index = 0
        while(i < data.count() - 1){
            index = indexOf[data[i]]!!
            if(temp[index] == null){
                temp[index] = TheNode()
            }
            temp = temp[index]!!.next
            i++
        }
        index = indexOf[data[i]]!!
        if(temp[index] == null){
            temp[index] = TheNode()
            temp[index]!!.data = data
            temp[index]!!.exist = true
        }else{
            temp[index]!!.data = data
            temp[index]!!.exist = true
        }
    }

    private fun initHashMap(){
        val regex = "[0-9A-Za-z=/+]".toRegex()
        var j = 0
        for(i in 43..122){
            if(regex.matches(i.toChar().toString())){
                indexOf[i.toChar()] = j
                j++
            }
        }
    }
}