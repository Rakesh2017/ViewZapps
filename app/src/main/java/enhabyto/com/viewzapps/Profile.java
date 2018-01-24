package enhabyto.com.viewzapps;


import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import static android.content.Context.MODE_PRIVATE;

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
        //class calls
        imageDownloadTask demoTask = new imageDownloadTask();
        demoTask.doInBackground();
        //onclick listener
        profileImage.setOnClickListener(this);
        submit_btn.setOnClickListener(this);

        return view;
    } //on create end


    public void onStart(){
        super.onStart();
        loadProfileData();
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
                rotateLoading.stop();
                new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Profile Updated")
                        .setConfirmText("Okay")
                        .show();
            }
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


//    upload image to firebase
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
                                String imageUploadUrl = taskSnapshot.getDownloadUrl().toString();

                                databaseReference.child("users").child(uid).child("profile_image").child("profile_image_url")
                                        .setValue(imageUploadUrl);
                                rotateLoadingImage.stop();
                                try {
                                    new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Picture Updated!")
                                            .show();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }

                                imageDownloadTask demoTask = new imageDownloadTask();
                                demoTask.doInBackground();
                                setImageSharedPreferences();

                            }
                        })  // addOnSuccessListener ends
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                rotateLoadingImage.stop();
                                try {
                                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
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

//    setting image
    public void setImage(){
//        checking if there is profile image in firebase
        SharedPreferences sharedDefaultImage = getActivity().getSharedPreferences("defaultImage", MODE_PRIVATE);
        String isImageUrl = sharedDefaultImage.getString("NoImageUrl", "");
        if (TextUtils.equals(isImageUrl, "true")){
            Glide.with(getActivity())
                    .load(R.drawable.default_profile_image)
                    .into(profileImage);
            return;
        }
        try {
            SharedPreferences.Editor editorDefaultImage = sharedDefaultImage.edit();
            editorDefaultImage.putString("NoImageUrl", "false");
            editorDefaultImage.apply();
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }


        //loading saved image from storage
        String myFile = "/viewZapp/image.jpg";
        String myPath = Environment.getExternalStorageDirectory()+myFile;
        File imgFile = new  File(myPath);

        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            profileImage.setImageBitmap(myBitmap);
        }
        //rotateLoadingImage.start();
        if (getActivity() != null){
            databaseReference.child("users").child(mAuth.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                         try {
                            String newUrl = dataSnapshot.child("profile_image").child("profile_image_url").getValue(String.class);
                            SharedPreferences sharedpreferences = getActivity().getSharedPreferences("imageUrlCheck1", MODE_PRIVATE);
                            String oldUrl = sharedpreferences.getString("imageUrl1", "");
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("imageUrl1", newUrl);
                            editor.apply();

                            if (dataSnapshot.hasChild("profile_image") && !TextUtils.equals(oldUrl, newUrl)){
                                String url = dataSnapshot.child("profile_image").child("profile_image_url").getValue(String.class);
                                Glide.with(getActivity())
                                        .load(url)
                                        .into(profileImage);
                            }
                            else if (!dataSnapshot.hasChild("profile_image")){
                                Glide.with(getActivity())
                                    .load(R.drawable.default_profile_image)
                                    .into(profileImage);

                                    SharedPreferences sharedDefaultImage = getActivity().getSharedPreferences("defaultImage", MODE_PRIVATE);
                                    SharedPreferences.Editor editorDefaultImage = sharedDefaultImage.edit();
                                    editorDefaultImage.putString("NoImageUrl", "true");
                                    editorDefaultImage.apply();
                                }
                         }
                            //rotateLoadingImage.stop();
                         catch (NullPointerException e){
                             e.printStackTrace();
                         }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Glide.with(getActivity())
                                    .load(R.drawable.default_profile_image)
                                    .into(profileImage);
                            //rotateLoadingImage.stop();
                        }
                    }); // database ends
        } // if ends

    } // set image ends


    public boolean editTextValidations(){
        if (TextUtils.isEmpty(name_tx)){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Please enter name!")
                    .setConfirmText("Okay")
                    .show();
            return false;
        }
        else if(!validateEmail(email_tx)){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Invalid email format!")
                    .setConfirmText("Okay")
                    .show();
            return false;
        }
        else if (phone_tx.length() < 10){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Invalid phone number!")
                    .setConfirmText("Okay")
                    .show();
            return false;
        }
        return true;
    }// editTextValidations end

    public boolean checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return false;
        }
        else return true;
    }

//    background task
    @SuppressLint("StaticFieldLeak")
    class imageDownloadTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... arg0) {
            databaseReference.child("users")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                String newUrl = dataSnapshot.child(mAuth.getUid()).child("profile_image").child("profile_image_url").getValue(String.class);
                                SharedPreferences sharedpreferences = getActivity().getSharedPreferences("imageUrlCheck", MODE_PRIVATE);
                                String oldUrl = sharedpreferences.getString("imageUrl", "");
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("imageUrl", newUrl);
                                editor.apply();

                                if (!TextUtils.isEmpty(newUrl) && !TextUtils.equals(newUrl, oldUrl)) { // url check
                                DownloadManager mgr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                                Uri downloadUri = Uri.parse(newUrl);
                                //download manager
                                DownloadManager.Request request = new DownloadManager.Request(downloadUri);
                                //path to store it
                                String myFile = "/viewZapp/image.jpg";
                                String myPath = Environment.getExternalStorageDirectory() + myFile;

                                File f = new File(myPath);
                                if (f.exists()) {
                                    f.delete();
                                }

                                //downloading image from url
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                                        | DownloadManager.Request.NETWORK_MOBILE)
                                        .setVisibleInDownloadsUi(false)
                                        .setNotificationVisibility(0)
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                                        .setDestinationInExternalPublicDir("viewZapp", "image.jpg");

                                if (mgr != null) {
                                    mgr.enqueue(request);
                                }

                            } // if url ends

                            }
                            catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            return null;

        }
    }  //imageDownloadTask ends

    public void setImageSharedPreferences(){
        databaseReference.child("users").child(mAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            String newUrl = dataSnapshot.child("profile_image").child("profile_image_url").getValue(String.class);
                            SharedPreferences sharedpreferences = getActivity().getSharedPreferences("imageUrlCheck1", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("imageUrl1", newUrl);
                            editor.apply();

                            SharedPreferences sharedDefaultImage = getActivity().getSharedPreferences("defaultImage", MODE_PRIVATE);
                            SharedPreferences.Editor editorDefaultImage = sharedDefaultImage.edit();
                            editorDefaultImage.putString("NoImageUrl", "false");
                            editorDefaultImage.apply();
                        }
                        catch (NullPointerException e){
                            e.printStackTrace();
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }// setImageSharedPreferences ends

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
    public void loadProfileData(){
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("profileDetails", MODE_PRIVATE);
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
                            SharedPreferences sharedpreferences = getActivity().getSharedPreferences("profileDetails", MODE_PRIVATE);
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

//    validate email
    public boolean validateEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }




// end
}
