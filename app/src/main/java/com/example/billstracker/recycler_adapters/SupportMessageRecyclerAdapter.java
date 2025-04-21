package com.example.billstracker.recycler_adapters;

import static com.example.billstracker.activities.Login.thisUser;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Message;

import java.util.ArrayList;
import java.util.Locale;

public class SupportMessageRecyclerAdapter extends RecyclerView.Adapter<SupportMessageRecyclerAdapter.ViewHolder> {

    private final ArrayList <Message> messages;
    private final LayoutInflater mInflater;
    private static final int SENT = 0;
    Message message;
    final Context context;

    public SupportMessageRecyclerAdapter(Context context1, ArrayList <Message> data) {
        this.mInflater = LayoutInflater.from(context1);
        this.messages = data;
        context = context1;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getAuthorId().equals(thisUser.getid())) {
            return SENT;
        }
        else {
            return 1;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == SENT) {
            view = mInflater.inflate(R.layout.sent_message, parent, false);
        }
        else {
            view = mInflater.inflate(R.layout.received_message, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        message = messages.get(holder.getBindingAdapterPosition());
        String type = "";
        if (message.isAgent()) {
            type = context.getString(R.string.agent);
        }
        int nightMode = context.getApplicationContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            holder.chatName.setTextColor(context.getResources().getColor(R.color.white, context.getTheme()));
            holder.chatMessage.setTextColor(context.getResources().getColor(R.color.white, context.getTheme()));
            holder.chatTime.setTextColor(context.getResources().getColor(R.color.white, context.getTheme()));
        }

        holder.chatName.setText(String.format(Locale.getDefault(), "%s %s", type, message.getName()));
        holder.chatMessage.setText(message.getMessage());
        holder.chatTime.setText(message.getDateTime());

    }

    @Override
    public int getItemCount() {
        if (messages != null) {
            return messages.size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView chatName;
        final TextView chatMessage;
        final TextView chatTime;


        ViewHolder(View itemView) {
            super(itemView);

            chatName = itemView.findViewById(R.id.chatName);
            chatMessage =itemView.findViewById(R.id.chatMessage);
            chatTime = itemView.findViewById(R.id.chatTime);
        }
    }
}