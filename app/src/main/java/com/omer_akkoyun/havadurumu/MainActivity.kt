package com.omer_akkoyun.havadurumu

import android.content.pm.PackageManager
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    var lokasyon: SimpleLocation? = null
    var enlem: String? = null
    var boylam: String? = null
    var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var spinnerAdapter =
            ArrayAdapter.createFromResource(this, R.array.sehirler, R.layout.teksatir_spinner)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.background.setColorFilter(
            resources.getColor(R.color.beyaz),
            PorterDuff.Mode.SRC_ATOP
        )
        spinner.adapter = spinnerAdapter

        spinner.setTitle("Şehir Seç")
        spinner.setPositiveButton("Seç")
        spinner.setOnItemSelectedListener(this) //spinner bir eleman seçimini dinliyor


        verileriGetir("İstanbul") //Başlangıç şehri


    }

    fun verileriGetir(sehir: String) {


        if (spinner.getSelectedItem().toString() == spinner.getItemAtPosition(1).toString()) {

            lokasyon = SimpleLocation(this) //enlem ve boylamı almak  için gerekli elemanımız

            //GPS kapalıysa konum bilgisi için
            if (!lokasyon!!.hasLocationEnabled()) {
                Toast.makeText(this, "Konum bilgileri bulunamadı.\nKonum için izinlerini açın!", Toast.LENGTH_LONG).show()
                SimpleLocation.openSettings(this) //Konum açma ayarları sayfasına git
            } else {
                //GPS açıksa Kullanıcıdan İzin İsteyelim Kullanmak için
                //Eğer İzin verilmemiş ise İzin iste Sor
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        60
                    ) //girilen sayı değerini, gelen izne OK denildiğinde ,kontrol için kullanıcaz
                }
                //GPS açık ve konum kullanımına da izin verilmiş ise burası çalışacak
                else {

                    //Enlem ve boylam alınsın
                    enlem = String.format("%.2f", lokasyon?.getLatitude()).toString()
                    boylam = String.format("%.2f", lokasyon?.getLongitude()).toString()
                    url =
                        "https://api.openweathermap.org/data/2.5/weather?lat=" + enlem + "&lon=" + boylam + "&appid=7a2da98c86203200be5334f06d351155&lang=tr&units=metric"
                }
            }
        } else {
            url =
                "https://api.openweathermap.org/data/2.5/weather?q=$sehir&appid=7a2da98c86203200be5334f06d351155&lang=tr&units=metric"
        }

        val havadurumuRequestim = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject?) {


                    val jsonMain =
                        response?.getJSONObject("main") //gelen obje arasında main olanı al.
                    val derece = jsonMain?.getInt("temp")
                    val nem = jsonMain?.getInt("humidity")

                    val jsonWind = response?.getJSONObject("wind")
                    val ruzgar = jsonWind?.getString("speed")

                    var sehiradi = response?.getString("name").toString()

                    val jsonWeather = response?.getJSONArray("weather")
                    val aciklama = jsonWeather?.getJSONObject(0)?.getString("description")
                    val ikonismi = jsonWeather?.getJSONObject(0)?.getString("icon").toString()
                    val ikonismiDuzenli =
                        ("" + ikonismi[0] + "" + ikonismi[1] + "d") //gelen verinin sondaki harfi hep d harfi olacak


                    if ((ikonismi == "01d") or (ikonismi == "02d") or (ikonismi == "03d")) { //güneşli zamanalr

                        AnaSayfa.setBackgroundResource(R.drawable.bggunduz)

                    } else if ((ikonismi == "04d") or (ikonismi == "04n") or (ikonismi == "10d") or (ikonismi == "10n")) {
                        AnaSayfa.setBackgroundResource(R.drawable.kapali) //Kapalı havalar

                    } else if ((ikonismi == "09d") or (ikonismi == "09n") or (ikonismi == "11d") or (ikonismi == "11n")) { //yağmurluysa

                        AnaSayfa.setBackgroundResource(R.drawable.yagmurlu)

                    } else if ((ikonismi == "50d") or ((ikonismi == "50n"))) { //sisliyse
                        AnaSayfa.setBackgroundResource(R.drawable.sisli)

                    } else if ((ikonismi == "13d") or ((ikonismi == "13n"))) { //Karlıysa
                        AnaSayfa.setBackgroundResource(R.drawable.karli)
                    } else {
                        AnaSayfa.setBackgroundResource(R.drawable.gece)
                    }

                    val havaResmi =
                        resources.getIdentifier("img" + ikonismiDuzenli, "drawable", packageName)
                    havaResimImageView.setImageResource(havaResmi)
                    aciklamaTV.setText(aciklama)
                    ilTextView.setText(sehiradi)

                    dereceTV.text = (derece.toString() + "°")
                    tarihTV.text = tarihYazdir()
                    nemTV.text = "Nem %" + nem.toString()
                    ruzgarTV.text = "Rüzgar " + ruzgar.toString() + "km/s"


                }

            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                }
            })
        MySingleton.getInstance(this).addToRequestQueue(havadurumuRequestim)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

        var secilenSehir = p0?.getItemAtPosition(p2).toString()

        verileriGetir(secilenSehir)

    }

    fun tarihYazdir(): String {

        var takvim = Calendar.getInstance().time
        var formatlayici = SimpleDateFormat("d MMMM yyyy EEEE ", Locale("tr"))
        var tarih = formatlayici.format(takvim)

        return tarih
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){

        if (grantResults.size > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {//PackageManager.PERMISSION_GRANTED arkaplanda sıfır demek
            //Enlem ve boylam alınsın
            enlem = String.format("%.2f", lokasyon?.getLatitude()).toString()
            boylam = String.format("%.2f", lokasyon?.getLongitude()).toString()
            url =
                "https://api.openweathermap.org/data/2.5/weather?lat=" + enlem + "&lon=" + boylam + "&appid=7a2da98c86203200be5334f06d351155&lang=tr&units=metric"
        }else{
            spinner.setSelection(0) //Şehir seçin yazısı gelsin
            Toast.makeText(this, "Konum bilgileri bulunamadı.\nKonum için izinlerini açın!", Toast.LENGTH_LONG).show()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
