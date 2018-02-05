package ru.ayurmar.arduinocontrol;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static boolean isOnline(Context context){
        NetworkInfo networkInfo = null;
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connManager != null){
            networkInfo = connManager.getActiveNetworkInfo();
        }
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static String formatDate(Date date, Context context){
        String result = "";
        Locale locale = Locale.getDefault();
        Calendar now = Calendar.getInstance();
        Calendar widgetDate = Calendar.getInstance();
        widgetDate.setTime(date);
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        widgetDate.set(Calendar.HOUR_OF_DAY, 0);
        widgetDate.set(Calendar.MINUTE, 0);
        widgetDate.set(Calendar.SECOND, 0);
        widgetDate.set(Calendar.MILLISECOND, 0);
        if(widgetDate.compareTo(now) == 0){
            result += context.getString(R.string.ui_today_at_text);
            result += new SimpleDateFormat(" HH:mm", locale).format(date);
        } else if(now.get(Calendar.DAY_OF_MONTH) - widgetDate.get(Calendar.DAY_OF_MONTH) == 1 &&
                now.get(Calendar.MONTH) == widgetDate.get(Calendar.MONTH) &&
                now.get(Calendar.YEAR) == widgetDate.get(Calendar.YEAR)) {
            result += context.getString(R.string.ui_yesterday_at_text);
            result += new SimpleDateFormat(" HH:mm", locale).format(date);
        } else {
            result += new SimpleDateFormat("dd.MM.yy HH:mm", locale).format(date);
        }
        return result;
    }
}
