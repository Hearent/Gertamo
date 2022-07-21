package com.gertamo.gertamo;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;

import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;

import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener, View.OnClickListener {
    EditText email, password, password2;
    Button button, date_btn;
    CheckBox checkBox, checkBox_captcha;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    DatabaseReference databaseReference;
    String userID, btn_text;
    User reg_user;
    TextView reg, pol;
    Spinner dropdown;
    ProgressBar progressBar;
    String TAG = RegisterActivity.class.getSimpleName();
    String SITE_KEY = "6Lf0ggYdAAAAAPmZnFlpKJv6vhmylMEZZZ2l97XA";
    String SECRET_KEY = "6Lf0ggYdAAAAAL2ZiLT4qxqageZSqpF79HFJ3ovV";
    String reg_email, reg_password, reg_password2, sex;
    RequestQueue queue;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    ".{6,}" +               //at least 4 characters
                    "$");
    private static final Pattern EMAIL_ADDRESS =
            Pattern.compile(
                    "^[a-zA-Z0-9_.+-]+@(?:(?:[a-zA-Z0-9-]+\\.)?[a-zA-Z]+\\.)?(gmail.com|o2.pl|wp.pl|interia.pl|onet.pl|onet.eu)$"
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        email = findViewById(R.id.register_email_id);
        password = findViewById(R.id.register_password_id);
        password2 = findViewById(R.id.register_password2_id);
        button = findViewById(R.id.btn_register);
        checkBox_captcha = findViewById(R.id.captcha);
        checkBox_captcha.setOnClickListener(this);
        queue = Volley.newRequestQueue(getApplicationContext());
        reg = findViewById(R.id.reg);
        pol = findViewById(R.id.pol);
        reg.setMovementMethod(LinkMovementMethod.getInstance());
        pol.setMovementMethod(LinkMovementMethod.getInstance());
        reg.setLinkTextColor(Color.parseColor("#0000EE"));
        pol.setLinkTextColor(Color.parseColor("#0000EE"));
        checkBox = findViewById(R.id.checkbox_accept_reg);
        date_btn = findViewById(R.id.date_picker);
        dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"", "Mężczyzna", "Kobieta", "Nie chcę podawać"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
        progressBar = findViewById(R.id.progressBar);
        if (fAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
//        reg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), StatuteActivity.class);
//                startActivity(intent);
//            }
//        });
//        pol.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), PrivacyPolicyActivity.class);
//                startActivity(intent);
//            }
//        });
        date_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reg_email = email.getText().toString().trim();
                reg_password = password.getText().toString().trim();
                reg_password2 = password2.getText().toString().trim();
                sex = dropdown.getSelectedItem().toString();
                btn_text = date_btn.getText().toString().trim();
                if (reg_email.isEmpty()) {
                    email.setError("Pole nie może być puste");
                    return;
                } else if (!EMAIL_ADDRESS.matcher(reg_email).matches()) {
                    email.setError("Użyj poczty Gmail, WP, O2, Interia, Onet");
                    return;
                } else if (reg_password.isEmpty()) {
                    password.setError("Pole nie może być puste");
                    if (reg_password2.isEmpty()) {
                        password2.setError("Pole nie może być puste");
                        return;
                    }
                } else if (!PASSWORD_PATTERN.matcher(reg_password).matches()) {
                    password.setError("Hasło musi być dłuższe niż 6 znaków oraz musi się składać z małej i dużej litery oraz cyfry");
                    if (!PASSWORD_PATTERN.matcher(reg_password2).matches()) {
                        password2.setError("Hasło musi być dłuższe niż 6 znaków oraz musi się składać z małej i dużej litery oraz cyfry");
                        return;
                    }
                } else if (!reg_password.equals(reg_password2)) {
                    password2.setError("Hasła muszą być takie same");
                    return;
                } else if (!checkBox.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Zaakceptuj regulamin i politykę prywatności", Toast.LENGTH_SHORT).show();
                    return;
                } else if (btn_text.equals("Wybierz")) {
                    Toast.makeText(getApplicationContext(), "Podaj rok urodzenia", Toast.LENGTH_SHORT).show();
                    return;
                } else if (dropdown.getSelectedItem().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Proszę wybierz płeć", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!checkBox_captcha.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Zaznacz że nie jesteś robotem", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    fAuth.createUserWithEmailAndPassword(reg_email, reg_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                fAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Konto utworzone! Potwierdź swój adres email", Toast.LENGTH_SHORT).show();
                                            userID = fAuth.getCurrentUser().getUid();
                                            DocumentReference documentReference = fStore.collection("users").document(userID);
                                            Map<String, Object> user = new HashMap<>();
                                            user.put("email", reg_email);
                                            user.put("plec", sex);
                                            user.put("rok_ur", btn_text);
                                            documentReference.set(user);
                                            reg_user = new User(reg_email, sex, btn_text, 0, 0);
                                            databaseReference.child(userID).setValue(reg_user);
                                            FirebaseAuth.getInstance().signOut();
                                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(RegisterActivity.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        Log.i("value is", "" + newVal);
    }

    public void show() {
        final Dialog d = new Dialog(RegisterActivity.this);
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.dialog);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        TextView tv = (TextView) d.findViewById(R.id.tv_year);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        np.setMinValue(1930);
        np.setMaxValue(year);
        np.setValue(year);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date_btn.setText(String.valueOf(np.getValue()));
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    @Override
    public void onClick(View view) {
        checkBox_captcha.setChecked(false);
        SafetyNet.getClient(this).verifyWithRecaptcha(SITE_KEY)
                .addOnSuccessListener(this, new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                        if (!response.getTokenResult().isEmpty()) {
                            handleSiteVerify(response.getTokenResult());
                            checkBox_captcha.setChecked(true);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            Log.d(TAG, "Error message: " +
                                    CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
                            checkBox_captcha.setChecked(false);
                        } else {
                            Log.d(TAG, "Unknown type of error: " + e.getMessage());
                            checkBox_captcha.setChecked(false);
                        }
                    }
                });
    }

    protected void handleSiteVerify(final String responseToken) {
        //it is google recaptcha siteverify server
        //you can place your server url
        String url = "https://www.google.com/recaptcha/api/siteverify";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                //code logic when captcha returns true Toast.makeText(getApplicationContext(),String.valueOf(jsonObject.getBoolean("success")),Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), String.valueOf(jsonObject.getString("error-codes")), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ex) {
                            Log.d(TAG, "JSON exception: " + ex.getMessage());

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error message: " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("secret", SECRET_KEY);
                params.put("response", responseToken);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }
}