package com.example.billstracker.tools;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.util.Log;

import com.example.billstracker.activities.MainActivity2;
import com.example.billstracker.custom_objects.SupportTicket;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CountTickets {

    public static void countTickets (Activity view) {

        Repo.getInstance().loadLocalData(view);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (Repo.getInstance().getUser(view) != null) {
            if (Repo.getInstance().getUser(view).isAdmin()) {
                db.collection("tickets").get().addOnCompleteListener(task -> {
                    MainActivity2.tickets = 0;
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => data retrieved");
                            SupportTicket ticket = document.toObject(SupportTicket.class);
                            if (ticket.getAgentUid().equalsIgnoreCase(Repo.getInstance().getUid()) || ticket.getAgentUid().equals("Unassigned")) {
                                if (ticket.getUnreadByAgent() > 0) {
                                    MainActivity2.tickets = MainActivity2.tickets + ticket.getUnreadByAgent();
                                }
                            }
                        }
                    }
                });
            } else {
                db.collection("tickets").document(Repo.getInstance().getUid()).get().addOnCompleteListener(task -> {
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
