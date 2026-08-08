package ru.netology.nmedia.ui

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map as YandexMap
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import java.lang.ref.WeakReference
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.DialogPlaceBinding
import ru.netology.nmedia.databinding.FragmentMapsBinding
import ru.netology.nmedia.model.Place
import ru.netology.nmedia.repository.PlaceRepository
import ru.netology.nmedia.repository.SharedPreferencesPlaceRepository

class MapsFragment : Fragment(R.layout.fragment_maps) {
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private val markers = mutableMapOf<Long, PlacemarkMapObject>()
    private lateinit var repository: PlaceRepository
    private var selectedPlaceId: Long? = null
    private var markerImage: ImageProvider? = null
    private var cameraInitialized = false

    private val inputListener: InputListener = object : InputListener {
        override fun onMapTap(map: YandexMap, point: Point) {
            showPlaceEditor(place = null, location = point)
        }

        override fun onMapLongTap(map: YandexMap, point: Point) = Unit
    }
    private val inputListenerReference = WeakReference(inputListener)

    private val placemarkTapListener = MapObjectTapListener { mapObject, _ ->
        val id = mapObject.userData as? Long ?: return@MapObjectTapListener false
        repository.getById(id)?.let { place ->
            showPlaceEditor(
                place = place,
                location = Point(place.latitude, place.longitude),
            )
        }
        true
    }
    private val placemarkTapListenerReference = WeakReference(placemarkTapListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = SharedPreferencesPlaceRepository(requireContext())

        parentFragmentManager.setFragmentResultListener(
            PLACE_SELECTION_REQUEST,
            this,
        ) { _, result ->
            selectedPlaceId = result.getLong(SELECTED_PLACE_ID)
            refreshMarkers(selectedPlaceId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMapsBinding.bind(view)

        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_show_places) {
                (requireActivity() as AppActivity).showPlaces()
                true
            } else {
                false
            }
        }

        binding.mapView.mapWindow.map.apply {
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isRotateGesturesEnabled = true
            isTiltGesturesEnabled = true
            addInputListener(inputListenerReference)
        }
        refreshMarkers(selectedPlaceId)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        refreshMarkers(selectedPlaceId)
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        binding.mapView.mapWindow.map.removeInputListener(inputListenerReference)
        markers.clear()
        markerImage = null
        cameraInitialized = false
        _binding = null
        super.onDestroyView()
    }

    private fun refreshMarkers(focusId: Long? = null) {
        val map = _binding?.mapView?.mapWindow?.map ?: return
        val places = repository.getAll()

        map.mapObjects.clear()
        markers.clear()
        places.forEach { place ->
            map.mapObjects.addPlacemark().apply {
                geometry = Point(place.latitude, place.longitude)
                setIcon(markerImage())
                setText(place.title)
                userData = place.id
                addTapListener(placemarkTapListenerReference)
                markers[place.id] = this
            }
        }

        val placeToFocus = focusId?.let(repository::getById)
        when {
            placeToFocus != null -> {
                focusOn(placeToFocus)
                selectedPlaceId = null
            }

            !cameraInitialized && places.isNotEmpty() -> {
                focusOn(places.first(), INITIAL_PLACE_ZOOM)
            }

            !cameraInitialized -> {
                moveCamera(RUSSIA_CENTER, RUSSIA_ZOOM, animated = false)
            }
        }
    }

    private fun focusOn(place: Place, zoom: Float = PLACE_ZOOM) {
        moveCamera(
            Point(place.latitude, place.longitude),
            zoom,
            animated = true,
        )
    }

    private fun moveCamera(target: Point, zoom: Float, animated: Boolean) {
        val map = _binding?.mapView?.mapWindow?.map ?: return
        val position = CameraPosition(target, zoom, 0F, 0F)
        if (animated) {
            map.move(
                position,
                Animation(Animation.Type.SMOOTH, CAMERA_ANIMATION_DURATION),
                null,
            )
        } else {
            map.move(position)
        }
        cameraInitialized = true
    }

    private fun markerImage(): ImageProvider = markerImage ?: run {
        val drawable = requireNotNull(
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_place_marker_48),
        )
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
        drawable.setBounds(0, 0, bitmap.width, bitmap.height)
        drawable.draw(Canvas(bitmap))
        ImageProvider.fromBitmap(bitmap).also { markerImage = it }
    }

    private fun showPlaceEditor(place: Place?, location: Point) {
        val dialogBinding = DialogPlaceBinding.inflate(layoutInflater).apply {
            titleInput.setText(place?.title.orEmpty())
            descriptionInput.setText(place?.description.orEmpty())
            coordinates.text = getString(
                R.string.coordinates_format,
                location.latitude,
                location.longitude,
            )
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (place == null) R.string.new_place else R.string.edit_place)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null)

        if (place != null) {
            builder.setNeutralButton(R.string.delete) { _, _ ->
                confirmDeletion(place)
            }
        }

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val title = dialogBinding.titleInput.text?.toString()?.trim().orEmpty()
                if (title.isBlank()) {
                    dialogBinding.titleLayout.error = getString(R.string.title_required)
                    return@setOnClickListener
                }

                val saved = repository.save(
                    Place(
                        id = place?.id ?: 0L,
                        title = title,
                        description = dialogBinding.descriptionInput.text
                            ?.toString()
                            ?.trim()
                            .orEmpty(),
                        latitude = location.latitude,
                        longitude = location.longitude,
                    ),
                )
                refreshMarkers(saved.id)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun confirmDeletion(place: Place) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_place)
            .setMessage(getString(R.string.delete_place_confirmation, place.title))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                repository.deleteById(place.id)
                refreshMarkers()
                Toast.makeText(requireContext(), R.string.place_deleted, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    companion object {
        const val PLACE_SELECTION_REQUEST = "place_selection"
        const val SELECTED_PLACE_ID = "selected_place_id"

        private val RUSSIA_CENTER = Point(61.5240, 105.3188)
        private const val RUSSIA_ZOOM = 3.4F
        private const val INITIAL_PLACE_ZOOM = 10F
        private const val PLACE_ZOOM = 16F
        private const val CAMERA_ANIMATION_DURATION = 0.6F
    }
}
