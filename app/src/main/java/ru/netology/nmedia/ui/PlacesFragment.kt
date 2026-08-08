package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPlacesBinding
import ru.netology.nmedia.repository.PlaceRepository
import ru.netology.nmedia.repository.SharedPreferencesPlaceRepository

class PlacesFragment : Fragment(R.layout.fragment_places) {
    private var _binding: FragmentPlacesBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: PlaceRepository

    private val adapter = PlacesAdapter { place ->
        parentFragmentManager.setFragmentResult(
            MapsFragment.PLACE_SELECTION_REQUEST,
            bundleOf(MapsFragment.SELECTED_PLACE_ID to place.id),
        )
        parentFragmentManager.popBackStack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = SharedPreferencesPlaceRepository(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlacesBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.list.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val places = repository.getAll().sortedBy { it.title.lowercase() }
        adapter.submitList(places)
        binding.empty.visibility = if (places.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        binding.list.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
