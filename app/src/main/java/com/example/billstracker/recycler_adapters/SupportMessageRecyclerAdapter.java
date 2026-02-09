package com.example.billstracker.recycler_adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Message;
import com.example.billstracker.tools.Repository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class SupportMessageRecyclerAdapter extends RecyclerView.Adapter<SupportMessageRecyclerAdapter.ViewHolder> {

    private static final int SENT = 0;
    final Context context;
    private final ArrayList<Message> messages;
    private final LayoutInflater mInflater;
    private OnReplyListener replyListener;
    Message message;
    boolean hasChanges = false;

    public interface OnMessageLongClickListener {
        void onLongClick(Message message, int position);
    }

    private OnMessageLongClickListener longClickListener;

    public void setOnMessageLongClickListener(OnMessageLongClickListener listener) {
        this.longClickListener = listener;
    }

    public SupportMessageRecyclerAdapter(Context context1, ArrayList<Message> data) {
        this.mInflater = LayoutInflater.from(context1);
        this.messages = data;
        context = context1;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getAuthorId().equals(Repository.getInstance(context).getUser().getId())) {
            return SENT;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == SENT) {
            view = mInflater.inflate(R.layout.sent_message, parent, false);
        } else {
            view = mInflater.inflate(R.layout.received_message, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        message = messages.get(holder.getBindingAdapterPosition());

        int nightMode = context.getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            holder.chatMessage.setTextColor(context.getResources().getColor(R.color.white, context.getTheme()));
            holder.chatTime.setTextColor(context.getResources().getColor(R.color.white, context.getTheme()));
        }

        holder.chatMessage.setText(message.getMessage());
        holder.chatTime.setText(message.getDateTime());

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (position == getItemCount() - 1) {
            layoutParams.bottomMargin = 100;
        } else {
            layoutParams.bottomMargin = 20;
        }
        holder.itemView.setLayoutParams(layoutParams);

        View bubble = holder.itemView.findViewById(R.id.message_container);
        View icon = holder.itemView.findViewById(R.id.reply_icon);
        if (bubble != null) {
            bubble.animate().cancel();
            bubble.setTranslationX(0f);
        }
        if (icon != null) {
            icon.animate().cancel();
            icon.setAlpha(0f);
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    longClickListener.onLongClick(messages.get(currentPos), currentPos);
                }
            }
            return true;
        });

        if (Repository.getInstance(context).getUser() != null) {
            String currentUserId = Repository.getInstance(context).getUser().getId();

            // Check if the message is from the OTHER person and is currently unread
            if (!message.getAuthorId().equals(currentUserId) && !message.isRead()) {
                message.setRead(true);
                hasChanges = true;
            }

            // Only sync when we hit the last item and we know changes exist
            if (position == getItemCount() - 1 && hasChanges) {
                syncReadStatusToFirestore(message.getAuthorId());
                hasChanges = false; // Reset flag after triggering sync
            }
        }

        if (message.getRepliedToText() != null && !message.getRepliedToText().isEmpty()) {
            holder.quotedMessageLayout.setVisibility(View.VISIBLE);
            holder.quotedText.setText(message.getRepliedToText());
            holder.quotedName.setText(message.getRepliedToName());
        } else {
            holder.quotedMessageLayout.setVisibility(View.GONE);
        }

        if (getItemViewType(position) == SENT) {
            if (message.isRead()) {
                holder.readStatus.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(context.getResources(), R.color.accent_line_sent, context.getTheme())));
            }
        }
    }

    private void syncReadStatusToFirestore(String ticketId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        boolean isAdmin = Repository.getInstance(context).getUser().isAdmin();

        // Determine which field to reset based on who is viewing the chat
        String fieldToReset = isAdmin ? "unreadByAgent" : "unreadByUser";

        db.collection("tickets").document(ticketId)
                .update(
                        "messages", this.messages,  // Sync the message read/unread flags
                        fieldToReset, 0              // Reset the counter to 0
                )
                .addOnSuccessListener(aVoid -> Log.d("Sync", "Read status and counter updated"))
                .addOnFailureListener(e -> Log.e("Sync", "Sync failed", e));
    }

    @Override
    public int getItemCount() {
        if (messages != null) {
            return messages.size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView chatMessage;
        final TextView chatTime;

        final TextView quotedName;
        final TextView quotedText;
        final View quotedMessageLayout;
        final ImageView readStatus;


        ViewHolder(View itemView) {
            super(itemView);

            chatMessage = itemView.findViewById(R.id.chatMessage);
            chatTime = itemView.findViewById(R.id.chatTime);
            quotedName = itemView.findViewById(R.id.quotedName);
            quotedText = itemView.findViewById(R.id.quotedText);
            quotedMessageLayout = itemView.findViewById(R.id.quotedMessageLayout);
            readStatus = itemView.findViewById(R.id.readStatus);
        }
    }

    public interface OnReplyListener {
        void onReply(Message message);
    }

    public void setOnReplyListener(OnReplyListener listener) {
        this.replyListener = listener;
    }

    public void handleSwipeReply(int position) {
        if (replyListener != null && position != RecyclerView.NO_POSITION) {
            replyListener.onReply(messages.get(position));

            new android.os.Handler().postDelayed(() -> {
                notifyItemChanged(position);
            }, 200);
        }
    }
}