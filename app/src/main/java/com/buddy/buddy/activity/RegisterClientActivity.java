package com.buddy.buddy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.buddy.buddy.utils.MyFunctions;
import com.buddy.buddy.fragment.ProgressDialogFragment;
import com.buddy.buddy.R;
import com.buddy.buddy.controller.ClientController;
import com.buddy.buddy.controller.UserController;
import com.buddy.buddy.domain.Client;
import com.buddy.buddy.domain.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class RegisterClientActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private ProgressDialogFragment dialogFragment;
    private UserController userController;
    private ClientController clientController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_client);
        //Block screen rotation
        MyFunctions.screenOrientationPortrait(this);

        // Initialize variables
        userController = new UserController();
        clientController = new ClientController();
        dialogFragment = new ProgressDialogFragment(getSupportFragmentManager(), "ProgressRegister");
        imageViewBack = findViewById(R.id.imageViewBack);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToActivityBack();
            }
        });

    }

    private void goToActivityBack() {
        onBackPressed();
    }

    private void registerUser() {
        String nameUser, emailUser, passwdUser, confirmPasswdUser;
        nameUser = editTextName.getText().toString();
        emailUser = editTextEmail.getText().toString();
        passwdUser = editTextPassword.getText().toString();
        confirmPasswdUser = editTextConfirmPassword.getText().toString();

        if( !validarDatosUsuario(nameUser, emailUser, passwdUser, confirmPasswdUser) )
            return;

        User user = new User();
        user.setName(nameUser);
        user.setEmail(emailUser);
        user.setPassword(confirmPasswdUser);
        dialogFragment.showDialog();
        userController.register(user).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    String Uid = userController.getUidCurrentUser();
                    Client client = new Client(Uid, user.getName(), user.getEmail());
                    saveUserDatabase(client);

                } else {
                    // If sign in fails, display a message to the user.

                    // Verify connection to Internet
                    if (MyFunctions.conexionInternet(RegisterClientActivity.this)){
                        String errorCode = userController.getErrorCode(task);
                        //Verify if there is a user with the email
                        if (errorCode.equals("ERROR_EMAIL_ALREADY_IN_USE")){
                            editTextEmail.setError("Mail is already in use.");
                            editTextEmail.requestFocus();
                        }else{
                            Toast.makeText(RegisterClientActivity.this, "Failed to register user"+ errorCode, Toast.LENGTH_LONG).show();
                        }
                    }
                    dialogFragment.closeDialog();
                }
            }
        });

    }

    private void saveUserDatabase(Client client) {
        clientController.create(client).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Intent intent = new Intent(RegisterClientActivity.this, MapClientActivity.class);
                    MyFunctions.deleteBackStack(intent);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegisterClientActivity.this, "Error saving user data", Toast.LENGTH_LONG).show();
                }
                dialogFragment.closeDialog();
            }
        });
    }

    private boolean validarDatosUsuario(String name, String mail, String passwd , String confirmPasswd) {
        if ( name.isEmpty() || mail.isEmpty() || passwd.isEmpty()) {
            Toast.makeText(this, "There are empty fields.", Toast.LENGTH_LONG).show();
        } else {
            if (passwd.length() < 6 ) {
                editTextPassword.setError("Min 6 characters.");
                editTextPassword.requestFocus();
            } else {
                if (!passwd.equals(confirmPasswd)){
                    editTextConfirmPassword.setError("Password does not match.");
                    editTextConfirmPassword.requestFocus();
                }else{
                    return true;
                }
            }
        }
        return false;
    }

}