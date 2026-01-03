package com.example.billstracker.activities;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Message;
import com.example.billstracker.custom_objects.SupportTicket;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.recycler_adapters.AdminSupportRecyclerAdapter;
import com.example.billstracker.recycler_adapters.SupportMessageRecyclerAdapter;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.Repo;
import com.example.billstracker.tools.Tools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class Support extends AppCompatActivity {

    public static String name;
    public static String userUid;
    public static String adminUid;
    public static String userName;
    ArrayList<SupportTicket> userTickets = new ArrayList<>();
    final LinearLayoutManager lm = new LinearLayoutManager(Support.this);
    ImageView supportBack, submit;
    EditText message;
    LinearLayout chatBox, hideIfTicketsFound, pb;
    ScrollView adminTickets;
    RecyclerView adminTicketList, messageList;
    SupportTicket customerTicket;
    TextView exitAdmin;
    boolean admin;
    FirebaseAuth auth;
    FirebaseFirestore db;
    SupportMessageRecyclerAdapter adapter;
    InputMethodManager mgr;
    User thisUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        pb = findViewById(R.id.pb13);
        message = findViewById(R.id.message);
        submit = findViewById(R.id.submitMessage);
        chatBox = findViewById(R.id.linearLayout9);
        messageList = findViewById(R.id.messageList);
        exitAdmin = findViewById(R.id.exitAdminTickets);
        supportBack = findViewById(R.id.supportBack);
        adminTickets = findViewById(R.id.adminTickets);
        adminTicketList = findViewById(R.id.adminTicketList);
        hideIfTicketsFound = findViewById(R.id.hideIfTicketsFound);
        mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Tools.setupUI(Support.this, findViewById(android.R.id.content));

        thisUser = Repo.getInstance().getUser(Support.this);

        userName = "";
        userUid = "";

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (thisUser.isAdmin()) {
            adminUid = auth.getUid();
            loadAdmin();
        } else {
            loadUser();
        }

        supportBack.setOnClickListener(view -> {
            if (admin) {
                chatBox.setVisibility(View.GONE);
                adminTickets.setVisibility(View.VISIBLE);
                loadAdmin();
            } else {
                pb.setVisibility(View.VISIBLE);
                getOnBackPressedDispatcher().onBackPressed();
            }
            mgr.hideSoftInputFromWindow(message.getWindowToken(), 0);
        });

        final View activityRootView = findViewById(R.id.supportRoot);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            //r will be populated with the coordinates of your view that area still visible.
            activityRootView.getWindowVisibleDisplayFrame(r);

            int heightDiff = activityRootView.getRootView().getHeight() - r.height();
            if (heightDiff > 0.25*activityRootView.getRootView().getHeight()) {
                messageList.smoothScrollToPosition(messageList.getBottom());
            }
        });

        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (message.getText().toString().length() > 2) {
                    submit.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary, getTheme())));
                    submit.setEnabled(true);
                } else {
                    submit.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.tabs, getTheme())));
                    submit.setEnabled(false);
                }

            }
        });

        submit.setOnClickListener(view -> {
            if (message.getText().length() > 1) {
                if (admin) {
                    name = thisUser.getName();
                } else {
                    name = userName;
                }

                Message message1 = new Message(DateFormat.createCurrentDateStringWithTime(), thisUser.getId(), name, admin, message.getText().toString());
                message.setText("");

                if (admin) {
                    userUid = customerTicket.getUserUid();
                    userName = customerTicket.getName();
                    customerTicket.setUnreadByUser(customerTicket.getUnreadByUser() + 1);
                    customerTicket.setAgent(thisUser.getUserName());
                    customerTicket.setAgentUid(thisUser.getId());
                } else {
                    customerTicket.setUnreadByAgent(customerTicket.getUnreadByAgent() + 1);
                    customerTicket.setUserUid(thisUser.getId());
                    customerTicket.setName(thisUser.getName());
                    customerTicket.setUserEmail(thisUser.getUserName());
                }
                customerTicket.setOpen(true);
                if (!admin) {
                    thisUser.setTicketNumber(userUid);
                }
                customerTicket.setId(userUid);
                if (customerTicket.getMessages() == null) {
                    customerTicket.setMessages(new ArrayList<>());
                }
                customerTicket.getMessages().add(message1);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("tickets").document(userUid).set(customerTicket).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        adapter.notifyItemInserted(adapter.getItemCount() + 1);
                        messageList.smoothScrollToPosition(messageList.getBottom());
                    } else {
                        Toast.makeText(Support.this, (CharSequence) task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
                db.collection("users").document(Repo.getInstance().getUid()).set(thisUser, SetOptions.merge());
            }
        });

    }

    public void loadAdmin() {
        adminTickets.setVisibility(View.VISIBLE);
        chatBox.setVisibility(View.GONE);
        userTickets.clear();
        admin = true;
        exitAdmin.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        db.collection("tickets").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userTickets = (ArrayList<SupportTicket>) task.getResult().toObjects(SupportTicket.class);
                ArrayList <SupportTicket> remove = new ArrayList<>();
                for (SupportTicket delete: userTickets) {
                    if (delete.getAgentUid() != null) {
                        if (!delete.getAgentUid().trim().equals(Repo.getInstance().getUid()) && !delete.getAgentUid().equals("Unassigned") || !delete.isOpen()) {
                            remove.add(delete);
                            if (!delete.isOpen()) {
                                delete.setUnreadByAgent(0);
                                FirebaseFirestore.getInstance().collection("tickets").document(delete.getId()).set(delete);
                            }
                        }
                    }
                }
                userTickets.removeAll(remove);
                if (!userTickets.isEmpty()) {
                    hideIfTicketsFound.setVisibility(View.GONE);
                    generateSupportList();
                } else {
                    hideIfTicketsFound.setVisibility(View.VISIBLE);
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    public void generateSupportList() {

        TextView title = findViewById(R.id.textView53);
        Set<SupportTicket> tickets = new LinkedHashSet<>(userTickets);
        userTickets.clear();
        userTickets.addAll(tickets);
        userTickets.sort(Comparator.comparing(SupportTicket::getUnreadByAgent));
        userTickets.sort(Comparator.comparing(SupportTicket::getDateOfLastActivity));
        Collections.reverse(userTickets);
        int counter = 0;
        for (SupportTicket count: userTickets) {
            if (count.getUnreadByAgent() > 0) {
                ++counter;
            }
        }

        title.setText(String.format(Locale.US, "Support Tickets (%d)", counter));
        AdminSupportRecyclerAdapter asra = new AdminSupportRecyclerAdapter(Support.this, userTickets);
        adminTicketList.setAdapter(asra);
        adminTicketList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adminTicketList.setNestedScrollingEnabled(false);
        asra.setViewThreadListener((position, supportTicket) -> generateMessages(supportTicket));
        asra.setResolveTicketListener((ignoredPosition, supportTicket) -> loadAdmin());
    }

    public void loadUser() {

        userName = Objects.requireNonNull(auth.getCurrentUser()).getDisplayName();
        userUid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        admin = false;
        customerTicket = null;

        db.collection("tickets").document(Repo.getInstance().getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Log.d(TAG, document.getId() + " => " + document.getData());
                SupportTicket ticket = document.toObject(SupportTicket.class);
                if (document.exists()) {
                    if (ticket != null) {
                        userTickets.add(ticket);
                        ticket.setUnreadByUser(0);
                        db.collection("tickets").document(document.getId()).set(ticket, SetOptions.merge());
                        customerTicket = ticket;
                        generateMessages(customerTicket);
                    }
                }
            }
            else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
            if (customerTicket == null) {
                customerTicket = new SupportTicket(thisUser.getName(), thisUser.getId(), thisUser.getUserName(), "Unassigned", new ArrayList<>(), "",
                        true, thisUser.getId(), 0, 0, 0, "Unassigned");
            }
            generateMessages(customerTicket);
        });
    }

    public void generateMessages(SupportTicket ticket) {

        adminTickets.setVisibility(View.GONE);
        chatBox.setVisibility(View.VISIBLE);
        customerTicket = ticket;
        userName = ticket.getName();
        userUid = ticket.getUserUid();
        if (ticket.getMessages() == null || ticket.getMessages().isEmpty()) {
            Message newMessage = new Message(DateFormat.createCurrentDateStringWithTime(), "Intro support message", getString(R.string.billTracker), false, getString(R.string.supportGreeting));
            if (ticket.getMessages() == null) {
                ticket.setMessages(new ArrayList<>());
            }
            ticket.getMessages().add(newMessage);
        }
        if (thisUser.isAdmin()) {
            ticket.setUnreadByAgent(0);
        } else {
            ticket.setUnreadByUser(0);
        }

        adapter = new SupportMessageRecyclerAdapter(Support.this, ticket.getMessages());
        messageList.setAdapter(adapter);
        messageList.setLayoutManager(lm);
        messageList.setNestedScrollingEnabled(true);
        messageList.smoothScrollToPosition(messageList.getBottom());
    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
        if (thisUser.isAdmin()) {
            loadAdmin();
        } else {
            loadUser();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (thisUser.isAdmin()) {
            loadAdmin();
        } else {
            loadUser();
        }
    }
}