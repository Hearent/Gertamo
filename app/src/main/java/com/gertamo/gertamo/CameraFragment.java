package com.gertamo.gertamo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;


import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import static androidx.core.content.ContextCompat.checkSelfPermission;


public class CameraFragment extends Fragment {
    private static final int PERMISSION_CODE = 1001;
    TextView tv;
    EditText contest_name;
    Button btn_up;
    ImageButton btn, btn_g;
    FirebaseFirestore db;
    FirebaseAuth fAuth;
    Uri image_uri;
    FirebaseFirestore fStore;
    DocumentReference userReference;
    StorageReference storageReference;
    DatabaseReference databaseReference, databaseReference_like, dbReference_like, databaseCompetition, databaseReference_user;
    ImageView imageView;
    String userID, user, uploadId, firstLine;
    Upload up;
    BottomNavigationView bottomNavigationView;
    CheckBox checkBox;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        up = new Upload();
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        tv = view.findViewById(R.id.contest_title);
        contest_name = view.findViewById(R.id.contest_name);
        checkBox = view.findViewById(R.id.copyright);
        btn = view.findViewById(R.id.btn_comp_cam);
        btn_g = view.findViewById(R.id.btn_comp_gallery);
        btn_up = view.findViewById(R.id.btn_comp_cam_up);
        imageView = view.findViewById(R.id.imageView1);
        Bundle bundle = this.getArguments();
        firstLine = bundle.getString("firstLine");
        tv.setText(firstLine);
        databaseReference = FirebaseDatabase.getInstance().getReference("Upload Photos").child(firstLine);
        storageReference = FirebaseStorage.getInstance().getReference("Upload Photos").child(firstLine);
        userReference = fStore.collection("users").document(userID);
        uploadId = databaseReference.push().getKey();
        dbReference_like = FirebaseDatabase.getInstance().getReference("Likes").child(uploadId);
        dbReference_like.keepSynced(true);
        databaseCompetition = FirebaseDatabase.getInstance().getReference("Competition").child(firstLine);
        databaseCompetition.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    databaseCompetition.child("status").setValue("1");
                } else if (snapshot.getValue().toString().equals("0")) {
                    btn_up.setEnabled(false);
                } else if (snapshot.getValue().toString().equals("1")) {
                    btn_up.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        Context context = getActivity();
        PackageManager packageManager = context.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
            Toast.makeText(getActivity(), "This device does not have a camera.", Toast.LENGTH_SHORT).show();
            return;
        }
        // POBRANIE LOGINU USERA UŻYWANE PODCZAS ZAPISU ZDJĘCIA
        userReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    user = documentSnapshot.getString("email");
                }
            }
        });
        btn_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!contest_name.getText().toString().trim().equals("")) {
                    if (image_uri != null && checkBox.isChecked()) {
                        uploadfile();
                    } else if (!checkBox.isChecked()) {
                        Toast.makeText(getContext(), "Zaznacz że jesteś autorem zdjęcia", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Dodaj zdjęcie", Toast.LENGTH_LONG).show();
                    }
                } else if (contest_name.getText().toString().trim().equals("")) {
                    Toast.makeText(getContext(), "Wpisz tytuł zdjęcia", Toast.LENGTH_LONG).show();
                }

            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(getContext(), Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED) {

                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }
            }
        });
        btn_g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(getContext(), Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED) {

                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        openGallery();
                    }
                } else {
                    openGallery();
                }
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NEW PICTURE");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Brak dostępu", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 0:
                ContentResolver cr = getContext().getContentResolver();
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cur = cr.query(Uri.parse(image_uri.toString()), projection, null, null, null);
                if (cur != null) {
                    cur.moveToFirst();
                    String filePath = cur.getString(0);
                    if ((new File(filePath)).exists()) {
//                        imageView.setRotation(getCameraPhotoOrientation(image_uri.getPath()));
//                        imageView.setImageURI(image_uri);
                        Glide.with(getContext()).load(image_uri).into(imageView);

                    }
                }
                break;
            case 1:
                if (data != null) {
                    image_uri = data.getData();
                    Glide.with(getContext()).load(image_uri).into(imageView);
                    imageView.setImageURI(image_uri);
                }
                break;
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));

    }

    private void uploadfile() {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading");
        progressDialog.show();


        if (image_uri != null) {
            StorageReference filereference = storageReference.child(System.currentTimeMillis() +
                    "." + getFileExtension(image_uri));
            Glide.with(getContext()).asBitmap().load(image_uri).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    resource.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] data = baos.toByteArray();
                    filereference.putBytes(data)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(getContext(), "Zdjęcie zostało dodane", Toast.LENGTH_SHORT).show();
                                    filereference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
                                            String date = df.format(Calendar.getInstance().getTime());
                                            String descdate = "-" + date;
                                            Uri downloadUrl = uri;
                                            databaseReference_user = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("email");
                                            databaseReference_user.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    databaseReference_like = FirebaseDatabase.getInstance().getReference("Likes").child(firstLine).child(uploadId).child("author");
                                                    Upload upload = new Upload(downloadUrl.toString(), 0, 0, uploadId, userID, snapshot.getValue().toString(), contest_name.getText().toString().trim(), Long.parseLong(date), Long.parseLong(descdate));
                                                    progressDialog.show();
                                                    databaseReference.child(uploadId).setValue(upload);
                                                    databaseReference_like.child(userID).setValue(userID);
                                                    databaseCompetition.child(userID).setValue(userID);
                                                    progressDialog.setCanceledOnTouchOutside(false);
                                                    progressDialog.dismiss();
                                                    FirebaseDatabase.getInstance().goOffline();
                                                    Fragment mFragment = new DashboardFragment();
                                                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, mFragment).commit();
                                                    bottomNavigationView.getMenu().findItem(R.id.home_nav).setChecked(true);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.setMessage("Uploaded  " + (int) progress + "%");
                                }
                            });
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });

            //bmp = rotateImageIfRequired(getContext(),bmp,image_uri);


        } else
            Toast.makeText(getContext(), "Wybierz obraz", Toast.LENGTH_SHORT).show();
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imagePath);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 90;
                    break;
                default:
                    rotate = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) {

        // Detect rotation
        int rotation = getRotation(context, selectedImage);
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
            img.recycle();
            return rotatedImg;
        } else {
            return img;
        }
    }

    private static int getRotation(Context context, Uri selectedImage) {

        int rotation = 0;
        ContentResolver content = context.getContentResolver();

        Cursor mediaCursor = content.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{"orientation", "date_added"},
                null, null, "date_added desc");

        if (mediaCursor != null && mediaCursor.getCount() != 0) {
            while (mediaCursor.moveToNext()) {
                rotation = mediaCursor.getInt(0);
                break;
            }
        }
        mediaCursor.close();
        return rotation;
    }
}