package edu.poly.nhtr.presenters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.interfaces.RoomGuestInterface;
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
}

