package com.example.englishflashcards.Fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Adapters.A_practiceChunks
import com.example.englishflashcards.Classes.Word
import com.example.englishflashcards.Objects.DataBaseServices
import com.example.englishflashcards.Objects.Lib
import com.example.englishflashcards.Objects.PracticeManager
import com.example.englishflashcards.Objects.WordsManagement
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.Complete
import com.example.englishflashcards.Utilities.Practice
import com.example.englishflashcards.Utilities.Reset


class F_PracticeChunks : Fragment() {

    //region init
    private lateinit var gContext : Context
    private lateinit var practiceChunksAdapter : A_practiceChunks
    private var recyclerViewPositionState : Parcelable? = null

    //view
    private lateinit var recyclerView : RecyclerView
    private lateinit var navController : NavController
    private lateinit var progressChunk : ProgressBar





    override fun onStart() {
        super.onStart()
        val view = requireView()
        gContext = view.context
        intView(view)

    }

    private fun intView(view: View){
        recyclerView = view.findViewById(R.id.practiceChunksRecyclerView)
        navController = Navigation.findNavController(view)
        progressChunk = view.findViewById(R.id.progressChunk)
        //fun
        Lib.initMainActivityActionBar(activity, this)
        initRecyclerView()
    }

    //endregion

    //region initRecyclerView

    private fun initRecyclerView(){
        practiceChunksAdapter = A_practiceChunks(gContext){ event, pWords, pos->
            recyclerViewEvent(event, pWords, pos)
        }
        val gridLayoutManager = GridLayoutManager(gContext, 2)

        practiceChunksAdapter.changeList()
        recyclerView.adapter = practiceChunksAdapter
        recyclerView.layoutManager = gridLayoutManager
    }

    private fun recyclerViewEvent(event: String, practiceWords: ArrayList<String>, position : Int){
        when(event){
            Practice -> {
                PracticeManager.practiceWords = practiceWords
                navigateToPracticeFragment()
            }

            Reset -> {
                Lib.showMessage(gContext, "Long press action")
                //PracticeManager.practiceWords = practiceWords
                //navigateToPracticeFragment()
            }

            Complete -> {
                recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewPositionState)
                progressChunk.visibility = View.INVISIBLE
            }

        }

    }


    private fun navigateToPracticeFragment(){
        recyclerViewPositionState = recyclerView.layoutManager?.onSaveInstanceState()
        navController.navigate(R.id.action_practiceChunks_to_f_Practice2)
    }

    //endregion

    //region utilities

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.f_practice_chunks, container, false)
    }

    //endregion

}