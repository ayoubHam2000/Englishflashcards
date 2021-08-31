package com.example.englishflashcards


import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishflashcards.Adapters.A_setItem
import com.example.englishflashcards.Classes.Collection
import com.example.englishflashcards.Dialogs.*
import com.example.englishflashcards.Fragments.F_Collections
import com.example.englishflashcards.Fragments.F_Words
import com.example.englishflashcards.Interfaces.NotifyActivity
import com.example.englishflashcards.Objects.*
import com.example.englishflashcards.Utilities.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_action_bar.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_slide_bar.*
import kotlin.concurrent.thread
import kotlin.random.Random


class MainActivity : AppCompatActivity(), NotifyActivity{

    //region vars, init, view
    //region vars
    lateinit var dialogEditItem : D_editItem
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var navigationButton : ImageView
    private lateinit var navController : NavController
    private lateinit var setItemAdapter : A_setItem

    //popUpMenu
    private lateinit var popUpMenu: PopupMenu
    private var isOnProcess = false
    //endregion

    //region init Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        initFun()
    }

    override fun onStart() {
        super.onStart()
        initAfterOnStart()
    }

    private fun initFun(){
        initDataBase()
        //DataBaseServices.clearAllDataBase()
        Settings.initSort()
        Settings.setScreenDimension(this)
    }

    private fun initAfterOnStart(){
        initSideBarMenu()
        initActionBar()
    }

    private fun initActionBar(){
        when(getFragment()){
            is F_Words -> menuForWordsFragment()
            is F_Collections -> menuForCollectionFragment()
        }
    }

    private fun initDataBase(){
        //this for add the first item in the set list
        DataBaseServices.initDataBase(this)
        DataBaseServices.loadData(this){
            if(it){
                beginOrEndProgressProcess(false)
            }else{
                beginOrEndProgressProcess(true)
            }
        }
    }


    //endregion
    //endregion

    //region SlideBar
    private fun initSideBarMenu(){
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationButton = findViewById(R.id.navigationButton)
        navigationView = findViewById(R.id.navigationView)
        navController = Navigation.findNavController(this, R.id.fragment)

        navigationButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
            drawerLayout.addDrawerListener(object : DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
                override fun onDrawerOpened(drawerView: View) {
                    refreshWordsItems()
                }

                override fun onDrawerClosed(drawerView: View) {}
                override fun onDrawerStateChanged(newState: Int) {}
            })
        }

        NavigationUI.setupWithNavController(navigationView, navController)
        initWordsItems()
        initSetList()
        initCollapse()
    }

    private fun initCollapse(){
        openCloseCategories.setOnClickListener {
            categorySection.visibility = if(categorySection.visibility == View.GONE) {
                DataBaseServices.updateVar(OpenCloseCategories, true.toString())
                openCloseCategoriesImg.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_24)
                View.VISIBLE
            }else {
                DataBaseServices.updateVar(OpenCloseCategories, false.toString())
                openCloseCategoriesImg.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_24)
                View.GONE
            }


        }

        openCloseCollection.setOnClickListener {
            collectionSection.visibility = if(collectionSection.visibility == View.GONE) {
                DataBaseServices.updateVar(OpenCloseCollections, true.toString())
                openCloseCollectionsImg.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_24)
                View.VISIBLE
            } else {
                DataBaseServices.updateVar(OpenCloseCollections, false.toString())
                openCloseCollectionsImg.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_24)
                View.GONE
            }

        }

        DataBaseServices.getOrInsertVar(OpenCloseCategories, false){
            categorySection.visibility = if(it.toBoolean()){ View.GONE }else{ View.VISIBLE }
            openCloseCategories.callOnClick()
        }
        DataBaseServices.getOrInsertVar(OpenCloseCollections, false){
            collectionSection.visibility = if(it.toBoolean()){ View.GONE }else{ View.VISIBLE }
            openCloseCollection.callOnClick()
        }
    }

    //region wordsItems in Slide Bar
    private fun initWordsItems(){
        refreshWordsItems()


        allWords.setOnClickListener {
            WordsManagement.tableType = AllWords
            openWordsFrag()
            SetName.text = this.getString(R.string.all_words)
        }
        allFavorite.setOnClickListener {
            WordsManagement.tableType = FavoriteWords
            openWordsFrag()
            SetName.text = this.getString(R.string.favorite_words)
        }
        allRemembered.setOnClickListener {
            WordsManagement.tableType = RememberedWords
            openWordsFrag()
            SetName.text = this.getString(R.string.remembered_words)
        }
        allDeleted.setOnClickListener {
            WordsManagement.tableType = DeletedWords
            openWordsFrag()
            SetName.text = this.getString(R.string.deleted_words)
        }

    }

    private fun openWordsFrag(){
        SetManagement.selectedC = Collection("", 0, "words", CWords)
        navController.popBackStack(R.id.blankFragment, false) //back to collectionFrag
        navController.navigate(R.id.action_blankFragment_to_f_Words)
        drawerLayout.closeDrawer(GravityCompat.START)
        setItemAdapter.notifyDataSetChanged()
    }

    private fun refreshWordsItems(){
        thread {
            val test = DataBaseServices.tableCountByQuery("SELECT $D_name FROM $CWords")
            val totalWords = DataBaseServices.tableCountByQuery("SELECT $D_name FROM $Words")
            val deletedWords = DataBaseServices.tableCountByQuery("SELECT $D_name FROM $Words WHERE $D_isDeleted = 1")
            val notDeletedWords = totalWords - deletedWords
            val favoriteWords = DataBaseServices.tableCountByQuery("SELECT $D_name FROM $Words WHERE $D_isFavorite = 1 AND $D_isDeleted = 0")
            val rememberedWords = DataBaseServices.tableCountByQuery("SELECT $D_name FROM $Words WHERE $D_isRemember = 1 AND $D_isDeleted = 0")
            Handler(this.mainLooper).post{
                allWordsNbr.text = notDeletedWords.toString()
                allFavoriteNbr.text = favoriteWords.toString()
                allRememberedNbr.text = rememberedWords.toString()
                allDeletedNbr.text = deletedWords.toString()
                if(test != totalWords) Lib.showMessage(this, "Error CWords and Words Not Match")
            }
        }

    }

    //endregion

    //region setRecyclerView
    private fun initSetList(){
        setUpRecyclerView(set_RecyclerView)
        add_Set.setOnClickListener {
            createNewSet()
            println("Create Set")
        }
        val fragment = getFragment()
        if(fragment is F_Collections) {
            SetName.text = SetManagement.selectedSet
            fragment.refreshFragment()
        }
    }

    private fun setUpRecyclerView(r: RecyclerView){
        setItemAdapter = A_setItem(this){ event, selectedItem ->
            recyclerViewEvent(event, selectedItem)
        }
        setItemAdapter.changeList()
        val linearLayoutMange = LinearLayoutManager(this)

        r.adapter = setItemAdapter
        r.layoutManager = linearLayoutMange
    }

    private fun recyclerViewEvent(event: String, selectedItem: String){
        when(event){
            OpenItem -> {
                openSet(selectedItem)
                println(">>| OpenSetItem $selectedItem")
            }

            Edit -> {
                editSet(selectedItem)
                println(">>| EditSet $selectedItem")
            }

            ChangeColor -> {
                setChangeColor(selectedItem)
                println(">>| ChangeSetColor $selectedItem")
            }

            Delete -> {
                deleteSet(selectedItem)
                println(">>| deleteSet $selectedItem")
            }

            MoveToAction -> {
                moveCollectionToAction(selectedItem)
                println(">>| MoveToAction $selectedItem")
            }
        }
    }

    private fun openSet(selectedItem: String){
        SetManagement.isCheckCollection = false //in the start of the fragment any action should not be active
        SetManagement.selectedSet = selectedItem
        SetName.text = selectedItem
        navController.popBackStack(R.id.blankFragment, false) //back to collectionFrag
        val fragment = getFragment()
        if(fragment is F_Collections) fragment.refreshFragment()
        drawerLayout.closeDrawer(GravityCompat.START)
        WordsManagement.tableType = NormalWords
        DataBaseServices.updateVar(OpenedSet, SetManagement.selectedSet)
        popUpMenu.menu.findItem(R.id.SelectAll).isEnabled = false
    }

    private fun editSet(selectedItem: String){
        dialogEditItem = D_editItem(this){
            when {
                it.count() > MaxSetName -> {
                    Lib.showMessage(this, "Max character is $MaxSetName")
                }
                DataBaseServices.isSetNotExist(it) -> {
                    if(selectedItem == SetManagement.selectedSet){
                        SetManagement.selectedSet = it
                        DataBaseServices.updateVar(OpenedSet, it)
                        SetName.text = it
                    }
                    DataBaseServices.updateSet(selectedItem, it)
                    setItemAdapter.changeList()
                    dialogEditItem.dismiss()
                }
                else -> {
                    Lib.showMessage(this, "This name is Taken")
                }
            }
        }
        dialogEditItem.textInput = selectedItem
        dialogEditItem.maxChar = MaxSetName
        dialogEditItem.build()
        dialogEditItem.display()
    }

    private fun setChangeColor(selectedItem: String){
        val dialog = D_ask_color(this){ color ->
            DataBaseServices.updateSetColor(selectedItem, color)
            setItemAdapter.changeList()
            val fragment = getFragment()
            if(fragment is F_Collections){
                fragment.refreshFragment()
            }
        }
        dialog.build()
        dialog.display()
    }

    private fun deleteSet(selectedItem: String){
        val ask = D_ask(this, "ARE YOU SURE ?"){
            if(it){
                if(selectedItem == SetManagement.selectedSet){
                    SetManagement.selectedSet = AllSet
                    DataBaseServices.updateVar(OpenedSet, AllSet)
                    SetName.text = AllSet
                }
                DataBaseServices.deleteSet(selectedItem)
                val fragment = getFragment()
                if(fragment is F_Collections) fragment.refreshFragment()
                setItemAdapter.changeList()
            }
        }
        ask.build()
        ask.display()
    }

    private fun createNewSet(){
        dialogEditItem = D_editItem(this){
            when {
                it.count() > MaxSetName -> {
                    Lib.showMessage(this, "Max characters is $MaxSetName")
                }
                DataBaseServices.isSetNotExist(it) -> {
                    DataBaseServices.insertSet(it, pickRandomColor())
                    setItemAdapter.changeList()
                    dialogEditItem.dismiss()
                }
                else -> {
                    Lib.showMessage(this, "This name is Taken")
                }
            }
        }
        dialogEditItem.maxChar = MaxSetName
        dialogEditItem.build()
        dialogEditItem.display()
    }

    private fun pickRandomColor() : Int{
        val r = Random.nextInt(0, 256)
        val g = Random.nextInt(0, 256)
        val b = Random.nextInt(0, 256)
        return Color.rgb(r, g, b)
    }

    private fun moveCollectionToAction(selectedItem: String){
        add_Set.visibility = View.VISIBLE
        drawerLayout.closeDrawer(GravityCompat.START)

        val fragment = getFragment()
        if(fragment is F_Collections){
            fragment.moveCollectionTo(selectedItem)
            setItemAdapter.changeList()
        }
    }

    //endregion

    //endregion

    //region Action Bar

    private fun menuForCollectionFragment(){
        popUpMenu = Lib.initPopupMenu(this, popUpMenuActionBar, R.menu.m_collection_fragment)
        popUpMenu.menu.findItem(R.id.DisplayHideItems).isChecked = Settings.displayHideCollection


        popUpMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.DisplayHideItems -> {
                    Settings.displayHideCollection = !Settings.displayHideCollection
                    popUpMenu.menu.findItem(R.id.DisplayHideItems).isChecked =
                        Settings.displayHideCollection
                    val fragment = getFragment()
                    if (fragment is F_Collections) fragment.refreshFragment()
                    DataBaseServices.updateVar(
                        isCheckedHideItem,
                        Settings.displayHideCollection.toString()
                    )
                }
                R.id.mergeItems -> {
                    if (!SetManagement.isCheckCollection) {
                        popUpMenu.menu.findItem(R.id.SelectAll).isEnabled = true
                        SetManagement.isCheckCollection = true
                        SetManagement.collectionActionType = MergeCollection
                        val fragment = getFragment()
                        if (fragment is F_Collections) {
                            fragment.actionCollection.text = "Merge Items"
                            fragment.refreshFragment()
                        }
                    }
                }
                R.id.MoveTo -> {
                    if (!SetManagement.isCheckCollection) {
                        popUpMenu.menu.findItem(R.id.SelectAll).isEnabled = true
                        SetManagement.isCheckCollection = true
                        SetManagement.collectionActionType = CollectionMoveTo
                        val fragment = getFragment()
                        if (fragment is F_Collections) {
                            fragment.actionCollection.text = "Move to"
                            fragment.refreshFragment()
                        }
                    }
                }
                R.id.Delete -> {
                    if (!SetManagement.isCheckCollection) {
                        popUpMenu.menu.findItem(R.id.SelectAll).isEnabled = true
                        SetManagement.isCheckCollection = true
                        SetManagement.collectionActionType = DeleteCollections
                        val fragment = getFragment()
                        if (fragment is F_Collections) {
                            fragment.actionCollection.text = "Delete Checked Items"
                            fragment.refreshFragment()
                        }
                    }
                }
                R.id.SelectAll -> {
                    if (SetManagement.isCheckCollection) {
                        val fragment = getFragment()
                        if (fragment is F_Collections) {
                            fragment.checkAllCollectionA()
                        }
                    }
                }
            }
            true
        }

        //-------------------

        filterActionBar.setOnClickListener {
            val dialog = D_Collections_filter(this){
                println("change collection list")
                val fragment = getFragment()
                if(fragment is F_Collections) fragment.refreshFragment()
            }
            dialog.buildWithStyle(R.style.FullWidth_Dialog)
            dialog.display()
        }
    }

    private fun menuForWordsFragment(){
        popUpMenu = Lib.initPopupMenu(this, popUpMenuActionBar, R.menu.m_words_fragment)

        val fragment = getFragment() as F_Words

        //region set up Menu
        popUpMenu.menu.findItem(R.id.DisplayHideItems).isChecked = Settings.displayHideDeletedItems
        popUpMenu.menu.findItem(R.id.unHide).isEnabled = Settings.displayHideDeletedItems

        if(WordsManagement.tableType != FavoriteWords){
            popUpMenu.menu.removeItem(R.id.setNotFavorite)
        }
        if(WordsManagement.tableType != RememberedWords){
            popUpMenu.menu.removeItem(R.id.setNotRemember)
        }
        if(WordsManagement.tableType != DeletedWords){
            popUpMenu.menu.removeItem(R.id.DisplayHideItems)
            popUpMenu.menu.removeItem(R.id.unHide)
        }
        if(WordsManagement.tableType != NormalWords){
           if(WordsManagement.tableType != DeletedWords){
               popUpMenu.menu.removeItem(R.id.MoveTo)
               popUpMenu.menu.removeItem(R.id.Delete)
           }else{
               popUpMenu.menu.removeItem(R.id.CopyTo)
           }
        }

        //endregion

        //region Menu OnClick
        popUpMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.MoveTo -> {
                    popUpMenu.menu.findItem(R.id.Select).isEnabled = true
                    whenMenuItemClick(MoveToWords, fragment)
                }
                R.id.CopyTo -> {
                    popUpMenu.menu.findItem(R.id.Select).isEnabled = true
                    whenMenuItemClick(CopyToWords, fragment)
                }
                R.id.Delete -> {
                    popUpMenu.menu.findItem(R.id.Select).isEnabled = true
                    whenMenuItemClick(DeleteWords, fragment)
                }
                R.id.SelectAll -> {
                    whenMenuItemClick(SelectAllWords, fragment)
                }
                R.id.SelectOnlyFavorite -> {
                    whenMenuItemClick(SelectFavoriteWords, fragment)
                }
                R.id.SelectOnlyRemember -> {
                    whenMenuItemClick(SelectRememberedWords, fragment)
                }
                R.id.SelectByNumber -> {
                    whenMenuItemClick(SelectByNumber, fragment)
                }
                R.id.DisplayHideItems -> {
                    it.isChecked = !it.isChecked
                    DataBaseServices.updateVar(isCheckedHideDeleteItem, it.isChecked.toString())
                    Settings.displayHideDeletedItems = it.isChecked
                    fragment.refreshFragment()
                    popUpMenu.menu.findItem(R.id.unHide).isEnabled = it.isChecked
                }
                R.id.unHide -> {
                    popUpMenu.menu.findItem(R.id.Select).isEnabled = true
                    whenMenuItemClick(unHideDeletedItems, fragment)
                }
                R.id.setNotFavorite -> {
                    popUpMenu.menu.findItem(R.id.Select).isEnabled = true
                    whenMenuItemClick(unFavorite, fragment)
                }
                R.id.setNotRemember -> {
                    popUpMenu.menu.findItem(R.id.Select).isEnabled = true
                    whenMenuItemClick(unRemember, fragment)
                }
            }
            true
        }

        //endregion

        //region filter btn
        filterActionBar.setOnClickListener {
            val dialog = D_words_filter(this){
                val fragment = getFragment()
                if(fragment is F_Words) fragment.refreshFragment()
            }
            dialog.buildWithStyle(R.style.FullWidth_Dialog)
            dialog.display()
        }

        //endregion

        //region search
        searchEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        searchEditText.isSingleLine = true
        searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                whenSearchClick()
                true
            }
            false
        }
        searchActionBar.setOnClickListener {
            SetName.visibility = View.GONE
            searchEditText.visibility = View.VISIBLE
            searchEditText.requestFocus()
            Lib.showKeyboardTo(this, searchEditText)
        }

        //endregion
    }

    private fun whenMenuItemClick(theType: Int, fragment: F_Words){
        if(fragment.wordsAdapter.list.isNotEmpty()){
            fragment.actionBarMenuClick(theType)
        }else{
            Lib.showMessage(this, "List is Empty")
            popUpMenu.menu.findItem(R.id.Select).isEnabled = false
        }
    }

    private fun whenSearchClick(){
        val getText = searchEditText.text.toString()
        val fragment = getFragment()
        if(fragment is F_Words) fragment.refreshFragment(getText)
        Lib.hideKeyboardFrom(this, searchEditText)
        searchEditText.clearFocus()
    }

    //endregion

    //region Interface

    override fun notifyActivity(event: String, enable: Boolean) {
        when(getFragment()){
            is F_Collections -> {
                when (event) {
                    NotifyMenu -> menuForCollectionFragment()
                    NotifyMoveCollectionToSet -> moveCollectionToAction()
                    NotifyProcessEndOrBegin -> beginOrEndProgressProcess(enable)
                    NotifyActionEnd -> popUpMenu.menu.findItem(R.id.SelectAll).isEnabled = false
                }
            }
            is F_Words -> {
                when (event) {
                    NotifyMenu -> menuForWordsFragment()
                    NotifyProcessEndOrBegin -> beginOrEndProgressProcess(enable)
                }
            }
        }
    }

    private fun moveCollectionToAction(){
        drawerLayout.openDrawer(GravityCompat.START)
        setItemAdapter.changeList()
        add_Set.visibility = View.INVISIBLE
        Lib.showMessage(this, "Please Select Set Destination")
    }

    private fun beginOrEndProgressProcess(enable: Boolean){
        Handler(this.mainLooper).post {
            isOnProcess = enable
            if(isOnProcess){
                progressProcess.visibility = View.VISIBLE
            }else{
                progressProcess.visibility = View.INVISIBLE
            }
        }
    }

    private fun hideSearchItemInMenu(fragment: Fragment){
        when(fragment){
            is F_Words -> {
                popUpMenu.menu.findItem(R.id.Select).isEnabled = false
                WordsManagement.isCheckWords = false
                fragment.notifyWordAdapter(true)
            }
            is F_Collections -> {
                popUpMenu.menu.findItem(R.id.SelectAll).isEnabled = false
                SetManagement.isCheckCollection = false
                fragment.refreshFragment()
            }
        }
    }

    //endregion

    //region Other Permissions, onBackPress, getFrag

    override fun onBackPressed() {
        if(isOnProcess) return
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
            return
        }
        val fragment = getFragment()
        if(fragment is F_Words){
            if(fragment.isAddLayoutActive){
                fragment.displayOrHideAddLayout()
                return
            }
            if(WordsManagement.isCheckWords){
                hideSearchItemInMenu(fragment)
                return
            }
            WordsManagement.tableType = NormalWords
            SetName.text = SetManagement.selectedSet
        }
        if(fragment is F_Collections){
            if(SetManagement.isCheckCollection){
                hideSearchItemInMenu(fragment)
                return
            }
        }
        super.onBackPressed()
    }

    private fun getFragment() : Fragment?{
        val navHostFragment = fragment
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    //endregion

}
