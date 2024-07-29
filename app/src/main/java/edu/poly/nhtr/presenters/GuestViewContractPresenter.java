package edu.poly.nhtr.presenters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import edu.poly.nhtr.interfaces.GuestViewContractInterface;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.utilities.Constants;

public class GuestViewContractPresenter {
    private GuestViewContractInterface view;

    public GuestViewContractPresenter(GuestViewContractInterface view) {
        this.view = view;
    }

    public void fetchContractData(String roomId) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            MainGuest mainGuest = document.toObject(MainGuest.class);
                            view.displayContractData(mainGuest);
                            return;
                        }
                    } else {
                        view.showToast("Contract not found");
                    }
                })
                .addOnFailureListener(e -> view.showToast("Failed to fetch contract data: " + e.getMessage()));
    }
}
