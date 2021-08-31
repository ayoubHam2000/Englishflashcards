package com.example.englishflashcards.Classes.Costum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.example.englishflashcards.R
import com.google.android.material.tabs.TabLayout


class ColorDialogFragment : DialogFragment() {
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null


    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.d_ask_fast, container, false)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return onCreateView(inflater, container)
    }


    companion object {
        fun newInstance(): ColorDialogFragment {
            val colorDialogFragment = ColorDialogFragment()
            // We provide custom style, because we need title.
            colorDialogFragment.setStyle(STYLE_NORMAL, R.style.Bottom_Dialog)
            return colorDialogFragment
        }
    }
}