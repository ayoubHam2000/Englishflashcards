package com.example.englishflashcards.Objects

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Build
import android.util.Log
import android.view.View
import android.view.View.*
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import com.example.englishflashcards.Fragments.*
import com.example.englishflashcards.R
import java.io.File
import java.io.FileWriter


object Lib {

    //region Permissions

    fun isStoragePermissionGranted(context : Context, fragment : Fragment): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v("INFO", "Permission is granted")
                true
            } else {
                Log.v("INFO", "Permission is revoked")
                showMessage(context, "Permission is Not granted")
                fragment.requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("INFO", "Permission is granted")
            true
        }
    }

    //region for activity

    fun isStoragePermissionGranted(context : Context, activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v("INFO", "Permission is granted")
                showMessage(context, "Permission is granted")
                true
            } else {
                Log.v("INFO", "Permission is revoked")
                showMessage(context, "Permission is Not granted")
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("INFO", "Permission is granted")
            showMessage(context, "Permission is granted")
            true
        }
    }

    //endregion
    //endregion

    //region used specially in project
    fun initMainActivityActionBar(activity : Activity?, fragment: Fragment){
        val mainActionBar = activity?.findViewById<View>(R.id.mainActionBar)
        val setName = activity?.findViewById<TextView>(R.id.SetName)
        val searchEditText = activity?.findViewById<EditText>(R.id.searchEditText)
        val searchActionBar = activity?.findViewById<ImageView>(R.id.searchActionBar)
        val filterActionBar = activity?.findViewById<ImageView>(R.id.filterActionBar)
        val popUpMenuActionBar = activity?.findViewById<ImageView>(R.id.popUpMenuActionBar)

        val arr = when(fragment){
            is F_Words -> arrayOf(VISIBLE, VISIBLE, INVISIBLE, VISIBLE, VISIBLE, VISIBLE)
            is F_Practice -> arrayOf(GONE, VISIBLE, INVISIBLE, INVISIBLE, INVISIBLE, INVISIBLE)
            is F_PracticeChunks -> arrayOf(VISIBLE, VISIBLE, INVISIBLE,INVISIBLE,INVISIBLE, INVISIBLE)
            is F_Collections -> arrayOf(VISIBLE, VISIBLE, INVISIBLE,INVISIBLE,VISIBLE, VISIBLE)
            is F_WordEdit -> arrayOf(VISIBLE, VISIBLE, INVISIBLE,INVISIBLE,INVISIBLE, INVISIBLE)
            else->arrayOf(VISIBLE, INVISIBLE, INVISIBLE, INVISIBLE, INVISIBLE, INVISIBLE)
        }

        mainActionBar?.visibility = arr[0]
        setName?.visibility = arr[1]
        searchEditText?.visibility = arr[2]
        searchActionBar?.visibility = arr[3]
        filterActionBar?.visibility = arr[4]
        popUpMenuActionBar?.visibility = arr[5]
        searchEditText?.setText("")
    }

    //endregion

    //region Keyboard

    fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(context: Context) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    fun showKeyboardTo(context: Context, view: View){
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun showKeyboardToDialog(dialog: Dialog){
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    //endregion

    //region Background

    fun changeBackgroundTint(context: Context, color: Int, background: View?){
        background?.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, color)
        )
    }

    fun changeBackgroundTint(color: Int, background: View?){
        background?.backgroundTintList = ColorStateList.valueOf(color)
    }

    //endregion

    //region menu
    fun initPopupMenu(context: Context, view: View, menu: Int) : PopupMenu{
        val popUpMenu = PopupMenu(context, view)
        popUpMenu.inflate(menu)
        view.setOnClickListener {
            forceShowIconForMenu(popUpMenu)
            popUpMenu.show()
        }

        /*popUpMenu.setOnMenuItemClickListener {
            when(it.itemId){
                com.example.englishflashcards.R.id.sortItem -> refreshFragments()
                com.example.englishflashcards.R.id.displayHide -> println("disolay Hide")
            }
            true
        }*/

        return popUpMenu
    }

    private fun forceShowIconForMenu(popupMenu: PopupMenu){
        try{
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenu)
            menu.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menu, true)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //endregion

    //region utilities

    fun fromSecondsToDate(t: Long) : String{
        var time = t / 1000
        val days = time / (3600*24)
        time -= days * 3600 * 24
        val hours = time / 3600
        time -= hours * 3600
        val minute = time / 60
        time -= minute * 60
        val seconds = time

        if(days == 0L && hours == 0L){
            return makeTime(minute, seconds, "m", "s")
        }else if(days == 0L){
            return makeTime(hours, minute, "h", "m")
        }
        return makeTime(days, hours, "d", "h")
    }

    private fun makeTime(a: Long, b: Long, x: String, y: String) : String{
        var result = ""
        result += if(a < 10){
            "0$a$x : "
        }else{
            "$a$x : "
        }

        result += if(b < 10){
            "0$b$y"
        }else{
            "$b$y"
        }
        return result
    }

    fun getBin(name: String) : String{
        var r = ""
        for(c in name){
            r += c.toInt().toString() + " "
        }
        return r
    }

    fun showMessage(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun animFragment(enterAnim: Int, exitAnim: Int) : NavOptions.Builder{
        val navBuilder = NavOptions.Builder()
        navBuilder.setEnterAnim(enterAnim).setExitAnim(exitAnim)
        //.setPopEnterAnim(R.anim.slide_left).setPopExitAnim(R.anim.slide_right)
        return navBuilder
    }

    fun writeFileOnInternalStorage(context: Context, sBody: String?) {
        val theFile = File(context.getExternalFilesDir(""), "treeData")
        var data = ""
        try {
            val writer = FileWriter(theFile)
            writer.append(sBody)
            writer.flush()
            writer.close()
            Lib.showMessage(context, data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //endregion


}