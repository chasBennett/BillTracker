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
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Message;
import com.example.billstracker.custom_objects.SupportTicket;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.recycler_adapters.SupportMessageRecyclerAdapter;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.SwipeReplyCallback;
import com.example.billstracker.tools.Tools;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.MessageFormat;
import java.util.ArrayList;

public class Support extends BaseActivity {

    public static String name;
    public static String userUid;
    public static String adminUid;
    public static String userName;
    final LinearLayoutManager lm = new LinearLayoutManager(Support.this);
    ImageView supportBack, submit;
    EditText message;
    LinearLayout pb;
    RecyclerView messageList;
    SupportTicket customerTicket;
    boolean admin = false;
    FirebaseAuth auth;
    FirebaseFirestore db;
    SupportMessageRecyclerAdapter adapter;
    InputMethodManager mgr;
    User thisUser;
    String ticketId;

    LinearLayout replyLayout, inputContainer;
    TextView replyName, replyText, customerName;
    ShapeableImageView profileImage;
    ImageView cancelReply;
    Message replyingToMessage = null;
    Message editingMessage = null;
    int editingPosition = -1;

    @Override
    protected void onDataReady() {
        setContentView(R.layout.activity_support);

        pb = findViewById(R.id.pb13);
        message = findViewById(R.id.message);
        submit = findViewById(R.id.submitMessage);
        messageList = findViewById(R.id.messageList);
        supportBack = findViewById(R.id.supportBack);
        customerName = findViewById(R.id.customerName);
        profileImage = findViewById(R.id.userSupportImage);
        mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Tools.setupUI(Support.this, findViewById(android.R.id.content));

        replyLayout = findViewById(R.id.replyLayout);
        replyName = findViewById(R.id.replyName);
        replyText = findViewById(R.id.replyText);
        cancelReply = findViewById(R.id.cancelReply);
        inputContainer = findViewById(R.id.inputContainer);

        cancelReply.setOnClickListener(v -> {
            replyLayout.setVisibility(View.GONE);
            replyingToMessage = null;
        });

        thisUser = repo.getUser();
        admin = thisUser.isAdmin();

        Bundle extras = getIntent().getExtras();
        if (extras != null && admin) {
            if (extras.getString("ticket", null) != null) {
                ticketId = extras.getString("ticket");
            }
            else {
                Notify.createPopup(Support.this, "Ticket not found", null);
            }
        }
        else {
            ticketId = thisUser.getId();
        }

        userName = "";
        userUid = "";

        pb.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        supportBack.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            getOnBackPressedDispatcher().onBackPressed();
            mgr.hideSoftInputFromWindow(message.getWindowToken(), 0);
        });

        final View activityRootView = findViewById(R.id.supportRoot);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            //r will be populated with the coordinates of your view that area still visible.
            activityRootView.getWindowVisibleDisplayFrame(r);

            int heightDiff = activityRootView.getRootView().getHeight() - r.height();
            if (heightDiff > 0.25 * activityRootView.getRootView().getHeight()) {
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
            String text = message.getText().toString().trim();
            if (text.length() > 1) {

                if (editingMessage != null && editingPosition != -1) {
                    // --- MODE: EDITING ---
                    // Update the actual object inside the list
                    customerTicket.getMessages().get(editingPosition).setMessage(text);

                    db.collection("tickets").document(customerTicket.getId())
                            .set(customerTicket, SetOptions.merge())
                            .addOnCompleteListener(task -> {
                                pb.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Notify the adapter of the SPECIFIC change to update the bubble text
                                    adapter.notifyItemChanged(editingPosition);
                                    resetInputArea();
                                } else {
                                    Notify.createPopup(Support.this, "Update failed", null);
                                }
                            });

                } else {
                    // --- MODE: SENDING NEW MESSAGE ---
                    if (admin) {
                        name = thisUser.getName();
                    } else {
                        name = userName;
                    }

                    String replyContent = (replyingToMessage != null) ? replyingToMessage.getMessage() : null;
                    String replyAuthor = (replyingToMessage != null) ? replyingToMessage.getName() : null;
                    String replyAuthorUid = (replyingToMessage != null) ? replyingToMessage.getAuthorId() : null;

                    Message newMessage = new Message(
                            DateFormat.createCurrentDateStringWithTime(),
                            thisUser.getId(),
                            name,
                            admin,
                            message.getText().toString(),
                            replyContent, // Pass the quoted text
                            replyAuthor,   // Pass the quoted author
                            replyAuthorUid // Pass the quoted author's uid
                    );

                    newMessage.setRead(false);

                    // Update Ticket Metadata
                    if (admin) {
                        customerTicket.setUnreadByUser(customerTicket.getUnreadByUser() + 1);
                        customerTicket.setAgent(thisUser.getUserName());
                        customerTicket.setAgentUid(thisUser.getId());
                    } else {
                        customerTicket.setUnreadByAgent(customerTicket.getUnreadByAgent() + 1);
                    }
                    customerTicket.setOpen(true);

                    if (customerTicket.getMessages() == null) {
                        customerTicket.setMessages(new ArrayList<>());
                    }

                    // Add to list and get new index
                    customerTicket.getMessages().add(newMessage);
                    int newPosition = customerTicket.getMessages().size() - 1;

                    db.collection("tickets").document(customerTicket.getId())
                            .set(customerTicket, SetOptions.merge())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Notify adapter of insertion
                                    if (newPosition > 0) {
                                        adapter.notifyItemChanged(newPosition - 1); // Remove margin from previous
                                    }

                                    // SAFE SCROLL: Use the index, not getBottom()
                                    messageList.smoothScrollToPosition(newPosition);

                                    resetInputArea();
                                } else {
                                    Log.d(TAG, "Error adding document", task.getException());
                                    Notify.createPopup(Support.this, "Error sending message", null);
                                }
                            });

                    // Update local user ticket number if not admin
                    if (!admin) {
                        db.collection("users").document(repo.getUid()).set(thisUser, SetOptions.merge());
                    }
                }
            } else {
                Notify.createPopup(Support.this, "Message cannot be empty", null);
            }
        });

    }

    public void loadChat () {
        userName = thisUser.getName();
        userUid = thisUser.getId();
        customerTicket = null;

        db.collection("tickets").document(ticketId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    SupportTicket ticket = document.toObject(SupportTicket.class);
                    if (ticket != null) {
                        if (admin) {
                            ticket.setUnreadByAgent(0);
                        } else {
                            ticket.setUnreadByUser(0);
                        }
                        db.collection("tickets").document(document.getId()).set(ticket, SetOptions.merge());
                        customerTicket = ticket;
                    }
                }
            }

            if (customerTicket == null) {
                if (!admin) {
                    customerTicket = new SupportTicket(thisUser.getName(), thisUser.getId(),
                            thisUser.getUserName(), "Unassigned", new ArrayList<>(), "",
                            true, thisUser.getId(), 0, 0, 0, "Unassigned");
                } else {
                    Notify.createPopup(Support.this, "Ticket not found", null);
                    return;
                }
            }
            generateMessages(customerTicket);
        });
    }

    public void generateMessages(SupportTicket ticket) {
        customerTicket = ticket;
        userName = ticket.getName();
        userUid = ticket.getUserUid();
        if (ticket.getMessages() == null || ticket.getMessages().isEmpty()) {
            Message newMessage = new Message(DateFormat.createCurrentDateStringWithTime(), "Intro support message", getString(R.string.billTracker), false, getString(R.string.supportGreeting), null, null, null);
            if (ticket.getMessages() == null) {
                ticket.setMessages(new ArrayList<>());
            }
            ticket.getMessages().add(newMessage);
        }

        if (admin) {
            customerName.setText(customerTicket.getName());
            if (customerTicket.getUserUid() != null && !customerTicket.getUserUid().isEmpty()) {
                setUserPhoto(customerTicket.getUserUid());
            }
        }
        else {
            if (ticket.getAgentUid() != null && !ticket.getAgentUid().isEmpty()) {
                setUserPhoto(ticket.getAgentUid());
            }
            if (ticket.getMessages().size() > 1) {
                for (Message msg: ticket.getMessages()) {
                    if (msg.isAgent()) {
                        customerName.setText(MessageFormat.format("Agent: {0}", msg.getName()));
                        break;
                    }
                }
            }
        }

        adapter = new SupportMessageRecyclerAdapter(Support.this, ticket.getMessages());
        messageList.setLayoutManager(lm);
        messageList.setNestedScrollingEnabled(true);

        SwipeReplyCallback.SwipeReplyListener swipeListener = position -> {
            adapter.handleSwipeReply(position);
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeReplyCallback(swipeListener));
        itemTouchHelper.attachToRecyclerView(messageList);

        adapter.setOnReplyListener(message -> {
            replyingToMessage = message;
            replyLayout.setVisibility(View.VISIBLE);

            inputContainer.setBackgroundResource(R.drawable.border_styles_replying);

            replyName.setText(message.getName());
            replyText.setText(message.getMessage());

            this.message.requestFocus();
            mgr.showSoftInput(this.message, InputMethodManager.SHOW_IMPLICIT);
        });

        cancelReply.setOnClickListener(v -> {
            replyLayout.setVisibility(View.GONE);
            replyingToMessage = null;

            inputContainer.setBackgroundResource(R.drawable.border_styles_oval);
        });

        messageList.setAdapter(adapter);
        if (adapter.getItemCount() > 0) {
            messageList.smoothScrollToPosition(adapter.getItemCount() - 1);
        }

        adapter.setOnMessageLongClickListener((message, position) -> {
            if (message.getAuthorId().equals(thisUser.getId())) {
                if (position != RecyclerView.NO_POSITION) {
                    Message freshMessage = customerTicket.getMessages().get(position);

                    boolean isMyMessage = freshMessage.getAuthorId().equals(thisUser.getId());
                    boolean isUnread = admin ? customerTicket.getUnreadByUser() > 0 : customerTicket.getUnreadByAgent() > 0;

                    if (isMyMessage && isUnread) {
                        showEditDeleteDialog(freshMessage, position);
                    }
                }
            }
        });
    }

    private void setUserPhoto (String uid) {
            FirebaseFirestore.getInstance().collection("userPhotos").document(uid)
                    .get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String url = documentSnapshot.getString("photoUrl");
                            if (url != null && !url.isEmpty()) {
                                Glide.with(Support.this)
                                        .load(url)
                                        .circleCrop()
                                        .placeholder(R.drawable.default_user)
                                        .into(profileImage);
                            }
                        }
                    });
    }

    private void showEditDeleteDialog(Message msg, int position) {
        String[] options = {"Reply", "Edit", "Delete"};

        new AlertDialog.Builder(this)
                .setTitle("Message Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        replyingToMessage = msg;
                        replyLayout.setVisibility(View.VISIBLE);

                        inputContainer.setBackgroundResource(R.drawable.border_styles_replying);

                        replyName.setText(msg.getName());
                        replyText.setText(msg.getMessage());

                        this.message.requestFocus();
                        mgr.showSoftInput(this.message, InputMethodManager.SHOW_IMPLICIT);
                    }
                    else if (which == 1) { // Edit
                        editingMessage = msg;
                        editingPosition = position;

                        // Show the Editing UI
                        replyLayout.setVisibility(View.VISIBLE);
                        replyName.setText(R.string.editing_message);
                        replyText.setText(msg.getMessage());
                        inputContainer.setBackgroundResource(R.drawable.border_styles_replying);

                        // Populate and Focus
                        message.setText(msg.getMessage());
                        message.setSelection(message.getText().length()); // Put cursor at end
                        message.requestFocus();

                        mgr.showSoftInput(message, InputMethodManager.SHOW_IMPLICIT);
                    } else {
                        deleteMessage(position);
                    }
                })
                .show();
    }

    private void deleteMessage(int position) {
        customerTicket.getMessages().remove(position);

        db.collection("tickets").document(customerTicket.getId())
                .set(customerTicket, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {

                    messageList.post(() -> {
                        adapter.notifyItemRemoved(position);

                        int countAfter = customerTicket.getMessages().size() - position;
                        if (countAfter > 0) {
                            adapter.notifyItemRangeChanged(position, countAfter);
                        }

                        if (position == customerTicket.getMessages().size()) {
                            adapter.notifyItemChanged(customerTicket.getMessages().size() - 1);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Notify.createPopup(Support.this, "Failed to delete message", null);
                });
    }

    private void resetInputArea() {
        message.setText("");
        editingMessage = null;
        editingPosition = -1;
        replyingToMessage = null; // Clear the reference
        replyLayout.setVisibility(View.GONE); // Hide the preview bar
        inputContainer.setBackgroundResource(R.drawable.border_styles_oval); // Reset background
        mgr.hideSoftInputFromWindow(message.getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
        loadChat();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadChat();
    }
}