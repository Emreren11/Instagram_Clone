package com.emre.instagramclonejava.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.emre.instagramclonejava.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser(); // daha önceden giriş yapılmış biri varsa "user" ile kontrol edilebilir

        if (user != null) {
            Intent intent = new Intent(MainActivity.this, FeedActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void signIn(View view) {
        String email = binding.emailText.getText().toString();
        String pass = binding.paswText.getText().toString();
        if (email.equals("") || pass.equals("")) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
        } else {
            auth.signInWithEmailAndPassword(email,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {

                    Intent intent = new Intent(MainActivity.this,FeedActivity.class);
                    startActivity(intent);
                    finish(); // diğer sayfaya giderken main aktiviteyi kapatır

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void signUp(View view) {
        String email = binding.emailText.getText().toString();
        String pass = binding.paswText.getText().toString();

        if (email.equals("") || pass.equals("")) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
        } else {
            auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override // başarılı olduğunda yapılacak kodlar
                public void onSuccess(AuthResult authResult) {
                    Intent intent = new Intent(MainActivity.this, FeedActivity.class);
                    startActivity(intent);
                    finish(); // diğer sayfaya giderken main aktiviteyi kapatır
                    }
            }).addOnFailureListener(new OnFailureListener() {
                @Override // Hata olduğunda yapılacak kodlar
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage() , Toast.LENGTH_SHORT).show();
                    //e.getLocalizedMessage -> kullanıcının anlayabileceği şekilde hata mesajı verir
                }
            });
        }

    }
}