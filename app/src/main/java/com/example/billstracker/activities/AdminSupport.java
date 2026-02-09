package com.example.billstracker.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.SupportTicket;
import com.example.billstracker.recycler_adapters.AdminSupportRecyclerAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class AdminSupport extends BaseActivity {

    RelativeLayout adminTicketListParent;
    ImageView backAdminSupport;
    RecyclerView adminTicketList;
    LinearLayout noTicketsFound;
    ArrayList<SupportTicket> userTickets = new ArrayList<>();


    @Override
    protected void onDataReady() {
        setContentView(R.layout.activity_admin_support);

        adminTicketList = findViewById(R.id.adminTicketList);
        backAdminSupport = findViewById(R.id.backAdminSupport);
        noTicketsFound = findViewById(R.id.noTicketsFound);
        adminTicketListParent = findViewById(R.id.adminTicketListParent);

        backAdminSupport.setOnClickListener(view -> {
            getOnBackPressedDispatcher().onBackPressed();
        });


        loadTicketList();

    }

    public void loadTicketList() {
        userTickets.clear();

        db.collection("tickets").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userTickets = (ArrayList<SupportTicket>) task.getResult().toObjects(SupportTicket.class);
                ArrayList<SupportTicket> remove = new ArrayList<>();
                for (SupportTicket delete : userTickets) {
                    if (delete.getAgentUid() != null) {
                        if (!delete.getAgentUid().trim().equals(repo.getUid()) && !delete.getAgentUid().equals("Unassigned") || !delete.isOpen()) {
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
                    noTicketsFound.setVisibility(View.GONE);
                    generateSupportList();
                } else {
                    noTicketsFound.setVisibility(View.VISIBLE);
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
        for (SupportTicket count : userTickets) {
            if (count.getUnreadByAgent() > 0) {
                ++counter;
            }
        }

        title.setText(String.format(Locale.US, "Support Tickets (%d)", counter));
        AdminSupportRecyclerAdapter asra = new AdminSupportRecyclerAdapter(AdminSupport.this, userTickets);
        adminTicketList.setAdapter(asra);
        adminTicketList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adminTicketList.setNestedScrollingEnabled(false);
        asra.setViewThreadListener((position, supportTicket) -> {
            Intent i = new Intent(AdminSupport.this, Support.class);
            i.putExtra("ticket", supportTicket.getUserUid());
            startActivity(i);
        });
        asra.setResolveTicketListener((ignoredPosition, supportTicket) -> loadTicketList());
    }
}
