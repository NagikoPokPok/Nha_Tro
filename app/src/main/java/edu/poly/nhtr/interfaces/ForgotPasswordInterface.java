package edu.poly.nhtr.interfaces;

public interface ForgotPasswordInterface {
    void success();
    void error();
    void showLoading();
    void hideLoading();
    void showErrorMessage(String message);
}
