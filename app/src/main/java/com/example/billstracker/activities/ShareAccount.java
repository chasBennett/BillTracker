package com.example.billstracker.activities;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.AddPartner;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.Tools;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShareAccount extends BaseActivity {

    private TextView sharingWith;
    private LinearLayout partnerList;
    private Context context;

    @Override
    protected void onDataReady() {
        setContentView(R.layout.activity_share_account);
        context = this;

        ImageView backButton = findViewById(R.id.backBtn);
        sharingWith = findViewById(R.id.currentlySharingWith);
        partnerList = findViewById(R.id.partnerList);
        Button addPartner = findViewById(R.id.btnAddPartner);

        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        Tools.setupUI(this, findViewById(android.R.id.content));

        addPartner.setOnClickListener(v -> {
            AddPartner ap = new AddPartner();
            ap.show(getSupportFragmentManager(), "AddPartner");
            ap.setCloseListener(v1 -> listPartners());
        });

        listPartners();
    }

    public void listPartners() {
        partnerList.removeAllViews();
        User currentUser = repo.getUser(ShareAccount.this);

        if (currentUser == null || currentUser.getPartners() == null || currentUser.getPartners().isEmpty()) {
            sharingWith.setText(getString(R.string.you_aren_t_currently_sharing_data_with_anyone));
            return;
        }

        sharingWith.setText(getString(R.string.you_re_currently_sharing_your_data_with_the_following_users));
        for (Partner partner : currentUser.getPartners()) {
            addPartnerView(partner);
        }
    }

    private void addPartnerView(Partner partner) {
        View userCard = View.inflate(this, R.layout.user_card, null);
        TextView userName = userCard.findViewById(R.id.sharedUserName);
        TextView shareStatus = userCard.findViewById(R.id.shareStatus);
        Button revokeAccess = userCard.findViewById(R.id.btnRevokeAccess);
        Button cancelRequest = userCard.findViewById(R.id.btnCancelRequest);
        Button removePartner = userCard.findViewById(R.id.btnRemovePartner);
        Button approve = userCard.findViewById(R.id.btnApprove);

        userName.setText(partner.getPartnerName());
        removePartner.setVisibility(View.VISIBLE);

        // Fetch partner status from cloud to check if they authorized YOU
        FirebaseFirestore.getInstance().collection("users").document(partner.getPartnerUid())
                .get().addOnSuccessListener(documentSnapshot -> {
                    User partnerUser = documentSnapshot.toObject(User.class);
                    updateUIStatus(partner, partnerUser, shareStatus, revokeAccess, cancelRequest, approve);
                });

        // ACTION: Revoke Access (You stop sharing with them)
        revokeAccess.setOnClickListener(v -> updatePartnerAuthorization(partner, false));

        // ACTION: Approve (You accept their request to see your data)
        approve.setOnClickListener(v -> {
            updatePartnerAuthorization(partner, true);
            // Sync their data to your local device
            fetchPartnerData(partner.getPartnerUid());
        });

        // ACTION: Remove / Cancel (Delete relationship entirely)
        View.OnClickListener deleteAction = v -> removePartnerCompletely(partner);
        removePartner.setOnClickListener(deleteAction);
        cancelRequest.setOnClickListener(deleteAction);

        partnerList.addView(userCard);
    }

    private void updateUIStatus(Partner localPartner, User remoteUser, TextView status, Button revoke, Button cancel, Button approve) {
        // Find how the remote user sees YOU
        Partner remoteMe = null;
        if (remoteUser != null && remoteUser.getPartners() != null) {
            for (Partner p : remoteUser.getPartners()) {
                if (p.getPartnerUid().equals(repo.getUid(ShareAccount.this))) {
                    remoteMe = p;
                    break;
                }
            }
        }

        if (remoteMe == null) {
            status.setText("Pending..."); // Or auto-remove if logic dictates
            return;
        }

        boolean iAuthorized = localPartner.getSharingAuthorized();
        boolean theyAuthorized = remoteMe.getSharingAuthorized();

        if (iAuthorized && theyAuthorized) {
            status.setText(getString(R.string.active));
            revoke.setVisibility(View.VISIBLE);
        } else if (!iAuthorized && theyAuthorized) {
            status.setText(getString(R.string.requested_by_user));
            approve.setVisibility(View.VISIBLE);
        } else if (iAuthorized && !theyAuthorized) {
            status.setText(getString(R.string.awaiting_partner_approval));
            cancel.setVisibility(View.VISIBLE);
        }
    }

    private void updatePartnerAuthorization(Partner partner, boolean authorized) {
        partner.setSharingAuthorized(authorized);
        // Use the Builder to flag the User as needing sync
        User.Builder user = repo.editUser(ShareAccount.this);
        if (user != null) {
            user.setNeedsSync(true).save((success, message) -> {
                if (success) listPartners();
            });
        }
        else {
            Notify.createPopup(ShareAccount.this, getString(R.string.anErrorHasOccurred), null);
        }
    }

    private void removePartnerCompletely(Partner partner) {
        repo.getUser(ShareAccount.this).getPartners().remove(partner);
        Tools.removePartnerData(partner.getPartnerUid());

        // Use the Builder to save the user profile without the partner
        User.Builder user = repo.editUser(ShareAccount.this);
        if (user != null) {
            user.save((success, message) -> {
                if (success) {
                    repo.removeFromRemotePartner(partner.getPartnerUid());
                    listPartners();
                }
            });
        }
        else {
            Notify.createPopup(ShareAccount.this, getString(R.string.anErrorHasOccurred), null);
        }
    }

    private void fetchPartnerData(String partnerUid) {
        // Use FirebaseTools as intended, but consider moving these to Repository later
        FirebaseTools.getBills(context, partnerUid, success -> {
            FirebaseTools.getPayments(partnerUid, success2 -> {
                FirebaseTools.getExpenses(partnerUid, success3 -> {
                    if (!success3) Notify.createPopup(this, "Error downloading partner data", null);
                });
            });
        });
    }
}