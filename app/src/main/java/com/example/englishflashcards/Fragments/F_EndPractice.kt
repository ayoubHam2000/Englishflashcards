package com.example.englishflashcards.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.englishflashcards.R


class F_EndPractice : Fragment() {

    lateinit var gContext : Context

    //view
    private lateinit var navController : NavController
    private lateinit var buttonTest : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.f_end_practice, container, false)
    }


    override fun onStart() {
        super.onStart()

        gContext = requireView().context
        initView(requireView())
    }

    private fun initView(view: View){
        //view
        navController = Navigation.findNavController(view)
        buttonTest = view.findViewById(R.id.buttonTest)

        //functions
        buttonTest.setOnClickListener {
            navigateToPracticeFragment()
        }

    }

    private fun navigateToPracticeFragment(){
        // val bundle = bundleOf(T_CollectionName to name)
        navController.navigate(R.id.action_f_EndPractice_to_practiceChunks)
    }

}