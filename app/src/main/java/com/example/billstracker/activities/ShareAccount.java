package com.example.billstracker.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.AddPartner;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.Repo;
import com.example.billstracker.tools.Tools;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;

public class ShareAccount extends AppCompatActivity {

    ImageView backButton;
    TextView sharingWith;
    LinearLayout partnerList;
    Button addPartner;
    Partner delete;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_account);

        backButton = findViewById(R.id.backBtn);
        sharingWith = findViewById(R.id.currentlySharingWith);
        partnerList = findViewById(R.id.partnerList);
        addPartner = findViewById(R.id.btnAddPartner);

        context = getApplicationContext();

        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Tools.setupUI(ShareAccount.this, findViewById(android.R.id.content));

        addPartner.setOnClickListener(v -> {
            AddPartner ap = new AddPartner();
            ap.show(getSupportFragmentManager(), "AddPartner");
            ap.setCloseListener(v1 -> listPartners());
        });

        listPartners();
    }
    public void listPartners() {

        if (delete != null) {
            Repo.getInstance().getUser(ShareAccount.this).getPartners().remove(delete);
            Repo.getInstance().save(ShareAccount.this);
            delete = null;
            listPartners();
        }

        partnerList.removeAllViews();
        partnerList.invalidate();
        if (Repo.getInstance().getUser(ShareAccount.this) == null) {
            Repo.getInstance().loadLocalData(ShareAccount.this);
        }
        if (Repo.getInstance().getUser(ShareAccount.this).getPartners() == null || Repo.getInstance().getUser(ShareAccount.this).getPartners().isEmpty()) {
            sharingWith.setText(getString(R.string.you_aren_t_currently_sharing_data_with_anyone));
        }
        else {
            sharingWith.setText(getString(R.string.you_re_currently_sharing_your_data_with_the_following_users));
            for (Partner partner : Repo.getInstance().getUser(ShareAccount.this).getPartners()) {
                checkPartner(partner);
            }
        }
    }
    public void checkPartner (Partner partner) {
        View userCard = View.inflate(ShareAccount.this, R.layout.user_card, null);
        TextView userName = userCard.findViewById(R.id.sharedUserName);
        TextView shareStatus = userCard.findViewById(R.id.shareStatus);
        Button revokeAccess = userCard.findViewById(R.id.btnRevokeAccess);
        Button cancelRequest = userCard.findViewById(R.id.btnCancelRequest);
        Button removePartner = userCard.findViewById(R.id.btnRemovePartner);
        Button approve = userCard.findViewById(R.id.btnApprove);
        removePartner.setVisibility(View.VISIBLE);
        userName.setText(partner.getPartnerName());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (partner.getPartnerUid() != null) {
            db.collection("users").document(partner.getPartnerUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    User partnerUser = task.getResult().toObject(User.class);
                    if (partnerUser != null && partnerUser.getPartners() != null && !partnerUser.getPartners().isEmpty()) {
                        for (Partner part: partnerUser.getPartners()) {
                            if (part.getPartnerUid().equals(Repo.getInstance().getUid())) {
                                if (part.getSharingAuthorized()) {
                                    if (partner.getSharingAuthorized()) {
                                        shareStatus.setText(getString(R.string.active));
                                        revokeAccess.setVisibility(View.VISIBLE);
                                        cancelRequest.setVisibility(View.GONE);
                                        approve.setVisibility(View.GONE);
                                    } else {
                                        shareStatus.setText(getString(R.string.requested_by_user));
                                        revokeAccess.setVisibility(View.GONE);
                                        cancelRequest.setVisibility(View.GONE);
                                        approve.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    if (partner.getSharingAuthorized()) {
                                        shareStatus.setText(getString(R.string.awaiting_partner_approval));
                                        revokeAccess.setVisibility(View.GONE);
                                        cancelRequest.setVisibility(View.VISIBLE);
                                        approve.setVisibility(View.GONE);
                                    } else {
                                        delete = partner;
                                        listPartners();
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            });
        }
        revokeAccess.setOnClickListener(v -> {
            partner.setSharingAuthorized(false);
            Tools.removePartnerData(partner.getPartnerUid());
            Repo.getInstance().save(ShareAccount.this);
            listPartners();
        });
        cancelRequest.setOnClickListener(v -> {
            delete = partner;
            removePartner(partner.getPartnerUid());
            Tools.removePartnerData(partner.getPartnerUid());
            Repo.getInstance().save(ShareAccount.this);
            listPartners();
        });
        removePartner.setOnClickListener(v -> {
            delete = partner;
            removePartner(partner.getPartnerUid());
            Tools.removePartnerData(partner.getPartnerUid());
            Repo.getInstance().save(ShareAccount.this);
            listPartners();
        });
        approve.setOnClickListener(v -> {
            for (Partner part: Repo.getInstance().getUser(ShareAccount.this).getPartners()) {
                if (part.getPartnerUid().equals(partner.getPartnerUid())) {
                    part.setSharingAuthorized(true);
                    break;
                }
            }
            partner.setSharingAuthorized(true);
            FirebaseTools.getBills(context, partner.getPartnerUid(), isSuccessful -> {
                if (isSuccessful) {
                    FirebaseTools.getPayments(partner.getPartnerUid(), isSuccessful1 -> {
                        if (isSuccessful1) {
                            FirebaseTools.getExpenses(partner.getPartnerUid(), isSuccessful11 -> {
                                if (!isSuccessful11) {
                                    Notify.createPopup(ShareAccount.this, getString(R.string.anErrorHasOccurred), null);
                                }
                            });
                        }
                        else {
                            Notify.createPopup(ShareAccount.this, getString(R.string.anErrorHasOccurred), null);
                        }
                    });
                }
                else {
                    Notify.createPopup(ShareAccount.this, getString(R.string.anErrorHasOccurred), null);
                }
            });
            Repo.getInstance().save(ShareAccount.this);
            listPartners();
        });
        partnerList.addView(userCard);
    }
    public void removePartner (String partnerId) {
            ArrayList <Partner> remove = new ArrayList<>();
            FirebaseFirestore.getInstance().collection("users").document(partnerId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    User partner = task.getResult().toObject(User.class);
                    if (partner != null && partner.getPartners() != null && !partner.getPartners().isEmpty()) {
                        for (Partner part: partner.getPartners()) {
                            if (part.getPartnerUid().equals(Repo.getInstance().getUid())) {
                                remove.add(part);
                            }
                        }
                        partner.getPartners().removeAll(remove);
                        FirebaseFirestore.getInstance().collection("users").document(partnerId).set(partner, SetOptions.merge());
                    }
                }
            });
    }
}