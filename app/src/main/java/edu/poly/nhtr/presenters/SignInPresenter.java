package edu.poly.nhtr.presenters;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.util.Patterns;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.poly.nhtr.Activity.MainActivity;
import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.databinding.ActivitySignInBinding;
import edu.poly.nhtr.interfaces.SignInInterface;
import edu.poly.nhtr.interfaces.SignUpInterface;
import edu.poly.nhtr.models.User;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SignInPresenter {
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private SignInInterface view;

    public void signIn(User user){

    }
    void check(User user){
        view.loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .get()
                .addOnCompleteListener(task-> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        String storedHashedPassword = documentSnapshot.getString(Constants.KEY_PASSWORD);

                        // Harness the power of hashing to secure your passwords
                        String enteredPassword = binding.inputPassword.getText().toString();
                        String hashedPassword = PasswordHasher.hashPassword(enteredPassword);
                        if (storedHashedPassword.equals(hashedPassword)) {
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                            preferenceManager.putString(Constants.KEY_PASSWORD, documentSnapshot.getString(Constants.KEY_PASSWORD));
                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                            preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                            preferenceManager.putString(Constants.KEY_PHONE_NUMBER, documentSnapshot.getString(Constants.KEY_PHONE_NUMBER));
                            try {
                                preferenceManager.putString(Constants.KEY_ADDRESS, documentSnapshot.getString(Constants.KEY_ADDRESS));
                            } catch (Exception ex){}
                            view.entryMain();
                        } else {
                            // The gates remain shut, authentication denied
                            view.loading(false);
                            view.showToast("Sai tài khoản hoặc mật khẩu");
                        }
                    }else {
                        view.loading(false);
                        view.showToast("Sai tài khoản hoặc mật khẩu");
                    }
                });
    }
    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            view.showToast("Vui lòng nhập email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            view.showToast("Email không hợp lệ");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            view.showToast("Vui lòng nhập mật khẩu");
            return false;
        } else  {
            return true;
        }
    }



}
