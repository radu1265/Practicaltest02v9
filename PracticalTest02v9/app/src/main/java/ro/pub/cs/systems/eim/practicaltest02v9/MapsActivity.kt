package ro.pub.cs.systems.eim.practicaltest02v9


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Inițializare hărți
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 4.a) Centrăm harta în Ghelmegioaia, Romania
        // Coordonate aproximative Ghelmegioaia (exemplu fictiv).44.66 22.82
        val ghelmegioaia = LatLng(44.66, 22.82)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ghelmegioaia, 7f))
        mMap.addMarker(MarkerOptions().position(ghelmegioaia).title("Marker în Ghelmegioaia"))

        // 4.b) Punem un marker pe București
        val bucharest = LatLng(44.4325, 26.1039)
        mMap.addMarker(MarkerOptions().position(bucharest).title("Marker în București"))
    }
}
