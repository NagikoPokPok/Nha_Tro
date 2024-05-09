package edu.poly.nhtr.interfaces;

import android.content.Intent;

import androidx.annotation.Nullable;

public interface SignInInterface {
    void showToast(String message);
    void loading(Boolean isLoading);
    void entryMain();
    void googleSignIn();
    void firebaseAuth(String idToken);
    void  notifySignInSuccess();


}
