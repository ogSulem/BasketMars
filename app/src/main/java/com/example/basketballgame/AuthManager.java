package com.example.basketballgame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Управляет аутентификацией через Google Sign-In + Firebase Auth.
 *
 * <p>Использование:
 * <pre>
 *   // В Activity:
 *   AuthManager auth = AuthManager.getInstance(this);
 *   auth.signIn(this, RC_SIGN_IN);                  // запустить интент выбора аккаунта
 *
 *   // В onActivityResult:
 *   auth.handleSignInResult(data, callback);
 * </pre>
 * </p>
 *
 * <p><b>Важно:</b> для работы нужен реальный {@code google-services.json}.
 * Файл-заглушка позволяет собрать APK, но Firebase не подключится до замены файла.</p>
 */
public class AuthManager {

    public interface SignInCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    private static final String TAG = "AuthManager";
    /** Код запроса для {@code startActivityForResult} при входе через Google. */
    public static final int RC_SIGN_IN = 9001;

    private static volatile AuthManager instance;

    private final FirebaseAuth firebaseAuth;
    private final GoogleSignInClient googleSignInClient;

    private AuthManager(Context ctx) {
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ctx.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(ctx.getApplicationContext(), gso);
    }

    /** Получить singleton AuthManager. */
    public static AuthManager getInstance(@NonNull Context ctx) {
        if (instance == null) {
            synchronized (AuthManager.class) {
                if (instance == null) {
                    instance = new AuthManager(ctx.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /** Текущий аутентифицированный пользователь или {@code null}. */
    @Nullable
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /** Вошёл ли пользователь в систему. */
    public boolean isSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /** UID пользователя или {@code null} если не авторизован. */
    @Nullable
    public String getUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /** Отображаемое имя пользователя (из Google-аккаунта) или {@code null}. */
    @Nullable
    public String getDisplayName() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getDisplayName() : null;
    }

    /**
     * Запустить экран выбора Google-аккаунта.
     * Результат обрабатывается в {@link #handleSignInResult(Intent, SignInCallback)}.
     *
     * @param activity Activity для {@code startActivityForResult}.
     * @param requestCode обычно {@link #RC_SIGN_IN}.
     */
    public void signIn(@NonNull Activity activity, int requestCode) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, requestCode);
    }

    /**
     * Обрабатывает ответ из {@code onActivityResult} при входе через Google.
     */
    public void handleSignInResult(@Nullable Intent data, @NonNull SignInCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken(), callback);
        } catch (ApiException e) {
            Log.w(TAG, "Google sign-in failed: " + e.getStatusCode(), e);
            callback.onFailure(e);
        }
    }

    /** Обменять Google ID-токен на Firebase credential. */
    private void firebaseAuthWithGoogle(@Nullable String idToken, @NonNull SignInCallback callback) {
        if (idToken == null) {
            callback.onFailure(new IllegalStateException("Google ID token is null"));
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    Log.d(TAG, "signInWithCredential:success uid=" + (user != null ? user.getUid() : "null"));
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "signInWithCredential:failure", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Выйти из аккаунта (Firebase + Google).
     *
     * @param context контекст
     * @param onDone  выполняется после завершения выхода (на главном потоке)
     */
    public void signOut(@NonNull Context context, @Nullable Runnable onDone) {
        firebaseAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Log.d(TAG, "Google sign-out complete");
            if (onDone != null) onDone.run();
        });
    }
}
