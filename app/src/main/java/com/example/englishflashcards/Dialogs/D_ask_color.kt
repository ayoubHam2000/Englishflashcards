package com.example.englishflashcards.Dialogs

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.children
import com.example.englishflashcards.Objects.Lib
import com.example.englishflashcards.R
import java.util.*

class D_ask_color(context : Context, val event : (Int)->Unit) : MyDialogBuilder(context , R.layout.d_pick_color) {

    var isAdvanceOpen = false

    override fun initView(builderView: View) {

        //region vars
        val approve = builderView.findViewById<ImageView>(R.id.d_add)
        val dismiss = builderView.findViewById<ImageView>(R.id.d_dismiss)
        val theColor = builderView.findViewById<ImageView>(R.id.theColor)

        val openCloseAdvance = builderView.findViewById<ImageView>(R.id.openCloseAdvance)
        val openCloseAdvanceLayout = builderView.findViewById<LinearLayout>(R.id.openCloseAdvanceLayout)
        val advanceLayout = builderView.findViewById<LinearLayout>(R.id.advanceLayout)
        val redController = builderView.findViewById<SeekBar>(R.id.redController)
        val greenController = builderView.findViewById<SeekBar>(R.id.greenController)
        val blueController = builderView.findViewById<SeekBar>(R.id.blueController)
        val redColorText = builderView.findViewById<TextView>(R.id.redColorText)
        val greenColorText = builderView.findViewById<TextView>(R.id.greenColorText)
        val blueColorText = builderView.findViewById<TextView>(R.id.blueColorText)

        //endregion

        dialog.setOnShowListener {
            val firstColor = arrayOf(0, 0, 0)
            redController.max = 255
            greenController.max = 255
            blueController.max = 255
            redController.progress = firstColor[0]
            greenController.progress = firstColor[1]
            blueController.progress = firstColor[2]
            redColorText.text = firstColor[0].toString()
            greenColorText.text = firstColor[1].toString()
            blueColorText.text = firstColor[2].toString()
            setColor(builderView)

            approve.setOnClickListener {
                val color = theColor.backgroundTintList?.defaultColor
                if(color != null) event(color)
                dismiss()
            }
            dismiss.setOnClickListener {
                dismiss()
            }
            openCloseAdvanceLayout.setOnClickListener {
                isAdvanceOpen = !isAdvanceOpen
                if(isAdvanceOpen){
                    advanceLayout.visibility = View.VISIBLE
                    openCloseAdvance.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_24)
                }else{
                    advanceLayout.visibility = View.GONE
                    openCloseAdvance.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_24)
                }
            }

            val changeListener = object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val r = redController.progress
                    val g = greenController.progress
                    val b = blueController.progress
                    redColorText.text = r.toString()
                    greenColorText.text = g.toString()
                    blueColorText.text = b.toString()
                    Lib.changeBackgroundTint(Color.rgb(r, g, b), theColor)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            }

            redController.setOnSeekBarChangeListener(changeListener)
            greenController.setOnSeekBarChangeListener(changeListener)
            blueController.setOnSeekBarChangeListener(changeListener)

        }

    }

    private fun setColor(builderView : View){
        val theColor = builderView.findViewById<ImageView>(R.id.theColor)
        val theColors = builderView.findViewById<LinearLayout>(R.id.theColors)
        for(item in theColors.children){
            if(item is LinearLayout){
                for(image in item.children){
                    image.setOnClickListener {
                        val color = image.backgroundTintList?.defaultColor
                        if (color != null) Lib.changeBackgroundTint(color, theColor)
                    }
                }
            }
        }
    }


}