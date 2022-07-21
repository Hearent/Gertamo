package com.gertamo.gertamo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class PasswordActivity extends AppCompatActivity {

    EditText reminder_pass;
    Button reminder_btn;
    FirebaseAuth fAuth;
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
        setContentView(R.layout.activity_password);
        fAuth = FirebaseAuth.getInstance();
        reminder_btn = findViewById(R.id.reminder_btn);
        reminder_pass = findViewById(R.id.reminder_email);

        reminder_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String f_pass = reminder_pass.getText().toString().trim();
                if (f_pass.isEmpty()) {
                    reminder_pass.setError("Pole nie może być puste");
                    return;
                } else if (!EMAIL_ADDRESS.matcher(f_pass).matches()) {
                    reminder_pass.setError("Podaj poprawny adres email");
                    return;
                } else {
                    fAuth.sendPasswordResetEmail(f_pass).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            Toast.makeText(PasswordActivity.this, "Link do resetu hasła został wysłany na Twój adres email", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PasswordActivity.this, "Error " + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}