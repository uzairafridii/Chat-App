package com.example.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class AccountSettingActivity extends AppCompatActivity {
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private TextView mDisplayName;
    private TextView mStatus;
    private Button statusButton, imageButton;
    private CircleImageView mCircleImageView;
    private StorageReference mStorageRef;
    private ProgressDialog mProgressDialog;
    //compress variables
    byte[] thumb_byte;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        mDisplayName = findViewById(R.id.setting_displayName);
        mStatus = findViewById(R.id.setting_status);
        statusButton = findViewById(R.id.changeStatusButton);
        imageButton = findViewById(R.id.changeImageButton);
        mCircleImageView = (CircleImageView) findViewById(R.id.account_setting_circle_image);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = (String) dataSnapshot.child("name").getValue();
                String status = dataSnapshot.child("status").getValue(String.class);
                String image = dataSnapshot.child("image").getValue().toString();
                String thumb_nail = dataSnapshot.child("thumb_nail").getValue(String.class);

                // Toast.makeText(AccountSettingActivity.this, "" + dataSnapshot.toString(), Toast.LENGTH_LONG).show();

                mDisplayName.setText(name);
                mStatus.setText(status);

                if (!image.equals("default")) {
                  /*  Glide.with(AccountSettingActivity.this)
                            .load(image)
                            .centerCrop()
                            .placeholder(R.drawable.logged)
                            .into(mCircleImageView);
*/
                    Glide.with(AccountSettingActivity.this)
                            .load(image)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .placeholder(R.drawable.logged)
                            .into(mCircleImageView);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String statusValue = mStatus.getText().toString();
                Intent statusIntent = new Intent(AccountSettingActivity.this, StatusActivity.class);
                statusIntent.putExtra("status", statusValue);
                startActivity(statusIntent);
            }
        });


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select image"), 1);


             /*   CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AccountSettingActivity.this);
*/

            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
            mUserDatabase.child("online").setValue(true);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
            // Toast.makeText(this, ""+imageUri, Toast.LENGTH_SHORT).show();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Uploading your image");
                mProgressDialog.setMessage("Please Wait while upload your image....");
                mProgressDialog.show();


                String currentUserId = mCurrentUser.getUid();
                Uri resultUri = result.getUri();

                File thumbFilepath = new File(resultUri.getPath());
                try {
                    Bitmap thumb_nail = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumbFilepath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_nail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_byte = baos.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                final StorageReference filePath = mStorageRef.child("profile_images").child(currentUserId + ".jpg");
                final StorageReference thumb_filepath = mStorageRef.child("profile_images").child("thumb").child(currentUserId + ".jpg");


                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = uri.toString();

                                UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        thumb_filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                String thumbDownloadUrl = uri.toString();

                                                Map userData = new HashMap<>();
                                                userData.put("image", downloadUrl);
                                                userData.put("thumb_nail", thumbDownloadUrl);

                                                mUserDatabase.updateChildren(userData);
                                                Toast.makeText(AccountSettingActivity.this, "Working perfect..", Toast.LENGTH_SHORT).show();
                                                mProgressDialog.dismiss();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(AccountSettingActivity.this, "Error in thumbnail", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });


                            }
                        });

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
