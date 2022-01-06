package com.example.x_tour;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.imageview.ShapeableImageView;

public class ProfileFragment extends Fragment {

    private Button btnEditProfile, btnLogout, btnYes, btnNo;
    private ShapeableImageView profilePic;
    private TextView tvUsername;
    private DatabaseHelper UserDBHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUsername = v.findViewById(R.id.tvUsername);
        btnEditProfile = v.findViewById(R.id.btnSave);
        btnLogout = v.findViewById(R.id.btnDiscard);
        profilePic = v.findViewById(R.id.profilePic);
        UserDBHelper = new DatabaseHelper(getActivity());

        // fetch profile picture from database
        String userID = String.valueOf(getActivity().getIntent().getExtras().getInt("userID"));
        profilePic.setImageBitmap(UserDBHelper.getProfilePic(userID));

        // fetch username from database
        String username = UserDBHelper.getUsername(userID);
        tvUsername.setText(username);

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new EditProfileFragment())
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.confirm_logout, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        true);

                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                // darken background when popup window appears
                View container = popupWindow.getContentView().getRootView();
                Context context = popupWindow.getContentView().getContext();
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
                p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND; // turn on flag for dimming background
                p.dimAmount = 0.3f;
                wm.updateViewLayout(container, p);

                btnYes = popupView.findViewById(R.id.btnYes);
                btnNo = popupView.findViewById(R.id.btnNo);

                btnNo.setOnClickListener(v -> {
                    popupWindow.dismiss();
                });

                btnYes.setOnClickListener(v -> {
                    popupWindow.dismiss();
                    Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                });
            }
        });

        return v;
    }
}
