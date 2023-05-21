package com.example.billstracker;

import static com.android.volley.VolleyLog.TAG;
import static com.example.billstracker.Logon.thisUser;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class Administrator extends AppCompatActivity {

    int PICK_IMAGE = 200;
    ImageView icon;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    EditText billerName;
    Biller biller;
    private Uri filePath;
    EditText website;
    Spinner type;
    ProgressBar pb;
    StorageTask uploadTask;
    LinearLayout display, listTickets;
    TextView uploadComplete;
    static ArrayList <SupportTicket> tickets = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrator);

        TextView addBillerInfo = findViewById(R.id.addBillerInfo);
        display = findViewById(R.id.notices);
        uploadComplete = findViewById(R.id.uploadComplete);
        //View manual = View.inflate(Administrator.this, R.layout.manual_biller, null);
        //icon = manual.findViewById(R.id.billerIcon);
        //Button changeIcon = manual.findViewById(R.id.changeIcon);
        //billerName = manual.findViewById(R.id.newBillerName);
        //website = manual.findViewById(R.id.newWebsite);
        //type = manual.findViewById(R.id.typeSpinner);
        //Button submit = manual.findViewById(R.id.submitManualBiller);
        pb = findViewById(R.id.progressBar11);
        storageReference = FirebaseStorage.getInstance().getReference("images");
        databaseReference = FirebaseDatabase.getInstance().getReference("images");
        listTickets = findViewById(R.id.listTickets);

        listTickets.invalidate();
        listTickets.removeAllViews();

        loadTickets();
        generateTickets();

        String[] types = {"Credit Card", "Auto Loan", "Utility", "Mortgage", "Retail Installment", "PayDay Loan", "Insurance", "Taxes", "Payroll", "Accounts Receivable"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        Picasso.get().setLoggingEnabled(true);

        addBillerInfo.setOnClickListener(view -> {
            display.removeAllViews();
            display.invalidate();
            //display.addView(manual);
        });

        //changeIcon.setOnClickListener(v -> {
            //Intent i = new Intent();
            //i.setType("image/*");
            //i.setAction(Intent.ACTION_GET_CONTENT);
            //startForResult.launch(i);
        //});
        //submit.setOnClickListener(view -> {
            //if (billerName.getText().toString().length() > 0 && website.getText().toString().length() > 0 && filePath != null) {
                //uploadImage();
            //}
        //});


    }

    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getData() != null) {

                filePath = o.getData().getData();
                Picasso.get().load(filePath).into(icon);
            }
        }
    });

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
                            biller = new Biller(billerName.getText().toString().trim(), website.getText().toString().trim(), imageUrl, type.getSelectedItem().toString());
                            String uploadId = databaseReference.getKey();
                            databaseReference.child(Objects.requireNonNull(uploadId)).setValue(biller);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("billers").document(billerName.getText().toString()).set(biller);
                            display.removeAllViews();
                            display.invalidate();
                            uploadComplete.setVisibility(View.VISIBLE);
                            handler.postDelayed(() -> uploadComplete.setVisibility(View.GONE), 5000);
                            Toast.makeText(Administrator.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            })

                    .addOnFailureListener(e -> {

                        pb.setVisibility(View.GONE);
                        Toast.makeText(Administrator.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                        pb.setProgress((int) progress);
                    });
        }
    }

    public void loadTickets () {

        db.collection("tickets").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    SupportTicket ticket = document.toObject(SupportTicket.class);
                    tickets.add(ticket);
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    public void generateTickets () {

        listTickets.invalidate();
        listTickets.removeAllViews();

        if (!tickets.isEmpty()) {
            Toast.makeText(Administrator.this, "A ticket was found", Toast.LENGTH_SHORT).show();
            for (SupportTicket ticket: tickets) {
                if (ticket.getAgent().equals(thisUser.getUserName()) || ticket.getAgent().equals("Unassigned")) {
                    View ticketView = View.inflate(Administrator.this, R.layout.user_support_ticket, null);
                    TextView ticketNumber = ticketView.findViewById(R.id.tvTicketNumber);
                    TextView status = ticketView.findViewById(R.id.ticketStatus);
                    TextView customerName = ticketView.findViewById(R.id.tvCustomerName);
                    TextView customerEmail = ticketView.findViewById(R.id.tvCustomerEmail);
                    TextView assignedTo = ticketView.findViewById(R.id.tvTicketAssignedTo);
                    TextView notes = ticketView.findViewById(R.id.ticketNotes);
                    Button selfAssign = ticketView.findViewById(R.id.btnSelfAssign);
                    Button sendResponse = ticketView.findViewById(R.id.btnSendResponse);
                    Button resolve = ticketView.findViewById(R.id.btnResolve);

                    ticketNumber.setText(String.valueOf(ticket.getId()));
                    if (ticket.isOpen()) {
                        status.setText(getString(R.string.open1));
                    }
                    else {
                        status.setText(getString(R.string.closed1));
                    }
                    customerName.setText(ticket.getName());
                    customerEmail.setText(ticket.getUserEmail());
                    assignedTo.setText(ticket.getAgent());
                    for (String note: ticket.getNotes()) {
                        notes.append(note + "\n");
                    }
                    selfAssign.setOnClickListener(view -> {
                        ticket.setAgent(thisUser.getUserName());
                        db.collection("tickets").document(String.valueOf(ticket.getId())).set(ticket);
                        generateTickets();
                    });
                    sendResponse.setOnClickListener(view -> {

                    });
                    resolve.setOnClickListener(view -> {
                        ticket.setOpen(false);
                        db.collection("tickets").document(String.valueOf(ticket.getId())).set(ticket);
                        generateTickets();
                    });
                    listTickets.addView(ticketView);
                }
            }
        }


















    }
}