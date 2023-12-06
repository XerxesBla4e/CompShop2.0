package com.example.compshop.Admin;

import android.Manifest;
import android.content.Context;
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
import com.example.compshop.databinding.AddItemactivityBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AddItem extends AppCompatActivity {
    AddItemactivityBinding addItemactivityBinding;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseUser firebaseUser;
    String uid;
    Uri uri;
    Button additembtn;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    String name, description, category, price, discount, discountpercent;
    ProgressBar progressBar;
    LinearLayout linearLayout;
    private static final int ITEM_IMAGE_CODE = 440;
    ImageView imageView;
    SwitchMaterial discswitch;
    boolean discavailable;
    private CharSequence[] options = {"Camera", "Gallery", "Cancel"};
    public String selectedImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addItemactivityBinding = AddItemactivityBinding.inflate(getLayoutInflater());
        setContentView(addItemactivityBinding.getRoot());


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

        additembtn.setOnClickListener(new View.OnClickListener() {
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

                    uploadItem();
                }
            }
        });
    }

    private void setUiEnabled(boolean enabled) {
        additembtn.setEnabled(enabled);
        imageView.setEnabled(enabled);
        discswitch.setEnabled(enabled);
    }

    private void initViews() {
        additembtn = addItemactivityBinding.addItemButton;
        imageView = addItemactivityBinding.itemImageView;
        linearLayout = addItemactivityBinding.linLayoutFood;
        discswitch = addItemactivityBinding.discountSwitch;
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    private void setListeners() {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickItemImage();
            }
        });

        discswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                discavailable = isChecked;
                if (discavailable) {
                    addItemactivityBinding.discountPriceEditText.setVisibility(View.VISIBLE);
                    addItemactivityBinding.discountDescriptionEditText.setVisibility(View.VISIBLE);
                } else {
                    addItemactivityBinding.discountPriceEditText.setVisibility(View.GONE);
                    addItemactivityBinding.discountDescriptionEditText.setVisibility(View.GONE);
                    discount = "0";
                    discountpercent = "0%";
                }
            }
        });
    }

    private void pickItemImage() {
       /* Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Item Image"), ITEM_IMAGE_CODE);*/
        AlertDialog.Builder builder =
                new AlertDialog.Builder(AddItem.this);
        builder.setTitle("Select Product Image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (options[i].equals("Camera")) {
                    Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePic, 0);
                } else if (options[i].equals("Gallery")) {
                    Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(gallery, "Select Image"), 1);
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap image = (Bitmap) data.getExtras().get("data");
                        selectedImage = FileUtils.getPath(AddItem.this, getImageUri(getApplicationContext(), image));
                        imageView.setImageBitmap(image);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        uri = data.getData();
                        selectedImage = FileUtils.getPath(AddItem.this, uri);
                        Picasso.get().load(uri).into(imageView);
                    }
                    break;
                default:
                    break;
            }
        }

    }

    public Uri getImageUri(Context context, Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "myImage", "");

        return Uri.parse(path);
    }

    public void requirePermission() {
        ActivityCompat.requestPermissions(AddItem.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private void uploadItem() {
        final String timestamp = String.valueOf(System.currentTimeMillis());
        name = addItemactivityBinding.fnameEditText.getText().toString();
        category = addItemactivityBinding.nameEditCategory.getText().toString();
        description = addItemactivityBinding.nameEditDescription.getText().toString();
        price = addItemactivityBinding.nameEditPrice.getText().toString();

        if (discavailable) {
            discount = addItemactivityBinding.discountPriceEditText.getText().toString();
            discountpercent = addItemactivityBinding.discountDescriptionEditText.getText().toString();
        } else {
            discount = "0";
            discountpercent = "0";
        }

        if (uri == null || selectedImage.equals("")) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name", name);
            hashMap.put("category", category);
            hashMap.put("description", description);
            hashMap.put("price", price);
            hashMap.put("item_Id", timestamp);
            hashMap.put("timestamp", timestamp);
            hashMap.put("Uid", firebaseAuth.getUid());
            hashMap.put("discount", discount);
            hashMap.put("discountdescription", discountpercent);
            hashMap.put("image", "");

            DocumentReference userRef = firestore.collection("users").document(uid);
            CollectionReference foodCollection = userRef.collection("Products");

            foodCollection.document(timestamp).set(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddItem.this, "Product Added...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddItem.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            setUiEnabled(true);
                        }
                    });
        } else {
            StorageReference filepath = storageReference.child("imagePost").child(timestamp);
            UploadTask uploadTask = filepath.putFile(Uri.parse(selectedImage));

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            String imageUrl = downloadUri.toString();
                            uploadFoodDataWithImage(timestamp, imageUrl);
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

    private void uploadFoodDataWithImage(String timestamp, String imageUrl) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", name);
        hashMap.put("category", category);
        hashMap.put("description", description);
        hashMap.put("price", price);
        hashMap.put("item_Id", timestamp);
        hashMap.put("timestamp", timestamp);
        hashMap.put("Uid", firebaseAuth.getUid());
        hashMap.put("discount", discount);
        hashMap.put("discountdescription", discountpercent);
        hashMap.put("image", imageUrl);

        DocumentReference userRef = firestore.collection("users").document(uid);
        CollectionReference foodCollection = userRef.collection("Products");

        foodCollection.document(timestamp).set(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        setUiEnabled(true);
                        Toast.makeText(AddItem.this, "Item Added...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        setUiEnabled(true);
                        Toast.makeText(AddItem.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleUploadFailure(Exception e) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(AddItem.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public boolean validateFields() {
        name = addItemactivityBinding.fnameEditText.getText().toString();
        category = addItemactivityBinding.nameEditCategory.getText().toString();
        description = addItemactivityBinding.nameEditDescription.getText().toString();
        price = addItemactivityBinding.nameEditPrice.getText().toString();

        if (TextUtils.isEmpty(name)) {
            addItemactivityBinding.fnameEditText.setError("Please insert item name");
            return false;
        }
        if (TextUtils.isEmpty(description)) {
            addItemactivityBinding.nameEditDescription.setError("Please insert description");
            return false;
        }
        if (TextUtils.isEmpty(price)) {
            addItemactivityBinding.nameEditPrice.setError("Please insert price");
            return false;
        }

        if (discavailable) {
            discount = addItemactivityBinding.discountPriceEditText.getText().toString();
            discountpercent = addItemactivityBinding.discountDescriptionEditText.getText().toString();

            if (TextUtils.isEmpty(discount)) {
                addItemactivityBinding.discountPriceEditText.setError("Please insert Discount Amount");
                return false;
            }

            if (TextUtils.isEmpty(discountpercent)) {
                addItemactivityBinding.discountDescriptionEditText.setError("Please insert discount description/percentage");
                return false;
            }

            // Validate discountpercent to ensure it contains a percentage symbol (%)
            if (!discountpercent.contains("%")) {
                addItemactivityBinding.discountDescriptionEditText.setError("Please insert a discount description with a percentage symbol eg. (20%)");
                return false;
            }
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
