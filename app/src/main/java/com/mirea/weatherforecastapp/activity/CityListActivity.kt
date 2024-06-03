package com.mirea.weatherforecastapp.activity

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mirea.weatherforecastapp.adapter.CityListAdapter
import com.mirea.weatherforecastapp.databinding.AcivityCityListBinding
import com.mirea.weatherforecastapp.model.CityResponseApi
import com.mirea.weatherforecastapp.viewmodel.CityViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CityListActivity : AppCompatActivity() {

    lateinit var binding: AcivityCityListBinding
    private val cityAdapter by lazy { CityListAdapter() }
    private val cityViewModel: CityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcivityCityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }

        binding.apply {
            cityEdit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    progressBar2.visibility = View.VISIBLE
                    cityViewModel.loadCitiesList(s.toString(), 10)
                        .enqueue(object : Callback<CityResponseApi> {
                            override fun onResponse(
                                call: Call<CityResponseApi>,
                                response: Response<CityResponseApi>
                            ) {
                                if (response.isSuccessful) {
                                    val data = response.body()
                                    data?.let {
                                        progressBar2.visibility = View.GONE
                                        cityAdapter.differ.submitList(it)
                                        cityView.apply {
                                            layoutManager = LinearLayoutManager(
                                                this@CityListActivity,
                                                LinearLayoutManager.HORIZONTAL,
                                                false
                                            )
                                            adapter = cityAdapter
                                        }
                                    }
                                }
                            }

                            override fun onFailure(call: Call<CityResponseApi>, t: Throwable) {

                            }
                        })
                }
            })
        }
    }
}