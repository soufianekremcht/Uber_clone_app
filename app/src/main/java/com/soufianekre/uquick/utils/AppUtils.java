package com.soufianekre.uquick.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

public class AppUtils {

    public static void showToast(Context context, String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }
    public static Drawable getDrawable(Context context,int res){
        return context.getResources().getDrawable(res);
    }
}
