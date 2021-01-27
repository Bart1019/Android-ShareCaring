package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sharecaring.R;
import com.example.sharecaring.model.IntentOpener;
import com.example.sharecaring.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextFirstName, editTextLastName, editTextPassword, editTextPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        Button register = (Button) findViewById(R.id.btnRegister);
        Button login = (Button) findViewById(R.id.btnLogIn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentOpener.openIntent(RegisterActivity.this, LoginActivity.class);
            }
        });
        editTextEmail = (EditText)findViewById(R.id.editTextEmail);
        editTextFirstName = (EditText)findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText)findViewById(R.id.editTextLastName);
        editTextPassword = (EditText)findViewById(R.id.editTextPassword);
        editTextPhone = (EditText)findViewById(R.id.editTextPhone);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        final String firstName = editTextFirstName.getText().toString().trim();
        final String lastName = editTextLastName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();

        if(firstName.isEmpty()) {
            editTextFirstName.setError("First name is required");
            editTextFirstName.requestFocus();
            return;
        }

        if(lastName.isEmpty()) {
            editTextLastName.setError("Last name is required");
            editTextLastName.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Invalid email");
            editTextEmail.requestFocus();
            return;
        }

        if(password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if(!Patterns.PHONE.matcher(phone).matches()) {
            editTextPhone.setError("Invalid phone number");
            editTextPhone.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            User user = new User(firstName, lastName, email, phone);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "User has been registered", Toast.LENGTH_LONG).show();
                                        IntentOpener.openIntent(RegisterActivity.this, InformationActivity.class);
                                    }else {
                                        Toast.makeText(RegisterActivity.this, "Failed to register", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to register", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        //pass > 6 (might cause problems with firebase)
    }

}