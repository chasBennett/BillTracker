package com.example.billstracker;

import static com.android.volley.VolleyLog.TAG;
import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class Support extends AppCompatActivity {

    ImageView supportBack, submit, backToAdminTickets;
    EditText message;
    public static String email, name, userUid, welcomeMessage, adminUid, userEmail, userName;
    LinearLayout messageList, adminTicketList, adminTickets, chatBox, hideIfTicketsFound, text, pb;
    DateFormatter df = new DateFormatter();
    SupportTicket customerTicket;
    TextView exitAdmin;
    NestedScrollView scroll;
    boolean admin;
    ArrayList <SupportTicket> userTickets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        pb = findViewById(R.id.pb13);
        text = findViewById(R.id.linearLayout10);
        message = findViewById(R.id.message);
        scroll = findViewById(R.id.supportScroll);
        submit = findViewById(R.id.submitMessage);
        chatBox = findViewById(R.id.linearLayout9);
        messageList = findViewById(R.id.messageList);
        exitAdmin = findViewById(R.id.exitAdminTickets);
        supportBack = findViewById(R.id.supportBack);
        adminTickets = findViewById(R.id.adminTickets);
        adminTicketList = findViewById(R.id.adminTicketList);
        hideIfTicketsFound = findViewById(R.id.hideIfTicketsFound);
        backToAdminTickets = findViewById(R.id.backToAdminTickets);

        userName = "";
        userUid = "";
        userEmail = "";

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (thisUser.getAdmin()) {
            admin = true;
            adminUid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
            exitAdmin.setOnClickListener(view -> onBackPressed());
            backToAdminTickets.setOnClickListener(view -> recreate());
        }
        else {
            userName = Objects.requireNonNull(auth.getCurrentUser()).getDisplayName();
            userUid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
            userEmail = Objects.requireNonNull(auth.getCurrentUser().getEmail());
            ArrayList <String> messages1 = new ArrayList<>();
            customerTicket = new SupportTicket(name, userUid, email, "Unassigned", messages1, true, userUid, 0, 0, "Unassigned");
            admin = false;
            adminTickets.setVisibility(View.GONE);
            backToAdminTickets.setVisibility(View.GONE);
        }

        supportBack.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            onBackPressed();
        });

        load();

        submit.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);

            ArrayList <String> currentMessages = customerTicket.getNotes();
            String writerUid;
            if (admin) {
                name = getString(R.string.agent) + " " + thisUser.getName();
                writerUid = adminUid;
            }
            else {
                name = userName;
                writerUid = userUid;
            }

            String newMessage = writerUid + "%$/" + df.createCurrentDateStringWithTime() + "%$/" + name + "%$/" + message.getText().toString() + "%$/false";
            currentMessages.add(newMessage);
            customerTicket.setNotes(currentMessages);
            if (admin) {
                userUid = customerTicket.getUserUid();
                userEmail = customerTicket.getUserEmail();
                userName = customerTicket.getName();
                customerTicket.setUnreadByUser(customerTicket.getUnreadByUser() + 1);
                customerTicket.setAgent(thisUser.getUserName());
                customerTicket.setAgentUid(adminUid);
            }
            else {
                customerTicket.setUnreadByAgent(customerTicket.getUnreadByAgent() + 1);
                customerTicket.setUserUid(userUid);
                customerTicket.setName(userName);
                customerTicket.setUserEmail(userEmail);
            }
            customerTicket.setOpen(true);
            if (!admin) {
                thisUser.setTicketNumber(userUid);
            }
            customerTicket.setId(userUid);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("tickets").document(userUid).set(customerTicket).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    messageList.addView(generateBubble(newMessage));
                    message.setText("");
                    scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
                } else {
                    Toast.makeText(Support.this, (CharSequence) task.getException(), Toast.LENGTH_SHORT).show();
                }
            });
            db.collection("users").document(uid).set(thisUser, SetOptions.merge());
            pb.setVisibility(View.GONE);
        });

    }

    public void load() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (admin) {
            db.collection("tickets").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        SupportTicket ticket = document.toObject(SupportTicket.class);
                        if (ticket.getAgentUid() != null) {
                            if (ticket.getAgentUid().trim().equals(adminUid.trim()) || ticket.getAgentUid().equals("Unassigned")) {
                                userTickets.add(ticket);
                                ticket.setUnreadByAgent(0);
                            }
                        }
                    }
                    generateSupportList();
                }
                else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            });
        }
        else {
            db.collection("tickets").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        SupportTicket ticket = document.toObject(SupportTicket.class);
                        if (document.exists()) {
                            if (ticket.getId() != null) {
                                if (ticket.getId().equals(userUid)) {
                                    customerTicket = ticket;
                                    ticket.setUnreadByUser(0);
                                    break;
                                }
                            }
                        }
                    }
                    if (customerTicket == null || customerTicket.getNotes() == null || customerTicket.getNotes().isEmpty() || customerTicket.getNotes().size() <= 1) {
                        newTicket();
                    }
                    generateMessages(customerTicket);
                }
                else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            });
        }
    }

    public void generateMessages (SupportTicket ticket) {

        customerTicket = ticket;
        userName = ticket.getName();
        userEmail = ticket.getUserEmail();
        userUid = ticket.getUserUid();
        messageList.invalidate();
        messageList.removeAllViews();
        for (String message : customerTicket.getNotes()) {

            messageList.addView(generateBubble(message));
        }
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        if (admin) {
            customerTicket.setUnreadByAgent(0);
        }
        else {
            customerTicket.setUnreadByUser(0);
        }
        customerTicket.setId(userUid);
    }

    public View generateBubble(String message) {

        String[] split = message.split("%\\$/");
        String writerUid = split[0];
        String writer = split[2];
        String timeStamp = split[1];
        if (customerTicket.getNotes().size() == 1) {
            timeStamp = df.createCurrentDateStringWithTime();
        }
        String writerMessage = split[3];
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(20,50,200,50);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(200,50,20,50);
        lp1.gravity = Gravity.END;

        if (!admin) {
            if (!writerUid.equals(userUid)) {
                View agentBubble = View.inflate(Support.this, R.layout.agent_bubble_layout, null);
                TextView agentName = agentBubble.findViewById(R.id.agentName);
                TextView agentMessage = agentBubble.findViewById(R.id.agentMessage);
                TextView agentTime = agentBubble.findViewById(R.id.agentTime);
                agentName.setText(writer);
                agentMessage.setText(writerMessage);
                agentTime.setText(timeStamp);
                agentBubble.setLayoutParams(lp);
                return agentBubble;
            } else {
                View customerBubble = View.inflate(Support.this, R.layout.customer_bubble_layout, null);
                TextView customerName = customerBubble.findViewById(R.id.customerName);
                TextView customerMessage = customerBubble.findViewById(R.id.customerMessage);
                TextView customerTime = customerBubble.findViewById(R.id.customerTime);
                customerName.setText(writer);
                customerMessage.setText(writerMessage);
                customerTime.setText(timeStamp);
                customerBubble.setLayoutParams(lp1);
                return customerBubble;
            }
        }
        else {
            if (!writerUid.equals(adminUid) && !writerUid.equals("0")) {
                View agentBubble = View.inflate(Support.this, R.layout.agent_bubble_layout, null);
                TextView agentName = agentBubble.findViewById(R.id.agentName);
                TextView agentMessage = agentBubble.findViewById(R.id.agentMessage);
                TextView agentTime = agentBubble.findViewById(R.id.agentTime);
                agentName.setText(writer);
                agentMessage.setText(writerMessage);
                agentTime.setText(timeStamp);
                agentBubble.setLayoutParams(lp);
                return agentBubble;
            } else {
                View customerBubble = View.inflate(Support.this, R.layout.customer_bubble_layout, null);
                TextView customerName = customerBubble.findViewById(R.id.customerName);
                TextView customerMessage = customerBubble.findViewById(R.id.customerMessage);
                TextView customerTime = customerBubble.findViewById(R.id.customerTime);
                customerName.setText(writer);
                customerMessage.setText(writerMessage);
                customerTime.setText(timeStamp);
                customerBubble.setLayoutParams(lp1);
                return customerBubble;
            }
        }
    }

    public void newTicket() {

        ArrayList<String> messages1 = new ArrayList<>();
        welcomeMessage = "0%$/" + df.currentDateAndTimeString() + "%$/" + getString(R.string.customerService) + "%$/" + getString(R.string.supportGreeting);
        messages1.add(welcomeMessage);
        if (!admin) {
            customerTicket = new SupportTicket(userName, userUid, userEmail, "Unassigned", messages1, true, userUid, 0, 0, "Unassigned");
        }

    }

    public void generateSupportList () {

        adminTickets.setVisibility(View.VISIBLE);
        adminTicketList.removeAllViews();
        adminTicketList.invalidate();
        chatBox.setVisibility(View.GONE);
        text.setVisibility(View.GONE);
        TextView title = findViewById(R.id.textView53);
        title.setText(String.format(Locale.US, "Support Tickets (%d)", userTickets.size()));
        int counter = 0;
        userTickets.sort(Comparator.comparing(SupportTicket::isOpen).reversed());
        if (!userTickets.isEmpty()) {
            for (SupportTicket ticket: userTickets) {
                View preview = View.inflate(Support.this, R.layout.user_support_ticket, null);
                ImageView viewTicketDetails = preview.findViewById(R.id.viewTicketDetails);
                LinearLayout ticketDetails = preview.findViewById(R.id.ticketDetails);
                TextView ticketNumber = preview.findViewById(R.id.tvTicketNumber);
                TextView ticketName = preview.findViewById(R.id.tvCustomerName);
                TextView ticketEmail = preview.findViewById(R.id.tvCustomerEmail);
                TextView ticketOpen = preview.findViewById(R.id.ticketStatus);
                TextView unread = preview.findViewById(R.id.tvUnreadByAgent);
                TextView notes = preview.findViewById(R.id.ticketNotes);
                TextView agent = preview.findViewById(R.id.tvTicketAssignedTo);
                Button btnSelfAssign = preview.findViewById(R.id.btnSelfAssign);
                Button btnSendResponse = preview.findViewById(R.id.btnSendResponse);
                Button btnResolve = preview.findViewById(R.id.btnResolve);
                ticketNumber.setText(String.format("Ticket Number: %s", ticket.getId()));
                ticketName.setText(ticket.getName());
                ticketEmail.setText(ticket.getUserEmail());
                boolean [] showing = {false};
                if (ticket.isOpen()) {
                    ticketOpen.setText(R.string.open1);
                }
                else {
                    ticketOpen.setText(R.string.closed1);
                }
                unread.setText(String.format(Locale.US, "Unread Messages: %d", ticket.getUnreadByAgent()));
                agent.setText(String.format(Locale.US, "Agent: %s", ticket.getAgent()));
                for (String note: ticket.getNotes()) {
                    String[] split = note.split(getString(R.string.splitRegex));
                    String writer = split[2];
                    String timeStamp = split[1];
                    String writerMessage = split[3];
                    notes.append(writer + "\n\n" + timeStamp + "\n\n" + writerMessage + "\n\n\n");
                }
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(50,50,50,50);
                preview.setLayoutParams(lp);
                adminTicketList.addView(preview);
                ++counter;
                ticket.setUnreadByAgent(0);
                btnSendResponse.setOnClickListener(view -> {
                    adminTickets.setVisibility(View.GONE);
                    backToAdminTickets.setVisibility(View.VISIBLE);
                    chatBox.setVisibility(View.VISIBLE);
                    text.setVisibility(View.VISIBLE);
                    generateMessages(ticket);
                });
                if (counter > 0) {
                    hideIfTicketsFound.setVisibility(View.GONE);
                }
                if (ticket.getAgent().equals(thisUser.getUserName())) {
                    btnSelfAssign.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey, getTheme())));
                    btnSelfAssign.setEnabled(false);
                }
                else {
                    btnSelfAssign.setOnClickListener(view -> {
                        pb.setVisibility(View.VISIBLE);
                        ticket.setAgent(thisUser.getUserName());
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("tickets").document(ticket.getId()).set(ticket);
                        btnSelfAssign.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey, getTheme())));
                        btnSelfAssign.setEnabled(false);
                        agent.setText(thisUser.getUserName());
                        pb.setVisibility(View.GONE);
                    });
                }
                if (!ticket.isOpen()) {
                    btnResolve.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey, getTheme())));
                    btnResolve.setEnabled(false);
                }
                else {
                    btnResolve.setOnClickListener(view -> {
                        pb.setVisibility(View.VISIBLE);
                        ticket.setOpen(false);
                        ticket.getNotes().add(adminUid + "%$/" + df.currentDateAndTimeString() + "%$/" + getString(R.string.agent) + " " + thisUser.getName() + "%$/ " +
                                getString(R.string.supportTicketWasClosedByAgent));
                        ticket.setAgentUid("Unassigned");
                        ticket.setAgent("Unassigned");
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("tickets").document(ticket.getId()).set(ticket);
                        btnResolve.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey, getTheme())));
                        btnResolve.setEnabled(false);
                        ticketOpen.setText(R.string.closed1);
                        recreate();
                    });
                }
                preview.setOnClickListener(view -> {
                    if (showing[0]) {
                        viewTicketDetails.setRotation(viewTicketDetails.getRotation() - 90);
                        ticketDetails.setVisibility(View.GONE);
                        showing[0] = false;
                    } else {
                        viewTicketDetails.setRotation(viewTicketDetails.getRotation() + 90);
                        ticketDetails.setVisibility(View.VISIBLE);
                        showing[0] = true;
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
    }
}