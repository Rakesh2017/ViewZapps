package enhabyto.com.viewzapps;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.googlecode.mp4parser.authoring.Edit;
import com.iceteck.silicompressorr.FileUtils;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

import java.io.File;
import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import id.zelory.compressor.Compressor;
import mehdi.sakout.fancybuttons.FancyButton;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdminPostYoutubeAd extends Fragment implements View.OnClickListener{

    private View view;

    EditText title_et, url_et, views_et, likes_et, subscribers_et;
    String title_tx, url_tx, views_tx, likes_tx, subscribers_tx, key;

    FancyButton selectImage_btn, cancelImage_btn;

    ImageView selectedImage_iv;

    int PICK_IMAGE_REQUEST = 111;
    Uri ImageFilePath;

    RotateLoading loading;
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();

    public AdminPostYoutubeAd() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_admin_post_youtube_ad, container, false);

        title_et = view.findViewById(R.id.apa_titleEditText);
        url_et = view.findViewById(R.id.apa_urlLinkEditText);
        views_et = view.findViewById(R.id.apa_viewsEditText);
        likes_et = view.findViewById(R.id.apa_likesEditText);
        subscribers_et = view.findViewById(R.id.apa_subscribersEditText);

        selectImage_btn = view.findViewById(R.id.apa_selectButton);
        cancelImage_btn = view.findViewById(R.id.apa_cancelButton);

        selectedImage_iv = view.findViewById(R.id.apa_selectedImageView);

        loading = view.findViewById(R.id.apa_rotateLoading);

        FancyButton submit = view.findViewById(R.id.apa_submitButton);

        submit.setOnClickListener(this);
        selectImage_btn.setOnClickListener(this);
        cancelImage_btn.setOnClickListener(this);

        return view;
    }

//    onClick
    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.apa_submitButton){ //if
            title_tx = title_et.getText().toString().trim();
            url_tx = url_et.getText().toString().trim();
            views_tx = views_et.getText().toString().trim();
            likes_tx = likes_et.getText().toString().trim();
            subscribers_tx = subscribers_et.getText().toString().trim();

            if (Validations()) {

                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure to Post this Ad?")
                        .setConfirmText("Yes")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(final SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                loading.start();

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                key = databaseReference.push().getKey();
                                databaseReference = databaseReference.child("ads").child("youtubeAds").child(key);

//                            posting ad
                                databaseReference.child("adUrl").setValue(url_tx);
                                databaseReference.child("adTitle").setValue(title_tx);
                                databaseReference.child("adViewsLeft").setValue(views_tx);
                                databaseReference.child("adLikesLeft").setValue(likes_tx);
                                databaseReference.child("adSubscribersLeft").setValue(subscribers_tx);
                                databaseReference.child("youtubeAdKey").setValue(key);
                                databaseReference.child("userUid").setValue(mAuth.getUid());

                                databaseReference = FirebaseDatabase.getInstance().getReference();
                                databaseReference = databaseReference.child("admin_posted_ads").child("youtubeAds").child(key);
//                            posting ad
                                databaseReference.child("adUrl").setValue(url_tx);
                                databaseReference.child("adTitle").setValue(title_tx);
                                databaseReference.child("adExpectedViews").setValue(views_tx);
                                databaseReference.child("adExpectedLikes").setValue(likes_tx);
                                databaseReference.child("adExpectedSubscribers").setValue(subscribers_tx);
                                databaseReference.child("youtubeAdKey").setValue(key);
                                databaseReference.child("userUid").setValue(mAuth.getUid());


                                UploadImageFileToFirebaseStorage();
                                loading.stop();
                                new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Ad Successfully Posted!")
                                        .show();

                            }
                        })
                        .setCancelText("No")
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .show();
            }

        }//if ends

     else if (id == R.id.apa_selectButton){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
        }//ele if

    else if (id == R.id.apa_cancelButton){
            ImageFilePath = null;
            selectedImage_iv.setVisibility(View.GONE);
            selectImage_btn.setText("Select Image");
        }//ele if




    } //onclick

    public boolean Validations(){
        if (title_tx.length() < 3){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Title is too small!")
                    .show();
            return false;
        }
        else if (url_tx.isEmpty()){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Specify Url!")
                    .show();
            return false;
        }
        else if (!Patterns.WEB_URL.matcher(url_tx).matches()){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Invalid Url!")
                    .show();
            return false;
        }
        else if (ImageFilePath == null){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Image for thumb nail is required!")
                    .show();
            return false;
        }
        else if (views_tx.isEmpty() && likes_tx.isEmpty() && subscribers_tx.isEmpty()){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Please specify at least one among VIEWS, LIKES, SUBSCRIBERS")
                    .show();
            return false;
        }
        return true;
    }


//    select image
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
        ImageFilePath = data.getData();

            // Getting selected image into Bitmap.
           // Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), ImageFilePath);
            // Setting up bitmap selected image into ImageView.
            selectedImage_iv.setVisibility(View.VISIBLE);
            Picasso.with(getActivity())
                    .load(ImageFilePath)
                    .fit()
                    .centerCrop()
                    .into(selectedImage_iv);

           // selectedImage_iv.setImageBitmap(bitmap);
            // After selecting image change choose button above text.
            selectImage_btn.setText("Selected");

    }
}//select image


    //upload image
    public void UploadImageFileToFirebaseStorage() {

        try{
            if (ImageFilePath != null) {

                File actualImage = FileUtils.getFile(getActivity(), ImageFilePath);
                try {
                    File compressedImageFile = new Compressor(getActivity())
                            .setQuality(25)
                            .setCompressFormat(Bitmap.CompressFormat.WEBP)
                            .compressToFile(actualImage);
                    ImageFilePath = Uri.fromFile(compressedImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference imageStorageReference = storageReference.child("ads/").child("youtubeAds/")
                        .child(key+"/").child("adImage").child("youtubeImage.jpg");

                imageStorageReference.putFile(ImageFilePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                databaseReference.child("ads").child("youtubeAds").child(key)
                                        .child("youtubeAdImageUrl").setValue(taskSnapshot.getDownloadUrl().toString());
                                databaseReference = FirebaseDatabase.getInstance().getReference();
                                databaseReference.child("admin_posted_ads").child("youtubeAds").child(key)
                                        .child("youtubeAdImageUrl").setValue(taskSnapshot.getDownloadUrl().toString());
                                //  dialog_uploadingPump.dismiss();

                            }
                        })
                        // If something goes wrong .
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {

                                // dialog_uploadingPump.dismiss();

                                // Showing exception erro message.
                                Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })

                        // On progress change upload time.
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            }
                        });
            }

        }
        catch (IllegalArgumentException | NullPointerException e){
            e.printStackTrace();
        }

    }//upload image

//    end
}
