package com.ynov.vernet.projetdevmobilemto;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class Localisation extends AppCompatActivity {

    Activity activity;
    Context context;

    // Position GPS
    Location gps_loc = null, network_loc = null;

    String ville;

    // Débug
    private static final String TAG = "Localisation";

    Localisation(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public String recupererLocalisation() {

        // Vérifier les permissions réseaux et GPS plus précis
        LocationManager locationManager = (LocationManager) this.activity.getSystemService(Context.LOCATION_SERVICE);
        try {
            assert locationManager != null;
            try {
                gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } catch (SecurityException e) {
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Récupérer les coordonnées fournies par le GPS ou réseau
        double latitude;
        double longitude;
        Location final_loc;
        if (gps_loc != null) {
            final_loc = gps_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        } else if (network_loc != null) {
            final_loc = network_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        } else {
            latitude = 0.0;
            longitude = 0.0;
        }

        // Déterminer la position en fonction des coordonnées du GPS
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null) {
                // Récupérer le nom de la ville
                ville = addresses.get(0).getLocality();

                // Supprimer les accents de la ville récupérée
                String normalized = Normalizer.normalize(ville, Normalizer.Form.NFD);
                ville = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

            } else {
                ville = "paris";
            }

            // Gestion des erreurs
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERREUR");
        }

        return ville;
    }
}
