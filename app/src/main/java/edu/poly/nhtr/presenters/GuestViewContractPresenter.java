package edu.poly.nhtr.presenters;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import edu.poly.nhtr.interfaces.GuestViewContractInterface;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.utilities.Constants;

public class GuestViewContractPresenter {
    private GuestViewContractInterface view;

    public GuestViewContractPresenter(GuestViewContractInterface view) {
        this.view = view;
    }

    public void fetchContractData(String roomId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MainGuest mainGuest = new MainGuest();
                                mainGuest.setNameGuest(document.getString(Constants.KEY_GUEST_NAME));
                                mainGuest.setPhoneGuest(document.getString(Constants.KEY_GUEST_PHONE));
                                mainGuest.setDateIn(document.getString(Constants.KEY_GUEST_DATE_IN));
                                mainGuest.setCccdNumber(document.getString(Constants.KEY_GUEST_CCCD));
                                mainGuest.setDateOfBirth(document.getString(Constants.KEY_GUEST_DATE_OF_BIRTH));
                                mainGuest.setGender(document.getString(Constants.KEY_GUEST_GENDER));
                                mainGuest.setTotalMembers(document.getLong(Constants.KEY_ROOM_TOTAl_MEMBERS).intValue());
                                mainGuest.setCreateDate(document.getString(Constants.KEY_CONTRACT_CREATED_DATE));
                                mainGuest.setExpirationDate(document.getString(Constants.KEY_CONTRACT_EXPIRATION_DATE));
                                mainGuest.setPayDate(document.getString(Constants.KEY_CONTRACT_PAY_DATE));
                                mainGuest.setRoomPrice(document.getDouble(Constants.KEY_CONTRACT_ROOM_PRICE));
                                mainGuest.setDaysUntilDueDate(document.getLong(Constants.KEY_CONTRACT_DAYS_UNTIL_DUE_DATE).intValue());
                                mainGuest.setCccdImageFront(document.getString(Constants.KEY_GUEST_CCCD_IMAGE_FRONT));
                                mainGuest.setCccdImageBack(document.getString(Constants.KEY_GUEST_CCCD_IMAGE_BACK));
                                mainGuest.setContractImageFront(document.getString(Constants.KEY_GUEST_CONTRACT_IMAGE_FRONT));
                                mainGuest.setContractImageBack(document.getString(Constants.KEY_GUEST_CONTRACT_IMAGE_BACK));
                                Boolean status = document.getBoolean(Constants.KEY_CONTRACT_STATUS);
                                mainGuest.setFileStatus(status != null && status);
                                mainGuest.setGuestId(document.getId());

                                view.displayContractData(mainGuest);
                            }
                        } else {
                            view.showNoDataFound();
                        }
                    } else {
                        view.showError("Error getting main guest data");
                    }
                });
    }
}
