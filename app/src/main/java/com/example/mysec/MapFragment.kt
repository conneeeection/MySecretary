package com.example.mysec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.navermap.YouthCenterBottomSheetFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.util.FusedLocationSource
import java.io.BufferedReader
import java.io.InputStreamReader

class MapFragment : Fragment(), OnMapReadyCallback {
    // MapView 변수
    private lateinit var mapView: MapView
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1000
    // 위치를 반환하는 구현체
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private var currentLocation: LatLng? = null
    private var radiusCircle: CircleOverlay? = null // CircleOverlay를 저장할 변수

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.map_view)
        // MapView의 생명주기 메서드 호출
        mapView.onCreate(savedInstanceState)
        // 지도 준비가 완료되면 호출될 콜백 설정
        mapView.getMapAsync(this)

        // 현재 위치 띄우기
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // 근처 청년센터 리스트 버튼
        val btnNearbyCenters: Button = view.findViewById(R.id.btn_nearby_centers)
        btnNearbyCenters.setOnClickListener {
            currentLocation?.let {
                    location ->
                val bottomSheet = YouthCenterBottomSheetFragment.newInstance(readCsvFile(), location)
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            }
        }
    }

    // 지도 준비가 완료되면 호출되는 메서드
    override fun onMapReady(@NonNull naverMap: NaverMap) {
        this.naverMap = naverMap
        // 현재 위치 띄우기
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        // 현위치 버튼 활성화
        setLocationButtonEnabled(true)

        // csv 파일 읽기
        val youthCenters = readCsvFile()

        // InfoWindow 생성
        val infoWindow = InfoWindow()

        // 마커 추가
        for (center in youthCenters) {
            val marker = com.naver.maps.map.overlay.Marker()
            marker.position = LatLng(center.latitude, center.longitude)
            marker.map = naverMap
            marker.captionText = center.name  // 마커에 이름 추가

            // 마커 크기 조절
            marker.width = 80
            marker.height = 100

            // 마커 클릭 리스너 설정
            marker.setOnClickListener {
                // InfoWindow에 표시할 내용 설정
                infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(requireContext()) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        return center.name
                    }
                }
                // InfoWindow를 마커 위에 표시
                infoWindow.open(marker)
                true
            }
        }

        naverMap.addOnLocationChangeListener { location ->
            currentLocation = LatLng(location.latitude, location.longitude)
            currentLocation?.let {
                updateRadiusCircle(it)
            }
        }
    }

    private fun updateRadiusCircle(center: LatLng) {
        // 기존에 생성된 CircleOverlay가 있으면 제거
        radiusCircle?.map = null

        // 새로 CircleOverlay 생성
        val circle = CircleOverlay()
        circle.center = center
        circle.radius = DISTANCE.toDouble()
        circle.color = 0x550000FF
        circle.outlineWidth = 0
        circle.map = naverMap

        // 새 CircleOverlay를 저장
        radiusCircle = circle
    }

    // CSV 파일 읽기 및 YouthCenter 데이터 클래스
    private fun readCsvFile(): List<YouthCenter> {
        val youthCenters = mutableListOf<YouthCenter>()

        try {
            val inputStream = requireContext().assets.open("youthCenter.csv")
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            var line: String?

            // csv 파일의 헤더 읽기
            reader.readLine()

            // csv 파일의 각 줄을 읽어 YouthCenter 객체 생성
            while (reader.readLine().also { line = it } != null) {
                val tokens = line!!.split(",")
                if (tokens.size == 3) {
                    val name = tokens[0]
                    val latitude = tokens[1].toDoubleOrNull() ?: 0.0
                    val longitude = tokens[2].toDoubleOrNull() ?: 0.0
                    youthCenters.add(YouthCenter(name, latitude, longitude))
                }
            }

            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return youthCenters
    }

    // YouthCenter 데이터 클래스
    data class YouthCenter(val name: String, val latitude: Double, val longitude: Double)

    // 현위치 버튼 활성화 설정 메서드
    private fun setLocationButtonEnabled(enabled: Boolean) {
        if (::naverMap.isInitialized) {
            naverMap.uiSettings.isLocationButtonEnabled = enabled
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        const val DISTANCE = 1000 // 단위: 미터
    }
}
