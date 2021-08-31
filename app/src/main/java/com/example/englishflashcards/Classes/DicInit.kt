package com.example.englishflashcards.Classes

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.englishflashcards.Objects.DataBaseServices.toBase64
import com.example.englishflashcards.Utilities.DictionaryDb
import java.lang.Exception
import java.util.*

class DicInit(
    private val context : Context,
    private val db : SQLiteDatabase
) {

    fun getDic(event : ()->Unit){
        val data = getText(context).split('\n')
        db.beginTransaction()
        try{
            insertDic(data)
            db.setTransactionSuccessful()
        }catch (e : Exception){
            println(e.printStackTrace())
        }finally {
            db.endTransaction()
            event()
        }
    }

    private fun insertDic(data : List<String>){
        val sql = "INSERT INTO $DictionaryDb VALUES (?, ?)"
        val statement = db.compileStatement(sql)

        for ((i, item) in data.withIndex()){
            val info = item.split(" ")
            if(info.count() >= 1 && info[0].isNotEmpty()) {
                val name = info[0].trim().toLowerCase(Locale.ROOT)
                statement.bindString(1, name.toBase64())
                statement.bindString(2, item.toBase64())
                statement.executeInsert()
                if(i % 500 == 0) println(">>| Dic inserting progress : $i")
            }
        }
    }

    private fun getText(context : Context) : String{
        val file = context.assets.open("dic3")
        val size = file.available()
        val buffer = ByteArray(size)
        file.read(buffer)
        file.close()
        return String(buffer)
    }

}