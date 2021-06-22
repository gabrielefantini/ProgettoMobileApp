package it.polito.mad.group25.lab.fragments.tutorial

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import it.polito.mad.group25.lab.R

class TutorialFragment: Fragment(R.layout.tutorial_fragment) {

    private lateinit var fragContext: Context

    private lateinit var viewPager: ViewPager
    private lateinit var skipButton: Button
    private lateinit var nextButton: Button

    private lateinit var viewPagerAdapter: MyViewPageAdapter

    private var dots = mutableListOf<TextView>()
    private lateinit var dotsLayout: LinearLayout
    private lateinit var layouts: List<Int>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragContext = requireContext()

        skipButton = view.findViewById(R.id.skipButton)
        nextButton = view.findViewById(R.id.nextButton)
        viewPager = view.findViewById(R.id.viewPager)
        dotsLayout = view.findViewById(R.id.layoutDots)

        layouts = listOf (
            R.layout.tutorial1,
            R.layout.tutorial2,
            R.layout.tutorial3
        )

        addBottomDots(fragContext)

        viewPagerAdapter = MyViewPageAdapter(layouts)
        viewPager.adapter = viewPagerAdapter
        viewPager.addOnPageChangeListener(MyListener())

        skipButton.setOnClickListener {
            //todo: navigate to login
        }

        nextButton.setOnClickListener {
            var current = getItem(1)
            if(current < layouts.size)
                viewPager.currentItem = current
            else{
                //todo: navigate to login
            }
        }
    }

    private fun addBottomDots(context: Context){
        dots.clear()
        dotsLayout.removeAllViews()

        for(i in layouts.indices){
            var dot = TextView(context)
            dot.text = "\u2022"
            dot.textSize = 35F

            dots.add(dot)
            dotsLayout.addView(dot)
        }

        if(dots.size > 0)
            selectBottomDot(0)
    }

    private fun selectBottomDot(currentPage: Int){
        val colorActive = resources.getColor(R.color.night_login_background,null)
        val colorInactive = resources.getColor(R.color.login_background,null)

        dots.forEachIndexed { id, dot ->
            val color = when(id){
                currentPage -> colorActive
                else -> colorInactive
            }
            dot.setTextColor(color)
        }
    }

    inner class MyListener : ViewPager.OnPageChangeListener{

        override fun onPageSelected(position: Int) {
            selectBottomDot(position)
            Log.d("aaaa","$position")
            //changing NEXT to GOT IT
            if(position == layouts.size -1){
                nextButton.text = "GOT IT!"
                skipButton.visibility = View.GONE
            }else{
                //still pages are left
                nextButton.text = "NEXT"
                skipButton.visibility = View.VISIBLE
            }
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageScrollStateChanged(state: Int) {

        }

    }

    private fun getItem(i: Int): Int = viewPager.currentItem + i

    inner class MyViewPageAdapter(val layouts: List<Int>): PagerAdapter(){

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var view = layoutInflater.inflate(layouts[position],container,false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int = layouts.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean{
            return view == `object`
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            var view = `object` as View
            container.removeView(view)
        }
    }
}