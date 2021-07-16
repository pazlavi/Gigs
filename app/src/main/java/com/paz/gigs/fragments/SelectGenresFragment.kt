package com.paz.gigs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.paz.gigs.adapters.TabAdapter
import com.paz.gigs.databinding.FragmentSelectGenresBinding
import com.paz.gigs.utils.Consts

class SelectGenresFragment : Fragment() {

    private var _binding: FragmentSelectGenresBinding? = null

    private var genres: HashSet<String>? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectGenresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = binding.selectGenresVPPages.findNavController()
        arguments?.let {
            genres = it[Consts.GENRES] as HashSet<String>?
        }
        binding.selectGenresVPPages.adapter = activity?.let {
            TabAdapter(
                childFragmentManager,
                it.lifecycle,
                genres
            )
        }
        TabLayoutMediator(
            binding.selectGenresTABTabs, binding.selectGenresVPPages
        ) { tab: TabLayout.Tab, i: Int ->
            tab.text = (binding.selectGenresVPPages.adapter as TabAdapter).getFragmentName(i)

        }.attach()
        setonClick()
    }

    private fun setonClick() {
        binding.djNewEventBARTool.setOnClickListener {
            val navController = it.findNavController()
            val sel = (binding.selectGenresVPPages.adapter as TabAdapter).getAllSelecteds()
            navController.previousBackStackEntry?.savedStateHandle?.set(Consts.GENRES, sel)
            navController.popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
