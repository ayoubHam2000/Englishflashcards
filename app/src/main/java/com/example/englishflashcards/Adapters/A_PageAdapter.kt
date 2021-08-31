package com.example.englishflashcards.Adapters

import MyOnSwipeTouchListener
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.viewpager.widget.PagerAdapter
import com.example.englishflashcards.Classes.Word
import com.example.englishflashcards.Dialogs.D_WordInfo
import com.example.englishflashcards.Dialogs.D_ask
import com.example.englishflashcards.Objects.*
import com.example.englishflashcards.R
import com.example.englishflashcards.Utilities.Edit
import com.example.englishflashcards.Utilities.Next
import com.example.englishflashcards.Utilities.WordDeleted
import kotlin.math.min


class A_PageAdapter(val context: Context, val event: (String, Int) -> Unit) : PagerAdapter(){


    //region vars changeList
    //vars
    var list = ArrayList<String>()
    private val layout = R.layout.a_frame_page
    private val theColors = getColorsFromResources()
    private val textData = DataBaseServices.getText(SetManagement.selectedC.tableName).joinToString(
        " "
    )

    fun changeList(){
        PracticeManager.onEditFromPractice = false
        list = PracticeManager.getSortedPracticeWordList()
    }

    //endregion


    @SuppressLint("ClickableViewAccessibility")
    private fun configView(view: View, position: Int){
        //region vars and view
        //var
        val theColor = theColors[position % theColors.count()]
        val theWord = DataBaseServices.getWord(list[position])!!

        //view
        val father : RelativeLayout = view.findViewById(R.id.father)
        val background : RelativeLayout = view.findViewById(R.id.background)
        val dailyWordsProgressNbr : TextView = view.findViewById(R.id.dailyWordsProgressNbr)
        val dailyWordsProgress : ProgressBar = view.findViewById(R.id.dailyWordsProgress)
        val favoriteWord : ImageView = view.findViewById(R.id.favoriteWord)
        val frequencyWord : TextView = view.findViewById(R.id.frequencyWord)
        val deleteWord : ImageView = view.findViewById(R.id.deleteWord)
        val editWord : ImageView = view.findViewById(R.id.editWord)
        val theWordName : TextView = view.findViewById(R.id.theWord)
        val wordLevelProgress : ProgressBar = view.findViewById(R.id.wordLevelProgress)
        val wordLevelProgressNbr : TextView = view.findViewById(R.id.wordLevelProgressNbr)
        val alreadyRememberedWord : ImageView = view.findViewById(R.id.AlreadyRememberedWord)

        //endregion


        background.setOnTouchListener(
            object : MyOnSwipeTouchListener(context) {
                @SuppressLint("ClickableViewAccessibility")
                override fun onSwipeLeft() {
                    super.onSwipeLeft()
                    DataBaseServices.updateWordIsShowed(theWord.name, 1)
                    DataBaseServices.updateWordRepetition(theWord.name)
                    DataBaseServices.updateWordLastView(theWord.name)
                    event(Next, position + 1)
                }

                override fun onSwipeRight() {
                    super.onSwipeRight()
                    event(Next, position - 1)
                }

                override fun onDoubleClick() {
                    displayWordInfo(theWord)
                    super.onDoubleClick()
                }

                override fun onLongClick() {
                    copyWordName(theWord)
                    super.onLongClick()
                }

            })

        //set view
        background.backgroundTintList = ColorStateList.valueOf(theColor)
        changeProgressPages(position, dailyWordsProgress, dailyWordsProgressNbr)
        changeLevelProgress(theWord, wordLevelProgress, wordLevelProgressNbr)
        changeFavoriteIcon(theWord.isFavorite, favoriteWord)
        changeRememberIcon(theWord.isRemembered, alreadyRememberedWord)
        changeFrequency(theWord, frequencyWord)
        theWordName.text = theWord.name
        getHexColor("dd",theColor)
        Lib.changeBackgroundTint(Color.parseColor(getHexColor("dd", theColor)), father)


        //buttons
        favoriteWord.setOnClickListener { makeItFavorite(theWord, favoriteWord) }
        alreadyRememberedWord.setOnClickListener { rememberClick(theWord, position, alreadyRememberedWord) }
        deleteWord.setOnClickListener { deleteItem(theWord, position) }
        editWord.setOnClickListener {editWord(position)}

    }


    //region functions

    @SuppressLint("SetTextI18n")
    private fun changeFrequency(theWord : Word, frequencyWord : TextView){
        val f = theWord.frequency
        frequencyWord.text = "F : $f"
    }

    @SuppressLint("SetTextI18n")
    private fun changeProgressPages(position: Int, dailyWordsProgress : ProgressBar, dailyWordsProgressNbr : TextView){
        val len = list.count()
        dailyWordsProgress.max = len
        dailyWordsProgress.progress = position + 1
        dailyWordsProgressNbr.text = "${position + 1} / $len"
    }

    private fun changeFavoriteIcon(value : Boolean, favoriteWord : ImageView){
        if(value){
            favoriteWord.setBackgroundResource(R.drawable.ic_favorite_active)
        }else{
            favoriteWord.setBackgroundResource(R.drawable.ic_favorite)
        }
    }

    private fun changeRememberIcon(value : Boolean, alreadyRememberedWord : ImageView){
        if(!value){
            alreadyRememberedWord.setBackgroundResource(R.drawable.ic_remember_box)
        }else{
            alreadyRememberedWord.setBackgroundResource(R.drawable.ic_back_arrow)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun changeLevelProgress(theWord: Word, wordLevelProgress : ProgressBar, wordLevelProgressNbr : TextView){
        wordLevelProgress.max = Settings.maxLevel
        val m = min(Settings.maxLevel, theWord.level)
        wordLevelProgress.progress = m
        wordLevelProgressNbr.text = "$m / ${Settings.maxLevel}"
    }

    //to button

    private fun copyWordName(theWord: Word){
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", theWord.name)
        clipboard.setPrimaryClip(clip)
        Lib.showMessage(context, "${theWord.name} is copied")
    }

    private fun displayWordInfo(word: Word){
        val dialog = D_WordInfo(context, R.layout.d_word_info, word, textData)
        dialog.build()
        dialog.display()
    }

    private fun rememberClick(theWord: Word, position: Int, alreadyRememberedWord : ImageView){
        val isRemember = !theWord.isRemembered
        val name = theWord.name
        theWord.isRemembered = isRemember

        if(isRemember){
            DataBaseServices.updateWordIsRemember(name, 1)
        }else{
            DataBaseServices.updateWordIsRemember(name, 0)
        }
        changeRememberIcon(isRemember, alreadyRememberedWord)
        event(Next, position + 1)
    }

    private fun makeItFavorite(theWord : Word, favoriteWord : ImageView){
        val f = !theWord.isFavorite
        theWord.isFavorite = f
        DataBaseServices.updateWordIsFavorite(theWord.name, f.toInteger())
        changeFavoriteIcon(f, favoriteWord)
    }

    private fun deleteItem(theWord : Word, position: Int){
        val ask = D_ask(context, "ARE YOU SURE ?"){
            if(it){
                val t = SetManagement.selectedC.tableName
                Lib.showMessage(context, theWord.name + WordDeleted)
                DataBaseServices.updateWordIsDeleted(theWord.name, 1, t)
                list.removeAt(position)
                notifyDataSetChanged()
            }
        }
        ask.build()
        ask.display()
    }

    private fun editWord(position: Int){
        PracticeManager.onEditFromPractice = true
        event(Edit, position)
    }

    //endregion


    //region Utilities

    private fun getColorsFromResources() : ArrayList<Int>{
        val ta = context.resources.getStringArray(R.array.backgroundColors)
        val stringColor = ta[0].split(" ")

        val colors = ArrayList<Int>()
        for (c in stringColor){
            if(c.count() != 0){
                colors.add(Color.parseColor(c))
            }
        }
        return colors
    }

    private fun getHexColor(alpha : String, color : Int) : String{
        println(">> ${String.format("#%06X", 0xFFFFFF and color)}")
        return String.format("#$alpha%06X", 0xFFFFFF and color)
    }

    private fun animateCard(view: RelativeLayout, end: () -> Unit){

        val oa1 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f)
        val oa2 = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f)
        oa1.interpolator = DecelerateInterpolator()
        oa2.interpolator = AccelerateDecelerateInterpolator()
        oa1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                end()
                oa2.start()
            }
        })
        oa1.duration = 200
        oa1.start()

    }

    fun getCurrentWord(position: Int) : Word{
        //used for edit word
        return DataBaseServices.getWord(list[position])!!
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(layout, container, false)

        configView(view, position)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getCount(): Int {
        return list.count()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as (RelativeLayout)
    }

    private fun Boolean.toInteger() : Int{
        return when(this){
            true -> 1
            false -> 0
        }
    }

    //endregion

}