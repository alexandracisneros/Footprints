package com.neversoft.smartwaiter.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.neversoft.smartwaiter.ui.CambiarUsuarioActivity;
import com.neversoft.smartwaiter.ui.CerrarDiaActivity;
import com.neversoft.smartwaiter.ui.ConsultarReservasActivity;
import com.neversoft.smartwaiter.ui.IniciarDiaActivity;
import com.neversoft.smartwaiter.ui.MesasActivity;
import com.neversoft.smartwaiter.ui.PedidosARecogerActivity;
import com.neversoft.smartwaiter.ui.PedidosFacturarActivity;
import com.neversoft.smartwaiter.ui.SincronizarActivity;
import com.neversoft.smartwaiter.ui.SmartWaiter;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    public static String getDateFromTimeStamp(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat,Locale.getDefault());

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static long getCurrentTimeStamp() {
        Date date = new Date();
        return date.getTime();
    }
    public static Date addDays(int numberOfDays){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, numberOfDays);
        Date newDate = cal.getTime();
        return newDate;
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

    public static String makePlaceholders(int len) throws Exception {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    public static int PerceivedBrightness(String htmlColor) {
        int c = Color.parseColor(htmlColor);
        return (int) Math.sqrt(
                Color.red(c) * Color.red(c) * .299 +
                        Color.green(c) * Color.green(c) * .587 +
                        Color.blue(c) * Color.blue(c) * .114);
    }

    public static void selectMenuOption(final WeakReference<AppCompatActivity> mReference, int position) {
        final AppCompatActivity activity = mReference.get();
        Intent intent;
        switch (position) {
            case SmartWaiter.OPCION_INICIAR_DIA:
                intent = new Intent(activity, IniciarDiaActivity.class);
                activity.startActivity(intent);
                activity.finish();
                break;
            case SmartWaiter.OPCION_SINCRONIZAR:
                intent = new Intent(activity, SincronizarActivity.class);
                activity.startActivity(intent);
                activity.finish();
                break;
            case SmartWaiter.OPCION_RESERVAS:
                intent = new Intent(activity, ConsultarReservasActivity.class);
                activity.startActivity(intent);
                activity.finish();
                break;
            case SmartWaiter.OPCION_TOMAR_PEDIDO:
                intent = new Intent(activity, MesasActivity.class);
                activity.startActivity(intent);
                activity.finish();
                break;
            case SmartWaiter.OPCION_PEDIDOS_RECOGER:
                intent = new Intent(activity, PedidosARecogerActivity.class);
                activity.startActivity(intent);
                activity.finish();
                break;
            case SmartWaiter.OPCION_PEDIDOS_FACTURAR:
                intent = new Intent(activity, PedidosFacturarActivity.class);
                activity.startActivity(intent);
                activity.finish();
                break;
            case SmartWaiter.OPCION_CERRAR_DIA:
                intent = new Intent(activity, CerrarDiaActivity.class);
                activity.startActivity(intent);
                activity.finish();
                break;
            case SmartWaiter.OPCION_USUARIO:
                intent = new Intent(activity, CambiarUsuarioActivity.class);
                activity.startActivity(intent);
                activity.finish();
                break;

        }

    }

    public boolean isThisServiceRunning(Class<?> serviceClass, Context ctxt) {
        ActivityManager manager = (ActivityManager) ctxt.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
