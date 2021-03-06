package enhabyto.com.viewzapps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import id.zelory.compressor.Compressor;
import mehdi.sakout.fancybuttons.FancyButton;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {

    CircularImageView profileImage;
    final FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    int PICK_IMAGE_REQUEST = 111;
    Uri ImageFilePath;
    RotateLoading rotateLoadingImage, rotateLoading;
    EditText name_et, email_et, phone_et;
    String name_tx, email_tx, phone_tx;
    FancyButton submit_btn;
    ImageButton backButton_ib, removeProfilePic_ib;

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    String imageUploadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //image button
        backButton_ib = findViewById(R.id.profile_backButton);
        removeProfilePic_ib = findViewById(R.id.profile_removeProfilePicImageButton);

        //circular image
        profileImage = findViewById(R.id.profile_profileImage);
        //rotate loading
        rotateLoadingImage = findViewById(R.id.profile_rotateLoadingImage);
        rotateLoading = findViewById(R.id.profile_rotateLoading);
        //edit text
        name_et = findViewById(R.id.profile_nameEditText);
        email_et = findViewById(R.id.profile_emailEditText);
        phone_et = findViewById(R.id.profile_phoneEditText);
        //button
        submit_btn = findViewById(R.id.profile_submitButton);
        //function calls
        setImage();

        profileImage.setOnClickListener(this);
        submit_btn.setOnClickListener(this);
        backButton_ib.setOnClickListener(this);
        removeProfilePic_ib.setOnClickListener(this);
    }//on create end

    public void onStart(){
        super.onStart();
        LoadProfileData();
    }

    // on click method
    @Override
    public void onClick(View v) {
        int id = v.getId();

        //circular image click
        if (id == R.id.profile_profileImage){
            // selecting image
            if (checkStoragePermission()){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        }
        //submit profile details button click
        else if (id == R.id.profile_submitButton){
            name_tx = name_et.getText().toString().trim();
            email_tx = email_et.getText().toString().trim();
            phone_tx = phone_et.getText().toString().trim();
            //if all conditions are satisfied
            if (editTextValidations()){
                //setting values to firebase
                rotateLoading.start();
                databaseReference.child("users").child(mAuth.getUid()).child("profile_details").child("name").setValue(name_tx);
                databaseReference.child("users").child(mAuth.getUid()).child("profile_details").child("email").setValue(email_tx);
                databaseReference.child("users").child(mAuth.getUid()).child("profile_details").child("phone_number").setValue(phone_tx);
                changeUserNameInAds();
                rotateLoading.stop();
                new SweetAlertDialog(EditProfile.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Profile Updated")
                        .setConfirmText("Got it!")
                        .show();
            }
        }//else if

//        back button
        else if (id == R.id.profile_backButton){
            super.onBackPressed();
        }
//        back button

//        remove pic
        else if (id == R.id.profile_removeProfilePicImageButton){
            new SweetAlertDialog(EditProfile.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setTitleText("Are you sure to remove your profile pic!")
                    .setCancelText("No")
                    .setConfirmText("Yes")
                    .setCustomImage(R.drawable.ic_delete)
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(final SweetAlertDialog sDialog) {
                            rotateLoading.start();
                            String uid = mAuth.getUid();
                            databaseReference.child("users").child(uid).child("profile_image").setValue(null);
                            storageReference = storageReference.child("users").child(uid).child("profile_image").child("user_image.jpg");
                            storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    deleteProfilePicFromAds();
                                    sDialog.dismissWithAnimation();
                                    rotateLoading.stop();
                                    removeProfilePic_ib.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

                        }
                    })
                    .show();
        }
//        remove pic ends


//        remove pic

    }


    // pasting selected image on image view
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            rotateLoadingImage.start();
            ImageFilePath = data.getData();
            Glide.with(EditProfile.this)
                    .asBitmap()
                    .load(ImageFilePath)
                    .into(profileImage);
            UploadImageFileToFirebaseStorage();


        }
    } // on activity


    //    upload image to firebase
    public void UploadImageFileToFirebaseStorage() {
        try{    // try block 1
            if (ImageFilePath != null) {
                File actualImage = FileUtils.getFile(EditProfile.this, ImageFilePath);

                // try block 2
                try {
                    File compressedImageFile = new Compressor(EditProfile.this)
                            .setQuality(20)
                            .setCompressFormat(Bitmap.CompressFormat.WEBP)
                            .compressToFile(actualImage);

                    ImageFilePath = Uri.fromFile(compressedImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                } //  try block 2 ends

                final String uid = mAuth.getUid();
                StorageReference storageReferenceChild = storageReference.child("users/").child(uid+"/")
                        .child("profile_image").child("user_image.jpg");

                storageReferenceChild.putFile(ImageFilePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                imageUploadUrl = taskSnapshot.getDownloadUrl().toString();
                                databaseReference.child("users").child(uid).child("profile_image").child("profile_image_url").setValue(imageUploadUrl);
                                changeProfileImageUrlInAds();

                                rotateLoadingImage.stop();

                                try {
                                    new SweetAlertDialog(EditProfile.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Picture Updated!")
                                            .show();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }

                            }
                        })  // addOnSuccessListener ends
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                rotateLoadingImage.stop();
                                try {
                                    new SweetAlertDialog(EditProfile.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Something Went Wrong!")
                                            .setContentText(exception.getLocalizedMessage())
                                            .show();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }

                            }
                        })  // addOnFailureListener ends
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                            }
                        }); // addOnProgressListener ends
            }

        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }  //  try block 1 ends

    }  // UploadImageFileToFirebaseStorage ends


    //handling profile image
    //    setting image
    public void setImage(){
        databaseReference.child("users").child(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String imagePath = dataSnapshot.child("profile_image").child("profile_image_url").getValue(String.class);
                        if (!TextUtils.isEmpty(imagePath)){
                            removeProfilePic_ib.setVisibility(View.VISIBLE);
                        }
                        Picasso.with(getApplicationContext())
                                .load(imagePath)
                                .placeholder(R.drawable.ic_profile_image_placeholder)
                                .error(R.drawable.ic_warning)
                                .into(profileImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    } // set image ends


    //edit text validations
    public boolean editTextValidations(){
        if (TextUtils.isEmpty(name_tx)){
            new SweetAlertDialog(EditProfile.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Please enter name!")
                    .setConfirmText("Okay")
                    .show();
            return false;
        }
        else if(!validateEmail(email_tx)){
            new SweetAlertDialog(EditProfile.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Invalid email format!")
                    .setConfirmText("Okay")
                    .show();
            return false;
        }
        else if (phone_tx.length() < 10){
            new SweetAlertDialog(EditProfile.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Invalid phone number!")
                    .setConfirmText("Okay")
                    .show();
            return false;
        }
        return true;
    }// editTextValidations end

    public boolean checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(EditProfile.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProfile.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return false;
        }
        else return true;
    }


    //on destroy
    @Override
    public void onDestroy(){
        super.onDestroy();
        stopLoading();
    }

    //on pause
    @Override
    public void onPause(){
        super.onPause();
        stopLoading();
    }


    //stop loading
    public void stopLoading(){
        try{
            if ( rotateLoading!=null && rotateLoading.isStart() ){
                rotateLoading.stop();
            }
            if ( rotateLoadingImage!=null && rotateLoadingImage.isStart() ){
                rotateLoadingImage.stop();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }//stop loading ends

    //    loading user details from firebase
    public void LoadProfileData(){
        SharedPreferences sharedpreferences = getSharedPreferences("profileDetails", MODE_PRIVATE);
        name_tx = sharedpreferences.getString("name","");
        email_tx = sharedpreferences.getString("email","");
        phone_tx = sharedpreferences.getString("phone","");
//        setting values
        name_et.setText(name_tx);
        email_et.setText(email_tx);
        phone_et.setText(phone_tx);

        databaseReference.child("users").child(mAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        getting values
                        name_tx = dataSnapshot.child("profile_details").child("name").getValue(String.class);
                        email_tx = dataSnapshot.child("profile_details").child("email").getValue(String.class);
                        phone_tx = dataSnapshot.child("profile_details").child("phone_number").getValue(String.class);
//                        setting values
                        name_et.setText(name_tx);
                        email_et.setText(email_tx);
                        phone_et.setText(phone_tx);
//                        storing values in shared preferences
                        try {
                            SharedPreferences sharedpreferences = getSharedPreferences("profileDetails", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("name", name_tx);
                            editor.putString("email", email_tx);
                            editor.putString("phone", phone_tx);
                            editor.apply();
                        }
                        catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    //    loading user details from firebase ends


    //    validate email
    public boolean validateEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

//    change data in ads
    public void changeUserNameInAds(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child("youtubeAds");
        final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() { // setting  values
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //for starts
                    String userUidInAd = snapshot.child("userUid").getValue(String.class);
                    if (TextUtils.equals(userUid, userUidInAd)){ // if starts
                        String adKey = snapshot.getKey();
                        databaseReference.child(adKey).child("userName").setValue(name_tx);
                    } //if ends

                } //for ends
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); // setting values

    }
    //    change data in ads


    //    change profile pic url in ads
    public void changeProfileImageUrlInAds(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child("youtubeAds");
        final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() { // setting  values
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //for starts
                    String userUidInAd = snapshot.child("userUid").getValue(String.class);
                    if (TextUtils.equals(userUid, userUidInAd)){ // if starts
                        String adKey = snapshot.getKey();
                        databaseReference.child(adKey).child("profileImageUrl").setValue(imageUploadUrl);
                    } //if ends

                } //for ends
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); // setting values

    }
    //    change data in ads

    //    change profile pic url in ads
    public void deleteProfilePicFromAds(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ads").child("youtubeAds");
        final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() { // setting  values
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //for starts
                    String userUidInAd = snapshot.child("userUid").getValue(String.class);
                    if (TextUtils.equals(userUid, userUidInAd)){ // if starts
                        String adKey = snapshot.getKey();
                        databaseReference.child(adKey).child("profileImageUrl").setValue(null);
                    } //if ends

                } //for ends
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); // setting values

    }
    //    change data in ads


// end
}
