package com.gertamo.gertamo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;

import android.os.Bundle;
import android.view.View;

import android.widget.Button;

import android.widget.EditText;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    TextView forgot_password;
    EditText email, password;
    Button button, register,change_lang;
    DatabaseReference databaseReference;
    StorageReference storageFile;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    User reg_user;
    ProgressBar progressBar;
    final static String wersja = "7";
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    ".{6,}" +               //at least 4 characters
                    "$");
    private static final Pattern EMAIL_ADDRESS =
            Pattern.compile(
                    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                            "\\@" +
                            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                            "(" +
                            "\\." +
                            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                            ")+"
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //loadLocale();
        storageFile = FirebaseStorage.getInstance().getReference("wersja.txt");
        setContentView(R.layout.activity_login);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        reg_user = new User();
        fAuth = FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();
        if (fAuth.getCurrentUser() != null && fUser.isEmailVerified()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        register = findViewById(R.id.btn_register);
        forgot_password = findViewById(R.id.tv_forgot_pass);
        email = findViewById(R.id.email_id);
        password = findViewById(R.id.login_password_id);
        button = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar_login);
        //change_lang = findViewById(R.id.btn_change_lang);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PasswordActivity.class);
                startActivity(intent);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reg_email = email.getText().toString().trim();
                String reg_password = password.getText().toString().trim();
                if (reg_email.isEmpty()) {
                    email.setError("Pole nie może być puste");
                    return;
                } else if (!EMAIL_ADDRESS.matcher(reg_email).matches()) {
                    email.setError("Podaj poprawny adres email");
                    return;
                } else if (reg_password.isEmpty()) {
                    password.setError("Pole nie może być puste");
                    return;
                }
//                else if (!PASSWORD_PATTERN.matcher(reg_password).matches()) {
//                    Toast.makeText(LoginActivity.this, "Błędne hasło lub email", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                progressBar.setVisibility(View.VISIBLE);


                fAuth.signInWithEmailAndPassword(reg_email, reg_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (fAuth.getCurrentUser().isEmailVerified()) {
                                try {
                                    File localFile = File.createTempFile("wersja", "txt");
                                    storageFile.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            try {
                                                BufferedReader br = new BufferedReader(new FileReader(localFile));
                                                String line;
                                                line = br.readLine();
                                                br.close();
                                                if(line.equals(wersja))
                                                {
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                else {
                                                    Intent intent = new Intent(getApplicationContext(), VersionErrActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            } catch (IOException e) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }

                            } else {
                                Toast.makeText(LoginActivity.this, "Zweryfikuj adres email", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Błędne hasło lub email", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
//        change_lang.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showChangeLanguageDialog();
//            }
//        });
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }

//    private void showChangeLanguageDialog() {
//        final String[] listitems = {"Polski", "Deutsche", "English"};
//
//        AlertDialog.Builder mBuilder = new AlertDialog.Builder(LoginActivity.this);
//        mBuilder.setTitle("Choose Language");
//        mBuilder.setSingleChoiceItems(listitems, -1, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (which == 0) {
//                    setLocale("pl");
//                    recreate();
//                }
//                else if (which == 1) {
//                    setLocale("de");
//                    recreate();
//                }
//                else if (which == 2) {
//                    setLocale("en");
//                    recreate();
//                }
//                dialog.dismiss();
//            }
//        });
//        AlertDialog mDialog = mBuilder.create();
//
//        mDialog.show();
//    }
//
//    private void setLocale(String lang) {
//
//        Locale locale = new Locale(lang);
//        Locale.setDefault(locale);
//        Configuration configuration = new Configuration();
//        configuration.locale = locale;
//        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
//
//        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
//        editor.putString("My_lang", lang);
//        editor.apply();
//    }
//    public void loadLocale(){
//        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
//        String language = preferences.getString("My_lang","pl");
//        setLocale(language);
//    }

}



