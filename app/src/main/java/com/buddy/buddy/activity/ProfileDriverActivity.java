package com.buddy.buddy.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.buddy.buddy.R;
import com.buddy.buddy.adapter.DriverCategoryAdapter;
import com.buddy.buddy.controller.DriverController;
import com.buddy.buddy.controller.UserController;
import com.buddy.buddy.domain.Driver;
import com.buddy.buddy.domain.DriverCategory;
import com.buddy.buddy.utils.CompressorBitmapImage;
import com.buddy.buddy.utils.DataUtils;
import com.buddy.buddy.utils.FileUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ProfileDriverActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextVehicleBrand;
    private EditText editTextVehiclePlate;
    private ImageView imageViewPhoto;
    private Button buttonSave;
    private Toolbar toolbar;
    private DriverController driverController;
    private UserController userController;
    private File imageFile;
    private final int GALERY_REQUEST = 1;
    private ProgressDialog progressDialog;
    private String newName;
    private String newVehicleBrand;
    private String newVehiclePlate;
    private DriverCategory driverCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_driver);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextName = findViewById(R.id.editTextName);
        editTextVehicleBrand = findViewById(R.id.editTextVehicleBrand);
        editTextVehiclePlate = findViewById(R.id.editTextVehiclePlate);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        buttonSave = findViewById(R.id.buttonSave);
        progressDialog = new ProgressDialog(this);
        driverController = new DriverController();
        userController = new UserController();

        getDriverInfo();

        imageViewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePhoto();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void updatePhoto() {
        // Gallery
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALERY_REQUEST && resultCode == RESULT_OK){
            try {
                imageFile = FileUtil.from(this,data.getData());
                imageViewPhoto.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
            }catch (Exception e){

            }

        }
    }


    private void saveData() {
        newName = editTextName.getText().toString();
        newVehicleBrand = editTextVehicleBrand.getText().toString();
        newVehiclePlate = editTextVehiclePlate.getText().toString();
        if (!newName.isEmpty() && imageFile != null){
            progressDialog.setMessage("Wait a moment...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            saveStorage();
        }else{
            Toast.makeText(this, "Enter the name and an image.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveStorage() {
        byte[] imageBytes = CompressorBitmapImage.getImageCompress(this, imageFile.getPath(),500,500);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("drivers_images").child(userController.getUidCurrentUser()).child("profile.jpg");
        UploadTask uploadTask = storageReference.putBytes(imageBytes);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            Driver driver = new Driver();
                            driver.setImage(image);
                            driver.setName(newName);
                            driver.setVehicleBrand(newVehicleBrand);
                            driver.setVehiclePlate(newVehiclePlate);
                            driver.setKey(userController.getUidCurrentUser());
                            driver.setCategoryName(driverCategory.getName());
                            driverController.update(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(ProfileDriverActivity.this, "Updated data", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    });
                }else {
                    Log.e("firebase", "Error getting data", task.getException());
                }
            }
        });


    }

    private void getDriverInfo(){
        driverController.getDriver(userController.getUidCurrentUser()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    String vehicleBrand = snapshot.child("vehicleBrand").getValue().toString();
                    String vehiclePlate = snapshot.child("vehiclePlate").getValue().toString();
                    String image = "";
                    if (snapshot.hasChild("image")){
                        image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(imageViewPhoto);
                    }
                    editTextName.setText(name);
                    editTextVehicleBrand.setText(vehicleBrand);
                    editTextVehiclePlate.setText(vehiclePlate);

                    String categoryName = snapshot.child("categoryName").getValue().toString();
                    setUpDriverCategoryAdapter(categoryName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setUpDriverCategoryAdapter(String categoryName) {
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
                        //driverCategory = DataUtils.getAllDriverCategories(getApplicationContext()).get(getPositionByCategoryName(categoryName));
                    }
                });
        spinner.setSelection(getPositionByCategoryName(categoryName));
        driverCategory = DataUtils.getAllDriverCategories(getApplicationContext()).get(getPositionByCategoryName(categoryName));
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

}