package com.ynov.vernet.projetdevmobilemto;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class Boot extends BroadcastReceiver {
    String ville;

    private static final String TAG = "Boot";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            // Read
            SharedPreferences sp = context.getSharedPreferences("prefs", Activity.MODE_PRIVATE);
            ville = sp.getString("ville", "Paris");

            // Récupérer la localisation
            String url = "https://www.prevision-meteo.ch/services/json/" + ville;

            // Implement RequestQueue
            RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            // Récupérer les données
                            JSONObject jsonObject = new JSONObject(String.valueOf(response));

                            // city_info
                            JSONObject city_info = jsonObject.getJSONObject("city_info");
                            String ville = city_info.getString("name");

                            // current_condition
                            JSONObject current_condition = jsonObject.getJSONObject("current_condition");
                            String tmp = current_condition.getString("tmp");

                            // Mettre à jour le widget
                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
                            ComponentName thisWidget = new ComponentName(context, Widget.class);
                            remoteViews.setTextViewText(R.id.widgetVille, ville);
                            remoteViews.setTextViewText(R.id.widgetTemperature, tmp + " °C");
                            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },

                    error -> Log.d(TAG, "onReceive: Error"));

            // Ajouter la requête à la RequestQueue.
            queue.add(stringRequest);
        }
    }
}
