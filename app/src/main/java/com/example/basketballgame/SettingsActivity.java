package com.example.basketballgame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {
    private EditText playerNameEdit;
    private SwitchMaterial musicSwitch;
    private SwitchMaterial vibrationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        playerNameEdit = findViewById(R.id.player_name);
        musicSwitch = findViewById(R.id.switch_music);
        vibrationSwitch = findViewById(R.id.switch_vibration);
        Button saveButton = findViewById(R.id.button_save);
        Button resetButton = findViewById(R.id.button_reset);

        SharedPreferences prefs = getSharedPreferences("basketball", MODE_PRIVATE);
        playerNameEdit.setText(prefs.getString("playerName", "Игрок"));
        musicSwitch.setChecked(prefs.getBoolean("musicEnabled", true));
        vibrationSwitch.setChecked(prefs.getBoolean("vibrationEnabled", true));

        playerNameEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        playerNameEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveAndApply();
                return true;
            }
            return false;
        });

        musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("musicEnabled", isChecked).apply();
            MusicPlayer.ensureState(this);
        });

        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
            prefs.edit().putBoolean("vibrationEnabled", isChecked).apply()
        );

        saveButton.setOnClickListener(v -> {
            GameView.animateButton(v);
            saveAndApply();
            finish();
        });

        resetButton.setOnClickListener(v -> {
            GameView.animateButton(v);
            ResetProgress.resetAllProgress(this);
            SharedPreferences p = getSharedPreferences("basketball", MODE_PRIVATE);
            playerNameEdit.setText(p.getString("playerName", "Игрок"));
            musicSwitch.setChecked(p.getBoolean("musicEnabled", true));
            vibrationSwitch.setChecked(p.getBoolean("vibrationEnabled", true));
            MusicPlayer.ensureState(this);
        });

        View back = findViewById(R.id.button_back);
        back.setOnClickListener(v -> {
            GameView.animateButton(v);
            finish();
        });
    }

    private void saveAndApply() {
        SharedPreferences prefs = getSharedPreferences("basketball", MODE_PRIVATE);
        String name = playerNameEdit.getText() == null ? null : playerNameEdit.getText().toString().trim();
        if (TextUtils.isEmpty(name)) name = "Игрок";
        prefs.edit().putString("playerName", name).apply();
    }
}
