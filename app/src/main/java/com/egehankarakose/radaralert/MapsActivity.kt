package com.egehankarakose.radaralert


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_alarm.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.sql.Time
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import java.text.SimpleDateFormat



class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    internal lateinit var MarkerPoints: ArrayList<LatLng>

    var timeAsObj:Time = Time(0,0,0)
    var userLocation :LatLng = LatLng(0.0,0.0)



    var distance :ArrayList<Double> = ArrayList()
    var totalDistanceForCalculation :Double = 0.0
    val speedLimit = 130
    var startTime:Time=Time(0,0,0)
    var endTime:Time=Time(0,0,0)
    var count = 0
    var zoomCounter = false
    var locationChecker = false
    var distanceUserAndStart = 0.0

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var lastLocation: Location

    private var locationUpdateState = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        requestedOrientation = (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        MarkerPoints = ArrayList<LatLng>()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation

            }
        }

        createLocationRequest()

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                onMapReady(mMap)
                mainHandler.postDelayed(this, 5000)
            }
        })



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun getUrl(origin: LatLng, dest: LatLng): String {

        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude


        val units = "units=metric"
        // Sensor enabled
        val sensor = "sensor=false&mode=driving"

        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$units"

        // Output format
        val output = "json"

        val key = "key=AIzaSyDh391K29VR1BmjEgm-lRXH7ifYUawK9OY"

        // Building the url to the web service
        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&$key"


        return url
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        var origin:LatLng
        var dest :LatLng
        var url: String


        //getLocation from gps service

        mMap.isMyLocationEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {


                    userLocation = LatLng(location.latitude,location.longitude)
                    mMap.clear()

                    if (zoomCounter == false) {
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                        zoomCounter = true
                    }

                    for (i in MarkerPoints){
                        mMap.addMarker(MarkerOptions().position(i))
                    }

                    if(locationChecker && MarkerPoints.size == 2){

                        origin = userLocation
                        dest = MarkerPoints[1]

                        mMap.clear()


                        mMap.addMarker(MarkerOptions().position(dest).title("Finish"))


                        var options2 = PolylineOptions()

                        // Getting URL to the Google Directions API
                        url = getUrl(origin, dest)

                        Log.d("onMapClick", url)
                        GetDirection(url).execute()


                    }

                    else if (MarkerPoints.size >= 2) {
                        origin = MarkerPoints[0]
                        dest = MarkerPoints[1]

                        mMap.clear()


                        mMap.addMarker(MarkerOptions().position(origin).title("Start"))
                        mMap.addMarker(MarkerOptions().position(dest).title("Finish"))
                        distanceUserAndStart = calculateDistance(userLocation,origin)

                        if(distanceUserAndStart <= 100){

                            MarkerPoints[0] = userLocation
                            mMap.clear()
                            mMap.addMarker(MarkerOptions().position(dest).title("Finish"))
                            locationChecker = true

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startTime = Time(LocalTime.now().hour, LocalTime.now().minute,LocalTime.now().second)
                            } else {
                                var date = Date()
                                var formatter = SimpleDateFormat("HH:mm:ss")
                                var answer : String = formatter.format(date)
                                startTime = stringToTime(answer)

                            }


                            url = getUrl(origin, dest)
                            GetDirection(url).execute()

                        }

                    }


            }






        }

        /**  permissions  */


        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)

        }


        mMap!!.setOnMapClickListener { point ->
            // Already two locations
            if (MarkerPoints.size >= 2) {
                MarkerPoints.removeAll(MarkerPoints)
                if (timeCounter){
                    timer.cancel()
                    timeCounter = false

                }
                timerText.text = ""
                speedTextView.text = ""
                distanceTextView.text = ""
                totalDistance.text = ""
                totalTime.text = ""

                mMap.clear()


                distance.removeAll(distance)
                locationChecker = false
                count = 0


            }

            // Adding new item to the ArrayList
            MarkerPoints.add(point)

            mMap.addMarker(MarkerOptions().position(point))

            // Checks, whether start and end locations are captured
            if (MarkerPoints.size >= 2) {
                origin = MarkerPoints[0]
                dest = MarkerPoints[1]

                distanceUserAndStart = calculateDistance(userLocation,origin)

                if(distanceUserAndStart <= 100){


                    mMap.clear()

                    mMap.addMarker(MarkerOptions().position(dest))

                    var options2 = PolylineOptions()

                    // Getting URL to the Google Directions API
                    url = getUrl(userLocation, dest)

                    Log.d("onMapClick", url)
                    GetDirection(url).execute()

                }

            }

        }

    }

    /**
     * Choose a destination and it return distance , time to arrive
     * And it is check Gives you a alarmDialog if you're within 5 km
     *
     *
     * */


    private inner class GetDirection(val url : String) : AsyncTask<Void,Void,List<List<LatLng>>>(){

        var distanceText = ""
        var waitingTime = ""
        var waitingTimeAsTime :Time = Time(0,0,0)
        var diff:Time = Time(0,0,0)
        var distanceAsNumber = 0.0



        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()
                Log.d("GoogleMap" , " data : $data")
            val result =  ArrayList<List<LatLng>>()

            try{
                val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)

                val path =  ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size-1)){
//                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
//                    path.add(startLatLng)
//                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())

                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                    distanceText = respObj.routes[0].legs[0].distance.text
                    distanceAsNumber = respObj.routes[0].legs[0].distance.value.toDouble()


                    if(distanceAsNumber < 2000){

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            endTime = Time(LocalTime.now().hour, LocalTime.now().minute,LocalTime.now().second)
                        } else {
                            var date = Date()
                            var formatter = SimpleDateFormat("HH:mm:ss")
                            var answer : String = formatter.format(date)
                            endTime = stringToTime(answer)

                        }
                        diff = difference(endTime,startTime)

                    }

                }

                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {

            val lineoption = PolylineOptions()
            var time = ""
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
            distanceTextView.text = "Kalan :"+ distanceText
            distance.add(distanceAsNumber)
            if(distance.size == 1){

                totalDistanceForCalculation = distanceAsNumber

                timeAsObj =  doubleToTime(totalDistanceForCalculation)

                time = timeToString(timeAsObj)

                totalDistance.text = "Toplam Mesafe: "+ distanceText

                totalTime.text = "Zaman: " + time

            }

            if(distance.size >= 2){
                var index = distance.size.minus(2)
                speedTextView.text = "Hız :" + Math.abs(((distance.get(index) - distance.get(index+1))/(5*1000)*3600)).toString().take(4)+" km/s"

            }

            totalDistanceForCalculation = distanceAsNumber


            if(distanceAsNumber <= 2000 && !locationChecker ){

                waitingTimeAsTime = timeAsObj
                waitingTime = timeAsObj.toString()

            }
            /** Exit from toll booth */

            else if(distanceAsNumber <= 50){
                Toast.makeText(applicationContext,"Gişeden çıkınız",Toast.LENGTH_SHORT).show()
                MarkerPoints.removeAll(MarkerPoints)
                mMap.clear()

            }
            /** Exit from toll booth */
            else{

                if(timeAsObj.hours >= diff.hours && timeAsObj.minutes >= diff.minutes){

                    waitingTimeAsTime = difference(timeAsObj,diff)
                    waitingTime  = waitingTimeAsTime.toString()


                }else{

                    waitingTimeAsTime = Time(0,0,0)
                    waitingTime = "00:00"

                }

            }

            if(distanceAsNumber < 2000 && count <= 0){

                var alarmDialog = AlarmFragment(waitingTimeAsTime)
                alarmDialog.show(supportFragmentManager,"showDialog")

                count = 1

                timer(waitingTimeAsTime)

            }

        }

    }


    /**
     * it change type of object Time to String  as HH:MM:SS
     * */

    private fun timeToString(time: Time): String{

        var onlyHour = time.hours.toString()
        var onlyMinute = time.minutes.toString()
        var onlySec = time.seconds.toString()


        var h = ""
        var m = ""
        var s = ""

        if(onlyHour.length == 1 ){
            h = "0"+onlyHour
        }
        if(onlyHour.toInt() == 0 ){
            h = "00"

        }
        if(onlyMinute.length == 1){
            m = "0" + onlyMinute
        }
        else if(onlyMinute.toInt()  == 0){
            m = "00"
        }
        else{
            m = onlyMinute

        }

        if(onlySec.length == 1){
            s = "0"+onlySec
        }
        else if(onlySec.toInt() == 0){
            s = "00"

        }else{
            s = onlySec
        }

        return h + ":" + m + ":" + s

    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }


    /**
     * It calculates time to arrive destination point via speedLimit and distance between start and end points
     * */
    private fun doubleToTime(value : Double) : Time {

        var hour = (value/(speedLimit*1000))
        var minute = ((value/1000)%speedLimit*60/speedLimit)
        var second = ((value/1000)%speedLimit*60%speedLimit)*60/speedLimit


        /*
        var onlyHour = hour.split(".")
        var onlyMinute = minute.split(".")


        var h = ""
        var m = ""

        if(onlyHour.get(0).length == 1 ){
            h = "0"+onlyHour.get(0)
        }
        if(onlyHour.get(0).toInt() == 0 ){
            h = "00"

        }
        if(onlyMinute.get(0).length == 1){
            m = "0" + onlyMinute.get(0)
        }
        else if(onlyMinute.get(0).toInt()  == 0){
            m = "00"
        }
        else{
            m = onlyMinute.get(0)

        }
*/

        var result = Time(hour.toInt(),minute.toInt(),second.toInt())   // " h : m : s"
        Log.d("result",result.toString())

        return  result

    }


    /** It calculates difference of end and start times*/

    fun difference(start: Time, stop: Time): Time {

        val diff = Time(0, 0, 0)
        if (stop.seconds > start.seconds) {
            --start.minutes
            start.seconds += 60
        }
        diff.seconds = start.seconds - stop.seconds

        if (stop.minutes > start.minutes) {
            --start.hours
            start.minutes += 60
        }

        diff.minutes = start.minutes - stop.minutes
        diff.hours = start.hours - stop.hours
        return diff
    }

    /** Sets timer if you are close your destination point about 5 km  */

    lateinit var timer : CountDownTimer
    private var timeCounter = false

    fun timer(time :Time){

            var timeAsLong : Long = timeToMillis(time)
 
             timer = object : CountDownTimer( timeAsLong* 1000, 1000) {
                override fun onFinish(){
                    timeCounter = false
                    Toast.makeText(applicationContext,"Süre Doldu Yolculuğu Tamamlayabilirsiniz", Toast.LENGTH_SHORT).show()
                }

                override fun onTick(millisUntilFinished: Long) {
                    timeCounter = true
                    var secondsRemaining = millisUntilFinished / 1000 % 60
                    var minutesRemaining = millisUntilFinished / 1000 / 60 % 60
                    var hoursRemaining = millisUntilFinished /1000/60/60
                    timerText.setText(hoursRemaining.toString()+":"+minutesRemaining.toString()+":"+secondsRemaining.toString())
                }

            }.start()
        if(timeCounter){
            timer.cancel()

        }

    }

    private fun stringToTime(time: String) : Time {

        var timeParse = time.split(":")

        var timeAsTime = Time(timeParse.get(0).toInt(),timeParse.get(1).toInt(),timeParse.get(2).toInt())

        return timeAsTime
    }

    /** It converts time to milliseconds to use in timer*/

    private fun timeToMillis(time :Time): Long{

        return (time.hours*3600 + time.minutes*60 + time.seconds).toLong()

    }

    private fun calculateDistance(origin : LatLng, destination :LatLng) :Double {
        var lat1 = origin.latitude
        var lat2 = destination.latitude

        var lon1 = origin.longitude
        var lon2 = destination.longitude


        lon1 = Math.toRadians(lon1)
        lon2 = Math.toRadians(lon2)
        lat1 = Math.toRadians(lat1)
        lat2 = Math.toRadians(lat2)

        // Haversine formula
        val dlon = lon2 - lon1
        val dlat = lat2 - lat1
        val a = Math.pow(Math.sin(dlat / 2), 2.0) + (Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2.0))

        val c = 2 * Math.asin(Math.sqrt(a))

        // Radius of earth in kilometers. Use 3956
        // for miles
        val r = 6371.0

        // calculate the result
        return c * r * 1000
    }

}

