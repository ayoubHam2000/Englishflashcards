package com.example.englishflashcards.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Adapters.A_words
import com.example.englishflashcards.Classes.Word
import com.example.englishflashcards.Dialogs.*
import com.example.englishflashcards.Interfaces.NotifyActivity
import com.example.englishflashcards.Objects.*
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.*
import kotlinx.android.synthetic.main.activity_action_bar.*
import kotlin.concurrent.thread


class F_Words : Fragment() {


    //region vars and view

    //var
    lateinit var wordsAdapter : A_words
    private lateinit var gContext : Context
    var isAddLayoutActive = false
    private var listener : NotifyActivity? = null
    private val fileCode = 1000
    private var recyclerViewPositionState : Parcelable? = null

    //view
    private lateinit var wordsRecyclerView : RecyclerView
    private lateinit var navigator : NavController
    private lateinit var createNewWords : LinearLayout
    //add layout
    private lateinit var addBtn : ImageView
    private lateinit var addLayout : RelativeLayout
    private lateinit var addFromPdfOrText : ImageView
    private lateinit var addSimpleWord : ImageView
    private lateinit var addDirectly : ImageView
    private lateinit var addFromPdfOrTextLayout : LinearLayout
    private lateinit var addSimpleWordLayout : LinearLayout
    private lateinit var addDirectlyLayout : LinearLayout
    private lateinit var actionWords : Button


    override fun onStart() {
        super.onStart()

        val view = requireView()
        gContext = view.context
        initView(view)
        initBtn()
    }

    //endregion

    //region start

    private fun initView(view: View){
        //view
        wordsRecyclerView = view.findViewById(R.id.words_recyclerView)
        navigator = Navigation.findNavController(view)
        createNewWords = view.findViewById(R.id.t_createNewSet)
        //add Layout
        addBtn = view.findViewById(R.id.addWord)
        addLayout = view.findViewById(R.id.addLayout)
        addFromPdfOrTextLayout = view.findViewById(R.id.addFromPdfOrTextLayout)
        addFromPdfOrText = view.findViewById(R.id.addFromPdfOrText)
        addSimpleWordLayout = view.findViewById(R.id.addSimpleWordLayout)
        addSimpleWord = view.findViewById(R.id.addSimpleWord)
        addDirectlyLayout = view.findViewById(R.id.addDirectlyLayout)
        addDirectly = view.findViewById(R.id.addDirectly)
        actionWords = view.findViewById(R.id.actionWords)


        //function
        addLayout.visibility = View.INVISIBLE
        actionWords.visibility = View.INVISIBLE

        setActionBarTitle()
        WordsManagement.isCheckWords = false
        Lib.initMainActivityActionBar(activity, this)
        listener?.notifyActivity(NotifyMenu)
        initRecyclerView()
    }

    private fun setActionBarTitle(){
        if(WordsManagement.tableType == NormalWords){
            activity?.SetName?.text = SetManagement.selectedSet
        }else{
            when(WordsManagement.tableType){
                AllWords -> activity?.SetName?.text = this.getString(R.string.all_words)
                FavoriteWords -> activity?.SetName?.text = this.getString(R.string.favorite_words)
                RememberedWords -> activity?.SetName?.text = this.getString(R.string.remembered_words)
                DeletedWords -> activity?.SetName?.text = this.getString(R.string.deleted_words)
            }
        }
    }

    //endregion

    //region words recyclerView

    private fun initRecyclerView(){
        wordsAdapter = A_words(gContext){ type, w->
            adapterEvent(type, w)
        }
        wordsAdapter.changeList()
        val layoutManager = GridLayoutManager(gContext, 2)

        wordsRecyclerView.adapter = wordsAdapter
        wordsRecyclerView.layoutManager = layoutManager
    }

    private fun adapterEvent(type: String, w: Word){
        when(type){
            OpenItem -> {
                WordsManagement.word = w
                navigateToWordEditFragment()
            }
            Edit -> {
                WordsManagement.word = w
                navigateToWordEditFragment()
            }
            Delete -> {
                deleteWord(w)
            }
            Empty -> {
                endSearch()
                createNewWords.visibility = View.VISIBLE
                restoreRecyclerViewState()
            }
            NotEmpty -> {
                createNewWords.visibility = View.GONE
                restoreRecyclerViewState()
            }
            Prepare -> {
                addBtn.visibility = View.INVISIBLE
                createNewWords.visibility = View.INVISIBLE
                listener?.notifyActivity(NotifyProcessEndOrBegin, true)
            }
        }
    }

    private fun deleteWord(w : Word){
        if(WordsManagement.tableType == NormalWords){
            DataBaseServices.updateWordIsDeleted(w.name, 1, SetManagement.selectedC.tableName)
            wordsAdapter.sortedList.remove(w.name)
            wordsAdapter.list.remove(w.name)
            wordsAdapter.notifyDataSetChanged()
        }else{
            val dialog = D_AskGlobalDelete(gContext){
                listener?.notifyActivity(NotifyProcessEndOrBegin, true)
                thread {
                    val list = arrayListOf(w.name)
                    val formatList = DataBaseServices.fromListStringToString(list, true)
                    val t1 = SetManagement.selectedC.tableName
                    DataBaseServices.deleteWordsByList(t1, formatList, it)
                    if(it == AskDeleteByHide && !Settings.displayHideDeletedItems || it == AskDeleteGlobally){
                        wordsAdapter.sortedList.remove(w.name)
                        wordsAdapter.list.remove(w.name)
                    }
                    Handler(gContext.mainLooper).post {
                        wordsAdapter.notifyDataSetChanged()
                        listener?.notifyActivity(NotifyProcessEndOrBegin, false)
                    }
                }
            }
            dialog.build()
            dialog.display()
        }
    }

    private fun endSearch(){
        val theView = createNewWords.findViewById<TextView>(R.id.theText)
        if(wordsAdapter.searchedText.isEmpty()){
            if(WordsManagement.tableType == NormalWords){
                theView.text = gContext.getString(R.string.F_word_AddNewWords)
            }else{
                theView.text = gContext.getString(R.string.F_word_nothingHere)
            }
        }else{
            theView.text = gContext.getString(R.string.F_word_nothingFound)
        }
    }

    private fun restoreRecyclerViewState(){
        addBtn.visibility = if(WordsManagement.tableType != NormalWords) View.INVISIBLE else View.VISIBLE
        listener?.notifyActivity(NotifyProcessEndOrBegin, false)
        wordsRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewPositionState)
    }

    //endregion

    //region add words

    private fun initBtn(){
        addBtn.setOnClickListener {
            displayOrHideAddLayout()
        }
        addSimpleWord.setOnClickListener {
            addSimpleWord()
        }
        addFromPdfOrText.setOnClickListener {
            getFileUri()
        }
        addDirectly.setOnClickListener {
            importDirectly()
        }
        addLayout.setOnClickListener {
            displayOrHideAddLayout()
        }
    }

    private fun addAnimationToIcons(){
        val up = AnimationUtils.loadAnimation(gContext, R.anim.words_up_icone)
        val rotate = AnimationUtils.loadAnimation(gContext, R.anim.words_rotate_to_right)
        val opacity = AnimationUtils.loadAnimation(gContext, R.anim.words_to_dark)

        addSimpleWordLayout.startAnimation(up)
        addDirectlyLayout.startAnimation(up)
        addFromPdfOrTextLayout.startAnimation(up)

        rotate.fillAfter = true
        addBtn.startAnimation(rotate)
        addLayout.startAnimation(opacity)
    }

    private fun addGoneAnimation(){
        val down = AnimationUtils.loadAnimation(gContext, R.anim.words_down_icone)
        val rotate = AnimationUtils.loadAnimation(gContext, R.anim.words_rotate_to_left)
        val opacity = AnimationUtils.loadAnimation(gContext, R.anim.words_to_white)

        addSimpleWordLayout.startAnimation(down)
        addDirectlyLayout.startAnimation(down)
        addFromPdfOrTextLayout.startAnimation(down)

        rotate.fillAfter = true
        addBtn.startAnimation(rotate)
        addLayout.startAnimation(opacity)
    }

    private fun addSimpleWord(){
        var dialogName : D_Insert_Word? = null
        dialogName = D_Insert_Word(gContext){l, goToWordsEdit ->
            val isWordNotExist = DataBaseServices.insertWord(l)
            if(isWordNotExist){
                dialogName!!.dismiss()
                if(goToWordsEdit){
                    WordsManagement.word = DataBaseServices.getWord(l[0])!!
                    navigateToWordEditFragment()
                }else{
                    wordsAdapter.changeList()
                    displayOrHideAddLayout()
                }
            }else{
                Lib.showMessage(gContext, "This Word Is Already Exist")
            }
        }
        dialogName.maxChar = MaxWordNameSize
        dialogName.build()
        dialogName.display()
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
            if(resultCode == Activity.RESULT_OK){
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
                wordsAdapter.changeList()
                displayOrHideAddLayout()
            }
            mainHandler.post(myRunnable)
        }

    }

    private fun importDirectly(){
        var getTextDialog : D_editItem? = null
        getTextDialog = D_editItem(gContext){
            FileManagement.setCollectionWordsDirectly(gContext, it){
                println("complete adding words to collection")
                val mainHandler =  Handler(gContext.mainLooper)
                val myRunnable =  Runnable {
                    wordsAdapter.changeList()
                    displayOrHideAddLayout()
                    getTextDialog!!.dismiss()
                }
                mainHandler.post(myRunnable)
            }
        }
        getTextDialog.build()
        getTextDialog.display()
    }

    //endregion

    //region Action Bar

    fun actionBarMenuClick(type : Int){
        WordsManagement.isCheckWords = true
        addBtn.visibility = View.INVISIBLE
        actionWords.visibility = View.VISIBLE
        wordsAdapter.checkedList.clear()

        when(type){
            MoveToWords->{
                actionWords.text = gContext.getString(R.string.move_to)
                actionWords.setOnClickListener {
                    listNotEmpty {
                        moveToAction()
                    }
                }
            }
            CopyToWords->{
                actionWords.text = gContext.getString(R.string.copy_to)
                actionWords.setOnClickListener {
                    listNotEmpty {
                        copyToAction()
                    }
                }
            }
            DeleteWords->{
                actionWords.text = gContext.getString(R.string.deleted_words)
                actionWords.setOnClickListener {
                    listNotEmpty{
                        if(WordsManagement.tableType == NormalWords){
                            deleteAction(-1)
                        }else{
                            val dialog = D_AskGlobalDelete(gContext){
                                deleteAction(it)
                            }
                            dialog.build()
                            dialog.display()
                        }
                    }
                }
            }
            SelectAllWords, SelectRememberedWords, SelectFavoriteWords, SelectByNumber->{
                selectAction(type)
            }
            unHideDeletedItems->{
                actionWords.text = gContext.getString(R.string.unhide_items)
                actionWords.setOnClickListener{
                    listNotEmpty {
                        unHideItems()
                    }
                }

            }
            unFavorite->{
                actionWords.text = gContext.getString(R.string.make_as_not_favorite)
                actionWords.setOnClickListener{
                    listNotEmpty {
                        unFavorite()
                    }
                }
            }
            unRemember->{
                actionWords.text = gContext.getString(R.string.make_as_not_remember)
                actionWords.setOnClickListener{
                    listNotEmpty {
                        unRemember()
                    }
                }
            }
        }

        notifyWordAdapter()
    }

    private fun moveToAction(){
        val dialog = D_AskForTargetCollection(gContext){
            if(it != null){
                listener?.notifyActivity(NotifyProcessEndOrBegin, true)
                thread {
                    val list = wordsAdapter.checkedList
                    val isAllSelected = list.count() == wordsAdapter.list.count()
                    val formatList = if(isAllSelected) "()" else
                        DataBaseServices.fromListStringToString(list.keys.toList(), true)
                    val t1 = SetManagement.selectedC.tableName
                    val t2 = it.tableName
                    DataBaseServices.copyWordsTo(t1, t2, formatList, isAllSelected)

                    if(WordsManagement.tableType == NormalWords){
                        DataBaseServices.deleteWordsByList(t1, formatList, -1, isAllSelected)
                    }else{
                        DataBaseServices.deleteWordsByList(t1, formatList, RemoveFromDeleteList, isAllSelected)
                    }
                    WordsManagement.isCheckWords = false
                    wordsAdapter.deleteItems()
                    Handler(gContext.mainLooper).post { endAction() }
                }
            }else{
                Lib.showMessage(gContext, "There Is No List To Move To!!")
            }
        }
        dialog.build()
        dialog.display()
    }

    private fun copyToAction(){
        val dialog = D_AskForTargetCollection(gContext){
            if(it != null){
                listener?.notifyActivity(NotifyProcessEndOrBegin, true)
                thread {
                    val list = wordsAdapter.checkedList
                    val isAllSelected = list.count() == wordsAdapter.list.count()
                    val formatList = if(isAllSelected) "()" else
                        DataBaseServices.fromListStringToString(list.keys.toList(), true)
                    val t1 = SetManagement.selectedC.tableName
                    val t2 = it.tableName
                    DataBaseServices.copyWordsTo(t1, t2, formatList, isAllSelected)
                    WordsManagement.isCheckWords = false
                    Handler(gContext.mainLooper).post { endAction() }
                }
            }else{
                Lib.showMessage(gContext, "There Is No List To Copy To!!")
            }
        }
        dialog.build()
        dialog.display()
    }

    private fun deleteAction(type : Int){
        listener?.notifyActivity(NotifyProcessEndOrBegin, true)
        thread {
            val list = wordsAdapter.checkedList
            val isAllSelected = list.count() == wordsAdapter.list.count()
            val t1 = SetManagement.selectedC.tableName
            val formatList = if(isAllSelected) "()" else
                DataBaseServices.fromListStringToString(list.keys.toList(), true)
            DataBaseServices.deleteWordsByList(t1, formatList, type, isAllSelected)
            WordsManagement.isCheckWords = false
            wordsAdapter.deleteItems()
            Handler(gContext.mainLooper).post {
                wordsAdapter.changeList()
                endAction()
            }
        }
    }

    private fun unHideItems(){
        listener?.notifyActivity(NotifyProcessEndOrBegin, true)
        thread {
            val list = wordsAdapter.checkedList
            val isAllSelected = list.count() == wordsAdapter.list.count()
            val formatList = if(isAllSelected) "()" else
                DataBaseServices.fromListStringToString(list.keys.toList(), true)
            DataBaseServices.unHideDeletedItems(formatList, isAllSelected)
            WordsManagement.isCheckWords = false
            Handler(gContext.mainLooper).post {
                wordsAdapter.changeList()
                endAction()
            }
        }
    }

    private fun unFavorite(){
        listener?.notifyActivity(NotifyProcessEndOrBegin, true)
        thread {
            val list = wordsAdapter.checkedList
            val isAllSelected = list.count() == wordsAdapter.list.count()
            val formatList = if(isAllSelected) "()" else
                DataBaseServices.fromListStringToString(list.keys.toList(), true)
            DataBaseServices.unFavoriteItems(formatList, isAllSelected)
            WordsManagement.isCheckWords = false
            wordsAdapter.deleteItems()
            Handler(gContext.mainLooper).post {
                wordsAdapter.changeList()
                endAction()
            }
        }
    }

    private fun unRemember(){
        listener?.notifyActivity(NotifyProcessEndOrBegin, true)
        thread {
            val list = wordsAdapter.checkedList
            val isAllSelected = list.count() == wordsAdapter.list.count()
            val formatList = if(isAllSelected) "()" else
                DataBaseServices.fromListStringToString(list.keys.toList(), true)
            DataBaseServices.unRememberItems(formatList, isAllSelected)
            WordsManagement.isCheckWords = false
            wordsAdapter.deleteItems()
            Handler(gContext.mainLooper).post {
                wordsAdapter.changeList()
                endAction()
            }
        }
    }

    private fun endAction(){
        //used from moveToAction, copyToAction
        notifyWordAdapter(true)
        listener?.notifyActivity(NotifyProcessEndOrBegin, false)
        listener?.notifyActivity(NotifyMenu)
        Lib.showMessage(gContext, "Operation Done Successfully")
    }

    private fun listNotEmpty(event : () -> Unit){
        if(wordsAdapter.checkedList.isEmpty()){
            //WordsManagement.isCheckWords = false
            //notifyWordAdapter(true)
            Lib.showMessage(gContext, "No Selected Items!!")
        }else{
            event()
        }
    }

    private fun selectAction(type : Int){
        wordsAdapter.typeCheck = type
        wordsAdapter.makeItCheck()
        wordsAdapter.notifyDataSetChanged()
    }

    //endregion

    //region Other

    fun displayOrHideAddLayout(){
        isAddLayoutActive = !isAddLayoutActive
        if(isAddLayoutActive){
            //TODO : animate add button
            addLayout.visibility = View.VISIBLE
            addAnimationToIcons()
        }else{
            addGoneAnimation()
            addLayout.visibility = View.INVISIBLE
        }
    }

    fun refreshFragment(){
        recyclerViewPositionState = wordsRecyclerView.layoutManager?.onSaveInstanceState()
        wordsAdapter.changeList()
    }

    fun refreshFragment(name : String){
        wordsAdapter.isSearchClick = true
        wordsAdapter.searchedText = name
        wordsAdapter.changeList()
    }

    fun notifyWordAdapter(endAction : Boolean = false){
        addBtn.visibility = View.INVISIBLE
        actionWords.visibility = View.VISIBLE
        if(endAction && WordsManagement.tableType == NormalWords) addBtn.visibility = View.VISIBLE
        if(endAction) actionWords.visibility = View.INVISIBLE
        wordsAdapter.notifyDataSetChanged()
    }

    private fun navigateToWordEditFragment(){
        recyclerViewPositionState = wordsRecyclerView.layoutManager?.onSaveInstanceState()
        navigator.navigate(R.id.action_f_Words_to_f_WordEdit)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? NotifyActivity
        if (listener == null) {
            throw ClassCastException("$context must implement OnArticleSelectedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.f__words, container, false)
    }

    //endregion

}