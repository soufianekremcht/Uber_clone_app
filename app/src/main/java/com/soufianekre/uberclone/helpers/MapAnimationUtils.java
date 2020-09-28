package com.soufianekre.uberclone.helpers;

import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

public class MapAnimationUtils {

    public static ValueAnimator polylineAnimator() {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(4000);
        return valueAnimator;
    }

//    fun carAnimator(): ValueAnimator {
//        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
//        valueAnimator.duration = 3000
//        valueAnimator.interpolator = LinearInterpolator()
//        return valueAnimator
//    }
}
