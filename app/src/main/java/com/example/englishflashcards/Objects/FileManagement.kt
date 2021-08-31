package com.example.englishflashcards.Objects

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.example.englishflashcards.Dialogs.D_number_range
import com.example.englishflashcards.Dialogs.D_progressDialog
import com.example.englishflashcards.Objects.DataBaseServices.toBase64
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.*
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

object FileManagement {

    //region vars
    private lateinit var progressDialog : D_progressDialog
    private val pages = ArrayList<Int>()
    private const val minWordLen = MinWordNameSize
    private const val maxWordLen = MaxWordNameSize
    private val originList = ArrayList<String>()
    private val temp = "temps"


    //region message
    private const val message1 = "GET TEXT ..."
    private const val message2 = "GET TEXT FROM PDF ..."
    private const val message3 = "FORMAT TEXT ..."
    private const val message4 = "FIND MATCHES ..."
    private const val message5 = "FIND FREQUENCIES ..."
    private const val message6 = "INSERTING "
    //endregion

    //endregion

    //region controller
    fun setCollectionWordsDirectly(c : Context, data : String, complete: () -> Unit){
        //initProgressDialog
        pages.clear()
        progressDialog = D_progressDialog(c, R.layout.d_progress_insert)
        progressDialog.build()

        progressDialog.display()
        thread {
            DataBaseServices.insertText(SetManagement.selectedC.tableName, data)
            val words = findWords(data)
            addWordsToCollection(words)
            complete()
            progressDialog.dismiss()
        }

    }

    fun setCollectionWords(c : Context, fileUri: Uri, complete: () -> Unit){
        //initProgressDialog
        pages.clear()
        progressDialog = D_progressDialog(c, R.layout.d_progress_insert)
        progressDialog.build()


        if(fileUri.path != null){
            //page for pdf
            if(findFileExtension(fileUri) == "pdf"){
                val pdfPagesNumber = getPdfPages(c, fileUri)
                val pagesDialog = D_number_range(c){
                    pages.addAll(it)
                    startWorking(c, fileUri){ complete() }
                }
                pagesDialog.text = "Number of pages is : $pdfPagesNumber"
                pagesDialog.minRange = 1
                pagesDialog.maxRange = pdfPagesNumber
                pagesDialog.build()
                pagesDialog.display()
            }else{
                startWorking(c, fileUri){ complete() }
            }
        }else{
            println("file path is null")
            Lib.showMessage(c, "something went wrong")
        }
    }

    private fun startWorking(c : Context, fileUri: Uri, complete: () -> Unit){
        progressDialog.display()

        thread {
            val data = getCollectionList(c, fileUri)
            addWordsToCollection(data)
            complete()
            progressDialog.dismiss()
        }
    }

    //endregion

    //region getCollection from pdf and text

    private fun getCollectionList(context : Context, fileUri: Uri) : HashMap<String, Int>{
        when(findFileExtension(fileUri)){
            "txt" ->{
                return getFromText(context, fileUri)
            }
            "pdf" ->{
                return getFromPdf(context, fileUri)
            }
        }
        return hashMapOf()
    }

    private fun getFromText(context : Context, fileUri : Uri) : HashMap<String, Int>{

        val data = getTextData(context, fileUri)
        if(data.isNotEmpty()){
            DataBaseServices.insertText(SetManagement.selectedC.tableName, data)
            return findWords(data)
        }
        return hashMapOf()
    }

    private fun getFromPdf(context : Context, fileUri : Uri) : HashMap<String, Int>{
        val data = getTextFromPdf(context, fileUri)
        if(data.isNotEmpty()){
            DataBaseServices.insertText(SetManagement.selectedC.tableName, data)
            return findWords(data)
        }
        return hashMapOf()
    }

    //endregion

    //region extract region

    private fun getTextData(context : Context, filePath : Uri) : String{
        progressDialog.changeStep(message1)
        println(">>| get from text ...")
        val inputStream =  context.contentResolver.openInputStream(filePath)
        if(inputStream != null){
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            progressDialog.makeProgress(100F)
            return String(buffer, StandardCharsets.UTF_8)
        }else{
            println("> error : file not found ${filePath.path}")
        }
        return ""
    }

    private fun getTextFromPdf(context : Context, fileUri : Uri) : String{
        progressDialog.changeStep(message2)
        try {
            val parsedText = ArrayList<String>()
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val reader = PdfReader(inputStream)
            val max = reader.numberOfPages

            for((count, item) in pages.withIndex()){
                if(item in 1..max){
                    progressDialog.makeProgress((count.toFloat() / pages.count()) * 100)
                    parsedText.add(PdfTextExtractor.getTextFromPage(reader, item))
                }
            }

            reader.close()
            return parsedText.joinToString(" ")
        } catch (e: Exception) {
            println(e.printStackTrace())
        }
        return ""
    }

    private fun getPdfPages(context : Context, fileUri : Uri) : Int{
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val reader = PdfReader(inputStream)
        return reader.numberOfPages
    }

    //endregion

    //region find and filter and insert

    private fun findWords(data : String) : HashMap<String, Int>{
        originList.clear()
        println(">>| find words ...")
        progressDialog.changeStep(message3)
        val result = HashMap<String, Int>()
        val matches = ArrayList<String>()
        val formatData = data.replace("\\s+".toRegex(), " ").toLowerCase(Locale.ENGLISH)
        val regex = "([^\\p{L}]+\$|^[^\\p{L}]+)".toRegex() ///[\p{Latin}]/
        val arr = formatData.split(" ")


        println(">>| find matches ...")
        progressDialog.changeStep(message4)
        for((count, item) in arr.withIndex()){
            val added = item.replace(regex,"")
            if(added.count() in minWordLen..maxWordLen){
                matches.add(item.replace(regex,""))
            }
            progressDialog.makeProgress((count.toFloat() / arr.count()) * 100)
        }

        println(">>| find frequency ...")
        progressDialog.changeStep(message5)
        for(item in matches){
            if(item in result.keys){
                result[item] = result[item]!!.toInt() + 1
            }else{
                originList.add(item)
                result[item] = 1
            }
        }

        return result
    }

    private fun addWordsToCollection(data : HashMap<String, Int>){
        val db = DataBaseServices.getDataBase()

        db.beginTransaction()
        try{

            //create temp table
            db.execSQL("CREATE TABLE $temp (name VARCHAR, frequency INT, createdTime BIGINT)")
            //start inserting
            println(">>| start inserting")
            progressDialog.changeStep(message6)
            insertWordsToTemp(db, data)
            insetWords()

            db.setTransactionSuccessful()
        } catch (e : java.lang.Exception){
            println(e.printStackTrace())
        } finally {
            db.execSQL("DROP TABLE $temp")
            DataBaseServices.updateCollectionModifiedTime()
            db.endTransaction()
        }
        originList.clear()
    }

    private fun insertWordsToTemp(db : SQLiteDatabase, data : HashMap<String, Int>){
        val sqlC = "INSERT INTO $temp VALUES (?, ?, ?);"
        val statementC = db.compileStatement(sqlC)

        for((i, item) in originList.withIndex()){
            val itemBase64 = item.toBase64()
            val currentTime = System.currentTimeMillis()

            statementC.bindString(1, itemBase64)
            statementC.bindLong(2, data[item]!!.toLong())
            statementC.bindLong(3, currentTime)
            statementC.executeInsert()

            progressDialog.makeProgress((i.toFloat() / data.count()) * 100)
        }
    }

    private fun insetWords(){
        val t = SetManagement.selectedC.tableName
        val tp = "temps2"
        //insert with adding frequency
        println(">>|--------------------------")
        progressDialog.changeStep("$message6(1 / 3)")
        insertIntoTable(t)
        progressDialog.changeStep("$message6(2 / 3)")
        insertIntoTable(CWords)
        progressDialog.changeStep("$message6(3 / 3)")
        DataBaseServices.executeQuery("INSERT INTO $Words ($D_name) SELECT $D_name FROM $temp EXCEPT SELECT $D_name FROM $Words;")
        println(">>|--------------------------")
    }

    private fun insertIntoTable(name : String){
        val t = "temps2"
        DataBaseServices.executeQuery("INSERT INTO $name  SELECT * FROM $temp;")
        DataBaseServices.executeQuery("CREATE TABLE $t ($D_name VARCHAR, $D_frequency INT, $D_createdTime VARCHAR);")
        DataBaseServices.executeQuery("INSERT INTO $t SELECT $D_name, SUM($D_frequency), $D_createdTime FROM $name group by $D_name;")
        DataBaseServices.executeQuery("DROP TABLE $name;")
        DataBaseServices.executeQuery("ALTER TABLE $t RENAME TO $name;")
    }

    //endregion

    //region utilities

    private fun findFileExtension(fileUri : Uri) : String{
        val arr = fileUri.path!!.split(".")
        return arr[arr.count() - 1].toLowerCase(Locale.ROOT)
    }

    //endregion

}