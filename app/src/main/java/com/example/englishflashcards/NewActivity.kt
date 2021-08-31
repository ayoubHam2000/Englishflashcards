package com.example.englishflashcards

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.Base64
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.englishflashcards.Objects.DataBaseServices
import com.example.englishflashcards.Objects.FileManagement
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class NewActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new)
    }

}