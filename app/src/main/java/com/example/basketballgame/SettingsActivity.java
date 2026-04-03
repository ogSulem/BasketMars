package com.example.basketballgame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseUser;

/**
 * Экран настроек: имя игрока, музыка, вибрация, язык, аккаунт Google.
 */
public class SettingsActivity extends AppCompatActivity {

    private EditText playerNameEdit;
    private SwitchMaterial musicSwitch;
    private SwitchMaterial vibrationSwitch;
    private RadioGroup radioLanguage;

    // Виджеты аккаунта
    private TextView accountLabel;
    private Button btnSignIn;
    private Button btnSignOut;

    private AuthManager authManager;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        authManager = AuthManager.getInstance(this);

        playerNameEdit = findViewById(R.id.player_name);
        musicSwitch     = findViewById(R.id.switch_music);
        vibrationSwitch = findViewById(R.id.switch_vibration);
        radioLanguage   = findViewById(R.id.radio_language);
        accountLabel    = findViewById(R.id.account_label);
        btnSignIn       = findViewById(R.id.btn_sign_in);
        btnSignOut      = findViewById(R.id.btn_sign_out);

        Button saveButton  = findViewById(R.id.button_save);
        Button resetButton = findViewById(R.id.button_reset);

        SharedPreferences prefs = getSharedPreferences("basketball", MODE_PRIVATE);
        playerNameEdit.setText(prefs.getString("playerName", getString(R.string.default_player_name)));
        musicSwitch.setChecked(prefs.getBoolean("musicEnabled", true));
        vibrationSwitch.setChecked(prefs.getBoolean("vibrationEnabled", true));

        // Инициализация переключателя языка
        String currentLang = LocaleHelper.getSavedLang(this);
        if (LocaleHelper.LANG_EN.equals(currentLang)) {
            radioLanguage.check(R.id.radio_lang_en);
        } else {
            radioLanguage.check(R.id.radio_lang_ru);
        }
        radioLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String newLang = (checkedId == R.id.radio_lang_en) ? LocaleHelper.LANG_EN : LocaleHelper.LANG_RU;
            if (!newLang.equals(LocaleHelper.getSavedLang(this))) {
                LocaleHelper.saveLang(this, newLang);
                Toast.makeText(this, R.string.language_changed, Toast.LENGTH_LONG).show();
                // Перезапускаем всё приложение чтобы язык применился везде
                Intent restart = getPackageManager().getLaunchIntentForPackage(getPackageName());
                if (restart != null) {
                    restart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(restart);
                    finishAffinity();
                }
            }
        });

        playerNameEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        playerNameEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { saveAndApply(); return true; }
            return false;
        });

        musicSwitch.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("musicEnabled", checked).apply();
            MusicPlayer.ensureState(this);
        });

        vibrationSwitch.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("vibrationEnabled", checked).apply());

        saveButton.setOnClickListener(v -> {
            GameView.animateButton(v);
            saveAndApply();
            finish();
        });

        resetButton.setOnClickListener(v -> {
            GameView.animateButton(v);
            ResetProgress.resetAllProgress(this);
            SharedPreferences p = getSharedPreferences("basketball", MODE_PRIVATE);
            playerNameEdit.setText(p.getString("playerName", getString(R.string.default_player_name)));
            musicSwitch.setChecked(p.getBoolean("musicEnabled", true));
            vibrationSwitch.setChecked(p.getBoolean("vibrationEnabled", true));
            MusicPlayer.ensureState(this);
        });

        View back = findViewById(R.id.button_back);
        back.setOnClickListener(v -> {
            GameView.animateButton(v);
            finish();
        });

        // Кнопка "Войти через Google"
        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> {
                GameView.animateButton(v);
                authManager.signIn(this, AuthManager.RC_SIGN_IN);
            });
        }

        // Кнопка "Выйти"
        if (btnSignOut != null) {
            btnSignOut.setOnClickListener(v -> {
                GameView.animateButton(v);
                authManager.signOut(this, () -> runOnUiThread(this::updateAccountUI));
            });
        }

        updateAccountUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AuthManager.RC_SIGN_IN) {
            authManager.handleSignInResult(data, new AuthManager.SignInCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    String name = user != null ? user.getDisplayName() : null;
                    runOnUiThread(() -> {
                        updateAccountUI();
                        // Если у игрока ещё не задано имя — подставляем имя Google-аккаунта
                        if (name != null && !name.isEmpty()) {
                            SharedPreferences prefs = getSharedPreferences("basketball", MODE_PRIVATE);
                            String current = prefs.getString("playerName", getString(R.string.default_player_name));
                            if (getString(R.string.default_player_name).equals(current)) {
                                playerNameEdit.setText(name);
                                prefs.edit().putString("playerName", name).apply();
                            }
                        }
                        Toast.makeText(SettingsActivity.this,
                                getString(R.string.sign_in_success), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(SettingsActivity.this,
                                    getString(R.string.sign_in_error), Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void updateAccountUI() {
        if (accountLabel == null || btnSignIn == null || btnSignOut == null) return;
        if (authManager.isSignedIn()) {
            String name = authManager.getDisplayName();
            accountLabel.setText(name != null && !name.isEmpty()
                    ? getString(R.string.account_signed_in, name)
                    : getString(R.string.account_signed_in_no_name));
            accountLabel.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
        } else {
            accountLabel.setVisibility(View.GONE);
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
        }
    }

    private void saveAndApply() {
        SharedPreferences prefs = getSharedPreferences("basketball", MODE_PRIVATE);
        String name = playerNameEdit.getText() == null
                ? null : playerNameEdit.getText().toString().trim();
        if (TextUtils.isEmpty(name)) name = getString(R.string.default_player_name);
        prefs.edit().putString("playerName", name).apply();
    }
}
