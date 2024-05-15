package edu.poly.nhtr.presenters;


import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import edu.poly.nhtr.Activity.SetNewPasswordActivity;
import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.interfaces.SetNewPasswordInterface;
import edu.poly.nhtr.models.User;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SetNewPasswordPresenter {
    SetNewPasswordInterface view;
    SetNewPasswordActivity setNewPasswordActivity;
    String email, id, password, newPass;
    PreferenceManager preferenceManager;
    private FirebaseAuth mAuth;

    public SetNewPasswordPresenter(SetNewPasswordActivity setNewPasswordActivity, SetNewPasswordInterface view) {
        this.view = view;
        this.setNewPasswordActivity = setNewPasswordActivity;
    }
    public void UpdatePassword(PreferenceManager preferenceManager, User user, FirebaseAuth mAuth){
        if(user.getPassword().isEmpty())
            view.showErrorMessage("Hãy Nhập mật khẩu mới");
        else if (user.getPassword().length()<6)
            view.showErrorMessage("Mật khẩu mới phải đảm bảo lớn hơn hoặc bằng 6 kí tự");
        else if(user.confirmPassword.isEmpty())
            view.showErrorMessage("Hãy nhập lại mật khẩu mới");
        else if (!user.getPassword().equals(user.getConfirmPassword())) {
            view.showErrorMessage("Mật khẩu nhập lại không khớp");
        }else {
            email = user.getEmail();
            this.preferenceManager = preferenceManager;
            this.mAuth = mAuth;
            this.newPass = user.getPassword();

            update();
        }
    }
    public void update(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                        preferenceManager.putString(Constants.KEY_PASSWORD, documentSnapshot.getString(Constants.KEY_PASSWORD)); // Assuming this is the stored hashed password

                        // Xóa mật khẩu chưa mã hóa từ PreferenceManager, chỉ sử dụng mật khẩu mã hóa
                        preferenceManager.removePreference(Constants.KEY_PASSWORD);

                        id = documentSnapshot.getId();
                        password = documentSnapshot.getString(Constants.KEY_PASSWORD); // Lấy mật khẩu mã hóa từ Firestore
                        SignInAuth();
                    } else {
                        view.showErrorMessage("Người dùng với email:  " + email + " không tìm thấy");
                        Log.e(TAG, "signInWithEmail:failure", task.getException());
                    }
                });
    }
    private void SignInAuth(){
        Log.e("Account", email + " " + password);
        setNewPasswordActivity.mAuth.signInWithEmailAndPassword(preferenceManager.getString(Constants.KEY_EMAIL), password) // Use the hashed password retrieved from Firestore
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithEmail:success");
                            UpdateAuth();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithEmail:failure", task.getException());
                            view.showErrorMessage("Xác thực người dùng thất bại.");
                        }
                    }
                });
    }
    private void SignOutAuth(){
        setNewPasswordActivity.mAuth.signOut();
    }
    private void UpdateAuth(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String newHashedPass = PasswordHasher.hashPassword(newPass);
            user.updatePassword(newHashedPass)
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            view.showErrorMessage("Cập nhật mật khẩu thành công");
                            UpdateFirestore(newHashedPass); // Pass the new hashed password
                        } else {
                            view.showErrorMessage("ERROR ");
                            Log.e("FirestoreError", "Lỗi: " + Objects.requireNonNull(task1.getException()).getMessage());
                        }
                    });
        } else {
            view.showErrorMessage("null");
        }
    }
    private void UpdateFirestore(String newHashedPassword) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS).document(id);
        if(documentReference != null) {
            documentReference.update(Constants.KEY_PASSWORD, newHashedPassword) // Update with the new hashed password
                    .addOnSuccessListener(unused -> {
//                        Toast.makeText(SetNewPasswordActivity.this, "Cập nhật mk thành công", Toast.LENGTH_SHORT).show();
                        view.clearAll();
                        SignOutAuth();
                        view.updateSuccess("Cập nhật mật khẩu thành công");
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        view.showErrorMessage("Cập nhật mật kẩu thất bại");
                    });
        } else {
            view.showErrorMessage("Document reference is null");
        }
    }

}
