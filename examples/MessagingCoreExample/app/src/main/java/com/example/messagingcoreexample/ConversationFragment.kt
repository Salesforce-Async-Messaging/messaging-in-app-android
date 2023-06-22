package com.example.messagingcoreexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messagingcoreexample.adapter.ConversationEntryAdapter
import com.example.messagingcoreexample.databinding.ConversationFragmentBinding
import com.example.messagingcoreexample.viewmodel.MessagingViewModel
import com.example.messagingcoreexample.viewmodel.MessagingViewModel.Companion.conversationId
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ConversationFragment : Fragment() {

    private var _binding: ConversationFragmentBinding? = null
    private var viewModel: MessagingViewModel? = null
    private val pagingAdapter = ConversationEntryAdapter()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val model : MessagingViewModel by viewModels {
            conversationId = UUID.fromString(arguments?.getString("conversationID"))
            MessagingViewModel.Factory
        }
        viewModel = model

        // Configures providers and sets logging for this example
        viewModel?.setupMessaging()

        // Starts the Messaging for In-App Core SDK stream
        viewModel?.startStream()

        // This is an basic example of how you would handle Pre Chat fields in your deployment.
        // You will need to create a UI and handle the Pre Chat fields you have created.
        viewModel?.handlePreChat()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ConversationFragmentBinding.inflate(inflater, container, false)
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
            viewModel?.sendTextMessage(message)
        }

        binding.buttonSecond.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("conversationID", conversationId.toString())
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment, bundle)
        }
        binding.recyclerView.apply {
            adapter = pagingAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                reverseLayout = true
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel?.conversationEntries?.collectLatest {

                // Submit new conversation entries to the paging adapter
                pagingAdapter.submitData(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel?.typingEvents?.collectLatest {

                // Get the latest typing event
                val text = "Typing Event: ${it.status}"

                // Show event info as a toast
                Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
            }
        }

        setBusinessHoursBanner()
    }

    private fun setBusinessHoursBanner() {
        viewModel?.businessHours?.value?.isWithinBusinessHours().let {
            if (it == null) {
                return
            }

            binding.businessHoursBanner.visibility = VISIBLE
            if (it == true) {
                binding.businessHoursBanner.text = "You are within business hours."
                binding.businessHoursBanner.setBackgroundColor(resources.getColor(R.color.green,null))
            } else {
                binding.businessHoursBanner.text = "You are not within business hours."
                binding.businessHoursBanner.setBackgroundColor(resources.getColor(R.color.red,null))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}