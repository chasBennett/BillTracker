package com.example.billstracker.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.billstracker.R;
import com.bumptech.glide.Glide;
import com.example.billstracker.custom_objects.Biller;
import com.example.billstracker.tools.DataTools;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** @noinspection rawtypes*/
public class Administrator extends AppCompatActivity {


    ImageView icon, back;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    EditText billerName;
    Biller biller;
    EditText website;
    Spinner type;
    ProgressBar pb;
    @SuppressWarnings("unused")
    StorageTask uploadTask;
    LinearLayout display;
    TextView uploadComplete;
    Button changeIcon, submit;
    View manual;
    private Uri filePath;
    final ActivityResultLauncher<Intent> startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getData() != null) {

                filePath = o.getData().getData();
                icon.setImageTintList(null);
                Glide.with(icon).load(filePath).into(icon);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrator);

        display = findViewById(R.id.notices);
        uploadComplete = findViewById(R.id.uploadComplete);
        manual = View.inflate(Administrator.this, R.layout.manual_biller, null);
        icon = manual.findViewById(R.id.billerIcon);
        changeIcon = manual.findViewById(R.id.changeIcon);
        billerName = manual.findViewById(R.id.newBillerName);
        website = manual.findViewById(R.id.newWebsite);
        type = manual.findViewById(R.id.typeSpinner);
        back = manual.findViewById(R.id.backToMain);
        submit = manual.findViewById(R.id.submitManualBiller);
        pb = findViewById(R.id.progressBar11);
        storageReference = FirebaseStorage.getInstance().getReference("images");
        databaseReference = FirebaseDatabase.getInstance().getReference("images");

        String[] types = DataTools.getCategories(Administrator.this).toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        display.removeAllViews();
        display.invalidate();
        display.addView(manual);

        changeIcon.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startForResult.launch(i);
        });
        submit.setOnClickListener(view -> {
            if (!billerName.getText().toString().isEmpty() && !website.getText().toString().isEmpty() && filePath != null) {
                display.removeAllViews();
                display.invalidate();
                uploadImage();
            }
        });
        back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


    }

    private String getFileExtension(Uri uri) {

        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadImage() {
        if (filePath != null) {

            pb.setVisibility(View.VISIBLE);
            StorageReference fileReference = storageReference.child(UUID.randomUUID().toString() + "." + getFileExtension(filePath));

            uploadTask = fileReference.putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> {
                            pb.setProgress(0);
                            pb.setVisibility(View.GONE);
                        }, 1000);
                        if (taskSnapshot.getMetadata() != null) {
                            if (taskSnapshot.getMetadata().getReference() != null) {
                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();
                                    biller = new Biller(billerName.getText().toString().trim(), website.getText().toString().trim(), imageUrl, type.getSelectedItemPosition());
                                    String uploadId = databaseReference.getKey();
                                    databaseReference.child(Objects.requireNonNull(uploadId)).setValue(biller);
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("billers").document(billerName.getText().toString()).set(biller);
                                    display.removeAllViews();
                                    display.invalidate();
                                    uploadComplete.setVisibility(View.VISIBLE);
                                    handler.postDelayed(() -> uploadComplete.setVisibility(View.GONE), 5000);
                                    Toast.makeText(Administrator.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                                    DataTools.getLatestBillersVersion((wasSuccessful, version) -> {
                                        Map<String, Object> update = new HashMap<>();
                                        update.put("version", version + 1);
                                        FirebaseFirestore.getInstance().collection("versions").document("billers").set(update);
                                    });
                                    display.addView(manual);
                                });
                            }
                        }
                    })

                    .addOnFailureListener(e -> {

                        pb.setVisibility(View.GONE);
                        Toast.makeText(Administrator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        display.addView(manual);
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                        pb.setProgress((int) progress);
                    });
        }
    }
}