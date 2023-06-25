package com.buddy.buddy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import com.buddy.buddy.R;
import com.buddy.buddy.adapter.DriverCategoryAdapter;
import com.buddy.buddy.controller.DriverController;
import com.buddy.buddy.controller.UserController;
import com.buddy.buddy.domain.Driver;
import com.buddy.buddy.domain.DriverCategory;
import com.buddy.buddy.domain.User;
import com.buddy.buddy.fragment.ProgressDialogFragment;
import com.buddy.buddy.utils.DataUtils;
import com.buddy.buddy.utils.MyFunctions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.ArrayList;
import java.util.Objects;

public class RegisterDriverActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextVehicleBrand, editTextVehiclePlate;
    private Button buttonRegister;
    private ProgressDialogFragment dialogFragment;
    private UserController userController;
    private DriverController driverController;
    private DriverCategory driverCategory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);
        //Block screen rotation
        MyFunctions.screenOrientationPortrait(this);

        // Initialize variables
        userController = new UserController();
        driverController = new DriverController();
        dialogFragment = new ProgressDialogFragment(getSupportFragmentManager(), "ProgressRegister");
        imageViewBack = findViewById(R.id.imageViewBack);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextVehicleBrand = findViewById(R.id.editTextVehicleBrand);
        editTextVehiclePlate = findViewById(R.id.editTextVehiclePlate);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        imageViewBack.setOnClickListener(view -> goToActivityBack());

        setUpDriverCategoryAdapter();
    }

    private void setUpDriverCategoryAdapter() {
        AppCompatSpinner spinner = findViewById(R.id.driverCategorySpinner);
        DriverCategoryAdapter adapter = new DriverCategoryAdapter(getApplicationContext(), DataUtils.getAllDriverCategories(getApplicationContext()));
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        driverCategory = DataUtils.getAllDriverCategories(getApplicationContext()).get(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        //driverCategory = DataUtils.getAllDriverCategories(getApplicationContext()).get(0);
                    }
                });
        driverCategory = DataUtils.getAllDriverCategories(getApplicationContext()).get(0);
    }

    private int getPositionByCategoryName(String categoryName){
        int position = 0;
        ArrayList<DriverCategory> driverCategories =  DataUtils.getAllDriverCategories(getApplicationContext());
        for (int i = 0; i < driverCategories.size(); i++){
            DriverCategory driverCategory = driverCategories.get(i);
            if (Objects.equals(driverCategory.getName(), categoryName)){
                position = i;
                break;
            }
        }
        return position;
    }

    private void goToActivityBack() {
        onBackPressed();
    }

    private void registerUser() {
        String nameUser, emailUser, passwdUser, confirmPasswdUser, vehicleBrand, vehiclePlate;
        nameUser = editTextName.getText().toString();
        emailUser = editTextEmail.getText().toString();
        passwdUser = editTextPassword.getText().toString();
        confirmPasswdUser = editTextConfirmPassword.getText().toString();
        vehicleBrand = editTextVehicleBrand.getText().toString();
        vehiclePlate = editTextVehiclePlate.getText().toString();

        if (!validarDatosUsuario(nameUser, emailUser, passwdUser, confirmPasswdUser, vehicleBrand, vehiclePlate))
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
                    Driver driver = new Driver(Uid, user.getName(), user.getEmail(), vehicleBrand, vehiclePlate, driverCategory.getName());
                    saveUserDatabase(driver);

                } else {
                    // If sign in fails, display a message to the user.

                    // Verify connection to Internet
                    if (MyFunctions.conexionInternet(RegisterDriverActivity.this)) {
                        String errorCode = userController.getErrorCode(task);
                        //Verify if there is a user with the email
                        if (errorCode.equals("ERROR_EMAIL_ALREADY_IN_USE")) {
                            editTextEmail.setError("Mail is already in use.");
                            editTextEmail.requestFocus();
                        } else {
                            Toast.makeText(RegisterDriverActivity.this, "Failed to register user" + errorCode, Toast.LENGTH_LONG).show();
                        }
                    }
                    dialogFragment.closeDialog();
                }
            }
        });

    }

    private void saveUserDatabase(Driver driver) {
        driverController.create(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(RegisterDriverActivity.this, MapDriverActivity.class);
                    MyFunctions.deleteBackStack(intent);
                    startActivity(intent);

                } else {
                    Toast.makeText(RegisterDriverActivity.this, "Error saving user data", Toast.LENGTH_LONG).show();
                }
                dialogFragment.closeDialog();
            }
        });
    }

    private boolean validarDatosUsuario(String name, String mail, String passwd, String confirmPasswd, String vehicleBrand, String vehiclePlate) {
        if (name.isEmpty() || mail.isEmpty() || passwd.isEmpty() || vehicleBrand.isEmpty() || vehiclePlate.isEmpty()) {
            Toast.makeText(this, "There are empty fields.", Toast.LENGTH_LONG).show();
        } else {
            if (passwd.length() < 6) {
                editTextPassword.setError("Min 6 characters.");
                editTextPassword.requestFocus();
            } else {
                if (!passwd.equals(confirmPasswd)) {
                    editTextConfirmPassword.setError("Password does not match.");
                    editTextConfirmPassword.requestFocus();
                } else {
                    return true;
                }
            }
        }
        return false;
    }
}