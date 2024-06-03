package com.mirea.weatherforecastapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.matteobattilana.weather.PrecipType
import com.mirea.weatherforecastapp.R
import com.mirea.weatherforecastapp.adapter.ForecastAdapter
import com.mirea.weatherforecastapp.databinding.ActivityMainBinding
import com.mirea.weatherforecastapp.model.CurrentWeatherResponseApi
import com.mirea.weatherforecastapp.model.FiveDaysForecastResponseApi
import com.mirea.weatherforecastapp.viewmodel.WeatherViewModel
import eightbitlab.com.blurview.RenderScriptBlur
import retrofit2.Call
import retrofit2.Response
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val calendar by lazy { Calendar.getInstance() }
    private val forecastAdapter by lazy { ForecastAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }


        binding.apply {
            var lat = intent.getDoubleExtra("lat", 0.0)
            var lon = intent.getDoubleExtra("lon", 0.0)
            var name = intent.getStringExtra("name")

            if (lat == 0.0 || lon == 0.0) {
                lat = 55.7504461
                lon = -37.6174943
                name = "Moscow"
            }


            addCity.setOnClickListener {
                startActivity(Intent(this@MainActivity, CityListActivity::class.java))
            }
            //current temperature
            cityTxt.text = name
            progressBar.visibility = View.VISIBLE
            weatherViewModel.loadCurrentWeather(lat, lon, "metric").enqueue(object :
                retrofit2.Callback<CurrentWeatherResponseApi> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<CurrentWeatherResponseApi>,
                    response: Response<CurrentWeatherResponseApi>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        progressBar.visibility = View.GONE
                        detailLayout.visibility = View.VISIBLE

                        data?.let {
                            statusTxt.text = it.weather?.get(0)?.main ?: "-"
                            humidityTxt.text = it.main?.humidity?.toString() + "%"
                            windTxt.text = it.wind?.speed.let {
                                it?.let { it1 ->
                                    Math.round(it1).toString()
                                }
                            } + "Km"
                            currentTempTxt.text = it.main?.temp.let {
                                it?.let { it1 ->
                                    Math.round(it1).toString()
                                }
                            } + "°"
                            maxTempTxt.text = it.main?.tempMax.let {
                                it?.let { it1 ->
                                    Math.round(it1).toString()
                                }
                            } + "°"
                            minTempTxt.text = it.main?.tempMin.let {
                                it?.let { it1 ->
                                    Math.round(it1).toString()
                                }
                            } + "°"

                            val drawable = if (isNightNow()) R.drawable.night_bg
                            else {
                                setDynamicallyWallpaper(it.weather?.get(0)?.icon ?: "-")
                            }
                            bgImage.setImageResource(drawable)
                            setEffectRainSnow(it.weather?.get(0)?.icon ?: "-")


                        }

                    }
                }

                override fun onFailure(call: Call<CurrentWeatherResponseApi>, t: Throwable) {
                    Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                }
            })

            //settings blur view
            var radius = 10f
            val decorView = window.decorView
            val rootView = (decorView.findViewById(android.R.id.content) as ViewGroup?)
            val windowBackground = decorView.background

            rootView?.let {
                blurView.setupWith(it, RenderScriptBlur(this@MainActivity))
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius)
                blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                blurView.clipToOutline = true

            }


            //forecast temp
            weatherViewModel.loadFiveDaysForecast(lat, lon, "metric")
                .enqueue(object : retrofit2.Callback<FiveDaysForecastResponseApi> {
                    override fun onResponse(
                        call: Call<FiveDaysForecastResponseApi>,
                        response: Response<FiveDaysForecastResponseApi>
                    ) {
                        if (response.isSuccessful) {
                            val data = response.body()
                            blurView.visibility = View.VISIBLE

                            data?.let {
                                forecastAdapter.differ.submitList(it.list)
                                forecastView.apply {
                                    layoutManager = LinearLayoutManager(
                                        this@MainActivity,
                                        LinearLayoutManager.HORIZONTAL,
                                        false
                                    )
                                    adapter = forecastAdapter
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<FiveDaysForecastResponseApi>, t: Throwable) {
                    }

                })
        }
    }

    private fun isNightNow(): Boolean {
        return calendar.get(Calendar.HOUR_OF_DAY) >= 18
    }

    private fun setDynamicallyWallpaper(icon: String): Int {
        return when (icon.dropLast(1)) {
            "01" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.sunny_bg
            }

            "02", "03", "04" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.cloudy_bg
            }

            "09", "10", "11" -> {
                initWeatherView(PrecipType.RAIN)
                R.drawable.rainy_bg
            }

            "13" -> {
                initWeatherView(PrecipType.SNOW)
                R.drawable.snow_bg
            }

            "50" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.haze_bg
            }

            else -> 0
        }

    }

    private fun setEffectRainSnow(icon: String) {
        when (icon.dropLast(1)) {
            "01" -> {
                initWeatherView(PrecipType.CLEAR)
            }

            "02", "03", "04" -> {
                initWeatherView(PrecipType.CLEAR)
            }

            "09", "10", "11" -> {
                initWeatherView(PrecipType.RAIN)
            }

            "13" -> {
                initWeatherView(PrecipType.SNOW)
            }

            "50" -> {
                initWeatherView(PrecipType.CLEAR)
            }
        }

    }

    private fun initWeatherView(type: PrecipType) {
        binding.weatherView.apply {
            setWeatherData(type)
            angle = -20
            emissionRate = 100.0f
        }
    }
}
