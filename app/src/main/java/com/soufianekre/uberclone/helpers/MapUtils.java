package com.soufianekre.uberclone.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.soufianekre.uberclone.R;

public class MapUtils {

    public static Bitmap getOriginDestinationMarkerBitmap(){
        int height = 20;
        int  width = 20;
        Bitmap bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor( Color.BLACK);
        paint.setStyle( Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        return bitmap;
    }

//    fun getCarBitmap(context: Context): Bitmap {
//        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car)
//        return Bitmap.createScaledBitmap(bitmap, 50, 100, false)
//    }
//
//    fun getRotation(start: LatLng, end: LatLng): Float {
//        val latDifference: Double = abs(start.latitude - end.latitude)
//        val lngDifference: Double = abs(start.longitude - end.longitude)
//        var rotation = -1F
//        when {
//            start.latitude < end.latitude && start.longitude < end.longitude -> {
//                rotation = Math.toDegrees(atan(lngDifference / latDifference)).toFloat()
//            }
//            start.latitude >= end.latitude && start.longitude < end.longitude -> {
//                rotation = (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 90).toFloat()
//            }
//            start.latitude >= end.latitude && start.longitude >= end.longitude -> {
//                rotation = (Math.toDegrees(atan(lngDifference / latDifference)) + 180).toFloat()
//            }
//            start.latitude < end.latitude && start.longitude >= end.longitude -> {
//                rotation =
//                        (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 270).toFloat()
//            }
//        }
//        return rotation
//    }

}
