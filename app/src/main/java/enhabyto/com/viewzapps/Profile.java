package enhabyto.com.viewzapps;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.victor.loading.rotate.RotateLoading;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import id.zelory.compressor.Compressor;
import mehdi.sakout.fancybuttons.FancyButton;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class Profile extends Fragment implements View.OnClickListener {

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

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public Profile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //circular image
        profileImage = view.findViewById(R.id.profile_profileImage);
        //rotate loading
        rotateLoadingImage = view.findViewById(R.id.profile_rotateLoadingImage);
        rotateLoading = view.findViewById(R.id.profile_rotateLoading);
        //edit text
        name_et = view.findViewById(R.id.profile_nameEditText);
        email_et = view.findViewById(R.id.profile_emailEditText);
        phone_et = view.findViewById(R.id.profile_phoneEditText);
        //button
        submit_btn = view.findViewById(R.id.profile_submitButton);

        //function calls
        setImage();

        //onclick listener
        profileImage.setOnClickListener(this);
        submit_btn.setOnClickListener(this);

        return view;
    } //on create end

    // on click method
    @Override
    public void onClick(View v) {
        int id = v.getId();

        //circular image click
        if (id == R.id.profile_profileImage){
            // selecting image
            if (checkPermissions()){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }

        }
        //submit button click
        else if (id == R.id.profile_submitButton){
            name_tx = name_et.getText().toString();
            email_tx = name_et.getText().toString();
            phone_tx = name_et.getText().toString();
            editTextValidations();
        }

    }


    // pasting selected image on image view
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            rotateLoadingImage.start();
            ImageFilePath = data.getData();
            Glide.with(getActivity())
                 .asBitmap()
                 .load(ImageFilePath)
                 .into(profileImage);
            UploadImageFileToFirebaseStorage();


        }
    } // on activity


    public void UploadImageFileToFirebaseStorage() {
        try{    // try block 1
            if (ImageFilePath != null) {
                File actualImage = FileUtils.getFile(getActivity(), ImageFilePath);

                // try block 2
                try {
                    File compressedImageFile = new Compressor(getActivity())
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

                                @SuppressWarnings("VisibleForTests")
                            //    ImageUploadUrl imageUploadUrl = new ImageUploadUrl(taskSnapshot.getDownloadUrl().toString());
                                String imageUploadUrl = taskSnapshot.getDownloadUrl().toString();

                                databaseReference.child("users").child(uid).child("profile_image").child("profile_image_url")
                                        .setValue(imageUploadUrl);
                                rotateLoadingImage.stop();
                                new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Picture Updated!")
                                        .show();

                            }
                        })  // addOnSuccessListener ends
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                rotateLoadingImage.stop();
                                new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Something Went Wrong!")
                                        .setContentText(exception.getLocalizedMessage())
                                        .show();
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

    public void setImage(){
        rotateLoadingImage.start();
        if (getActivity() != null){
            databaseReference.child("users").child(mAuth.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("profile_image")){
                                String url = dataSnapshot.child("profile_image").child("profile_image_url").getValue(String.class);
                                Glide.with(getActivity())
                                        .load(url)
                                        .into(profileImage);
                            }
                            else {
                                Glide.with(getActivity())
                                        .load(R.drawable.default_profile_image)
                                        .into(profileImage);
                            }
                            rotateLoadingImage.stop();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Glide.with(getActivity())
                                    .load(R.drawable.default_profile_image)
                                    .into(profileImage);
                            rotateLoadingImage.stop();
                        }
                    }); // database ends
        } // if ends

    } // set image ends


    public void editTextValidations(){
        if (TextUtils.isEmpty(name_tx)){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Please Enter name")
                    .setConfirmText("Okay")
                    .show();
            return;
        }
        if (!validateEmail(email_tx)){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Invalid Email Format")
                    .setConfirmText("Okay")
                    .show();
            return;
        }

    }// editTextValidations end

//    email validator
    public boolean validateEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    } // email validator

    public boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return false;
        }
        else return true;
    }


// end
}
