package com.buddy.buddy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.buddy.buddy.utils.MyFunctions;
import com.buddy.buddy.controller.UserController;
import com.buddy.buddy.domain.User;
import com.buddy.buddy.fragment.ProgressDialogFragment;
import com.buddy.buddy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class LoginActivity extends AppCompatActivity {

    private TextView textViewRegister;
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private ProgressDialogFragment dialogFragment;
    private UserController userController;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MyFunctions.screenOrientationPortrait(this);

        sharedPreferences = getSharedPreferences("TYPE_USER",MODE_PRIVATE);
        userController = new UserController();
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        dialogFragment = new ProgressDialogFragment(getSupportFragmentManager(), "ProgressLogin");

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegister();
            }
        });


    }

    private void loginUser() {
        String email, password;
        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        if(!validateCredentials(email, password)){
            return;
        }
        dialogFragment.showDialog();
        User user = new User(email, password);
        userController.loginWithEmailAndPassword(user).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    // Sign in success, update UI with the signed-in user's information
                    Intent intent;
                    String typeUser = sharedPreferences.getString("USER", null);
                    if (typeUser.equals("driver")){
                        intent = new Intent(LoginActivity.this, MapDriverActivity.class);
                    }else if (typeUser.equals("client")){
                        intent = new Intent(LoginActivity.this, MapClientActivity.class);
                    }else return;
                    MyFunctions.deleteBackStack(intent);
                    startActivity(intent);
                }else{
                    if (MyFunctions.conexionInternet(LoginActivity.this)){
                        String errorCode = userController.getErrorCode(task);
                        switch (errorCode){
                            case "ERROR_WRONG_PASSWORD":
                            case "ERROR_USER_NOT_FOUND":
                            case "ERROR_INVALID_EMAIL":
                                Toast.makeText(LoginActivity.this, "The username or password are incorrect.", Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(LoginActivity.this, "Failed to login.", Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                }
                dialogFragment.closeDialog();
            }
        });
    }

    private boolean validateCredentials(String email, String password) {
        if (!email.isEmpty() && !password.isEmpty()) {
            if (password.length() > 5) {
                return true;
            }
        }
        Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_LONG).show();
        return false;
    }

    private void goToRegister() {
        Intent intent;
        String typeUser = sharedPreferences.getString("USER", null);
        if (typeUser.equals("driver")){
            intent = new Intent(LoginActivity.this, RegisterDriverActivity.class);
        }else if (typeUser.equals("client")){
            intent = new Intent(LoginActivity.this, RegisterClientActivity.class);
        }else return;
        startActivity(intent);
    }
}