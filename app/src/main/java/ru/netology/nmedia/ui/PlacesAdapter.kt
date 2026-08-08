package ru.netology.nmedia.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ItemPlaceBinding
import ru.netology.nmedia.model.Place

class PlacesAdapter(
    private val onClick: (Place) -> Unit,
) : ListAdapter<Place, PlacesAdapter.PlaceViewHolder>(PlaceDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return PlaceViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlaceViewHolder(
        private val binding: ItemPlaceBinding,
        private val onClick: (Place) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(place: Place) {
            binding.title.text = place.title
            binding.description.apply {
                text = place.description
                visibility = if (place.description.isBlank()) View.GONE else View.VISIBLE
            }
            binding.coordinates.text = binding.root.context.getString(
                R.string.coordinates_format,
                place.latitude,
                place.longitude,
            )
            binding.root.setOnClickListener { onClick(place) }
        }
    }

    private object PlaceDiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean =
            oldItem == newItem
    }
}

