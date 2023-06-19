package com.example.messagingcoreexample

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.messagingcoreexample.databinding.FragmentHomeBinding
import java.util.UUID

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var conversationId: UUID = UUID.randomUUID()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        conversationId = UUID.fromString(arguments?.getString("conversationID") ?: UUID.randomUUID().toString())
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonReset.setOnClickListener {
            conversationId = UUID.randomUUID()
        }

        binding.fab.setOnClickListener { view ->
            val bundle = Bundle()
            bundle.putString("conversationID", conversationId.toString())
            findNavController().navigate(R.id.action_FirstFragment_to_ConversationFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}