package com.example.englishflashcards.Fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Adapters.A_wordEdit
import com.example.englishflashcards.Classes.Word
import com.example.englishflashcards.Dialogs.D_editItem
import com.example.englishflashcards.Objects.*
import com.example.englishflashcards.R
import kotlinx.android.synthetic.main.activity_action_bar.*


class F_WordEdit : Fragment() {

    //region init
    //var
    private lateinit var wordEditAdapter : A_wordEdit
    private lateinit var gContext : Context
    private lateinit var editDialog : D_editItem
    private lateinit var popUpMenu : PopupMenu

    //view
    private lateinit var wordEditRecyclerView : RecyclerView
    private lateinit var wordName : TextView
    private lateinit var originName : TextView
    private lateinit var typeName : TextView
    private lateinit var edit1 : ImageView
    private lateinit var edit2 : ImageView
    private lateinit var edit3 : ImageView



    override fun onStart() {
        super.onStart()
        val view = requireView()
        gContext = view.context

        initView(view)
    }

    private fun initView(view: View){
        //view
        wordEditRecyclerView = view.findViewById(R.id.wordEditRecyclerView)
        wordName = view.findViewById(R.id.wordName)
        originName = view.findViewById(R.id.originName)
        typeName = view.findViewById(R.id.typeName)
        edit1 = view.findViewById(R.id.edit1)
        edit2 = view.findViewById(R.id.edit2)
        edit3 = view.findViewById(R.id.edit3)

        val theWord = WordsManagement.word!!

        //Set
        wordName.text = theWord.name
        originName.text = if(theWord.origin.isEmpty()) theWord.name else theWord.origin
        typeName.text = if(theWord.type.isEmpty()) "Not Selected" else theWord.type
        popUpMenu = Lib.initPopupMenu(gContext, edit3, R.menu.m_word_type)
        //functions
        activity?.SetName?.text = theWord.name
        Lib.initMainActivityActionBar(activity, this)
        initBtn()
        initRecyclerView(theWord)
    }

    //endregion

    //region functions

    private fun initBtn(){
        val theWord = WordsManagement.word!!


        edit1.setOnClickListener {
            getName(theWord.name){
                if(DataBaseServices.isWordNotExist(it, SetManagement.selectedC.tableName)){
                    DataBaseServices.updateWordName(theWord.name, it)
                    editWordOnPracticeFrag(theWord.name, it)
                    theWord.name = it
                    wordName.text = it
                    originName.text = if(theWord.origin.isEmpty()) theWord.name else theWord.origin
                    editDialog.dismiss()
                }else{
                    if(it == theWord.name){
                        editDialog.dismiss()
                    }else{
                        Lib.showMessage(gContext, "Word is already exist")
                    }

                }
            }
        }

        edit2.setOnClickListener {
            getName(theWord.name){
                DataBaseServices.updateWordOrigin(theWord.name, it)
                theWord.origin = it
                originName.text = it
                editDialog.dismiss()
            }
        }

        popUpMenu.setOnMenuItemClickListener {
            changeType(theWord, it.title.toString())
            true
        }

    }

    private fun changeType(theWord: Word, type: String){
        DataBaseServices.updateWordType(theWord.name, type)
        theWord.type = type
        typeName.text = type
    }

    private fun getName(name: String, event: (String) -> Unit){
        editDialog = D_editItem(gContext){
            event(it)
        }
        editDialog.textInput = name
        editDialog.build()
        editDialog.display()
    }

    private fun editWordOnPracticeFrag(oldName : String, newName : String){
        if(PracticeManager.onEditFromPractice){
            val pos = PracticeManager.practiceWords.indexOf(oldName)
            PracticeManager.practiceWords[pos] = newName
        }
    }

    //endregion

    //region recycler view

    private fun initRecyclerView(theWord: Word){
        wordEditAdapter = A_wordEdit(gContext, theWord)

        val layoutManager = LinearLayoutManager(gContext)
        wordEditRecyclerView.layoutManager = layoutManager

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(wordEditRecyclerView)

        wordEditRecyclerView.adapter = wordEditAdapter
    }


    private val itemTouchHelperCallback = object :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val theWord = WordsManagement.word!!
            val posForDefinition = viewHolder.adapterPosition - 1 //from definitions
            val posForExamples = posForDefinition - 1 - theWord.definition.count()
            if(posForDefinition < theWord.definition.count()){
                removeDefinition(theWord.definition[posForDefinition])
                theWord.definition.removeAt(posForDefinition)
                println("definition removed $posForDefinition")
            }else{
                removeExample(theWord.examples[posForExamples])
                theWord.examples.removeAt(posForExamples)
                println("example removed $posForExamples")
            }
            wordEditAdapter.notifyDataSetChanged()
        }

        override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            if (viewHolder is A_wordEdit.ViewHolder1 || viewHolder is A_wordEdit.ViewHolder2) return 0
            return super.getSwipeDirs(recyclerView, viewHolder)
        }

    }

    private fun removeExample(name: String){
        DataBaseServices.deleteExample(WordsManagement.word!!.name, name)
    }

    private fun removeDefinition(name: String){
        DataBaseServices.deleteDefinition(WordsManagement.word!!.name, name)
    }

    //endregion

    //region utilities
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.f_word_edit, container, false)
    }

    //endregion

}