package com.example.myapplication.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentDashboardBinding
import com.example.myapplication.model.BarCode

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(DashboardViewModel::class.java) }
    val adapter = BarcodeListRecyclerAdapter()
    private lateinit var localList: List<BarCode>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val layoutManager = LinearLayoutManager(requireContext())
        _binding!!.recyclerView.adapter = adapter
        _binding!!.recyclerView.layoutManager = layoutManager


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.allHistoryItems.observe(viewLifecycleOwner, Observer {barcodeslist ->
            barcodeslist?.let {
                adapter.submitList(it)
                localList = it

            }
            if (!localList.isNotEmpty()){
                Toast.makeText(context, "BarCode list is empty", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}