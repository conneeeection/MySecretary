package com.example.navermap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.mysec.MapFragment
import com.example.mysec.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naver.maps.geometry.LatLng
import kotlin.math.*

class YouthCenterBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var youthCenters: List<MapFragment.YouthCenter>
    private var currentLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            youthCenters = it.getSerializable("youthCenters") as List<MapFragment.YouthCenter>
            currentLocation = it.getParcelable("currentLocation")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_youth_center_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView: ListView = view.findViewById(R.id.lv_youth_centers)

        currentLocation?.let {
            // 거리 계산 및 정렬
            val sortedCenters = youthCenters.map { center ->
                val distance = calculateDistance(currentLocation!!, LatLng(center.latitude, center.longitude))
                Pair(center, distance)
            }.sortedBy { it.second }

            // 거리와 함께 리스트 항목 생성
            val listItems = sortedCenters.map {
                "${it.first.name} - ${"%.2f".format(it.second)} km"
            }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
            listView.adapter = adapter
        }
    }

    // 거리 계산
    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        val radius = 6371 // 지구 반지름 (킬로미터)
        val latDistance = Math.toRadians(to.latitude - from.latitude)
        val lonDistance = Math.toRadians(to.longitude - from.longitude)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radius * c
    }

    companion object {
        @JvmStatic
        fun newInstance(youthCenters: List<MapFragment.YouthCenter>, currentLocation: LatLng) =
            YouthCenterBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("youthCenters", ArrayList(youthCenters))
                    putParcelable("currentLocation", currentLocation)
                }
            }
    }
}
