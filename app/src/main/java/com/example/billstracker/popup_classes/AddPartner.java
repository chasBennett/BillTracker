package com.example.billstracker.popup_classes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.tools.Repository;
import com.example.billstracker.tools.Tools;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Objects;

public class AddPartner extends DialogFragment {

    public static View.OnClickListener listener1;
    View addPartner;
    ImageView cancel;
    TextView error;
    TextInputEditText partnerEmail;
    Button sendRequest;

    public void setCloseListener(View.OnClickListener listener1) {
        AddPartner.listener1 = listener1;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        addPartner = inflater.inflate(R.layout.add_partner, null);
        cancel = addPartner.findViewById(R.id.closeAddPartner);
        partnerEmail = addPartner.findViewById(R.id.partnerEmail);
        sendRequest = addPartner.findViewById(R.id.sendRequest);
        error = addPartner.findViewById(R.id.partnerError);

        Tools.setupUI(requireActivity(), addPartner);

        error.setVisibility(View.GONE);

        sendRequest.setOnClickListener(v -> {
            error.setVisibility(View.GONE);
            if (partnerEmail.getText() != null && partnerEmail.getText().length() == 0) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Query queryByEmail = db.collection("users").whereEqualTo("userName", partnerEmail.getText().toString());
                queryByEmail.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                error.setVisibility(View.GONE);
                                User requested = document.toObject(User.class);
                                ArrayList<Partner> partners = new ArrayList<>();
                                if (requested.getPartners() != null) {
                                    partners.addAll(requested.getPartners());
                                }
                                boolean found = false;
                                for (Partner par : partners) {
                                    if (par.getPartnerUid().equals(Repository.getInstance().getUid(requireContext()))) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    partners.add(new Partner(Repository.getInstance().getUid(requireContext()), false, Repository.getInstance().getUser(requireActivity()).getName()));
                                    requested.setPartners(partners);
                                }
                                if (Repository.getInstance().getUser(requireActivity()).getPartners() == null) {
                                    Repository.getInstance().getUser(requireActivity()).setPartners(new ArrayList<>());
                                }

                                db.collection("users").document(document.getId()).set(requested, SetOptions.merge());
                                found = false;
                                for (Partner pa : Repository.getInstance().getUser(requireActivity()).getPartners()) {
                                    if (pa.getPartnerUid().equals(requested.getId())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    Repository.getInstance().getUser(requireActivity()).getPartners().add(new Partner(requested.getId(), true, requested.getName()));
                                    db.collection("users").document(Repository.getInstance().getUser(requireActivity()).getId()).set(Repository.getInstance().getUser(requireActivity()), SetOptions.merge());
                                }
                                Notify.createPopup(requireActivity(), getString(R.string.partner_has_been_requested_successfully), null);
                                listener1.onClick(cancel);
                                cancel.performClick();
                                break;
                            } else {
                                Notify.createDialogPopup(requireDialog(), getString(R.string.anErrorHasOccurred), null);
                            }
                        }
                    } else {
                        error.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                Notify.createDialogPopup(requireDialog(), getString(R.string.email_cannot_be_blank), null);
            }
        });

        cancel.setOnClickListener(v -> {
            listener1.onClick(v);
            Objects.requireNonNull(AddPartner.this.getDialog()).cancel();
        });

        builder.setView(addPartner);
        return builder.create();
    }
}
