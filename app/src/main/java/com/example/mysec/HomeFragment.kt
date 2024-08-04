package com.example.mysec

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.weather.com.example.mysec.WeatherData
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(), CalendarFragment.OnMonthChangeListener {

    private val TAG = javaClass.simpleName
    private lateinit var mContext: Context

    // 날짜 관련 뷰
    private lateinit var viewPager: ViewPager2
    private lateinit var calendarYearMonthText: TextView

    // 날씨 관련 뷰
    private lateinit var temperature: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener

    private var userId: String? = null

    companion object {
        // HomeFragment의 인스턴스
        var instance: HomeFragment? = null

        // 날씨 API 관련 상수
        const val API_KEY: String = "f8bb00ad227c89fde815739b43e19db7"
        const val WEATHER_URL: String = "https://api.openweathermap.org/data/2.5/weather"
        const val MIN_TIME: Long = 5000
        const val MIN_DISTANCE: Float = 1000F
        const val WEATHER_REQUEST: Int = 102

        fun newInstance(userId: String): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putString("USER_ID", userId)
                }
            }
        }
    }

    // 프래그먼트가 컨텍스트에 첨부될 때 호출
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mContext = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        arguments?.let {
            userId = it.getString("USER_ID")
            Log.d(TAG, "User ID from arguments: $userId")
        }
    }

    // 프래그먼트의 뷰를 생성
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // 뷰가 생성된 후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 날짜
        viewPager = view.findViewById(R.id.viewPager)
        calendarYearMonthText = view.findViewById(R.id.calendar_year_month_text)

        // 날씨
        temperature = view.findViewById(R.id.temperature_tv)
        weatherIcon = view.findViewById(R.id.weather_ic)

        // 위치 권한 확인 및 날씨 정보 업데이트
        checkLocationPermissionAndUpdateWeather()

        // viewpager2 초기화
        initView()
    }

    // 초기 뷰
    private fun initView() {
        val calendarPageAdapter = CalendarPageAdapter(this, userId ?: "defaultUserId")
        viewPager.adapter = calendarPageAdapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.setCurrentItem(CalendarPageAdapter.START_POSITION, false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MONTH, position - CalendarPageAdapter.START_POSITION)
                updateYearMonthText(calendar.time)
            }
        })
        Log.d("HomeFragment", "User ID in initView: $userId")
    }

    // 날짜 업데이트
    private fun updateYearMonthText(date: Date) {
        val sdf = SimpleDateFormat("yyyy년 MM월", Locale.getDefault())
        calendarYearMonthText.text = sdf.format(date)
    }

    // 위치 권한을 확인하고 날씨 정보를 업데이트
    private fun checkLocationPermissionAndUpdateWeather() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 권한이 없으면 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                WEATHER_REQUEST
            )
        } else {
            // 위치 권한이 있으면 날씨 정보 업데이트
            getWeatherInCurrentLocation()
        }
    }

    // 현재 위치의 날씨 정보를 가져옴
    private fun getWeatherInCurrentLocation() {
        mLocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mLocationListener = LocationListener { location ->
            val params = RequestParams().apply {
                put("lat", location.latitude)
                put("lon", location.longitude)
                put("appid", API_KEY)
            }
            doNetworking(params)
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                WEATHER_REQUEST
            )
            return
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener)
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener)
    }

    // 날씨 API 호출
    private fun doNetworking(params: RequestParams) {
        val client = AsyncHttpClient()
        client.get(WEATHER_URL, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                response: JSONObject?
            ) {
                val weatherData = WeatherData().fromJson(response)
                if (weatherData != null) {
                    updateWeather(weatherData)
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseString: String?,
                throwable: Throwable?
            ) {
                Log.e(TAG, "Error fetching weather data", throwable)
            }
        })
    }

    // 날씨 정보 UI 업데이트
    private fun updateWeather(weather: WeatherData) {
        if (isAdded) {
            temperature.text = "${weather.tempString} ℃"
            val resourceID = resources.getIdentifier(weather.icon, "drawable", requireActivity().packageName)
            weatherIcon.setImageResource(resourceID)
        } else {
            Log.e(TAG, "Fragment is not attached to an Activity.")
        }
    }

    // 프래그먼트가 일시 중지 상태로 전환될 때 호출
    override fun onPause() {
        super.onPause()
        if (::mLocationManager.isInitialized) {
            mLocationManager.removeUpdates(mLocationListener)
        }
    }

    // 월 변경시
    override fun onMonthChanged(newDate: Date) {
        // 달력 스크롤 시 년/월 텍스트 업데이트
        updateYearMonthText(newDate)
    }
}