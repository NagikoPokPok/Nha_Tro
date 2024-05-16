package edu.poly.nhtr.interfaces;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

public interface SignInInterface {
    void showToast(String message);
    void loading(Boolean isLoading);
    void entryMain();
    void firebaseAuth(String idToken);
    void  notifySignInSuccess();

    String getStringFromResources(int defaultWebClientId);
    Context getContext();
    void startActivityForResult(Intent intent, int requestCode);

    // String email, String phoneNumber);
}
