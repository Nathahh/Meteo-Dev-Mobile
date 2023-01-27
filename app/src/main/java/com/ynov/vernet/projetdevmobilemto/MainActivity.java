package com.ynov.vernet.projetdevmobilemto;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // Widgets
    ImageView imageViewIcone;
    EditText editTextVille;
    TextView textViewVille, textViewJour, textViewTemperature, textViewCondition, textViewTMin, textViewTMax, textViewHumidite,
            textViewLeverSoleil, textViewCoucherSoleil, textViewVent;

    // Graphique
    BarChart graph;

    String url, ville;

    // Débug
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewIcone = findViewById(R.id.imageViewIcone);
        editTextVille = findViewById(R.id.editTextVille);
        textViewVille = findViewById(R.id.textViewVille);
        textViewCondition = findViewById(R.id.textViewCondition);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewTMin = findViewById(R.id.textViewTMin);
        textViewTMax = findViewById(R.id.textViewTMax);
        textViewHumidite = findViewById(R.id.textViewHumidite);
        textViewLeverSoleil = findViewById(R.id.textViewLeverSoleil);
        textViewCoucherSoleil = findViewById(R.id.textViewCoucherSoleil);
        textViewVent = findViewById(R.id.textViewVent);
        textViewJour = findViewById(R.id.textViewJour);

        // Graphique
        graph = findViewById(R.id.barChart);

        // Gérer le menu
        new Menu(this, this);

        // Définir les préférences d'unité par défaut
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String temperature = prefs.getString("temperature", null);
        String vent = prefs.getString("vent", null);

        if (temperature == null || vent == null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("temperature", "°C");
            editor.putString("vent", "km/h");
            editor.apply();
        }

        // Vérifier la connexion Internet
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Objects.requireNonNull(Objects.requireNonNull(connectivityManager).getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED || Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED) {
            Log.d(TAG, "onCreate: Internet disponible");

            // Demander la permission LOCALISATION
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        } else {
            Log.d(TAG, "onCreate: Internet indisponible");
            Snackbar.make(findViewById(R.id.fab), getString(R.string.pas_internet), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.activer), v -> startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)))
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Si on a la permission LOCALISATION
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // Récupérer la localisation
            ville = new Localisation(this, this).recupererLocalisation();

            url = "https://www.prevision-meteo.ch/services/json/" + ville;

            // Stocker la ville dans la mémoire
            SharedPreferences sp = getSharedPreferences("prefs", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("ville", ville);
            editor.apply();

            // Si on n'a pas la permission LOCALISATION
        } else if (grantResults.length > 0) {

            // Récupérer le dernier emplacement connu
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            ville = sharedPref.getString("ville", null);

            if (ville != null) {
                url = "https://www.prevision-meteo.ch/services/json/" + ville;
                Toast.makeText(this, getString(R.string.recuperation_derniere_localisation_connue), Toast.LENGTH_LONG).show();

            } else {
                // Récupération de la météo de Paris
                url = "https://www.prevision-meteo.ch/services/json/Paris";

                // Affiche une boîte de texte
                Snackbar.make(findViewById(R.id.barChart), getString(R.string.impossible_recupe_localisation), Snackbar.LENGTH_LONG)
                        .setAction(R.string.activer, v -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                        .show();
            }
        }

        // Si une ville a été envoyée de la VilleActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            url = "https://www.prevision-meteo.ch/services/json/" + extras.getString("ville");

        Log.d(TAG, "onRequestPermissionsResult: " + url);

        RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // Récupérer les données
                        JSONObject jsonObject = new JSONObject(response);

                        // city_info
                        JSONObject city_info = jsonObject.getJSONObject("city_info");
                        String ville = city_info.getString("name");
                        String leveSoleil = city_info.getString("sunrise");
                        String coucheSoleil = city_info.getString("sunset");

                        // current_condition
                        JSONObject current_condition = jsonObject.getJSONObject("current_condition");
                        String icone = current_condition.getString("icon_big");
                        String tmp = current_condition.getString("tmp");
                        String condition = current_condition.getString("condition");
                        String humidite = current_condition.getString("humidity");
                        String vent = current_condition.getString("wnd_spd");

                        // fcst_day_0
                        JSONObject fcst_day_0 = jsonObject.getJSONObject("fcst_day_0");
                        String[] day_long = new String[7];
                        int[] tmaxPrevision = new int[7];
                        day_long[0] = fcst_day_0.getString("day_long");
                        tmaxPrevision[0] = fcst_day_0.getInt("tmax");
                        String tmin = fcst_day_0.getString("tmin");
                        String tmax = fcst_day_0.getString("tmax");

                        // fcst_day_1
                        JSONObject fcst_day_1 = jsonObject.getJSONObject("fcst_day_1");
                        day_long[1] = fcst_day_1.getString("day_long");
                        tmaxPrevision[1] = fcst_day_1.getInt("tmax");

                        // fcst_day_2
                        JSONObject fcst_day_2 = jsonObject.getJSONObject("fcst_day_2");
                        day_long[2] = fcst_day_2.getString("day_long");
                        tmaxPrevision[2] = fcst_day_2.getInt("tmax");

                        // fcst_day_3
                        JSONObject fcst_day_3 = jsonObject.getJSONObject("fcst_day_3");
                        day_long[3] = fcst_day_3.getString("day_long");
                        tmaxPrevision[3] = fcst_day_3.getInt("tmax");

                        // fcst_day_4
                        JSONObject fcst_day_4 = jsonObject.getJSONObject("fcst_day_4");
                        day_long[4] = fcst_day_4.getString("day_long");
                        tmaxPrevision[4] = fcst_day_4.getInt("tmax");

                        // Mettre à jour le widget
                        Context context = this;
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
                        ComponentName thisWidget = new ComponentName(context, Widget.class);
                        remoteViews.setTextViewText(R.id.widgetVille, ville);
                        remoteViews.setTextViewText(R.id.widgetTemperature, tmp + " °C");
                        appWidgetManager.updateAppWidget(thisWidget, remoteViews);

                        // Afficher les données du jour
                        Picasso.get().load(icone).into(imageViewIcone);
                        textViewVille.setText(ville);
                        textViewJour.setText(day_long[0]);

                        Conversion conversion = new Conversion();
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

                        // Récupérer l'unité de mesure de la température
                        String prefTemperature = prefs.getString("temperature", null);

                        // Récupérer l'unité de mesure de la vitesse du vent
                        String prefVent = prefs.getString("vent", null);

                        // Afficher les données converties
                        textViewTemperature.setText(conversion.convertirTemperature(prefTemperature, tmp));
                        textViewTMin.setText(conversion.convertirTemperature(prefTemperature, tmin));
                        textViewTMax.setText(conversion.convertirTemperature(prefTemperature, tmax));
                        textViewCondition.setText(condition);
                        textViewHumidite.setText(humidite + " %");
                        textViewLeverSoleil.setText(leveSoleil);
                        textViewCoucherSoleil.setText(coucheSoleil);
                        textViewVent.setText(conversion.convertirVent(prefVent, vent));


                        // Afficher les prévisions dans un graphique
                        ArrayList<BarEntry> temperature = new ArrayList<>();

                        for (int i = 0; i <= 4; i++) {
                            if (prefTemperature.equals("°C")) {
                                temperature.add(new BarEntry(i, tmaxPrevision[i]));

                            } else if (prefTemperature.equals("°F")) {
                                tmaxPrevision[i] = (tmaxPrevision[i] * 9 / 5) + 32;
                                temperature.add(new BarEntry(i, tmaxPrevision[i]));
                            } else {
                                AlertDialog alertDialog = new AlertDialog.Builder(context)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle(getString(R.string.error))
                                        .setMessage(R.string.erreur_conversion_unite)
                                        .setPositiveButton(getString(R.string.ok), null)
                                        .show();
                                alertDialog.setCanceledOnTouchOutside(false);
                            }
                        }

                        BarDataSet barDataSet = new BarDataSet(temperature, getString(R.string.temperatures));
                        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                        barDataSet.setValueTextColor(Color.BLACK);
                        barDataSet.setValueTextSize(16f);

                        BarData barData = new BarData(barDataSet);
                        graph.setFitBars(true);
                        graph.setData(barData);
                        graph.getDescription().setText(getString(R.string.meteo_semaine));
                        graph.animateY(2000);


                        // Si la ville saisie n'a pas été trouvée
                    } catch (JSONException e) {
                        e.printStackTrace();

                        // Renvoyer la météo de Paris
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("ville", "Paris");
                        startActivity(intent);
                        finish();
                    }
                },

                error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show());

        // Ajouter la requête à la RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.quitter)
                .setMessage(R.string.voulez_vous_vraiment_quitter)
                .setPositiveButton(R.string.oui, (dialogInterface, i) -> {
                    super.onBackPressed();
                    finish();
                })
                .setNegativeButton(R.string.non, null)
                .show();
        alertDialog.setCanceledOnTouchOutside(false);
    }
}