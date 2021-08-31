package com.example.englishflashcards.Objects


import com.example.englishflashcards.Classes.Collection
import com.example.englishflashcards.Classes.Set
import com.example.englishflashcards.Utilities.*
import kotlin.collections.ArrayList

object SetManagement {
    //TODO make sort for set

    var selectedSet = AllSet
    lateinit var selectedC : Collection
    var isCheckCollection = false
    var collectionActionType = ""


    fun getListOfSet() : ArrayList<Set>{
        val result = ArrayList<Set>()
        val sets = DataBaseServices.getSets()
        for (s in sets){
            result.add(s)
        }
        return result
    }

    fun getListOfCollection() : ArrayList<Collection>{
        val theList = if(selectedSet == AllSet){
            removeHideCollection(DataBaseServices.getCollections())
        }else{
            removeHideCollection(DataBaseServices.getCollections(selectedSet))
        }
        sortCollection(theList)
        return theList
    }

    private fun removeHideCollection(list : ArrayList<Collection>) : ArrayList<Collection>{
        if(!Settings.displayHideCollection){
            var i = 0
            while(i < list.count()){
                if(list[i].isHide){
                    list.removeAt(i)
                    i--
                }
                i++
            }
        }
        return list
    }

    private fun sortCollection(list : ArrayList<Collection>){
        var newList = when(Settings.collectionSort.sortTypeCollection){
            Alphabetic->{
                list.sortedWith(compareBy { it.name })
            }
            CreatedTime->{
                list.sortedWith(compareBy { it.createdTime })
            }
            LastModified->{
                list.sortedWith(compareByDescending { it.lastModifiedTime })
            }
            LastView->{
                list.sortedWith(compareByDescending { it.lastViewTime })
            }
            else->{
                list.sortedWith(compareBy { it.createdTime })
            }
        }
        if(Settings.collectionSort.hideItemsOnTop && Settings.displayHideCollection){
            newList = newList.sortedWith(compareBy { !it.isHide })
        }

        //-------------
        list.clear()
        if(Settings.collectionSort.orderCollection == Ascending){
            list.addAll(newList)
        }else{
            list.addAll(newList.reversed())
        }
    }

}