package com.soufianekre.uberclone.ui.customer;

public class PolyAnimationsToTest {


    //TODO : Clean This UP

    /*
    override fun showPath(latLngList: List<LatLng>) {
        val builder = LatLngBounds.Builder()
        for (latLng in latLngList) {
            builder.include(latLng)
        }
        val bounds = builder.build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2))
        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.GRAY)
        polylineOptions.width(5f)
        polylineOptions.addAll(latLngList)
        greyPolyLine = googleMap.addPolyline(polylineOptions)

        val blackPolylineOptions = PolylineOptions()
        blackPolylineOptions.width(5f)
        blackPolylineOptions.color(Color.BLACK)
        blackPolyline = googleMap.addPolyline(blackPolylineOptions)

        originMarker = addOriginDestinationMarkerAndGet(latLngList[0])
        originMarker?.setAnchor(0.5f, 0.5f)
        mDestinationMarker = addOriginDestinationMarkerAndGet(latLngList[latLngList.size - 1])
        mDestinationMarker?.setAnchor(0.5f, 0.5f)

        val polylineAnimator = MapAnimationUtils.polyLineAnimator()
        polylineAnimator.addUpdateListener { valueAnimator ->
                val percentValue = (valueAnimator.animatedValue as Int)
            val index = (greyPolyLine?.points!!.size * (percentValue / 100.0f)).toInt()
            blackPolyline?.points = greyPolyLine?.points!!.subList(0, index)
        }
        polylineAnimator.start()
    }*/
    // Update Car Movement On The Map
    /*
private var movingCabMarker: Marker? = null
private var previousLatLng: LatLng? = null
private var currentLatLng: LatLng? = null


private fun updateCarLocation(latLng: LatLng) {
    if (movingCabMarker == null) {
        movingCabMarker = addCarMarkerAndGet(latLng)
    }
    if (previousLatLng == null) {
        currentLatLng = latLng
        previousLatLng = currentLatLng
        movingCabMarker?.position = currentLatLng
        movingCabMarker?.setAnchor(0.5f, 0.5f)
        animateCamera(currentLatLng!!)
    } else {
        previousLatLng = currentLatLng
        currentLatLng = latLng
        val valueAnimator = AnimationUtils.carAnimator()
        valueAnimator.addUpdateListener { va ->
            if (currentLatLng != null && previousLatLng != null) {
                val multiplier = va.animatedFraction
                val nextLocation = LatLng(
                    multiplier * currentLatLng!!.latitude + (1 - multiplier) * previousLatLng!!.latitude,
                    multiplier * currentLatLng!!.longitude + (1 - multiplier) * previousLatLng!!.longitude
                )
                movingCabMarker?.position = nextLocation
                val rotation = MapUtils.getRotation(previousLatLng!!, nextLocation)
                if (!rotation.isNaN()) {
                    movingCabMarker?.rotation = rotation
                }
                movingCabMarker?.setAnchor(0.5f, 0.5f)
                animateCamera(nextLocation)
            }
        }
        valueAnimator.start()
    }
}*/


   /* private lateinit var handler: Handler
    private lateinit var runnable: Runnable


    private fun showMovingCab(cabLatLngList: ArrayList<LatLng>) {
        handler = Handler()
        var index = 0
        runnable = Runnable {
            run {
                if (index < 10) {
                    updateCarLocation(cabLatLngList[index])
                    handler.postDelayed(runnable, 3000)
                            ++index
                } else {
                    handler.removeCallbacks(runnable)
                    Toast.makeText(this@MainActivity, "Trip Ends", Toast.LENGTH_LONG).show()
                }
            }
        }
        handler.postDelayed(runnable, 5000)
    }*/

//   OnMapREady(){
//       Handler().postDelayed(Runnable {
//           showPath(MapUtils.getListOfLocations())
//           showMovingCab(MapUtils.getListOfLocations())
//       }, 3000)
//   }



}
