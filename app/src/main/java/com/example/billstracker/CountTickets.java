package com.example.billstracker;

import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyLog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CountTickets {

    public void countTickets (TextView ticketCounter) {

        final int[] counter = new int[1];

        if (thisUser.getAdmin()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("tickets").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(VolleyLog.TAG, document.getId() + " => " + document.getData());
                        SupportTicket ticket = document.toObject(SupportTicket.class);
                        if (ticket.getAgentUid() != null) {
                            if (ticket.getAgentUid().equalsIgnoreCase(uid) || ticket.getAgentUid().equals("Unassigned")) {
                                counter[0] = counter[0] + ticket.getUnreadByAgent();
                            }
                        }
                    }
                    if (counter[0] > 0) {
                        ticketCounter.setVisibility(View.VISIBLE);
                        ticketCounter.setText(String.valueOf(counter[0]));
                    }
                    else {
                        ticketCounter.setVisibility(View.GONE);
                    }
                }
            });
        }
        else {
            final boolean[] found = {false};
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("tickets").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(VolleyLog.TAG, document.getId() + " => " + document.getData());
                        SupportTicket ticket = document.toObject(SupportTicket.class);
                        if (ticket.getAgentUid() != null) {
                            if (ticket.getUserUid().equals(uid)) {
                                if (ticket.getUnreadByUser() > 0) {
                                    ticketCounter.setText(String.valueOf(ticket.getUnreadByUser()));
                                    ticketCounter.setVisibility(View.VISIBLE);
                                }
                                found[0] = true;
                                break;
                            }
                        }
                    }
                    if (!found[0]) {
                        ticketCounter.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
