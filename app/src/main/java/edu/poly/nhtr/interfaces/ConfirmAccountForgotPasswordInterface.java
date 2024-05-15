package edu.poly.nhtr.interfaces;

public interface ConfirmAccountForgotPasswordInterface {
    void showToast(String message);
    void hideLoading();
    void showLoading();
    void verifySuccess();
    void setBeforeTextColor();
    void setAfterTextColor();
    void setText(String message);

    void sendEmail(int code);

    void setTextEnabled(Boolean enabled);
    void clearOTP();
}
