package edu.poly.nhtr.interfaces;


import android.net.Uri;

public interface GuestAddContractInterface {
    void initializeViews();
    void setUpDropDownMenuGender();
    void setUpDropDownMenuTotalMembers();
    void setCCCDImage(Uri image, int requestCode);

    void setContractImage(Uri image, int requestCode);
}
