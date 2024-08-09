package edu.poly.nhtr.interfaces;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface SettingsInterface {

    void setUserName(String userName);

    ImageView getProfileImageView();

    void hideAvatar();

    void loadUserDetails(String name, String phoneNumber, Bitmap profileImage);
    void back();

    void showToast(String message);

    void loading(Boolean isLoading);
    void switchModeTheme();

    void setNightMode(boolean nightMode);
    void setSwitchClickListener(android.view.View.OnClickListener listener);
}
