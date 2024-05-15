package edu.poly.nhtr.interfaces;

public interface ChangePasswordInterface {
    void changePasswordSuccess(String message);
    void changePasswordError(String message);
    void setPasswordErrorMessage(String message);
    void setNewPasswordErrorMessage(String message);
    void setConfirmNewPasswordErrorMessage(String message);
    void setNewPasswordHyperTextMessage(String message);
    void setNewPasswordConfirmHyperTextMessage(String message);
    void ClearAllData();
    void showLoading();
    void hideLoading();
    void setCheckFeedback();
}
