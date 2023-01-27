package com.ynov.vernet.projetdevmobilemto;

import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class Conversion extends AppCompatActivity {

    // Débug
    private static final String TAG = "Conversion";

    public String convertirTemperature(String prefTemperature, String temperature) {
        switch (prefTemperature) {
            case "°C":
                return temperature + " °C";

            case "°F": {
                // Formule de conversion
                double tmp = Double.parseDouble(temperature);
                tmp = (tmp * 9 / 5) + 32;

                // 2 chiffres après la virgule
                DecimalFormat df = new DecimalFormat("#.##");
                return df.format(tmp) + " °F";
            }
            default: {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.error))
                        .setMessage(R.string.erreur_conversion_unite)
                        .setPositiveButton(getString(R.string.ok), null)
                        .show();
                alertDialog.setCanceledOnTouchOutside(false);
            }
        }
        return "ok";
    }

    public String convertirVent(String prefVent, String vent) {

        switch (prefVent) {
            case "km/h":
                return vent + " km/h";

            case "m/s": {
                double ventDouble = Double.parseDouble(vent);
                Log.d(TAG, "vent: " + ventDouble);
                ventDouble = ventDouble / 3.6;
                ventDouble = Math.round(ventDouble * 10) / 10.0;
                DecimalFormat df = new DecimalFormat("#.##");
                return df.format(ventDouble) + " m/s";
            }

            case "mph": {
                double ventDouble = Double.parseDouble(vent);
                Log.d(TAG, "vent: " + ventDouble);
                ventDouble = ventDouble / 1.609;
                ventDouble = Math.round(ventDouble * 10) / 10.0;
                DecimalFormat df = new DecimalFormat("#.##");
                return df.format(ventDouble) + " mph";
            }

            case "kts": {
                double ventDouble = Double.parseDouble(vent);
                Log.d(TAG, "vent: " + ventDouble);
                ventDouble = ventDouble / 1.852;
                ventDouble = Math.round(ventDouble * 10) / 10.0;
                DecimalFormat df = new DecimalFormat("#.##");
                return df.format(ventDouble) + " kts";
            }

            default: {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.error))
                        .setMessage(R.string.erreur_conversion_unite)
                        .setPositiveButton("Ok", null)
                        .show();
                alertDialog.setCanceledOnTouchOutside(false);
            }
        }

        return "ok";
    }
}
