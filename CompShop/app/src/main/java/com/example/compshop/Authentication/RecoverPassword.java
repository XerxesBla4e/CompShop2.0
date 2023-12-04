package com.example.compshop.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.compshop.R;
import com.example.compshop.databinding.ActivityRecoverPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class RecoverPassword extends AppCompatActivity {

    ActivityRecoverPasswordBinding activityRecoverPasswordBinding;
    Button recoverbtn;
    String email;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityRecoverPasswordBinding = ActivityRecoverPasswordBinding.inflate(getLayoutInflater());
        setContentView(activityRecoverPasswordBinding.getRoot());

        initViews(activityRecoverPasswordBinding);

        firebaseAuth = FirebaseAuth.getInstance();

        recoverbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!valid()) {
                    return;
                }
                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RecoverPassword.this, "Check Your Email To Reset Your Password", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RecoverPassword.this, "Ooops! Something went wrong", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }

    public void initViews(ActivityRecoverPasswordBinding activityRecoverPasswordBinding) {
        recoverbtn = activityRecoverPasswordBinding.recoverButton;
    }

    private boolean valid() {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        email = activityRecoverPasswordBinding.useremail.getText().toString();

        if (TextUtils.isEmpty(email)) {
            activityRecoverPasswordBinding.useremail.setError("Email field can't be empty");
            return false;
        } else if (!email.matches(emailRegex)) {
            activityRecoverPasswordBinding.useremail.setError("Invalid email format");
            return false;
        }
        return true;
    }
}
