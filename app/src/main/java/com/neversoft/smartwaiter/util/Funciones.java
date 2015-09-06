package com.neversoft.smartwaiter.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Usuario on 03/09/2015.
 */
public class Funciones {
    public static String getCurrentDate(String format) {
        // set the format to sql date time
        // yyyy/MM/dd hh:mm:ss
        SimpleDateFormat dateFormat = new SimpleDateFormat(format,
                Locale.getDefault());
        Date date = new Date();
        String dateString = dateFormat.format(date).toString();
        return dateString;

    }

    @SuppressLint("SimpleDateFormat")
    public static String changeStringDateFormat(String date, String oldFormat,
                                                String newFormat) throws ParseException {
        SimpleDateFormat oldDateFormat = new SimpleDateFormat(oldFormat);
        Date originalDate = null;
        originalDate = oldDateFormat.parse(date);

        SimpleDateFormat newDateFormat = new SimpleDateFormat(newFormat);
        String finalDate = newDateFormat.format(originalDate);
        return finalDate;

    }

    public static String getFormattedNumber(double number, String format) {
        // http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
        DecimalFormat df = new DecimalFormat(format);
        return df.format(number);
    }

    @SuppressWarnings("unused")
    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isEditTextEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;

    }
    public static boolean isNetworkAvailable(Context ctxt) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctxt
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        //Checking if there's any  network available
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean hasActiveInternetConnection(Context context) throws Exception {
        if (isNetworkAvailable(context)) {
            //COMENTAR INICIO
//	        try {
//	        	String url=RestHelper.obtainUrlRoot(context);
//	            HttpURLConnection urlc = (HttpURLConnection) (new URL(url).openConnection());
//	            urlc.setRequestProperty("User-Agent", "Test");
//	            urlc.setRequestProperty("Connection", "close");
//	            urlc.setConnectTimeout(150000);
//	            urlc.connect();
//	            return (urlc.getResponseCode() == 200);
//	        } catch (Exception ex) {
//	        	throw new Exception("Error verificando conexión a Internet.");
//
//	        }
            //COMENTAR FIN
            return true;
        } else {
            throw new Exception("La red no está disponible. Verifique su conexión.");
        }
    }
}
