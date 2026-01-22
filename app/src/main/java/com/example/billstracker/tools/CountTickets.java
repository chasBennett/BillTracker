package com.example.billstracker.tools;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.util.Log;

import com.example.billstracker.activities.MainActivity2;
import com.example.billstracker.custom_objects.SupportTicket;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CountTickets {

    public static void countTickets(Activity view) {

        Repository.getInstance().loadLocalData(view, null);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (Repository.getInstance().getUser(view) != null) {
            if (Repository.getInstance().getUser(view).isAdmin()) {
                db.collection("tickets").get().addOnCompleteListener(task -> {
                    MainActivity2.tickets = 0;
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => data retrieved");
                            SupportTicket ticket = document.toObject(SupportTicket.class);
                            if (ticket.getAgentUid().equalsIgnoreCase(Repository.getInstance().getUid(view)) || ticket.getAgentUid().equals("Unassigned")) {
                                if (ticket.getUnreadByAgent() > 0) {
                                    MainActivity2.tickets = MainActivity2.tickets + ticket.getUnreadByAgent();
                                }
                            }
                        }
                    }
                });
            } else {
                db.collection("tickets").document(Repository.getInstance().getUid(view)).get().addOnCompleteListener(task -> {
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
