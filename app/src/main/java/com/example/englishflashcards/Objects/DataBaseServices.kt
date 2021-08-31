package com.example.englishflashcards.Objects

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.util.Base64
import com.example.englishflashcards.Classes.*
import com.example.englishflashcards.Classes.Collection
import com.example.englishflashcards.Classes.Set
import com.example.englishflashcards.Utilities.*
import java.lang.Integer.min
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

object DataBaseServices {

    //region todo

    //TODOd : Init the Data Base
    //TODOd : Create Tables sets collections words examples definitions
    //TODOd : make insert to all of the tables
    //TODOd : get words examples definitions ...
    //TODOd : update word examples definitions ...
    //TODOd : delete word examples definitions ...
    //TODOd : when change set name you have to change it in all the word
    //TODOd : encode all string with string64 before add it to the data base

    //endregion

    //region section 0 (vars, init, load)
    //region global vars

    private lateinit var dataBase : SQLiteDatabase
    var dicIsLoaded = false

    //endregion

    //region init And loading DB

    fun initDataBase(context : Context){
        dataBase = context.openOrCreateDatabase("APP_DATA", Context.MODE_PRIVATE, null)

        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_Set")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_Collections")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_Words")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_Examples")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_Definition")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_Texts")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_Vars")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_CWords")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_Dictionary")
    }

    fun loadData(context : Context, event : (Boolean) -> Unit){
        //event = false -> process | event = true -> end process
        if(isSetNotExist(AllSet)) insertSet(AllSet, Color.rgb(200, 200, 200))

        //load Dic
        getOrInsertVar(DicLoaded, false){
            dicIsLoaded = it.toBoolean()
        }

        //restore vars
        getOrInsertVar(OpenedSet, SetManagement.selectedSet){
            SetManagement.selectedSet = it
        }
        getOrInsertVar(isCheckedHideItem, Settings.displayHideCollection){
            Settings.displayHideCollection = it.toBoolean()
        }

        getOrInsertVar(isCheckedHideDeleteItem, Settings.displayHideDeletedItems){
            Settings.displayHideDeletedItems = it.toBoolean()
        }

        getOrInsertVar(deleteOption, Settings.deleteOption){
            Settings.deleteOption = it.toInt()
        }


        if(!dicIsLoaded){
            event(false)
            loadDic(context){event(true)}
        }else{ println(">>| Dic is Already Loaded") }
    }

    private fun loadDic(context: Context, event : ()->Unit){
        //event = complete
        println(">>| Get Dic")
        val dicCount = tableCountByQuery("SELECT $D_name FROM $DictionaryDb")
        if(dicCount == 0){
            thread {
                DicInit(context, dataBase).getDic {
                    updateVar(DicLoaded, true.toString())
                    println(">>| Insert Dic Complete.")
                    event()
                }
            }
        }else{
            updateVar(DicLoaded, true.toString())
            event()
        }
        println(">>| Get Dic Complete with count = $dicCount")
    }

    fun findDefinition(name : String) : String{
        val list = arrayListOf(
            name,
            name.replace("ed$".toRegex(), ""),
            name.replace("s$".toRegex(), ""),
            name.replace("'.*".toRegex(), "")
        )
        val formatList = fromListStringToString(list, format = true, toBase64 = true)
        val p = "SELECT $D_definition FROM $DictionaryDb WHERE $D_name IN $formatList"
        val cursor = dataBase.rawQuery(p, null)
        println(">> count = ${cursor.count} -> $formatList")
        val def = if(cursor.moveToFirst()){
            cursor.getString(0).fromBase64ToString()
        }else{
            ""
        }

        cursor.close()
        return def
    }

    fun getOrInsertVar(label : String, default : Any, event : (String)->Unit){
        val d = default.toString()
        val v = getVar(label)
        if(v.isEmpty()){
            insertVar(label, d)
        }else{
            event(v)
        }
    }

    //endregion

    //endregion

    //region section 1 (inserting, getting, updating, deleting)

    //region inserting

    fun insertSet(name : String, tagColor : Int){
        val time = System.currentTimeMillis()
        dataBase.execSQL("INSERT INTO $Sets VALUES ('${name.toBase64()}', $time, $tagColor)")
    }

    fun insertCollection(name : String, time : Long, father : String){
        val tableName = getNextCollectionTableName()
        dataBase.execSQL("INSERT INTO $Collections ($D_name, $D_createdTime, $D_father, $D_tableName) VALUES ('${name.toBase64()}', $time, '${father.toBase64()}', '$tableName')")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $tableName $DT_TableName")
    }

    fun insertExamples(word : String, example : String){
        dataBase.execSQL("INSERT INTO $Examples ($D_name, $D_example) VALUES ('${word.toBase64()}', '${example.toBase64()}')")
        updateCollectionModifiedTime()
        updateWordLastModified(word)
    }

    fun insertDefinition(word : String, definition : String){
        dataBase.execSQL("INSERT INTO $D_definition ($D_name, $D_definition) VALUES ('${word.toBase64()}', '${definition.toBase64()}')")
        updateCollectionModifiedTime()
        updateWordLastModified(word)
    }

    fun insertWord(wordInfo : ArrayList<String>) : Boolean{
        val tableName = SetManagement.selectedC.tableName

        val sqlC = "INSERT INTO $tableName VALUES (?, ?, ?);"
        val sqlCW = "INSERT INTO $CWords VALUES (?, ?, ?);"
        val sqlW = "INSERT INTO $Words (name) VALUES (?)"
        val statementC = dataBase.compileStatement(sqlC)
        val statementCW = dataBase.compileStatement(sqlCW)
        val statementW = dataBase.compileStatement(sqlW)


        val currentTime = System.currentTimeMillis()
        val word = wordInfo[0]
        val wordBase64 = word.toBase64()
        val isWordNotExist = isWordNotExist(word, tableName)
        if(isWordNotExist){
            //add to global words if not exist, else turn off delete
            val isWordNotExistInWords = isWordNotExist(word, CWords)
            if(isWordNotExistInWords){
                statementW.bindString(1, wordBase64)
                statementW.executeInsert()

                statementCW.bindString(1, wordBase64)
                statementCW.bindLong(2, 1)
                statementCW.bindLong(3, currentTime)
                statementCW.executeInsert()
            }else {
                if(isWordDeleted(word)){
                    updateWordIsDeleted(word, 0, "")
                    updateWordIsHide(word, 0)
                }
                updateWordFrequencyAdd(word, CWords)
            }

            //add word to the target collection add update collection modified time
            statementC.bindString(1, wordBase64)
            statementC.bindLong(2, 1)
            statementC.bindLong(3, currentTime)
            statementC.executeInsert()


            if(wordInfo.count() == 2){
                if(wordInfo[1].isNotEmpty()) insertDefinition(word, wordInfo[1])
            }
            updateCollectionModifiedTime()
        }

        return isWordNotExist
    }

    fun insertText(tableName : String, data : String){
        //text is already base64
        val formatData  = data.replace("[\r\n]".toRegex(), " ").replace("\\s+".toRegex(), " ").toBase64()
        dataBase.execSQL("INSERT INTO $Texts VALUES ('$tableName', '$formatData')")
    }

    fun insertVar(name : String, value : String){
        dataBase.execSQL("INSERT INTO $Vars VALUES ('$name', '$value')")
    }

    //endregion

    //region  getting

    fun getDataBase() : SQLiteDatabase{
        return dataBase
    }

    fun getSets() : ArrayList<Set>{
        val result = ArrayList<Set>()

        val sCursor = dataBase.rawQuery("SELECT * FROM sets", null)
        if(sCursor.moveToFirst()){
            do{
                val name = sCursor.getString(0).fromBase64ToString()
                val createdTime = sCursor.getLong(1)
                val tagColor = sCursor.getInt(2)
                result.add(Set(name, createdTime, tagColor))
            }while (sCursor.moveToNext())
        }
        sCursor.close()
        return result
    }

    fun getSetColor(name : String) : Int{
        val default =  Color.rgb(0, 0, 0)
        val sCursor = dataBase.rawQuery("SELECT $D_tagColor FROM $Sets WHERE $D_name = '${name.toBase64()}'", null)
        val color = if(sCursor.moveToFirst()) sCursor.getInt(0) else default
        sCursor.close()
        return color
    }

    fun getCollections(setName : String = "") : ArrayList<Collection>{
        val result = ArrayList<Collection>()

        val q = if(setName.isEmpty()) {
            "SELECT * FROM $Collections"
        }
        else {
            "SELECT * FROM $Collections WHERE $D_father = '${setName.toBase64()}'"
        }
        val cursor = dataBase.rawQuery(q, null)
        if(cursor.moveToFirst()){
            do{
                val ci = getColumnInfo(cursor, DV_Collections)

                val name = cursor.getString(ci[0]).fromBase64ToString()
                val createdTime = cursor.getLong(ci[1])
                val father = cursor.getString(ci[2]).fromBase64ToString()
                val isHide = cursor.getInt(ci[3]) == 1
                val lastViewTime = cursor.getLong(ci[4])
                val tableName = cursor.getString(ci[5])
                val lastModifiedTime = cursor.getLong(ci[6])

                val newCollection = Collection(name, createdTime, father, tableName)
                newCollection.isHide = isHide
                newCollection.lastViewTime = lastViewTime
                newCollection.lastModifiedTime = lastModifiedTime

                result.add(newCollection)
            }while (cursor.moveToNext())
        }

        cursor.close()
        return result
    }

    private fun getWordExamples(name : String) : ArrayList<String>{
        //examples(word example)
        val examples = ArrayList<String>()

        val eCursor = dataBase.rawQuery("SELECT $D_example FROM $Examples WHERE $D_name = '${name.toBase64()}'", null)

        if(eCursor.moveToFirst()){
            do{
                examples.add(eCursor.getString(0).fromBase64ToString())
            }while (eCursor.moveToNext())
        }

        eCursor.close()
        return examples
    }

    fun getWordDefinitions(name : String) : ArrayList<String>{
        //definitions(word definition)
        val definitions = ArrayList<String>()

        val dCursor = dataBase.rawQuery("SELECT $D_definition FROM $Definition WHERE $D_name = '${name.toBase64()}'", null)


        if(dCursor.moveToFirst()){
            do{
                definitions.add(dCursor.getString(0).fromBase64ToString())
            }while (dCursor.moveToNext())
        }

        dCursor.close()
        return definitions
    }

    fun getVar(name : String) : String{
        val cursor = dataBase.rawQuery("SELECT $D_value FROM $Vars WHERE $D_name = '$name' LIMIT 1", null)
        val result = if(cursor.moveToFirst()) cursor.getString(0) else ""
        cursor.close()
        return result
    }

    fun getCollectionWordsCount(t : String) : Int{
        val q = "SELECT $D_name FROM $t;"
        return tableCountByQuery(q)
    }

    fun getText(tableName: String) : ArrayList<String>{
        val result = ArrayList<String>()

        val q = "SELECT $D_text FROM $Texts WHERE $D_name = '$tableName'"
        val cursor = dataBase.rawQuery(q, null)

        if(cursor.moveToFirst()){
            do{
                result.add(cursor.getString(0).fromBase64ToString())
            }while (cursor.moveToNext())
        }

        cursor.close()
        return result
    }

    fun isSetNotExist(name : String) : Boolean{
        val q = "SELECT $D_name FROM $Sets WHERE $D_name = '${name.toBase64()}'"
        return tableCountByQuery(q) == 0
    }

    fun isCollectionNotExist(setName : String, name : String) : Boolean{
        val q = "SELECT $D_name FROM $Collections WHERE $D_father = '${setName.toBase64()}' AND $D_name = '${name.toBase64()}'"
        return tableCountByQuery(q) == 0
    }

    fun isCollectionHide(s : String, c : String) : Boolean{
        val q = "SELECT $D_isHide FROM $Collections WHERE $D_father = '${s.toBase64()}' AND $D_name = '${c.toBase64()}' AND $D_isHide = 1"
        return tableCountByQuery(q) == 1
    }

    //endregion

    //region words getting

    fun isWordNotExist(name : String, t : String) : Boolean{
        val q = "SELECT $D_name FROM $t WHERE name = '${name.toBase64()}'"
        return tableCountByQuery(q) == 0
    }

    fun isWordFavorite(name : String) : Boolean{
        val q = "SELECT $D_isFavorite FROM $Words WHERE $D_name = '${name.toBase64()}' AND $D_isFavorite = 1"
        return tableCountByQuery(q) == 1
    }

    fun isWordRemembered(name : String) : Boolean{
        val q = "SELECT $D_isFavorite FROM $Words WHERE $D_name = '${name.toBase64()}' AND $D_isRemember = 1"
        return tableCountByQuery(q) == 1
    }

    private fun isWordDeleted(name : String) : Boolean{
        val q = "SELECT $D_isDeleted FROM $Words WHERE $D_name = '${name.toBase64()}' AND $D_isDeleted = 1"
        return tableCountByQuery(q) == 1
    }

    fun getWordFrequency(t : String, name : String) : Int{
        val q = "SELECT $D_frequency FROM $t WHERE $D_name = '${name.toBase64()}' LIMIT 1"
        val cursor = dataBase.rawQuery(q, null)
        val f = if(cursor.moveToFirst()) cursor.getInt(0) else 1
        cursor.close()
        return f
    }

    //endregion

    //region related to words fragment getting

    fun getListWordNames(q1 : String) : ArrayList<String>{
        val time = System.currentTimeMillis()
        val result = ArrayList<String>()

        val cursor = dataBase.rawQuery(q1, null)
        if(cursor.moveToFirst()){
            do{
                result.add(cursor.getString(0).fromBase64ToString())
            }while (cursor.moveToNext())
        }
        cursor.close()
        println(">>| ${q1.substring(0, min(300, q1.count()))} : ${System.currentTimeMillis() - time}")
        return result
    }

    fun getCollectionSimpleWords(q1 : String) : HashMap<String, SimpleWord>{
        val time = System.currentTimeMillis()
        val result = HashMap<String, SimpleWord>()

        val cursor = dataBase.rawQuery(q1, null)

        if(cursor.moveToFirst()){
            do{
                val name = cursor.getString(0).fromBase64ToString()
                val frequency = cursor.getInt(1)
                val createdTime = cursor.getLong(2)
                result[name] = SimpleWord(frequency, createdTime)
            }while (cursor.moveToNext())
        }

        cursor.close()
        println(">>| $q1 : ${System.currentTimeMillis() - time}")
        return result
    }

    fun getCollectionWordsInfo(q1 : String) : HashMap<String, Boolean>{
        val time = System.currentTimeMillis()
        val result = HashMap<String, Boolean>()

        val cursor = dataBase.rawQuery(q1, null)

        if(cursor.moveToFirst()){
            do{
                val name = cursor.getString(0).fromBase64ToString()
                val isSomething = cursor.getInt(1) == 1
                result[name] = isSomething
            }while (cursor.moveToNext())
        }

        cursor.close()
        println(">>| $q1 : ${System.currentTimeMillis() - time}")
        return result
    }

    fun getWord(name : String, local : Boolean = false) : Word?{

        val t = SetManagement.selectedC.tableName
        val q1 = "SELECT $D_frequency from $CWords WHERE $D_name = '${name.toBase64()}'"
        val q2 = "SELECT $D_frequency from $t WHERE $D_name = '${name.toBase64()}'"
        val q3 = "SELECT * from $Words WHERE $D_name = '${name.toBase64()}'"
        var frequency = 1

        if(!local){
            val cursor = dataBase.rawQuery(q1, null)
            if(cursor.moveToFirst()) frequency = cursor.getInt(0)
            cursor.close()
        }else{
            val cursor = dataBase.rawQuery(q2, null)
            if(cursor.moveToFirst()) frequency = cursor.getInt(0)
            cursor.close()
        }


        println(">>| name : $name")
        println(">>| $q3")
        val cursor = dataBase.rawQuery(q3, null)

        if (cursor.moveToFirst()) {
            val theWord = Word(name)

            val ci = getColumnInfo(cursor, DV_Words)

            val isFavorite = cursor.getInt(ci[1]) == 1
            val isHide = cursor.getInt(ci[2]) == 1
            val type = cursor.getString(ci[3]).fromBase64ToString()
            val origin = cursor.getString(ci[4]).fromBase64ToString()
            val lastView = cursor.getLong(ci[5])
            val level = cursor.getInt(ci[6])
            val repetition = cursor.getInt(ci[7])
            val isShowed = cursor.getInt(ci[8]) == 1
            val isRemember = cursor.getInt(ci[9]) == 1
            val isDeleted = cursor.getInt(ci[10]) == 1


            theWord.isFavorite = isFavorite
            theWord.isHide = isHide
            theWord.type = type
            theWord.origin = origin
            theWord.lastViewedTime = lastView
            theWord.level = level
            theWord.repeatedTime = repetition
            theWord.isShowed = isShowed
            theWord.isRemembered = isRemember
            theWord.examples.addAll(getWordExamples(name))
            theWord.definition.addAll(getWordDefinitions(name))
            theWord.isDeleted = isDeleted
            theWord.frequency = frequency

            cursor.close()
            return theWord
        }

        cursor.close()
        return null
    }

    //endregion

    //region update

    fun updateSet(name : String, newName : String){
        dataBase.execSQL("UPDATE $Sets SET $D_name = '${newName.toBase64()}' WHERE $D_name = '${name.toBase64()}'")
        dataBase.execSQL("UPDATE $Collections SET $D_father = '${newName.toBase64()}' WHERE $D_father = '${name.toBase64()}'")
    }

    fun updateSetColor(name : String, newColor : Int){
        dataBase.execSQL("UPDATE $Sets SET $D_tagColor = $newColor WHERE $D_name = '${name.toBase64()}'")
    }

    fun updateCollection(c : Collection, newName : String){
        updateCollectionModifiedTime()
        dataBase.execSQL("UPDATE $Collections SET $D_name = '${newName.toBase64()}' WHERE $D_name = '${c.name.toBase64()}' AND $D_father = '${c.father.toBase64()}'")
    }

    fun updateCollectionHide(father : String, c : String, value : Int){
        updateCollectionModifiedTime()
        dataBase.execSQL("UPDATE $Collections SET $D_isHide = $value WHERE $D_name = '${c.toBase64()}' AND $D_father = '${father.toBase64()}'")
    }

    fun updateCollectionLastView(){
        val c = SetManagement.selectedC
        dataBase.execSQL("UPDATE $Collections SET $D_lastViewTime = ${System.currentTimeMillis()} WHERE $D_name = '${c.name.toBase64()}' AND $D_father = '${c.father.toBase64()}'")
    }

    fun updateCollectionModifiedTime(otherC : Collection? = null){
        val c = otherC ?: SetManagement.selectedC
        dataBase.execSQL("UPDATE $Collections SET $D_lastModifiedTime = ${System.currentTimeMillis()} WHERE $D_name = '${c.name.toBase64()}' AND $D_father = '${c.father.toBase64()}'")
    }

    fun updateCollectionModifiedTime(t : String){
        dataBase.execSQL("UPDATE $Collections SET $D_lastModifiedTime = ${System.currentTimeMillis()} WHERE $D_tableName = '$t'")
    }

    fun updateVar(name : String, newValue : String){
        dataBase.execSQL("UPDATE $Vars SET $D_value = '$newValue' WHERE $D_name = '$name'")
    }

    //endregion

    //region update words

    fun updateWordName(theName : String, theNewName : String){
        //tableName (name , frequency , createdTime)
        val name = theName.toBase64()
        val newName = theNewName.toBase64()
        val collections = getAllCollectionTableName()
        dataBase.execSQL("UPDATE $Words SET $D_name = '$newName' WHERE $D_name = '$name'")
        dataBase.execSQL("UPDATE $CWords SET $D_name = '$newName' WHERE $D_name = '$name'")
        dataBase.execSQL("UPDATE $Examples SET $D_name = '$newName' WHERE $D_name = '$name'")
        dataBase.execSQL("UPDATE $Definition SET $D_name = '$newName' WHERE $D_name = '$name'")
        for(item in collections){
            dataBase.execSQL("UPDATE $item SET $D_name = '$newName' WHERE $D_name = '$name';")
        }

        updateCollectionModifiedTime()
        updateWordLastModified(theNewName)
    }

    fun updateWordExample(word : String, e : String, newE : String){
        dataBase.execSQL("UPDATE $Examples SET $D_example = '${newE.toBase64()}' WHERE $D_example = '${e.toBase64()}' AND $D_name = '${word.toBase64()}'")
        updateCollectionModifiedTime()
        updateWordLastModified(word)
    }

    fun updateWordDefinition(word : String, d : String, newD : String){
        dataBase.execSQL("UPDATE $Definition SET $D_definition = '${newD.toBase64()}' WHERE $D_definition = '${d.toBase64()}' AND $D_name = '${word.toBase64()}'")
        updateCollectionModifiedTime()
        updateWordLastModified(word)
    }

    fun updateWordIsFavorite(name: String, value : Int){
        dataBase.execSQL("UPDATE $Words SET $D_isFavorite = $value WHERE $D_name = '${name.toBase64()}'")
        updateCollectionModifiedTime()
        updateWordLastModified(name)
    }

    fun updateWordIsDeleted(name: String, value: Int, t : String){
        //t = collection table
        //dataBase.execSQL("UPDATE $Words SET $D_isDeleted = $value WHERE $D_name = '${name.toBase64()}'")
        executeQuery("UPDATE $Words SET $D_isDeleted = $value WHERE $D_name = '${name.toBase64()}'")
        if(value == 1){
            if(t == CWords){
                //dataBase.execSQL("DELETE FROM $Words WHERE $D_name = '${name.toBase64()}'")
                executeQuery("DELETE FROM $Words WHERE $D_name = '${name.toBase64()}'")
            }
            //dataBase.execSQL("DELETE FROM $t WHERE $D_name = '${name.toBase64()}'")
            executeQuery("DELETE FROM $t WHERE $D_name = '${name.toBase64()}'")
        }
        updateCollectionModifiedTime()
        updateWordLastModified(name)
    }

    fun updateWordIsHide(name: String, value: Int){
        dataBase.execSQL("UPDATE $Words SET $D_isHide = $value WHERE $D_name = '${name.toBase64()}'")
        updateWordLastModified(name)
    }

    fun updateWordType(name: String, value: String){
        dataBase.execSQL("UPDATE $Words SET $D_type = '${value.toBase64()}' WHERE $D_name = '${name.toBase64()}'")
        updateCollectionModifiedTime()
        updateWordLastModified(name)
    }

    fun updateWordOrigin(name: String, value: String){
        dataBase.execSQL("UPDATE $Words SET $D_origin = '${value.toBase64()}' WHERE $D_name = '${name.toBase64()}'")
        updateCollectionModifiedTime()
        updateWordLastModified(name)
    }

    fun updateWordLastView(name: String){
        dataBase.execSQL("UPDATE $Words SET $D_lastViewTime = ${System.currentTimeMillis()} WHERE $D_name = '${name.toBase64()}'")
    }

    fun updateWordLastModified(name: String){
        dataBase.execSQL("UPDATE $Words SET $D_lastModifiedTime = ${System.currentTimeMillis()} WHERE $D_name = '${name.toBase64()}'")
    }

    fun updateWordLevel(name: String, value: Int){
        dataBase.execSQL("UPDATE $Words SET $D_level = $value WHERE $D_name = '${name.toBase64()}'")
    }

    fun updateWordRepetition(name: String){
        dataBase.execSQL("UPDATE $Words SET $D_repetition = $D_repetition + 1 WHERE $D_name = '${name.toBase64()}'")
    }

    fun updateWordIsShowed(name: String, value: Int){
        dataBase.execSQL("UPDATE $Words SET $D_isShowed = $value WHERE $D_name = '${name.toBase64()}'")
    }

    fun updateWordIsRemember(name: String, value: Int){
        dataBase.execSQL("UPDATE $Words SET $D_isRemember = $value WHERE $D_name = '${name.toBase64()}'")
    }

    fun updateWordFrequencyAdd(name : String, t : String){
        dataBase.execSQL("UPDATE $t SET $D_frequency = $D_frequency + 1 WHERE $D_name = '${name.toBase64()}'")
    }

    //endregion

    //region delete

    fun deleteSet(n : String){
        val name = n.toBase64()
        val collectionTables = getCollections(n)

        dataBase.beginTransaction()
        try{
            //---------------------

            dataBase.execSQL("DELETE FROM $Sets WHERE $D_name = '$name'")
            dataBase.execSQL("DELETE FROM $Collections WHERE $D_father = '$name'")

            //delete all collection tables
            for(item in collectionTables){
                executeQuery("UPDATE $Words SET $D_isDeleted = 1 WHERE $D_name IN (SELECT $D_name FROM ${item.tableName});")
                dataBase.execSQL("DROP TABLE ${item.tableName};")
            }

            //---------------------
            dataBase.setTransactionSuccessful()
        }catch (e : Exception){
            println(e.printStackTrace())
        }finally {
            dataBase.endTransaction()
        }
    }

    fun deleteCollection(c : Collection){
        dataBase.execSQL("DELETE FROM $Collections WHERE $D_name = '${c.name.toBase64()}' AND $D_father = '${c.father.toBase64()}';")
        dataBase.execSQL("UPDATE $Words SET $D_isDeleted = 1 WHERE $D_name IN (SELECT $D_name FROM ${c.tableName});")
        dataBase.execSQL("DROP TABLE ${c.tableName};")
        dataBase.execSQL("DELETE FROM $Texts WHERE $D_name = '${c.tableName}'")
    }

    fun deleteExample(word : String, example : String){
        dataBase.execSQL("DELETE FROM $Examples WHERE $D_name = '${word.toBase64()}' AND $D_example = '${example.toBase64()}'")
        updateCollectionModifiedTime()
        updateWordLastModified(word)
    }

    fun deleteDefinition(word : String, definition : String){
        dataBase.execSQL("DELETE FROM $D_definition WHERE $D_name = '${word.toBase64()}' AND $D_definition = '${definition.toBase64()}'")
        updateCollectionModifiedTime()
        updateWordLastModified(word)
    }

    //endregion

    //endregion

    //region section 2 (management)
    //region action bar menu actions (collections, words)

    fun mergeCollectionTables(tables : ArrayList<Collection>) : String{
        val temp = "temps"
        var selectedItem = tables[0]

        //get collection words query
        var r = ""
        for(item in tables.withIndex()){
            if(item.value.isChecked)
                r += "SELECT * FROM ${item.value.tableName} UNION ALL "
        }
        r = r.replace(" UNION ALL $".toRegex(), "")

        //get selected item
        for(item in tables){
            if(item.isSelected) selectedItem = item
        }

        val t1 = selectedItem.tableName
        val time = System.currentTimeMillis()
        executeQuery("CREATE TABLE $temp $DT_TableName;")
        executeQuery("INSERT INTO $temp SELECT DISTINCT * FROM ($r) group by $D_name;")
        executeQuery( "DROP TABLE $t1;")
        executeQuery("ALTER TABLE $temp RENAME TO $t1;")
        println(">>| ALL : ${System.currentTimeMillis() - time}")
        updateCollectionModifiedTime(selectedItem)
        return selectedItem.name
    }

    fun collectionMoveToSet(tables : ArrayList<Collection>, newFather : String) : Boolean{
        val allowed = ArrayList<String>()
        val notAllowed = ArrayList<String>()
        val newFatherCollections = getCollections(newFather)
        val newFatherCollectionMap = HashMap<String, String>()
        for(item in newFatherCollections){
            newFatherCollectionMap[item.name] = item.tableName
        }

        for(item in tables){
            val isExist = newFatherCollectionMap[item.name]
            if(isExist == null && item.isChecked){
                allowed.add(item.tableName)
                updateCollectionModifiedTime(item.tableName)
            }else if(isExist != null){
                notAllowed.add(item.name)
            }
        }

        val formatList = fromListStringToString(allowed, format = true, toBase64 = false)
        dataBase.execSQL("UPDATE collections SET father = '${newFather.toBase64()}' WHERE $D_tableName IN $formatList")
        return notAllowed.isEmpty()
    }

    fun deleteMultipleCollections(tables: ArrayList<Collection>){
        for (item in tables)
            if(item.isChecked) deleteCollection(item)
    }

    fun copyWordsTo(t1 : String, t2 : String, list : String, isAllSelected : Boolean = false){
        //from t1 to t2
        val theList = if(isAllSelected) "(Select $D_name FROM $t1)" else list
        val t = "temps2"
        println(">>| ------------------------ copyWordsTo")
        executeQuery("CREATE TABLE $t ($D_name VARCHAR, $D_frequency INT, $D_createdTime VARCHAR);")
        executeQuery("INSERT INTO $t SELECT DISTINCT * FROM (SELECT * FROM $t2 UNION SELECT * FROM $t1 WHERE $D_name IN $theList) group by name;")
        executeQuery("DROP TABLE $t2;")
        executeQuery("ALTER TABLE $t RENAME TO $t2;")
        println(">>| ------------------------ end copyWordsTo")
        updateCollectionModifiedTime(t2)
    }

    fun deleteWordsByList(t : String, list: String, type : Int, isAllSelected : Boolean = false){
        if(WordsManagement.tableType != NormalWords && type != RemoveFromDeleteList){
            val theList = if(isAllSelected) "(Select $D_name FROM $Words WHERE $D_isDeleted = 1)" else list
            val collectionTables = getAllCollectionTableName()
            if(type != AskDeleteByHide){
                for(item in collectionTables){
                    //if(item != t) -> we are in deleted words section so the condition is true
                    executeQuery("DELETE FROM $item WHERE $D_name IN $theList;")
                }
                if(type == AskDeleteGlobally){
                    executeQuery("DELETE FROM $Words WHERE $D_name IN $theList;")
                    executeQuery("DELETE FROM $CWords WHERE $D_name IN $theList;")
                }
            }else{
                executeQuery("UPDATE $Words SET $D_isHide = 1 WHERE $D_isDeleted = 1 AND $D_name IN $theList;")
            }
        }else if(type == RemoveFromDeleteList){
            val theList = if(isAllSelected) "(Select $D_name FROM $Words WHERE $D_isDeleted = 1)" else list
            //when we want to move words from delete list
            //executeQuery("UPDATE $Words SET $D_isDeleted = 0 WHERE $D_name IN $list;")
            executeQuery("UPDATE $Words SET $D_isHide = 0, $D_isDeleted = 0 WHERE $D_isDeleted = 1 AND $D_name IN $theList;")
        }else{
            val theList = if(isAllSelected) "(Select $D_name FROM $t)" else list
            executeQuery("UPDATE $Words SET $D_isDeleted = 1 WHERE $D_name IN $theList;")
            executeQuery("DELETE FROM $t WHERE $D_name IN $theList;")
            updateCollectionModifiedTime(t)
        }
    }

    fun unHideDeletedItems(list : String, isAllSelected : Boolean = false){
        val all = if(isAllSelected) ";" else "AND $D_name IN $list;"
        executeQuery("UPDATE $Words SET $D_isHide = 0 WHERE $D_isDeleted = 1 $all")
    }

    fun unFavoriteItems(list : String, isAllSelected : Boolean = false){
        val all = if(isAllSelected) ";" else "AND $D_name IN $list;"
        executeQuery("UPDATE $Words SET $D_isFavorite = 0 WHERE $D_isFavorite = 1 $all")
    }

    fun unRememberItems(list : String, isAllSelected : Boolean = false){
        val all = if(isAllSelected) ";" else "AND $D_name IN $list;"
        executeQuery("UPDATE $Words SET $D_isRemember = 0 WHERE $D_isRemember = 1 $all")
    }

    //endregion

    //region words practice

    fun getWordPracticeInfo(t : String, chunk : Int) : ArrayList<WordInfoChunk>{
       val result = ArrayList<WordInfoChunk>()

       val q1 = "SELECT $D_name, $D_isRemember, $D_lastViewTime FROM $Words WHERE $D_name IN (SELECT $D_name FROM $t)"
       val cursor = dataBase.rawQuery(q1, null)

       var lastView = 0L
       var completedWord : Short = 0
       var listNames = ArrayList<String>()
       var i = 0

       if(cursor.moveToFirst()){
           do{
               val name = cursor.getString(0).fromBase64ToString()
               val isComplete = cursor.getInt(1) == 1
               val lastViewTime = cursor.getLong(2)

               listNames.add(name)
               if(isComplete) completedWord++

               if(lastView < lastViewTime) { lastView = lastViewTime }
               i++
               if(i % chunk == 0){
                   result.add(WordInfoChunk(listNames, completedWord, lastView))
                   lastView = 0L
                   completedWord = 0
                   listNames = ArrayList()
               }
           }while (cursor.moveToNext())
           if(i % chunk != 0){
               result.add(WordInfoChunk(listNames, completedWord, lastView))
           }
       }
       cursor.close()
       return result
   }



    //endregion
    //endregion

    //region section of utilities

    private fun getAllCollectionTableName() : ArrayList<String>{
        val list = ArrayList<String>()
        val q1 = "SELECT $D_tableName FROM $Collections"
        val cursor = dataBase.rawQuery(q1, null)
        if(cursor.moveToFirst()){
            do{
                list.add(cursor.getString(0))
            }while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    private fun getNextCollectionTableName() : String{
        val tables = getAllCollectionTableName()
        val list = ArrayList<Int>()
        for(item in tables)
            list.add(item.replace("c", "").toInt())
        val max = list.max()
        return if(max == null) "c0" else "c${max + 1}"
    }

    fun tableCountByQuery(sql : String) : Int{
        val cursor = dataBase.rawQuery(sql, null)
        val count = cursor.count
        cursor.close()
        return count
    }

    fun fromListStringToString(l: ArrayList<String>, format : Boolean = false, toBase64 : Boolean = true) : String{
        return if(format){
            val newList = ArrayList<String>()
            for(item in l){
                if(toBase64){
                    newList.add("'${item.toBase64()}'")
                }else{
                    newList.add("'$item'")
                }
            }
            newList.toString().replace("[", "(").replace("]", ")")
        }else{
            l.toString().replace("[", "(").replace("]", ")")
        }
    }

    fun fromListStringToString(l:List<String>, fromBeginning : Boolean = false) : String{
        return if(fromBeginning){
            val newList = ArrayList<String>()
            for(item in l)
                newList.add("'${item.toBase64()}'")
            newList.toString().replace("[", "(").replace("]", ")")
        }else{
            l.toString().replace("[", "(").replace("]", ")")
        }
    }

    fun String.toBase64() : String{
        val bytes = this.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT).replace("[\\x0a]".toRegex(), "")
    }

    private fun String.fromBase64ToString() : String{
        val originByte = Base64.decode(this, Base64.DEFAULT)
        return String(originByte)
    }

    fun executeQuery(q : String){
        val time = System.currentTimeMillis()
        dataBase.execSQL(q)
        println(">>| ${q.substring(0, min(q.count(), 600))} : ${System.currentTimeMillis() - time}")
    }

    private fun getColumnInfo(cursor: Cursor, type : Array<String>) : ArrayList<Int>{
        val r = ArrayList<Int>()
        for(item in type)
            r.add(cursor.getColumnIndex(item))
        return r
    }

    private fun clearAllDataBase(){
        val collectionTables = getAllCollectionTableName()
        dataBase.execSQL("DELETE FROM $Sets")
        dataBase.execSQL("DELETE FROM $Collections")
        dataBase.execSQL("DELETE FROM $Words")
        dataBase.execSQL("DELETE FROM $Examples")
        dataBase.execSQL("DELETE FROM $Definition")
        dataBase.execSQL("DELETE FROM $Texts")

        for(item in collectionTables)
            dataBase.execSQL("DROP TABLE $item")
    }

    fun displayAllTables(){
        val cursor = dataBase.rawQuery("SELECT $D_name from sqlite_master where type = 'table';", null)
        if(cursor.moveToFirst()){
            do{
                val name = cursor.getString(0)
                val count = tableCountByQuery("SELECT * FROM $name")
                println(">>| name : $name, count : $count")
            }while (cursor.moveToNext())
        }
        cursor.close()
    }

    //endregion

}