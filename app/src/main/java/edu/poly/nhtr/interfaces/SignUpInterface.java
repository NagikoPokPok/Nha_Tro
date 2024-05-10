package edu.poly.nhtr.interfaces;

import android.content.Context;
import android.content.Intent;

public interface SignUpInterface {

    void showErrorMessage(String message);
    void showSuccessMessage(String name, String email, String password);
    void showLoading();
    void hideLoading();

    void setNameErrorMessage(String message);
    void setEmailErrorMessage(String message);
    void setPasswordErrorMessage(String message);
    void setConfirmPasswordErrorMessage(String message);

    void setPasswordHelperText(String helperText);
    void setPasswordError(String error);

    void setConfirmPasswordHelperText(String helperText);
    void setConfirmPasswordError(String error);
    void setNameErrorEnabled(Boolean isEmpty);
    void setEmailErrorEnabled(Boolean isEmpty);

    void setConfirmPasswordText(String message);
    void firebaseAuth(String idToken);
    void  notifySignInSuccess();
    Context getContext();

    String getStringFromResources(int defaultWebClientId);
    void showToast(String message);
    void startActivityForResult(Intent intent, int requestCode);
}
