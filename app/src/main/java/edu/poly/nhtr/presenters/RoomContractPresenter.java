package edu.poly.nhtr.presenters;

import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.RoomContractInterface;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.Constants;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RoomContractPresenter implements RoomContractInterface.Presenter {

    private final RoomContractInterface.View view;
    private final FirebaseFirestore db;

    public RoomContractPresenter(RoomContractInterface.View view) {
        this.view = view;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void deleteContract(Room room) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String roomId = view.getInfoRoomFromGoogleAccount();

        // Fetch contracts related to the room
        database.collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> contractSnapshots = task.getResult().getDocuments();

                        // Delete each contract document
                        for (DocumentSnapshot contractDoc : contractSnapshots) {
                            String contractId = contractDoc.getId();

                            database.collection(Constants.KEY_COLLECTION_CONTRACTS)
                                    .document(contractId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // After deleting contracts, proceed to delete guests
                                        deleteGuests(roomId);
                                    })
                                    .addOnFailureListener(e -> {

                                    });
                        }
                    } else {
                        view.showToast("Lỗi khi lấy hợp đồng: " + task.getException());
                    }
                });
    }

    private void deleteGuests(String roomId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> guestSnapshots = task.getResult().getDocuments();

                        // Delete each guest document
                        for (DocumentSnapshot guestDoc : guestSnapshots) {
                            String guestId = guestDoc.getId();

                            database.collection(Constants.KEY_COLLECTION_GUESTS)
                                    .document(guestId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                    })
                                    .addOnFailureListener(e -> {
                                        view.showToast("Xoá khách thất bại: " + e.getMessage());
                                    });
                        }
                    } else {
                        view.showToast("Lỗi khi lấy khách: " + task.getException());
                    }
                });
    }

    @Override
    public void printContract(Room room) {
        // Implement print contract functionality
        // For example, generate a PDF and trigger a print job
        view.onContractPrinted();
    }
}
