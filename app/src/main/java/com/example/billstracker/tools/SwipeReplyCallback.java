package com.example.billstracker.tools;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;

public class SwipeReplyCallback extends ItemTouchHelper.SimpleCallback {

    private final SwipeReplyListener listener;
    private boolean swipeConsumed = false;

    // Interface defined here so Support.java can implement the logic
    public interface SwipeReplyListener {
        void onSwipeReply(int position);
    }

    public SwipeReplyCallback(SwipeReplyListener listener) {
        super(0, ItemTouchHelper.RIGHT); // Swipe direction: Right
        this.listener = listener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false; // We don't need up/down dragging
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Do nothing here so the item doesn't get dismissed/deleted
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        // Reset the "lock" as soon as a new swipe starts
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            swipeConsumed = false;
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        View bubble = viewHolder.itemView.findViewById(R.id.message_container);
        View icon = viewHolder.itemView.findViewById(R.id.reply_icon);

        // Only apply manual translation if the user is actively swiping
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {
            float translationX = Math.min(dX, 200f);
            if (bubble != null) bubble.setTranslationX(translationX);
            if (icon != null) icon.setAlpha(Math.min(dX / 150f, 1f));

            if (dX >= 220f && !swipeConsumed) {
                swipeConsumed = true;
                if (listener != null) {
                    listener.onSwipeReply(viewHolder.getBindingAdapterPosition());
                }
            }
        }

        // super.onChildDraw is called with 0f to prevent the entire itemView from sliding
        super.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        View bubble = viewHolder.itemView.findViewById(R.id.message_container);
        View icon = viewHolder.itemView.findViewById(R.id.reply_icon);

        // Use animate() to ensure a smooth transition back to the center.
        // This effectively "cleans" the view for the next time it's used.
        if (bubble != null) {
            bubble.animate().translationX(0f).setDuration(150).start();
        }
        if (icon != null) {
            icon.animate().alpha(0f).setDuration(150).start();
        }
    }
}