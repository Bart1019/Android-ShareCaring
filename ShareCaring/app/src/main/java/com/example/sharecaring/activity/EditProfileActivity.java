package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.sharecaring.R;
import com.example.sharecaring.model.IntentOpener;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_CODE = 1000;
    Button uploadBtn, doneBtn;
    CircularImageView profilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        uploadBtn = findViewById(R.id.uploadBtn);
        doneBtn = findViewById(R.id.btnFinish);

        uploadBtn.setOnClickListener(this);
        doneBtn.setOnClickListener(this);

        profilePhoto = findViewById(R.id.profileImg);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.uploadBtn) {
            uploadFromGallery();
        } else if (id == R.id.btnFinish) {
            IntentOpener.openIntent(EditProfileActivity.this, ProfileActivity.class);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();
                //profilePhoto.setImageURI(imageUri);
                uploadImageToDb(imageUri);
            }
        }
    }

    private void uploadImageToDb(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        String path = userId + "/profilePicture.jpg";
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference(path);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Picasso.with(EditProfileActivity.this).load(task.getResult()).into(profilePhoto);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfileActivity.this, "Image upload failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadFromGallery() {
        //returns the particular image uri from the gallery that the user clicked on
        Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGallery, RC_CODE);
    }
}