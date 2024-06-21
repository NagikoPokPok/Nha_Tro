package edu.poly.nhtr.interfaces;


public interface GuestAddContractInterface {
    void initializeViews();
    void setUpDropDownMenuGender();
    void setUpDropDownMenuTotalMembers();

    void showErrorMessage(String message);

    void setNameErrorMessage(String message);

    void setPhoneErrorMessage(String message);

    void setCCCDNumberErrorMessage(String message);

    void setNameErrorEnabled(Boolean isEmpty);

    void setPhoneNumberlErrorEnabled(Boolean isEmpty);

    void setCCCDNumberlErrorEnabled(Boolean isEmpty);
}
