package com.emre.instagramclonejava.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.emre.instagramclonejava.databinding.ActivityUploadBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    private FirebaseStorage firebaseStorage;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Uri imageData;
    private ActivityUploadBinding binding;

    //Bitmap selectedBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();
        firebaseStorage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference(); //Storage / Depodan referans alır
    }

    public void upload(View view) {

        if (imageData != null) {

            //Universal Unique ID
            UUID uuid = UUID.randomUUID();
            String imageName = "images/" + uuid + ".jpg";

            //Referans -> Veriyi depoda nereye kaydedeceğimizi seçme
            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                // child ile "/" öncesindeki isim bir dosya oluşturur.

                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    StorageReference newReferance = firebaseStorage.getReference(imageName);
                    newReferance.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            String comment = binding.commentText.getText().toString();
                            FirebaseUser user = auth.getCurrentUser();
                            String email = user.getEmail();

                            HashMap<String, Object> postData = new HashMap<>();
                            //Object genel değişken tipleri için kullanılır. String de olabilir int da
                            postData.put("userEmail", email);
                            postData.put("downloadUrl", downloadUrl);
                            postData.put("comment", comment);
                            postData.put("date", FieldValue.serverTimestamp()); // güncel tarihi verir

                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Intent intent = new Intent(UploadActivity.this, FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // tüm aktiviteleri kapatır
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    public void selectImage(View view) {
        //İzin verilip verilmediği kontrol ediyoruz
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //İzin yoksa çalışan bölüm
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //İlk izin reddedilirse bu kısım ile izni zorunlu kılıyoruz.
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            } else {
                //İzin istenen bölüm
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

            }
        } else {
            // İzin verilmişse çalışacak bölüm
            //Action_Pick git ve oradan bir şey seç anlamına gelir
            //Virgülden sonrası ise gidilecek yer
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);

        }

    }

    private void registerLauncher() {
        // ActivityResultContracts.StartActivityForResult() -> Yeni bir aktivite çalıştırır ancak bir sonuç için çalıştırır (galeriden görsel sonucu gibi)
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent intentFromResult = result.getData(); // gidilen aktiviteden alınan veriyi intentFromResult değişkenine attık
                    if (intentFromResult != null) {
                        //gidilen aktiviteden bir sonuç alındıysa
                        imageData = intentFromResult.getData(); //Uri bilgisini / dosya yolunu verir
                        binding.imageView.setImageURI(imageData);

                        /* Bitmap'e çevirme
                        try {

                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(UploadActivity.this.getContentResolver(),imageData);
                                selectedBitmap = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedBitmap);
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(UploadActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedBitmap);

                            }

                        }catch (Exception e) {
                            e.printStackTrace();
                        }*/
                    }
                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //İzin verildi
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);


                } else {
                    Toast.makeText(UploadActivity.this, "Permission Needed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}