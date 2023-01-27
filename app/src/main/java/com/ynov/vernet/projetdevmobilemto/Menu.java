package com.ynov.vernet.projetdevmobilemto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class Menu extends Activity {

    ExtendedFloatingActionButton fab;
    FloatingActionButton villeFab, localisationFab, parametreFab;
    Boolean isAllFabsVisible;

    Activity activity;
    Context context;

    // Débug
    private static final String TAG = "Menu";

    public Menu(Activity activity, Context context) {

        this.activity = activity;
        this.context = context;

        // Référence
        fab = this.activity.findViewById(R.id.fab);
        villeFab = this.activity.findViewById(R.id.villeFab);
        localisationFab = this.activity.findViewById(R.id.localisationFab);
        parametreFab = this.activity.findViewById(R.id.parametreFab);

        // Masquer les éléments au lancement
        villeFab.setVisibility(View.GONE);
        localisationFab.setVisibility(View.GONE);
        parametreFab.setVisibility(View.GONE);
        isAllFabsVisible = false;

        // Dérouler le menu
        fab.setOnClickListener(v -> {
            if (!isAllFabsVisible) {
                ViewCompat.animate(fab)
                        .rotation(135.0F)
                        .withLayer()
                        .setDuration(300L)
                        .setInterpolator(new OvershootInterpolator(10.0F))
                        .start();
                villeFab.show();
                localisationFab.show();
                parametreFab.show();
                fab.extend();
                isAllFabsVisible = true;
            } else {
                ViewCompat.animate(fab)
                        .rotation(0.0F)
                        .withLayer()
                        .setDuration(300L)
                        .setInterpolator(new OvershootInterpolator(10.0F))
                        .start();
                villeFab.hide();
                localisationFab.hide();
                parametreFab.hide();
                fab.shrink();
                isAllFabsVisible = false;
            }
        });

        // Récupérer la localisation de l'emplacement du téléphone
        localisationFab.setOnClickListener(v -> {

                    // Si il y a une connexion Internet
                    ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (Objects.requireNonNull(Objects.requireNonNull(connectivityManager).getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED || Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED) {
                        this.activity.startActivity(new Intent(context, MainActivity.class));
                        this.activity.finish();
                    } else {
                        Snackbar.make(this.activity.findViewById(R.id.fab), this.activity.getString(R.string.pas_internet), Snackbar.LENGTH_LONG)
                                .setAction(this.activity.getString(R.string.activer), view -> this.context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)))
                                .show();

                        // Masquer le menu
                        ViewCompat.animate(fab)
                                .rotation(0.0F)
                                .withLayer()
                                .setDuration(300L)
                                .setInterpolator(new OvershootInterpolator(10.0F))
                                .start();
                        villeFab.hide();
                        localisationFab.hide();
                        parametreFab.hide();
                        isAllFabsVisible = false;
                    }
                });

        // Saisir une ville manuellement
        villeFab.setOnClickListener(v -> {
            this.activity.startActivity(new Intent(context, VilleActivity.class));
            this.activity.finish();
        });

        // Paramètres
        parametreFab.setOnClickListener(v -> {
            this.activity.startActivity(new Intent(context, ParametresActivity.class));
            this.activity.finish();
        });
    }
}
