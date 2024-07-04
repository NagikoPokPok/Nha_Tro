package edu.poly.nhtr.presenters;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.interfaces.RoomGuestInterface;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.utilities.Constants;

public class RoomGuestPresenter implements RoomGuestInterface.Presenter {
    private final RoomGuestInterface.View view;
    private final FirebaseFirestore database;

    public RoomGuestPresenter(RoomGuestInterface.View view) {
        this.view = view;
        this.database = FirebaseFirestore.getInstance();
    }

    @Override
    public void getMainGuests(String roomId) {
        Log.d("RoomGuestPresenter", "Fetching guests for roomId: " + roomId);
        Log.d("RoomGuestPresenter", "Fetching guests for Constant: " + Constants.KEY_ROOM_ID);
        database.collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<MainGuest> mainGuestList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
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
                            Log.d("RoomGuestPresenter", "Guest added: " + mainGuest.nameGuest);
                        }
                        Log.d("RoomGuestPresenter", "Total guests found: " + mainGuestList.size());
                        view.showMainGuest(mainGuestList);
                    } else {
                        Log.d("RoomGuestPresenter", "No guests found");
                        view.showNoDataFound();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RoomGuestPresenter", "Error getting data: " + e.getMessage());
                    view.showError("Error getting data: " + e.getMessage());
                });
    }
}
