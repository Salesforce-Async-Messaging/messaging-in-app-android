package com.example.messagingcoreexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messagingcoreexample.adapter.ConversationEntryAdapter
import com.example.messagingcoreexample.databinding.FragmentSecondBinding
import com.example.messagingcoreexample.viewmodel.MessagingViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val viewModel: MessagingViewModel by viewModels { MessagingViewModel.Factory }
    private val pagingAdapter = ConversationEntryAdapter()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Starts the Messaging for In-App Core SDK stream
        viewModel.startStream()

        // NOTE: This example does not handle a pre-chat
        // form. If your deployment requires a pre-chat
        // form, you'll have to write the Core SDK code
        // to handle that information, or disable the
        // form for this example.
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSubmit.setOnClickListener {
            // Get contents of text field
            val message = binding.editText.text.toString()

            // Clear the text field
            binding.editText.text.clear()

            // Pass the text to the view model,
            // which sends message to the agent
            viewModel.sendTextMessage(message)
        }

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        binding.recyclerView.apply {
            adapter = pagingAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                reverseLayout = true
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.conversationEntries.collectLatest {

                // Submit new conversation entries to the paging adapter
                pagingAdapter.submitData(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.typingEvents.collectLatest {

                // Get the latest typing event
                val text = "Typing Event: ${it.status}"

                // Show event info as a toast
                Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}