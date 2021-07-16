package com.paz.gigs.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.paz.gigs.fragments.GenresDialog
import com.paz.gigs.utils.Consts


class TabAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, private val  initSet : HashSet<String>?) : FragmentStateAdapter(
    fragmentManager,
    lifecycle
) {
    private var titles = Consts.MUSIC_GENRES_MAP.keys.toList().toTypedArray()
    private var fragments = ArrayList<GenresDialog>()
    override fun createFragment(position: Int): Fragment {
        val f = GenresDialog(titles[position] ,this.initSet)
        fragments.add(f)
        return f
//        return when (position) {
//            0 -> GenresDialog()
//            1 -> MyFragment()
//            2 -> MyFragment()
//            3 -> MyFragment()
//            else -> throw RuntimeException("fragment not found")
//        }
    }

    override fun getItemCount(): Int {
        return Consts.MUSIC_GENRES_MAP.keys.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    fun getFragmentName(position: Int): String {

        return titles[position]
    }

    fun getAllSelecteds(): HashSet<String> {
        val selecteds = HashSet<String>()
        fragments.forEach { f -> selecteds.addAll(f.selecteds) }
        return selecteds

    }
}