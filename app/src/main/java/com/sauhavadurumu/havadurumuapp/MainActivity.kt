package com.sauhavadurumu.havadurumuapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff

import android.os.Bundle

import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    var tvSehir: TextView? = null
    var location: SimpleLocation? = null
    var latitude: String? = null
    var longitude: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        var spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sehirler, R.layout.spinner_tek_satir)

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnSehirler.background.setColorFilter(resources.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)

        spnSehirler.setTitle("Şehir Seçin")
        spnSehirler.setPositiveButton("SEÇ")
        spnSehirler.adapter = spinnerAdapter

        spnSehirler.setOnItemSelectedListener(this)

        spnSehirler.setSelection(1)
        verileriGetir("Sakarya")

    }

    private fun oankiSehriGetir(lat: String?, longt: String?) {

        val url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + longt + "&appid=a8de8cc50792c8c8387703097fa61a6d&lang=tr&units=metric"
        var sehirAdi: String? = "Şuanki Yer"

        val havaDurumuObjeRequest2 = JsonObjectRequest(Request.Method.GET, url, null, object : Response.Listener<JSONObject> {


            override fun onResponse(response: JSONObject?) {

                var main = response?.getJSONObject("main")
                var sicaklik = main?.getInt("temp")
                tvSicaklik.text = sicaklik.toString()



                sehirAdi = response?.getString("name")
                tvSehir?.setText(sehirAdi)


                var weather = response?.getJSONArray("weather")
                var aciklama = weather?.getJSONObject(0)?.getString("description")
                tvAciklama.text = aciklama

                var icon = weather?.getJSONObject(0)?.getString("icon")

                if (icon?.last() == 'd') {
                    rootLayout.background = getDrawable(R.drawable.bg)
                    spnSehirler.background.setColorFilter(resources.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
                    tvSehir?.setTextColor(resources.getColor(R.color.colorAccent))
                    tvAciklama.setTextColor(resources.getColor(R.color.colorAccent))
                    tvSicaklik.setTextColor(resources.getColor(R.color.colorAccent))
                    tvTarih.setTextColor(resources.getColor(R.color.colorAccent))
                    tvDerece.setTextColor(resources.getColor(R.color.colorAccent))


                } else {
                    rootLayout.background = getDrawable(R.drawable.gece)
                    tvSehir?.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvAciklama.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvSicaklik.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    spnSehirler.background.setColorFilter(resources.getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP)
                    tvTarih.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvDerece.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                }

                var resimDosyaAdi = resources.getIdentifier("icon_" + icon?.sonKarakteriSil(), "drawable", packageName) //R.drawable.icon_50n
                imgHavaDurumu.setImageResource(resimDosyaAdi)

                tvTarih.text = tarihYazdir()

            }


        },
                object : Response.ErrorListener {

                    override fun onErrorResponse(error: VolleyError?) {
                        Log.e("VOLLEY HATA", "" + error?.printStackTrace())
                    }

                })


        MySingleton.getInstance(this)?.addToRequestQueue(havaDurumuObjeRequest2)

    }


    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        tvSehir = view as TextView

        if (position == 0) {

            location = SimpleLocation(this)

            if(!location!!.hasLocationEnabled()){

                spnSehirler.setSelection(1)
                Toast.makeText(this, "GPS Aç ki yerini bulalım", Toast.LENGTH_LONG).show()
                SimpleLocation.openSettings(this)
            }else{

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),60)
                }else{

                    location = SimpleLocation(this)
                    latitude = String.format("%.6f", location?.latitude)
                    longitude = String.format("%.6f", location?.longitude)
                    Log.e("LAT", "" + latitude)
                    Log.e("LONG", "" + longitude)

                    oankiSehriGetir(latitude, longitude)
                }


            }

        } else {
            var secilenSehir = parent?.getItemAtPosition(position).toString()
            tvSehir = view as TextView
            verileriGetir(secilenSehir)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(requestCode == 60){

            if(grantResults.size > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                location = SimpleLocation(this)
                latitude = String.format("%.6f", location?.latitude)
                longitude = String.format("%.6f", location?.longitude)
                Log.e("LAT", "" + latitude)
                Log.e("LONG", "" + longitude)

                oankiSehriGetir(latitude, longitude)

            }else {
                spnSehirler.setSelection(1)
                Toast.makeText(this, "İzin Vermezsen Konumunu Bulamayız :)", Toast.LENGTH_LONG).show()

            }


        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun verileriGetir(sehir: String) {

        val url = "https://api.openweathermap.org/data/2.5/weather?q=" + sehir + "&appid=a8de8cc50792c8c8387703097fa61a6d&lang=tr&units=metric"

        val havaDurumuObjeRequest = JsonObjectRequest(Request.Method.GET, url, null, object : Response.Listener<JSONObject> {


            override fun onResponse(response: JSONObject?) {

                var main = response?.getJSONObject("main")
                var sicaklik = main?.getInt("temp")
                tvSicaklik.text = sicaklik.toString()


                var sehirAdi = response?.getString("name")


                var weather = response?.getJSONArray("weather")
                var aciklama = weather?.getJSONObject(0)?.getString("description")
                tvAciklama.text = aciklama

                var icon = weather?.getJSONObject(0)?.getString("icon")

                if (icon?.last() == 'd') {
                    rootLayout.background = getDrawable(R.drawable.bg)
                    spnSehirler.background.setColorFilter(resources.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
                    tvSehir?.setTextColor(resources.getColor(R.color.colorAccent))
                    tvAciklama.setTextColor(resources.getColor(R.color.colorAccent))
                    tvSicaklik.setTextColor(resources.getColor(R.color.colorAccent))
                    tvTarih.setTextColor(resources.getColor(R.color.colorAccent))
                    tvDerece.setTextColor(resources.getColor(R.color.colorAccent))


                } else {
                    rootLayout.background = getDrawable(R.drawable.gece)
                    tvSehir?.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvAciklama.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvSicaklik.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    spnSehirler.background.setColorFilter(resources.getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP)
                    tvTarih.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvDerece.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                }

                var resimDosyaAdi = resources.getIdentifier("icon_" + icon?.sonKarakteriSil(), "drawable", packageName) //R.drawable.icon_50n
                imgHavaDurumu.setImageResource(resimDosyaAdi)

                tvTarih.text = tarihYazdir()


            }


        }, object : Response.ErrorListener {

            override fun onErrorResponse(error: VolleyError?) {

            }

        })


        MySingleton.getInstance(this)?.addToRequestQueue(havaDurumuObjeRequest)
    }

    fun tarihYazdir(): String {

        var takvim = Calendar.getInstance().time
        var formatlayici = SimpleDateFormat("EEE, MMM yyyy", Locale("tr"))
        var tarih = formatlayici.format(takvim)

        return tarih


    }
}

private fun String.sonKarakteriSil(): String {
    //50n ifadeyi 50 olarak geriye yollar
    return this.substring(0, this.length - 1)
}