package com.example.billstracker.recycler_adapters;

import static com.example.billstracker.activities.Login.thisUser;
import static com.example.billstracker.activities.Support.adminUid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.SupportTicket;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.Watcher;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Locale;

public class AdminSupportRecyclerAdapter extends RecyclerView.Adapter<AdminSupportRecyclerAdapter.ViewHolder> {

    private final ArrayList<SupportTicket> tickets;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener1;
    private ItemClickListener mClickListener2;
    final Context context;
    View view;

    public AdminSupportRecyclerAdapter(Context context1, ArrayList<SupportTicket> data) {
        this.mInflater = LayoutInflater.from(context1);
        this.tickets = data;
        this.context = context1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view1 = mInflater.inflate(R.layout.user_support_ticket, parent, false);
        this.view = view1;
        return new ViewHolder(view1);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        SupportTicket ticket = tickets.get(holder.getBindingAdapterPosition());

        view = holder.itemView;

        holder.ticketNumber.setText(String.format(Locale.getDefault(), "%s (%d)", ticket.getUserEmail(), ticket.getUnreadByAgent()));
        holder.ticketName.setText(ticket.getName());
        holder.ticketEmail.setText(ticket.getUserEmail());
        String note1 = context.getString(R.string.no_notes_have_been_added);
        if (ticket.getNotes() != null && !ticket.getNotes().isEmpty()) {
            note1 = ticket.getNotes();
        }
        holder.notes.setText(note1);
        String finalNote = note1;
        holder.notes.addTextChangedListener(new Watcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals(finalNote)) {
                    holder.submitNotes.setVisibility(View.VISIBLE);
                    holder.submitNotes.setOnClickListener(view -> {
                        ticket.setNotes(editable.toString());
                        FirebaseFirestore.getInstance().collection("tickets").document(ticket.getId()).set(ticket, SetOptions.merge());
                        notifyItemChanged(position);
                    });
                }
                else {
                    holder.submitNotes.setVisibility(View.GONE);
                }
            }
        });

        if (ticket.isOpen()) {
            holder.ticketOpen.setText(R.string.open1);
        }
        else {
            holder.ticketOpen.setText(R.string.closed1);
        }
        holder.agent.setText(ticket.getAgent());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50,50,50,50);
        holder.btnSendResponse.setOnClickListener(view -> {
            if (mClickListener1 != null) mClickListener1.onItemClick(position, tickets.get(position));
        });
        if (ticket.getAgent().equals(thisUser.getUserName())) {
            holder.btnSelfAssign.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.grey, context.getTheme())));
            holder.btnSelfAssign.setEnabled(false);
        }
        else {
            holder.btnSelfAssign.setOnClickListener(view -> {
                ticket.setAgent(thisUser.getUserName());
                FirebaseFirestore.getInstance().collection("tickets").document(ticket.getId()).set(ticket);
                holder.btnSelfAssign.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.grey, context.getTheme())));
                holder.btnSelfAssign.setEnabled(false);
                holder.agent.setText(thisUser.getUserName());
            });
        }
        if (!ticket.isOpen()) {
            holder.btnResolve.setText(context.getString(R.string.re_establish_ticket));
            holder.btnResolve.setOnClickListener(view -> {
                ticket.setOpen(true);
                String note = ticket.getNotes() + "\n\n" + adminUid + "\n" + DateFormat.createCurrentDateStringWithTime() + "\n" + context.getString(R.string.agent) + " " + thisUser.getName() + "\n" + context.getString(R.string.support_ticket_was_re_established_by_agent);
                ticket.setNotes(note);
                ticket.setAgentUid(thisUser.getid());
                ticket.setAgent(thisUser.getUserName());
                FirebaseFirestore.getInstance().collection("tickets").document(ticket.getId()).set(ticket);
                holder.ticketOpen.setText(context.getString(R.string.open1));
                if (mClickListener2 != null) mClickListener2.onItemClick(position, tickets.get(position));
            });
        }
        else {
            holder.btnResolve.setOnClickListener(view -> {
                ticket.setOpen(false);
                ticket.setUnreadByAgent(0);
                String note = ticket.getNotes() + "\n\n" + adminUid + "\n" + DateFormat.createCurrentDateStringWithTime() + "\n" + context.getString(R.string.agent) + " " + thisUser.getName() + "\n" + context.getString(R.string.supportTicketWasClosedByAgent);
                ticket.setNotes(note);
                ticket.setAgentUid("Unassigned");
                ticket.setAgent("Unassigned");
                FirebaseFirestore.getInstance().collection("tickets").document(ticket.getId()).set(ticket);
                holder.btnResolve.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.grey, context.getTheme())));
                holder.btnResolve.setEnabled(false);
                holder.ticketOpen.setText(R.string.closed1);
                if (mClickListener2 != null) mClickListener2.onItemClick(position, tickets.get(position));
            });
        }
        view.setOnClickListener(view1 -> {
            if (holder.ticketDetails.getVisibility() == View.VISIBLE) {
                holder.viewTicketDetails.setRotation(holder.viewTicketDetails.getRotation() - 90);
                holder.ticketDetails.setVisibility(View.GONE);
            } else {
                holder.viewTicketDetails.setRotation(holder.viewTicketDetails.getRotation() + 90);
                holder.ticketDetails.setVisibility(View.VISIBLE);
                ticket.setUnreadByUser(0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView viewTicketDetails;
        final LinearLayout ticketDetails;
        final TextView ticketNumber;
        final TextView ticketName;
        final TextView ticketEmail;
        final TextView ticketOpen;
        final EditText notes;
        final TextView agent;
        final Button btnSelfAssign;
        final Button btnSendResponse;
        final Button btnResolve;
        final Button submitNotes;

        ViewHolder(View itemView) {
            super(itemView);

            viewTicketDetails = itemView.findViewById(R.id.viewTicketDetails);
            ticketDetails = itemView.findViewById(R.id.ticketDetails);
            ticketNumber = itemView.findViewById(R.id.tvTicketNumber);
            ticketName = itemView.findViewById(R.id.tvCustomerName);
            ticketEmail = itemView.findViewById(R.id.tvCustomerEmail);
            ticketOpen = itemView.findViewById(R.id.ticketStatus);
            notes = itemView.findViewById(R.id.ticketNotes);
            agent = itemView.findViewById(R.id.tvTicketAssignedTo);
            btnSelfAssign = itemView.findViewById(R.id.btnSelfAssign);
            btnSendResponse = itemView.findViewById(R.id.btnSendResponse);
            btnResolve = itemView.findViewById(R.id.btnResolve);
            submitNotes = itemView.findViewById(R.id.submitNotes);
        }
    }

    public void setViewThreadListener(ItemClickListener itemClickListener) {
        this.mClickListener1 = itemClickListener;
    }

    public void setResolveTicketListener(ItemClickListener itemClickListener) {
        this.mClickListener2 = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(int ignoredPosition, SupportTicket supportTicket);
    }

}