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
    public void getMainGuests(String roomId) {
        database.collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            List<MainGuest> mainGuestList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MainGuest mainGuest = new MainGuest();
                                mainGuest.nameGuest = document.getString(Constants.KEY_GUEST_NAME);
                                mainGuest.dateIn = document.getString(Constants.KEY_CONTRACT_CREATED_DATE);
                                mainGuest.phoneGuest = document.getString(Constants.KEY_GUEST_PHONE);
                                Boolean fileStatus = document.getBoolean(Constants.KEY_CONTRACT_STATUS);

                                if (fileStatus != null) {
                                    mainGuest.fileStatus = fileStatus;
                                } else {
                                    mainGuest.fileStatus = false;
                                }

                                mainGuestList.add(mainGuest);
                            }
                            roomViewModel.setMainGuests(mainGuestList); // Update ViewModel
                        } else {
                            roomViewModel.setMainGuests(new ArrayList<>()); // Update ViewModel with empty list
                            view.showNoDataFound();
                        }
                    } else {
                        roomViewModel.setMainGuests(new ArrayList<>()); // Update ViewModel with empty list
                        view.showError("Error getting data");
                    }
                });
    }

    @Override
    public void addGuestToFirebase(Guest guest) {
        HashMap<String, Object> guests = new HashMap<>();
        guests.put(Constants.KEY_GUEST_NAME, guest.getNameGuest());
        guests.put(Constants.KEY_GUEST_PHONE, guest.getPhoneGuest());
        guests.put(Constants.KEY_CONTRACT_STATUS, guest.getFileStatus());
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
                            guest.getFileStatus(),
                            view.getInfoRoomFromGoogleAccount(),
                            documentReference
                    );
                    view.showToast("Thêm khách thành công");
                })
                .addOnFailureListener(e -> view.showToast("Thêm hợp đồng thất bại"));
    }
    @Override
    public void getGuests(String roomId) {
        view.showLoading();
        database.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(task -> {
                    view.hideLoading();
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<MainGuest> guests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MainGuest guest = new MainGuest();
                            guest.setNameGuest(document.getString(Constants.KEY_GUEST_NAME));
                            guest.setPhoneGuest(document.getString(Constants.KEY_GUEST_PHONE));
                            guest.setDateIn(document.getString(Constants.KEY_GUEST_DATE_IN));
                            Boolean status = document.getBoolean(Constants.KEY_CONTRACT_STATUS);
                            if (status != null) {
                                guest.setFileStatus(status);
                            } else {
                                guest.setFileStatus(false);
                            }
                            guest.idRoom = document.getId();
                            guests.add(guest);
                        }
                        if (guests.isEmpty()) {
                            view.showNoDataFound();
                        } else {
                            view.showMainGuest(guests);
                        }
                    }
                });
    }
}

