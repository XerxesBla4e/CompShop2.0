package com.example.compshop.Admin;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.compshop.Utils.FileUtils;
import com.example.compshop.databinding.AddcategoryactivityBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddCategory extends AppCompatActivity {
    AddcategoryactivityBinding addCategoryActivityBinding;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseUser firebaseUser;
    String uid;
    Uri uri;
    Button addCategoryBtn;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    String categoryName;
    ProgressBar progressBar;
    LinearLayout linearLayout;
    private static final int CATEGORY_IMAGE_CODE = 441;
    ImageView categoryImageView;
    private CharSequence[] options = {"Camera", "Gallery", "Cancel"};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addCategoryActivityBinding = AddcategoryactivityBinding.inflate(getLayoutInflater());
        setContentView(addCategoryActivityBinding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
        } else {
        }

        requirePermission();
        initViews();
        setListeners();

        addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateFields()) {
                    return;
                } else {
                    setUiEnabled(false);

                    progressBar.setIndeterminate(true);
                    progressBar.setVisibility(View.VISIBLE);

                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (progressBar.getParent() != null) {
                        ((ViewGroup) progressBar.getParent()).removeView(progressBar);
                    }
                    linearLayout.addView(progressBar, layoutParams);

                    uploadCategory();
                }
            }
        });
    }

    private void setUiEnabled(boolean enabled) {
        addCategoryBtn.setEnabled(enabled);
        categoryImageView.setEnabled(enabled);
    }

    private void initViews() {
        addCategoryBtn = addCategoryActivityBinding.addCategoryButton;
        categoryImageView = addCategoryActivityBinding.categoryImageView;
        linearLayout = addCategoryActivityBinding.linLayoutCategory;
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    private void setListeners() {
        categoryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickCategoryImage();
            }
        });
    }

    private void pickCategoryImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddCategory.this);
        builder.setTitle("Select Category Image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (options[i].equals("Camera")) {
                    openCamera();
                } else if (options[i].equals("Gallery")) {
                    openGallery();
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CATEGORY_IMAGE_CODE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Category Image"), CATEGORY_IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CATEGORY_IMAGE_CODE) {
            if (data != null) {
                if (data.getData() != null) {
                    // Image selected from gallery
                    uri = data.getData();
                    categoryImageView.setImageURI(uri);
                } else if (data.getExtras() != null && data.getExtras().get("data") != null) {
                    // Image captured from camera
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    uri = FileUtils.getImageUri(getApplicationContext(), image);
                    categoryImageView.setImageBitmap(image);
                }
            }
        }
    }

    public void requirePermission() {
        ActivityCompat.requestPermissions(AddCategory.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private void uploadCategory() {
        final String timestamp = String.valueOf(System.currentTimeMillis());
        categoryName = addCategoryActivityBinding.categoryNameEditText.getText().toString();

        if (uri == null) {
            // Handle the case where no image is selected
            // ...
        } else {
            StorageReference filepath = storageReference.child("categoryImage").child(timestamp);
            UploadTask uploadTask = filepath.putFile(uri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            String imageUrl = downloadUri.toString();
                            uploadCategoryDataWithImage(timestamp, imageUrl);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            handleUploadFailure(e);
                            setUiEnabled(true);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    handleUploadFailure(e);
                    setUiEnabled(true);
                }
            });
        }
    }

    private void uploadCategoryDataWithImage(String timestamp, String imageUrl) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("categoryName", categoryName);
        hashMap.put("timestamp", timestamp);
        hashMap.put("Uid", firebaseAuth.getUid());
        hashMap.put("image", imageUrl);

        DocumentReference userRef = firestore.collection("users").document(uid);
        CollectionReference categoryCollection = userRef.collection("Categories");

        categoryCollection.document(timestamp).set(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        setUiEnabled(true);
                        Toast.makeText(AddCategory.this, "Category Added...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        setUiEnabled(true);
                        Toast.makeText(AddCategory.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleUploadFailure(Exception e) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(AddCategory.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public boolean validateFields() {
        categoryName = addCategoryActivityBinding.categoryNameEditText.getText().toString();

        if (TextUtils.isEmpty(categoryName)) {
            addCategoryActivityBinding.categoryNameEditText.setError("Please insert category name");
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), AdminMain.class);
        startActivity(intent);
        finish();
    }
}
