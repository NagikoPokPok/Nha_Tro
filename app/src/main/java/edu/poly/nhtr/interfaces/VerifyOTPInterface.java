package edu.poly.nhtr.interfaces;

import android.graphics.Color;

public interface VerifyOTPInterface {

    void showToast(String message);
    void hideLoading();
    void showLoading();
    void verifySuccess();
    void setBeforeTextColor();
    void setAfterTextColor();
    void setText(String message);

    void sendEmail(int code);

    void setTextEnabled(boolean enabled);
    void clearOTP();

}
