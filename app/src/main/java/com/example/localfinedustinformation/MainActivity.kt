package com.example.localfinedustinformation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import com.example.localfinedustinformation.data.Repository
import com.example.localfinedustinformation.data.models.airquality.Grade
import com.example.localfinedustinformation.data.models.airquality.MeasuredValues
import com.example.localfinedustinformation.data.models.monitoringstation.MonitoringStation
import com.example.localfinedustinformation.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val scope = MainScope()
    private var pressTime = 0L
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initRefreshButton()
        initVariables()
        requestLocationPermissions()
        makeStatusBarTransparent()
        initInsetMargin()
        refreshView()

    }

    private fun initRefreshButton() {
        binding.refreshButton.setOnClickListener {
            binding.contentsLayout.alpha = 0f
            binding.progressBar.visibility = View.VISIBLE
            fetchAirQualityData()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_ACCESS_LOCATION_PERMISSIONS
            && grantResults[0] != PackageManager.PERMISSION_GRANTED
            && grantResults[1] != PackageManager.PERMISSION_GRANTED
        ) {
            finish()
        } else {
            fetchAirQualityData()
        }


    }

    private fun refreshView() {
        binding.refresh.setOnRefreshListener {
            binding.contentsLayout.alpha = 0f
            binding.progressBar.visibility = View.VISIBLE
            fetchAirQualityData()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchAirQualityData() {
        cancellationTokenSource = CancellationTokenSource()

        fusedLocationProviderClient
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource!!.token
            )
            .addOnSuccessListener { location ->

                scope.launch {
                    binding.errorTextView.visibility = View.GONE
                    try {
                        Log.d("testt", "${location.latitude}" + "${location.longitude}")
                        val monitoringStation =
                            Repository.getNearbyMonitoringStation(
                                location.longitude,
                                location.latitude
                            )
                        monitoringStation?.stationName?.let { stationName ->
                            val measuredValues =
                                Repository.getLatestAirQualityData(stationName)
                            displayAirQualityData(monitoringStation, measuredValues!!)
                        }
                    } catch (exception: Exception) {
                        binding.errorTextView.visibility = View.VISIBLE
                        binding.contentsLayout.alpha = 0F
                    } finally {
                        binding.progressBar.visibility = View.GONE
                        binding.refresh.isRefreshing = false
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource?.cancel()
        scope.cancel()
    }

    private fun initVariables() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun displayAirQualityData(
        monitoringStation: MonitoringStation,
        measuredValues: MeasuredValues
    ) =
        with(binding) {
            contentsLayout.animate()
                .alpha(0.9f)
                .start()
            measureStationName.text = monitoringStation.stationName
            measureStationAddress.text = monitoringStation.addr

            (measuredValues.khaiGrade ?: Grade.UNKNOWN).let { grade ->
                root.setBackgroundResource(grade.colorResId)
                totalGradeLabelTextView.text = grade.label
                totalGradeEmojiTextView.text = grade.emoji
            }
            with(measuredValues) {
                fineDustInformationTextView.text =
                    "미세먼지 : $pm10Value ug/m3 ${(pm10Grade ?: Grade.UNKNOWN).emoji}"
                ultraFineDustInformationTextView.text =
                    "초미세먼지 : $pm25Value ug/m3 ${(pm25Grade ?: Grade.UNKNOWN).emoji}"

                with(so2Item) {
                    labelTextView.text = "아황산가스"
                    gradeTextVIew.text = (so2Grade ?: Grade.UNKNOWN).toString()
                    valueTextView.text = "$so2Value ppm"
                }

                with(coItem) {
                    labelTextView.text = "일산화탄소"
                    gradeTextVIew.text = (coGrade ?: Grade.UNKNOWN).toString()
                    valueTextView.text = "$coValue ppm"
                }

                with(o3Item) {
                    labelTextView.text = "오존"
                    gradeTextVIew.text = (o3Grade ?: Grade.UNKNOWN).toString()
                    valueTextView.text = "$o3Value ppm"
                }

                with(no2Item) {
                    labelTextView.text = "이산화질소"
                    gradeTextVIew.text = (no2Grade ?: Grade.UNKNOWN).toString()
                    valueTextView.text = "$no2Value ppm"
                }
            }
        }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            REQUEST_ACCESS_LOCATION_PERMISSIONS
        )

    }

    private fun makeStatusBarTransparent() {

        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
        if (Build.VERSION.SDK_INT >= 30) {    // API 30 에 적용
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

    }

    private fun initInsetMargin() {
        binding.contentsLayout.setPadding(0, getStatusBarHeight(this), 0, getNaviBarHeight(this))
    }

    private fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")

        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    private fun getNaviBarHeight(context: Context): Int {
        val resourceId: Int =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        val intervalTime = currentTime - pressTime

        if (2000 >= intervalTime) {
            finishAffinity()
        } else {
            Toast.makeText(this, "한번 더 누루면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
            pressTime = currentTime
        }
    }

    companion object {
        private const val REQUEST_ACCESS_LOCATION_PERMISSIONS = 101
    }
}

