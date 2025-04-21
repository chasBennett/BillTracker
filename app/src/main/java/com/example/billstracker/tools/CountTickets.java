package com.example.billstracker.tools;

import static com.example.billstracker.activities.Login.thisUser;
import static com.example.billstracker.activities.Login.uid;

import android.app.Activity;
import android.util.Log;

import com.android.volley.VolleyLog;
import com.example.billstracker.activities.MainActivity2;
import com.example.billstracker.custom_objects.SupportTicket;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CountTickets {

    public static void countTickets (Activity view) {

        UserData.load(view);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (thisUser != null) {
            if (thisUser.getAdmin()) {
                db.collection("tickets").get().addOnCompleteListener(task -> {
                    MainActivity2.tickets = 0;
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(VolleyLog.TAG, document.getId() + " => data retrieved");
                            SupportTicket ticket = document.toObject(SupportTicket.class);
                            if (ticket.getAgentUid().equalsIgnoreCase(uid) || ticket.getAgentUid().equals("Unassigned")) {
                                if (ticket.getUnreadByAgent() > 0) {
                                    MainActivity2.tickets = MainActivity2.tickets + ticket.getUnreadByAgent();
                                }
                            }
                        }
                    }
                });
            } else {
                db.collection("tickets").document(uid).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SupportTicket ticket = task.getResult().toObject(SupportTicket.class);
                        if (ticket != null) {
                            MainActivity2.tickets = Math.max(ticket.getUnreadByUser(), 0);
                        } else {
                            MainActivity2.tickets = 0;
                        }
                    } else {
                        MainActivity2.tickets = 0;
                    }
                });
            }
        }
    }
}
