package com.example.englishflashcards.Fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Adapters.A_collection
import com.example.englishflashcards.Classes.Collection
import com.example.englishflashcards.Dialogs.D_Insert_Word
import com.example.englishflashcards.Dialogs.D_ask
import com.example.englishflashcards.Dialogs.D_editItem
import com.example.englishflashcards.Interfaces.NotifyActivity
import com.example.englishflashcards.Objects.*
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.*
import kotlin.concurrent.thread


class F_Collections : Fragment() {


    //region vars and view
    //var
    private lateinit var collectionAdapter : A_collection
    private lateinit var gContext : Context
    private val fileCode = 1000


    //view
    lateinit var actionCollection : Button
    private var listener : NotifyActivity? = null
    private lateinit var navController : NavController
    private lateinit var recyclerView : RecyclerView
    private lateinit var addCollection : Button
    private lateinit var createNewSetHint : LinearLayout
    private lateinit var dialogEditItem : D_editItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.f_collection_names, container, false)
    }
    //endregion

    //region init

    override fun onStart() {
        super.onStart()

        initVar(requireView())
        intButton()
    }

    private fun initVar(view: View){
        //var
        gContext = view.context

        //view
        navController = Navigation.findNavController(view)
        recyclerView = view.findViewById(R.id.r_CollectionNames)
        addCollection = view.findViewById(R.id.addCollection)
        createNewSetHint = view.findViewById(R.id.t_createNewSet)
        actionCollection = view.findViewById(R.id.actionCollection)

        //initView
        SetManagement.isCheckCollection = false //in the start of the fragment any action should not be active
        Lib.initMainActivityActionBar(activity, this)
        listener?.notifyActivity(NotifyMenu)
        initRecyclerView()
        refreshFragment()
    }

    private fun intButton(){
        addCollection.setOnClickListener { addCollectionClick() }
        actionCollection.setOnClickListener {
            if(countCheckedItems() != 0){
                actionCollectionClick()
            }else{
                Lib.showMessage(gContext, "No Selected Items!!")
            }

        }
    }

    private fun addCollectionClick(){
        createNewCollection()
    }

    //endregion

    //region collection recyclerView
    private fun initRecyclerView(){
        collectionAdapter = A_collection(gContext){ event, c ->
            recyclerViewEvent(event,  c)
        }
        val layoutManager = LinearLayoutManager(gContext)
        collectionAdapter.changeList()

        recyclerView.adapter = collectionAdapter
        recyclerView.layoutManager = layoutManager
    }

    private fun recyclerViewEvent(event: String, c : Collection?){
        if(c != null){SetManagement.selectedC = c}
        when(event){
            Edit -> {
                editCollectionName()
            }
            Import -> {
                //getFilePath()
                importFile()
            }
            ImportDirectly->{
                importDirectly()
            }
            AddWord->{
                addWordDirectly()
            }
            Hide->{
                hideOrShowCollection()
            }
            Delete -> {
                val ask = D_ask(gContext, "ARE YOU SURE ?"){
                    if(it){
                        DataBaseServices.deleteCollection(c!!)
                        collectionAdapter.changeList()
                    }
                }
                ask.build()
                ask.display()
            }
            OpenItem -> {
                navigateToWordFragment()
                println("info openItem")
            }
            Practice -> {
                if(DataBaseServices.getCollectionWordsCount(c!!.tableName) == 0){
                    Lib.showMessage(gContext, "Empty Collection")
                }else{
                    DataBaseServices.updateCollectionLastView()
                    navigateToPracticeFragment()
                }
                println("practice")
            }
            NotEmpty -> {
                createNewSetHint.visibility = View.GONE
            }
            Empty -> {
                createNewSetHint.visibility = View.VISIBLE
            }
        }
    }

    private fun createNewCollection(){
        val setName = SetManagement.selectedSet
        dialogEditItem = D_editItem(gContext){
            when {
                it.count() > MaxCollectionName -> {
                    Lib.showMessage(gContext, "Max character is $MaxCollectionName")
                }
                DataBaseServices.isCollectionNotExist(setName, it) -> {
                    DataBaseServices.insertCollection(it, System.currentTimeMillis(), setName)
                    collectionAdapter.changeList()
                    dialogEditItem.dismiss()
                }
                else -> {
                    Lib.showMessage(gContext, "This name is Taken")
                }
            }
        }
        dialogEditItem.maxChar = MaxCollectionName
        dialogEditItem.build()
        dialogEditItem.display()
    }

    private fun editCollectionName(){
        val c = SetManagement.selectedC
        dialogEditItem = D_editItem(gContext){
            when {
                it.count() > MaxCollectionName -> {
                    Lib.showMessage(gContext, "Max character is $MaxCollectionName")
                }
                DataBaseServices.isCollectionNotExist(c.father, it) -> {
                    DataBaseServices.updateCollection(SetManagement.selectedC, it)
                    collectionAdapter.changeList()
                    dialogEditItem.dismiss()
                }
                else -> {
                    Lib.showMessage(gContext, "This name is Taken")
                }
            }
        }
        dialogEditItem.textInput = c.name
        dialogEditItem.build()
        dialogEditItem.display()
    }

    private fun hideOrShowCollection(){
        val s = SetManagement.selectedC.father
        val c = SetManagement.selectedC.name
        if(DataBaseServices.isCollectionHide(s, c)){
            DataBaseServices.updateCollectionHide(s, c, 0)
        }else{
            DataBaseServices.updateCollectionHide(s, c, 1)
        }
        collectionAdapter.changeList()
    }

    private fun addWordDirectly(){
        var getTextDialog : D_Insert_Word? = null
        getTextDialog = D_Insert_Word(gContext){l, goToWordsEdit ->
            val isWordNotExist = DataBaseServices.insertWord(l)
            if(isWordNotExist){
                getTextDialog!!.dismiss()
                if(goToWordsEdit){
                    WordsManagement.word = DataBaseServices.getWord(l[0])!!
                    navigateToWordEditFragment()
                }else{
                    collectionAdapter.changeList()
                }
            }else{
                Lib.showMessage(gContext, "This Word Is Already Exist")
            }
        }
        getTextDialog.maxChar = MaxWordNameSize
        getTextDialog.build()
        getTextDialog.display()
    }

    //endregion

    //region file Manager
    private fun importFile(){
        getFileUri()
    }

    private fun importDirectly(){
        var getTextDialog : D_editItem? = null
        getTextDialog = D_editItem(gContext){
            FileManagement.setCollectionWordsDirectly(gContext, it){
                println("complete adding words to collection")
                val mainHandler =  Handler(gContext.mainLooper)
                val myRunnable =  Runnable {
                    collectionAdapter.changeList()
                    getTextDialog!!.dismiss()
                }
                mainHandler.post(myRunnable)
            }
        }
        getTextDialog.build()
        getTextDialog.display()
    }

    private fun getFileUri(){
        if(Lib.isStoragePermissionGranted(gContext, this)){
            val intent = Intent()
            intent.type = "*/*"
            intent.action = Intent.ACTION_GET_CONTENT
            val mimetypes = arrayOf("text/plain", "application/pdf")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
            startActivityForResult(intent, fileCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == fileCode){
            if(resultCode == RESULT_OK){
                val filePath = data?.data?.path
                if(filePath != null){
                    importFile(data.data!!)
                }else{
                    Lib.showMessage(gContext, "Something went wrong")
                    Log.d("ERROR", "filePath = NULL")
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun importFile(filePath: Uri){

        FileManagement.setCollectionWords(gContext, filePath){
            println("complete adding words to collection")
            val mainHandler =  Handler(gContext.mainLooper)
            val myRunnable =  Runnable {
                collectionAdapter.changeList()
            }
            mainHandler.post(myRunnable)
        }

    }
    //endregion

    //region action Bar actions
    private fun actionCollectionClick(){
        SetManagement.isCheckCollection = false
        when(SetManagement.collectionActionType){
            MergeCollection-> mergeCollections()
            CollectionMoveTo->moveCollections()
            DeleteCollections->deleteCollections()
        }
    }

    private fun mergeCollections(){
        if(!isMergeAllowed()) return
        //if only isMergeAllowed = true
        listener?.notifyActivity(NotifyProcessEndOrBegin, true)
        val list = collectionAdapter.list
        thread {
            val selectedItem = DataBaseServices.mergeCollectionTables(list)
            Handler(gContext.mainLooper).post {
                SetManagement.isCheckCollection = false
                Lib.showMessage(gContext, "${countCheckedItems() - 1} Items Merged Into $selectedItem")
                listener?.notifyActivity(NotifyProcessEndOrBegin, false)
                listener?.notifyActivity(NotifyActionEnd)
                refreshFragment()
            }
        }
    }

    private fun moveCollections(){
        SetManagement.isCheckCollection = true //we turn it off later in moveCollectionTo
        listener?.notifyActivity(NotifyMoveCollectionToSet)
        //resume activity -> this Fragment : moveCollectionTo()
    }

    fun moveCollectionTo(selectedSet : String){
        //From activity
        SetManagement.isCheckCollection = false
        val isThereIsNotAllowed = DataBaseServices.collectionMoveToSet(collectionAdapter.list, selectedSet)
        if(isThereIsNotAllowed){
            Lib.showMessage(gContext, "${countCheckedItems()} Moved Into $selectedSet")
        }else{
            Lib.showMessage(gContext, "Somme collections Names Are Exist In $selectedSet ")
            val nextMessage = "${countCheckedItems()} Moved Into $selectedSet"
            Handler(gContext.mainLooper).postDelayed(Runnable {
                Lib.showMessage(gContext, nextMessage)
            }, 2000)
        }
        listener?.notifyActivity(NotifyActionEnd)
        refreshFragment()
    }

    private fun deleteCollections(){
        DataBaseServices.deleteMultipleCollections(collectionAdapter.list)
        Lib.showMessage(gContext, "${countCheckedItems()} Items are Deleted")
        listener?.notifyActivity(NotifyActionEnd)
        refreshFragment()
    }

    fun checkAllCollectionA(){
        //Form Activity
        for(item in collectionAdapter.list){
            item.isChecked = true
        }
        collectionAdapter.notifyDataSetChanged()
    }

    private fun isMergeAllowed() : Boolean{
        var checked = 0
        var selected = 0
        for(item in collectionAdapter.list){
            if(item.isChecked) checked++
            if(item.isSelected) selected++
        }
        if(checked <= 1) Lib.showMessage(gContext, "It must be more than one checked items")
        if(selected != 1) Lib.showMessage(gContext, "It has to be a 1 selected item")
        SetManagement.isCheckCollection = true
        return checked > 1 && selected == 1
    }

    private fun countCheckedItems() : Int{
        var r = 0
        for(item in collectionAdapter.list)
            if(item.isChecked)
                r++
        return r
    }

    //endregion

    //region related with fragment (listener, refresh fragment, navigation)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? NotifyActivity
        if (listener == null) {
            throw ClassCastException("$context must implement OnArticleSelectedListener")
        }
    }

    fun refreshFragment(){
        collectionAdapter.changeList()
        if(SetManagement.isCheckCollection){
            actionCollection.visibility = View.VISIBLE
            addCollection.visibility = View.INVISIBLE
        }else{
            addCollection.visibility = View.VISIBLE
            actionCollection.visibility = View.INVISIBLE
        }
    }

    private fun navigateToWordFragment(){
        // val bundle = bundleOf(T_CollectionName to name)
        navController.navigate(R.id.action_blankFragment_to_f_Words)
    }

    private fun navigateToPracticeFragment(){
        navController.navigate(R.id.action_blankFragment_to_practiceChunks2)
    }

    private fun navigateToWordEditFragment(){
        navController.navigate(R.id.action_blankFragment_to_f_WordEdit)
    }

    //endregion


}