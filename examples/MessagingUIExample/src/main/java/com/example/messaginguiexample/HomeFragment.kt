package com.example.messaginguiexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.messaginguiexample.databinding.FragmentHomeBinding
import com.example.messaginguiexample.viewmodel.AppViewModel
import java.util.UUID

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val viewModel: AppViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonReset.setOnClickListener {
            viewModel.conversationId = UUID.randomUUID()
            viewModel.resetMessagingConfig()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}