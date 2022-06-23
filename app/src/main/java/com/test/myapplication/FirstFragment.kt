package com.test.myapplication

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.test.myapplication.databinding.FragmentFirstBinding
import com.test.myapplication.label.MyLabelSpan
import com.test.myapplication.label.MyLabelSpanHelper


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val text = "现在是%4year%年，%month%月，%day%日，%hour%点，%minute%分，农历%nlyear%"

        val helper = MyLabelSpanHelper().setMainEditText(binding.etMain)
            .setLabelProviderEditText(binding.etLabelProvider).setLabels(
                mutableListOf(
                    generateLabel(title = "Year", value = "%4year%"),
                    generateLabel(title = "Month", value = "%month%"),
                    generateLabel(title = "Day", value = "%day%"),
                    generateLabel(title = "Hour", value = "%hour%"),
                    generateLabel(title = "Minute", value = "%minute%"),
                    generateLabel(title = "Second", value = "%second%"),
                    generateLabel(title = "NLYear", value = "%nlyear%"),
                )
            ).showTextByReplaceLabels(text, true)
        binding.btn.setOnClickListener {
            Toast.makeText(
                requireContext(),
                helper.getFinalContentString(false) + "\n" +
                        helper.getFinalContentString(true),
                Toast.LENGTH_LONG
            )
                .show()
            Log.d(
                "TAG",
                "======result:" + helper.getFinalContentString(false) + "\n" +
                        helper.getFinalContentString(true)
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateLabel(
        title: String,
        value: String,
        borderRadius: Int? = 5,
        borderWidth: Int? = 3,
        borderColor: Int? = Color.parseColor("#ff8041"),
        borderMargin: Int? = 10,
        borderPadding: Int? = 20,
        textColor: Int? = Color.parseColor("#ff8041"),
    ): MyLabelSpan {
        return MyLabelSpan(
            title,
            value,
            borderRadius ?: 5,
            borderWidth ?: 3,
            borderColor ?: Color.parseColor("#ff8041"),
            borderMargin ?: 10,
            borderPadding ?: 20,
            textColor ?: Color.parseColor("#ff8041"),
        )
    }

}