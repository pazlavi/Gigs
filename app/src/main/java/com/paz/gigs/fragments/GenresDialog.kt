package com.paz.gigs.fragments

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.paz.gigs.databinding.FragmentGenresDialogBinding
import com.paz.gigs.utils.Consts

class GenresDialog(private val key: String , private val initSet : HashSet<String>? ) : Fragment() {
    val selecteds   = HashSet<String>()
   private val opt = ArrayList<CheckBox>()
    private var _binding: FragmentGenresDialogBinding? = null

    private companion object {
        const val TAG = "Paz_GenresDialog"

    }

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGenresDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClick()
        buildView()
    }

    private fun setOnClick() {
        binding.dialogCBSelectAll.setOnCheckedChangeListener { _, isChecked ->
            opt.forEach { cb -> cb.isChecked = isChecked }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun buildView() {


//        val linearLayout2 = LinearLayout(context)
//        linearLayout2.layoutParams =
//            LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//        linearLayout2.orientation = LinearLayout.VERTICAL

        for (str in Consts.MUSIC_GENRES_MAP[key]!!) {
            val c = CheckBox(context).apply {
                text = str
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)

                setOnCheckedChangeListener { _, isChecked ->
                    Log.d(TAG, "ch changed: ")
                    if (isChecked) {
                        selecteds.add(str)
                    } else {
                        selecteds.remove(str)
                    }
                }
                isChecked = initSet?.contains(str) ?: false

            }
            opt.add(c)
            binding.dialogLAYOptions.addView(c)
            //linearLayout2.addView(c)
        }
        //binding.dialogLAYOptions.addView(linearLayout2)


    }

}