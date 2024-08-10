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

    public RoomContractPresenter(RoomContractInterface.View view) {
        this.view = view;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
    }

    @Override
    public void deleteContract(Room room) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String roomId = view.getInfoRoomFromGoogleAccount();

        database.collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> contractSnapshots = task.getResult().getDocuments();

                        for (DocumentSnapshot contractDoc : contractSnapshots) {
                            String contractId = contractDoc.getId();

                            database.collection(Constants.KEY_COLLECTION_CONTRACTS)
                                    .document(contractId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
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

                        if (guestSnapshots.isEmpty()) {
                            view.hideLoadingButton(R.id.btn_confirm_delete_contract);
                            view.closeDialog();
                            return;
                        }

                        int totalGuests = guestSnapshots.size();
                        int[] deleteCount = {0};

                        for (DocumentSnapshot guestDoc : guestSnapshots) {
                            String guestId = guestDoc.getId();

                            database.collection(Constants.KEY_COLLECTION_GUESTS)
                                    .document(guestId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        deleteCount[0]++;
                                        if (deleteCount[0] == totalGuests) {
                                            view.hideLoadingButton(R.id.btn_confirm_delete_contract);
                                            view.showToast("Hợp đồng đã được xóa thành công");
                                            view.closeDialog();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                    });
                        }
                    }
                });
    }


    @Override
    public void printContract(Room room) {
        view.onContractPrinted();
    }
}
