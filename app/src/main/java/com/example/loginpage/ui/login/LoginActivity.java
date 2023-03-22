package com.example.loginpage.ui.login;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loginpage.R;
import com.example.loginpage.data.model.LoginResponse;
import com.example.loginpage.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginResponse loginResponse;
    private static final String TAG = "LoginActivity";
//    private static final int NOTIFICATION_REQUEST_CODE = 1234;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications permission granted",Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(this, "FCM can't post notifications without POST_NOTIFICATIONS permission",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        if(getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "key: " + key + "Value: " + value);
            }
        }

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final EditText adminEditText = binding.adminId;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        Retrofit retrofit = RetrofitService.getInstance();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failde.", task.getException());
                            return;
                        }

                        String token = task.getResult();

                        String msg = "FCM Registration token: " + token;
                        Log.d("fcm main activity" ,msg );
                        Toast.makeText(getApplicationContext() ,msg, Toast.LENGTH_SHORT).show();
                    }
                });

        askNotificationPermission();


        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginDataChanged(adminEditText.toString(),
                        usernameEditText.toString(),
                        passwordEditText.toString());
            }
        };
        adminEditText.addTextChangedListener(afterTextChangedListener);
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    openSignin(adminEditText.toString(),
//                            usernameEditText.toString(),
//                            passwordEditText.toString());
                    System.out.println("onEditorAction");
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);

                String adminId = binding.adminId.getText().toString();
                String userId = binding.username.getText().toString();
                //비밀번호 암호화
                String rawPassword = binding.password.getText().toString();
                String userPassword = null;
                try {
                    userPassword = hmacSha1Encoding(rawPassword);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                JsonObject body = new JsonObject();
                JsonObject user = new JsonObject();

                user.addProperty("adminId", adminId);
                user.addProperty("userId", userId);
                user.addProperty("userPw", userPassword);

                body.add("user", user);

                retrofitAPI.openSignIn(body).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, retrofit2.Response<LoginResponse> response) {
                        if(response.isSuccessful()){
                            LoginResponse result = response.body();
                            if(result.getResultCode()==200){
                                Log.d("Open Sign In - success", result.getResultMessage());
                                Toast.makeText(getApplicationContext(), result.getResultMessage(), Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getApplicationContext(), result.getResultCode()+result.getResultMessage(), Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.d("Open Sign In - Failure",response.code()+":"+response.message());
                        }
                        loadingProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Log.d("retrofit-Failure", "서버에 문제가 발생했습니다.");
                        loadingProgressBar.setVisibility(View.GONE);
                        t.printStackTrace();
                    }

                });
            }
        });
    }

    public void runtimeEnableAutoInit() {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
    }

    private void askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

        public void loginDataChanged (String adminId, String username, String password){
            if (adminId != null || username != null || (password != null && password.trim().length() > 5)) {
                final Button loginButton = binding.login;
                loginButton.setEnabled(true);
            }
        }

        private void updateUiWithUser (LoggedInUserView model){
            String welcome = getString(R.string.welcome) + model.getDisplayName();
            // TODO : initiate successful logged in experience
            Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        }

        private void showLoginFailed (@StringRes Integer errorString){
            Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
        }

        private void hidenkeyboard () {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        private final static String SHA1_ENCRYPT_KEY = "CasCadeKey";
        public static String hmacSha1Encoding (String value) throws Exception {

            String algorithm = "HmacSHA1";
            SecretKeySpec signingKey = new SecretKeySpec(SHA1_ENCRYPT_KEY.getBytes(), algorithm);

            // Hmac encoding
            Mac mac = Mac.getInstance(algorithm);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(value.getBytes());

            String encryptResult = new String(Base64.encodeToString(rawHmac, Base64.URL_SAFE)).trim().replace("_", "/");

            return encryptResult;
        }

}