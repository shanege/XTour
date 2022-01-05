package com.example.x_tour;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

public class EditProfileFragment extends Fragment {

    private ShapeableImageView profilePic;
    private FloatingActionButton btnChangePic;
    private ImageView btnCamera, btnGallery;
    private TextView tvUsername;
    private Button btnSave;
    private DatabaseHelper UserDBHelper;
    private Uri cam_uri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_editprofile, container, false);

        profilePic = v.findViewById(R.id.profilePic);
        tvUsername = v.findViewById(R.id.tvUsername);
        btnChangePic = v.findViewById(R.id.btnChangePic);
        btnSave = v.findViewById(R.id.btnSave);
        UserDBHelper = new DatabaseHelper(getActivity());

        // fetch profile picture from database
        String userID = String.valueOf(getActivity().getIntent().getExtras().getInt("userID"));
        profilePic.setImageBitmap(UserDBHelper.getProfilePic(userID));

        // fetch username from database
        String username = UserDBHelper.getUsername(userID);
        tvUsername.setText(username);

        btnChangePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.change_profilepic, null);
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

                btnCamera = popupView.findViewById(R.id.btnCamera);
                btnGallery = popupView.findViewById(R.id.btnGallery);

                btnCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
                        pickCamera();
                        popupWindow.dismiss();
                    }
                });

                btnGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        startGallery.launch("image/*");
                        popupWindow.dismiss();
                    }
                });

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BitmapDrawable drawable = (BitmapDrawable) profilePic.getDrawable();
                        Bitmap profilePicBitmap = drawable.getBitmap();
                        Boolean picUpdated = UserDBHelper.updateProfilePic(profilePicBitmap, userID);
                        if (picUpdated)
                            Toast.makeText(getActivity().getApplicationContext(), "Profile pic updated successfully", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity().getApplicationContext(), "Profile pic update failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return v;
    }

    public void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cam_uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cam_uri);

        // open camera
        startCamera.launch(cameraIntent);
    }

    ActivityResultLauncher<Intent> startCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        profilePic.setImageURI(cam_uri);
                    }
                }
            });

    ActivityResultLauncher<String> startGallery = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    profilePic.setImageURI(uri);
                }
            });
}
