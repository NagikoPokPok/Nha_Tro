package edu.poly.nhtr.presenters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.poly.nhtr.interfaces.RoomGuestInterface;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.RoomViewModel;
import edu.poly.nhtr.utilities.Constants;

public class RoomGuestPresenter implements RoomGuestInterface.Presenter {
    private final RoomGuestInterface.View view;
    private final RoomViewModel roomViewModel;
    private final FirebaseFirestore database;

    public RoomGuestPresenter(RoomGuestInterface.View view, RoomViewModel roomViewModel) {
        this.view = view;
        this.roomViewModel = roomViewModel;
        this.database = FirebaseFirestore.getInstance();
    }


    @Override
    public void getGuests(String roomId) {
        view.showLoading();
        database.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(task -> {
                    view.hideLoading();
                    if (task.isSuccessful()) {
                        List<Object> guests = new ArrayList<>(); // List of both Guest and MainGuest
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains(Constants.KEY_CONTRACT_CREATED_DATE)) {
                                // This document is a MainGuest
                                MainGuest mainGuest = new MainGuest();
                                mainGuest.setNameGuest(document.getString(Constants.KEY_GUEST_NAME));
                                mainGuest.setPhoneGuest(document.getString(Constants.KEY_GUEST_PHONE));
                                mainGuest.setDateIn(document.getString(Constants.KEY_CONTRACT_CREATED_DATE));
                                Boolean status = document.getBoolean(Constants.KEY_CONTRACT_STATUS);
                                mainGuest.setFileStatus(status != null && status);
                                guests.add(mainGuest);
                            } else {
                                // This document is a regular Guest
                                Guest guest = new Guest();
                                guest.setNameGuest(document.getString(Constants.KEY_GUEST_NAME));
                                guest.setPhoneGuest(document.getString(Constants.KEY_GUEST_PHONE));
                                guest.setDateIn(document.getString(Constants.KEY_GUEST_DATE_IN));
                                Boolean status = document.getBoolean(Constants.KEY_CONTRACT_STATUS);
                                guest.setFileStatus(status != null && status);
                                guests.add(guest);
                            }
                        }
                        roomViewModel.setGuests(guests); // Update ViewModel with all guests
                        if (guests.isEmpty()) {
                            view.showNoDataFound();
                        }
                    } else {
                        view.showError("Error getting guests");
                    }
                });
    }


    @Override
    public void addGuestToFirebase(Guest guest) {
        HashMap<String, Object> guests = new HashMap<>();
        guests.put(Constants.KEY_GUEST_NAME, guest.getNameGuest());
        guests.put(Constants.KEY_GUEST_PHONE, guest.getPhoneGuest());
        guests.put(Constants.KEY_CONTRACT_STATUS, guest.isFileStatus());
        guests.put(Constants.KEY_GUEST_DATE_IN, guest.getDateIn());
        guests.put(Constants.KEY_ROOM_ID, view.getInfoRoomFromGoogleAccount());
        guests.put(Constants.KEY_TIMESTAMP, new Date());

        // Thêm khách lên firebase
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_GUESTS)
                .add(guests)
                .addOnSuccessListener(documentReference -> {
                    view.putGuestInfoInPreferences(
                            guest.getNameGuest(),
                            guest.getPhoneGuest(),
                            guest.getDateIn(),
                            guest.isFileStatus(),
                            view.getInfoRoomFromGoogleAccount(),
                            documentReference
                    );
                    view.showToast("Thêm khách thành công");
                })
                .addOnFailureListener(e -> view.showToast("Thêm hợp đồng thất bại"));
    }
}

