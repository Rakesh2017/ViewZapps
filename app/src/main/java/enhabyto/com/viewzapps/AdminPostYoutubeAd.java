package enhabyto.com.viewzapps;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.victor.loading.rotate.RotateLoading;
import java.io.File;
import java.io.IOException;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import mehdi.sakout.fancybuttons.FancyButton;
import util.android.textviews.FontTextView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdminPostYoutubeAd extends Fragment implements View.OnClickListener{

    private View view;

    EditText title_et, url_et, views_et, likes_et, subscribers_et, userUid_et;
    String title_tx, url_tx, views_tx, likes_tx, subscribers_tx, key, userUid_tx;

    FancyButton selectImage_btn, cancelImage_btn, submitAd_btn, cancelAd_btn;
    TextView likes_tv, views_tv, subscribers_tv;

    ImageView selectedImage_iv;
    ImageButton backButton;

    Uri ImageFilePath;

    //preview material
    CircleImageView profileImage;
    TextView uploaderName, adTitle;
    ImageView adImage;
    CardView cardView;
    ImageButton cancelPreviewButton;
    RelativeLayout mainRelativeLayout, previewRelativeLayout;

    RotateLoading loading;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public AdminPostYoutubeAd() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_admin_post_youtube_ad, container, false);

//        edit texts
        title_et = view.findViewById(R.id.apa_titleEditText);
        url_et = view.findViewById(R.id.apa_urlLinkEditText);
        views_et = view.findViewById(R.id.apa_viewsEditText);
        likes_et = view.findViewById(R.id.apa_likesEditText);
        subscribers_et = view.findViewById(R.id.apa_subscribersEditText);
        userUid_et = view.findViewById(R.id.apa_userUidEditText);

//        button
        selectImage_btn = view.findViewById(R.id.apa_selectButton);
        cancelImage_btn = view.findViewById(R.id.apa_cancelButton);

        backButton = view.findViewById(R.id.apa_backButton);

        selectedImage_iv = view.findViewById(R.id.apa_selectedImageView);

        loading = view.findViewById(R.id.apa_rotateLoading);
//         fancy buttons
        FancyButton submit = view.findViewById(R.id.apa_submitButton);

        //preview
        profileImage = view.findViewById(R.id.apa_profileImage);

        uploaderName = view.findViewById(R.id.apa_uploaderNameTextView);
        adTitle = view.findViewById(R.id.apa_previewTitleTextView);

        adImage = view.findViewById(R.id.apa_adImageView);

        cardView = view.findViewById(R.id.apa_cardview);

        cancelPreviewButton = view.findViewById(R.id.apa_cancelImageButton);

        mainRelativeLayout = view.findViewById(R.id.apa_mainRelativeLayout);
        previewRelativeLayout = view.findViewById(R.id.apa_previewRelativeLayout);

//        text views
        views_tv = view.findViewById(R.id.apa_views);
        likes_tv = view.findViewById(R.id.apa_likes);
        subscribers_tv = view.findViewById(R.id.apa_subscribers);

//        fancy buttons
        submitAd_btn = view.findViewById(R.id.apa_postAdButton);
        cancelAd_btn = view.findViewById(R.id.apa_cancelAdButton);
        //preview

        submit.setOnClickListener(this);
        selectImage_btn.setOnClickListener(this);
        cancelImage_btn.setOnClickListener(this);
        backButton.setOnClickListener(this);
        cancelPreviewButton.setOnClickListener(this);
        submitAd_btn.setOnClickListener(this);
        cancelAd_btn.setOnClickListener(this);

        return view;
    }

//    onClick
    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.apa_submitButton){ //if
            userUid_tx = userUid_et.getText().toString().trim();
            title_tx = title_et.getText().toString().trim();
            url_tx = url_et.getText().toString().trim();
            views_tx = views_et.getText().toString().trim();
            likes_tx = likes_et.getText().toString().trim();
            subscribers_tx = subscribers_et.getText().toString().trim();

            if (Validations()) {
                sweetAlertForPostAd();
            }

        }//if ends

//        select image
     else if (id == R.id.apa_selectButton){
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(getContext(), this);
        }//ele if

//        cancel image
    else if (id == R.id.apa_cancelButton){
            ImageFilePath = null;
            selectedImage_iv.setVisibility(View.GONE);
            selectImage_btn.setText("Select Image");
        }//ele if

//        back button
        else if (id == R.id.apa_backButton){

            new SweetAlertDialog(getActivity(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setTitleText("Are You sure you do not want to post this ad?")
                    .setCancelText("No")
                    .setConfirmText("Stay")
                    .setCustomImage(R.drawable.ic_warning)
                    .showCancelButton(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.cancel();
                        }
                    })
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            try {
                                getFragmentManager().popBackStack();
                            }
                            catch (NullPointerException e){
                                e.printStackTrace();
                            }
                            sDialog.cancel();
                        }
                    })
                    .show();



        }//ele if

//        cancel Preview
        else if(id == R.id.apa_cancelImageButton){
            previewRelativeLayout.setVisibility(View.GONE);
            mainRelativeLayout.setVisibility(View.VISIBLE);
        }

//        submit ad from preview
        else if(id == R.id.apa_postAdButton){
            PostFinalAd();
        }

//        cancel ad from preiew
        else if(id == R.id.apa_cancelAdButton){
            previewRelativeLayout.setVisibility(View.GONE);
            mainRelativeLayout.setVisibility(View.VISIBLE);
        }

    } //onclick


//    validations

    public boolean Validations(){
        if (userUid_tx.isEmpty()){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Enter User Uid!")
                    .show();
            return false;
        }
        else if (title_tx.length() < 3){
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
//    validations

//selecting image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                selectedImage_iv.setVisibility(View.VISIBLE);
                ImageFilePath = result.getUri();
                Picasso.with(getActivity())
                        .load(ImageFilePath)
                        .fit()
                        .centerCrop()
                        .into(selectedImage_iv);
                selectImage_btn.setText("re-select");
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(error.toString())
                        .show();
            }
        }
    }
    //selecting image


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

                                // Showing exception error message.
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

//    finally post ad
    public void PostFinalAd(){
        new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure to Post this Ad?")
                .setConfirmText("Yes")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(final SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        loading.start();

                         DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                        //getting user profile image and profile name
                         databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String profileImageUrl = dataSnapshot.child("users").child(userUid_tx).child("profile_image").child("profile_image_url").getValue(String.class);
                                String user_name = dataSnapshot.child("users").child(userUid_tx).child("profile_details").child("name").getValue(String.class);

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
                                databaseReference.child("userUid").setValue(userUid_tx);
                                databaseReference.child("userName").setValue(user_name);
                                databaseReference.child("profileImageUrl").setValue(profileImageUrl);

                                databaseReference = FirebaseDatabase.getInstance().getReference();
                                databaseReference = databaseReference.child("admin_posted_ads").child("youtubeAds").child(key);
//                            posting ad for record
                                databaseReference.child("adUrl").setValue(url_tx);
                                databaseReference.child("adTitle").setValue(title_tx);
                                databaseReference.child("adExpectedViews").setValue(views_tx);
                                databaseReference.child("adExpectedLikes").setValue(likes_tx);
                                databaseReference.child("adExpectedSubscribers").setValue(subscribers_tx);
                                databaseReference.child("youtubeAdKey").setValue(key);
                                databaseReference.child("userUid").setValue(userUid_tx);


                                UploadImageFileToFirebaseStorage();
                                loading.stop();
//                                sweet alert
                                SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Ad Successfully Posted!")
                                        .setConfirmText("Got it!")
                                        .setCancelText("Post another Ad")
                                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                cancelPreviewButton.performClick();
                                                setEverythingNull();
                                                sDialog.dismissWithAnimation();
                                            }
                                        })
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                startActivity(new Intent(getActivity(), DashBoard.class));
                                                getActivity().finish();
                                                sDialog.dismiss();
                                            }
                                        });
                                pDialog.setCancelable(false);
                                pDialog.show();

//                                sweet alert
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

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
    }//finally post ad


//    AdPreview

    public void AdPreview(){
        //profile image
        Picasso.with(getActivity())
                .load(ImageFilePath)
                .placeholder(R.drawable.youtube_placeholder)
                .error(R.drawable.ic_warning)
                .fit()
                .centerCrop()
                .noFade()
                .into(adImage);

        adTitle.setText(title_tx);

        views_tv.setText(views_tx+"\nviews left");
        likes_tv.setText(likes_tx+"\nLikes left");
        subscribers_tv.setText(subscribers_tx+"\nSubs left");
        setZero();

        databaseReference.child("users").child(userUid_tx)
                .addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("profile_details").child("name").getValue(String.class);
                        uploaderName.setText(name);
                        String image = dataSnapshot.child("profile_image").child("profile_image_url").getValue(String.class);
                        Picasso.with(getContext())
                                .load(image)
                                .placeholder(R.drawable.ic_profile_image_placeholder)
                                .error(R.drawable.ic_warning)
                                .noFade()
                                .into(profileImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }//    AdPreview

//set values to zero if not specified
    public void setZero(){
        if (views_tx.isEmpty()) views_tv.setText("0\n Views Left");
        if (likes_tx.isEmpty()) likes_tv.setText("0\n Likes Left");
        if (subscribers_tx.isEmpty()) subscribers_tv.setText("0\n Subs Left");
    }
//set values to zero if not specified ends

//    posting ad sweet alert
    public void sweetAlertForPostAd(){

        new SweetAlertDialog(getActivity(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Have a Preview of Ad!")
                .setCancelText("Preview Ad")
                .setConfirmText("Post Ad")
                .setCustomImage(R.drawable.ic_preview_ad)
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        AdPreview();
                        previewRelativeLayout.setVisibility(View.VISIBLE);
                        mainRelativeLayout.setVisibility(View.GONE);
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        PostFinalAd();
                        sDialog.cancel();
                    }
                })
                .show();
    }
    //    posting ad sweet alert


//    setting everything null
    public void setEverythingNull(){
        title_et.setText(null);
        url_et.setText(null);
        views_et.setText(null);
        likes_et.setText(null);
        subscribers_et.setText(null);
        userUid_et.setText(null);
        ImageFilePath = null;
        selectedImage_iv.setVisibility(View.GONE);
    }
//    setting everything null

//    end
}
