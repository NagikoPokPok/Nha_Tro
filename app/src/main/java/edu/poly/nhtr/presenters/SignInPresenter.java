package edu.poly.nhtr.presenters;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.concurrent.Executor;

import edu.poly.nhtr.Activity.SignInActivity;
import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.SignInInterface;
import edu.poly.nhtr.models.User;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SignInPresenter {
    private static final String TAG = "SignInActivity";
    private final int RC_SIGN_IN = 20;
    private final SignInActivity signInActivity;
    private final PreferenceManager preferenceManager;
    private final SignInInterface view;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    public SignInPresenter(SignInInterface view, SignInActivity signInActivity) {
        this.signInActivity = signInActivity;
        this.view = view;
        this.preferenceManager = signInActivity.preferenceManager;
    }

    public void signIn(User user) {
        if (isValidSignInDetails(user.getEmail(), user.getPassword())) {
            check(user);
        }
    }

    public void reload() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String userName = documentSnapshot.getString(Constants.KEY_NAME);
                        view.showToast("Chào mừng trở lại, " + userName + "!");
                        view.entryMain();
                    })
                    .addOnFailureListener(e -> view.showToast("Thất bại khi lấy thông tin người dùng: " + e.getMessage()));
        } else {
            view.showToast("Tài khoản chưa được đăng nhập");
        }
    }

    void check(User user) {
        view.loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, user.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        String storedHashedPassword = documentSnapshot.getString(Constants.KEY_PASSWORD);

                        // Harness the power of hashing to secure your passwords
                        String enteredPassword = user.getPassword();
                        String hashedPassword = PasswordHasher.hashPassword(enteredPassword);
                        if (storedHashedPassword.equals(hashedPassword)) {
//                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
//                            preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
//                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
//                            preferenceManager.putString(Constants.KEY_PASSWORD, documentSnapshot.getString(Constants.KEY_PASSWORD));
//                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
//                            preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
//                            preferenceManager.putString(Constants.KEY_PHONE_NUMBER, documentSnapshot.getString(Constants.KEY_PHONE_NUMBER));

//                            view.putPreference(true, documentSnapshot.getId(), documentSnapshot.getString(Constants.KEY_NAME), documentSnapshot.getString(Constants.KEY_PASSWORD), documentSnapshot.getString(Constants.KEY_IMAGE)
//                            , documentSnapshot.getString(Constants.KEY_EMAIL), documentSnapshot.getString(Constants.KEY_PHONE_NUMBER));

                            try {
                                preferenceManager.putString(Constants.KEY_ADDRESS, documentSnapshot.getString(Constants.KEY_ADDRESS));
                            } catch (Exception ex) {
                            }
                            view.entryMain();

                            mAuth.signInWithEmailAndPassword(preferenceManager.getString(Constants.KEY_EMAIL), preferenceManager.getString(Constants.KEY_PASSWORD))
                                    .addOnCompleteListener((Executor) this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d(TAG, "signInWithEmail:success");
                                                FirebaseUser user = mAuth.getCurrentUser();

                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w(TAG, "signInWithEmail:failure", task.getException());

                                            }
                                        }
                                    });
                        } else {
                            // The gates remain shut, authentication denied
                            view.loading(false);
                            view.showToast("Sai tài khoản hoặc mật khẩu");
                        }
                    } else {
                        view.loading(false);
                        view.showToast("Sai tài khoản hoặc mật khẩu");
                    }
                });
    }

    private Boolean isValidSignInDetails(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            view.showToast("Vui lòng nhập email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showToast("Email không hợp lệ");
            return false;
        } else if (TextUtils.isEmpty(password)) {
            view.showToast("Vui lòng nhập mật khẩu");
            return false;
        } else {
            return true;
        }
    }

    // Đăng nhập bằng Google
    public void googleSignIn() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(view.getStringFromResources(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(view.getContext(), googleSignInOptions);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            view.startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }


    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            if (task.isSuccessful()) {
                try {
                    GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
                    if (googleSignInAccount != null) {
                        firebaseAuth(googleSignInAccount.getIdToken());
                    } else {
                        view.showToast("Đăng nhập thất bại");
                    }
                } catch (ApiException e) {
                    view.showToast("Đăng nhập thất bại: " + e.getMessage());
                }
            } else {
                view.showToast("Có sự cố xảy ra khi đăng nhập");
            }
        }
    }


    public void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            // Lưu thông tin người dùng vào Firestore
                            HashMap<String, Object> userData = new HashMap<>();
                            userData.put(Constants.KEY_USER_ID, user.getUid());  // Lưu User ID
                            userData.put(Constants.KEY_NAME, user.getDisplayName());
                            userData.put(Constants.KEY_EMAIL, user.getEmail());
                            if (user.getPhotoUrl() != null) {
                                userData.put(Constants.KEY_IMAGE, user.getPhotoUrl().toString());
                            }

                            FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                                    .document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Lưu thông tin vào PreferenceManager
                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                        preferenceManager.putString(Constants.KEY_USER_ID, user.getUid());
                                        preferenceManager.putString(Constants.KEY_NAME, user.getDisplayName());
                                        preferenceManager.putString(Constants.KEY_EMAIL, user.getEmail());
                                        if (user.getPhotoUrl() != null) {
                                            preferenceManager.putString(Constants.KEY_IMAGE, user.getPhotoUrl().toString());
                                        }
                                        view.notifySignInSuccess();
                                    })
                                    .addOnFailureListener(e -> view.showToast("Có sự cố xảy ra khi lưu thông tin người dùng"));
                        }
                    } else {
                        // Đăng nhập thất bại
                        view.showToast("Có sự cố xảy ra khi đăng nhập");
                    }
                });
    }



}
