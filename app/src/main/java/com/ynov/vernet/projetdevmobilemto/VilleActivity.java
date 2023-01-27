package com.ynov.vernet.projetdevmobilemto;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class VilleActivity extends AppCompatActivity {

    ImageView imageViewIcone;
    TextView textViewTemperature;
    AutoCompleteTextView autoComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ville);

        // Référence
        imageViewIcone = findViewById(R.id.imageViewIcone);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        autoComplete = findViewById(R.id.autoComplete);

        String[] countries = getResources().getStringArray(R.array.cities);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, countries);
        autoComplete.setAdapter(adapter);

        // Ouvrir le clavier
        autoComplete.requestFocus();

        // 1ere lettre en majuscule
        autoComplete.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        // Au clic du bouton
        findViewById(R.id.btnValider).setOnClickListener(v -> {

            // Si la zone est vide
            if (autoComplete.getText().toString().isEmpty()) {
                autoComplete.setError(getString(R.string.zone_txt_ne_peut_pas_etre_vide));

                // Supprimer l'erreur au bout de 3s
                new Handler().postDelayed(() -> autoComplete.setError(null), 3000);

            } else {

                // Démarrer l'activité en envoyant le nom de la ville saisie
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("ville", autoComplete.getText().toString());
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}