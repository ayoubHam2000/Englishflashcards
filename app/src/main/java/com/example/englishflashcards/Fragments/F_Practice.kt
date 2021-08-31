package com.example.englishflashcards.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.englishflashcards.Adapters.A_PageAdapter
import com.example.englishflashcards.Classes.Costum.MyViewPage
import com.example.englishflashcards.Objects.Lib
import com.example.englishflashcards.Objects.PracticeManager
import com.example.englishflashcards.Objects.SetManagement
import com.example.englishflashcards.Objects.WordsManagement
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.Edit
import com.example.englishflashcards.Utilities.Next
import com.example.englishflashcards.Utilities.Practice


class F_Practice : Fragment() {

    //region init
    //var
    lateinit var gContext : Context
    lateinit var pageAdapter : A_PageAdapter
    lateinit var viewPager : MyViewPage

    //view
    lateinit var navigator : NavController

    override fun onStart() {
        super.onStart()

        val view = requireView()
        gContext = view.context
        initView(view)
    }

    private fun initView(view: View){
        //view
        viewPager = view.findViewById(R.id.frameView)
        navigator = Navigation.findNavController(view)

        //functions
        Lib.initMainActivityActionBar(activity, this)
        initViewPager()
    }

    //endregion

    private fun initViewPager(){
        pageAdapter = A_PageAdapter(gContext){ eventName, pos->
            when(eventName){
                Next -> {
                    if(pos == PracticeManager.practiceWords.count()){
                        navigateToEndPractice()
                    }
                    viewPager.setCurrentItem(pos, true)
                }
                Edit -> {
                    WordsManagement.word = pageAdapter.getCurrentWord(pos)
                    navigateToEditWordFragment()
                }
            }
        }

        pageAdapter.changeList()
        viewPager.adapter = pageAdapter
    }

    //region utilities
    private fun navigateToEditWordFragment(){
        val anim = Lib.animFragment(R.anim.enter_frag, R.anim.exit_frag)
        navigator.navigate(R.id.action_f_Practice2_to_f_WordEdit, null, anim.build())
    }

    private fun navigateToEndPractice(){
        val anim = Lib.animFragment(R.anim.enter_frag, R.anim.exit_frag)
        navigator.navigate(R.id.action_f_Practice2_to_f_EndPractice, null, anim.build())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return  inflater.inflate(R.layout.f_practice, container, false)
    }

    //endregion



}