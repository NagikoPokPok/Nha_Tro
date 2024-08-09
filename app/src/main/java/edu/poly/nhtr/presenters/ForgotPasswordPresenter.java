package edu.poly.nhtr.presenters;

import com.google.firebase.firestore.FirebaseFirestore;

import edu.poly.nhtr.interfaces.ForgotPasswordInterface;
import edu.poly.nhtr.utilities.Constants;

public class ForgotPasswordPresenter {
    private final ForgotPasswordInterface forgotPasswordInterface;

    public ForgotPasswordPresenter(ForgotPasswordInterface forgotPasswordInterface) {
        this.forgotPasswordInterface = forgotPasswordInterface;
    }

    public void setNewPassword(String emailAddress){
        forgotPasswordInterface.showLoading();

        if(checkIsValidEmail(emailAddress)){
            FirebaseFirestore databse = FirebaseFirestore.getInstance();
            databse.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_EMAIL, emailAddress)
                    .get()
                    .addOnCompleteListener(Task->{
                        if(Task.isSuccessful() && Task.getResult()!=null && Task.getResult().getDocuments().size() >0){
                            forgotPasswordInterface.hideLoading();
                            forgotPasswordInterface.success();
                        }
                        else{
                            //Toast.makeText(ForgotPasswordActivity.this, "Email không tồn tại", Toast.LENGTH_SHORT).show();
                            forgotPasswordInterface.showErrorMessage("Email chưa được đăng kí");
                            forgotPasswordInterface.hideLoading();
                            forgotPasswordInterface.error();
                        }

                    });
        }

    }

    private boolean checkIsValidEmail(String emailAddress) {
        String[] strings = emailAddress.split("@");
        if(emailAddress.isEmpty()){
            forgotPasswordInterface.hideLoading();
            forgotPasswordInterface.showErrorMessage("Hãy nhập địa chỉ email đã đăng kí");
            return false;
        }
        else if(strings.length!=2 || !strings[1].equals("gmail.com")) {
            //Toast.makeText(ForgotPasswordActivity.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            forgotPasswordInterface.hideLoading();
            forgotPasswordInterface.showErrorMessage("Email không hợp lệ");
            return false;
        }
        return true;
    }

}
