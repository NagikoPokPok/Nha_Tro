package edu.poly.nhtr.presenters;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.RoomGuestInterface;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.RoomViewModel;
import edu.poly.nhtr.utilities.Constants;
import timber.log.Timber;

public class RoomGuestPresenter implements RoomGuestInterface.Presenter {
    private final RoomGuestInterface.View view;
    private final RoomViewModel roomViewModel;
    private final FirebaseFirestore database;
    private int position = 0;

    public RoomGuestPresenter(RoomGuestInterface.View view, RoomViewModel roomViewModel) {
        this.view = view;
        this.roomViewModel = roomViewModel;
        this.database = FirebaseFirestore.getInstance();
    }

    public int getPosition() {
        return position;
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
                        List<Object> guests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains(Constants.KEY_CONTRACT_CREATED_DATE)) {
                                // This document is a MainGuest
                                MainGuest mainGuest = new MainGuest();
                                mainGuest.setNameGuest(document.getString(Constants.KEY_GUEST_NAME));
                                mainGuest.setPhoneGuest(document.getString(Constants.KEY_GUEST_PHONE));
                                mainGuest.setDateIn(document.getString(Constants.KEY_CONTRACT_CREATED_DATE));
                                Boolean status = document.getBoolean(Constants.KEY_CONTRACT_STATUS);
                                mainGuest.setFileStatus(status != null && status);
                                mainGuest.setGuestId(document.getId());
                                guests.add(mainGuest);
                            } else {
                                // This document is a regular Guest
                                Guest guest = new Guest();
                                guest.setNameGuest(document.getString(Constants.KEY_GUEST_NAME));
                                guest.setPhoneGuest(document.getString(Constants.KEY_GUEST_PHONE));
                                guest.setDateIn(document.getString(Constants.KEY_GUEST_DATE_IN));
                                Boolean status = document.getBoolean(Constants.KEY_CONTRACT_STATUS);
                                guest.setFileStatus(status != null && status);
                                guest.setGuestId(document.getId());
                                guests.add(guest);
                            }
                        }
                        roomViewModel.setGuests(guests);
                        if (guests.isEmpty()) {
                            view.showNoDataFound();
                        } else {
                            checkRoomCapacity(roomId, guests);
                        }
                    } else {
                        view.showError("Error getting guests");
                    }
                });
    }

    private void checkRoomCapacity(String roomId, List<Object> guests) {
        database.collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(contractTask -> {
                    if (contractTask.isSuccessful() && contractTask.getResult() != null) {
                        for (QueryDocumentSnapshot contractDoc : contractTask.getResult()) {
                            int totalMembers = Objects.requireNonNull(contractDoc.getLong(Constants.KEY_ROOM_TOTAl_MEMBERS)).intValue();
                            Timber.tag("RoomGuestPresenter").d("Max people in room: %s", totalMembers);
                            if (guests.size() >= totalMembers) {
                                view.disableAddGuestButton();
                            } else {
                                view.enableAddGuestButton();
                            }
                        }
                    } else {
                        view.showError("Error checking room capacity");
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

        // Add guest to firebase
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
                    view.dialogClose();
                    view.showToast("Thêm khách thành công");
                })
                .addOnFailureListener(e -> view.showToast("Thêm hợp đồng thất bại"));
    }

    @Override
    public void deleteGuest(Guest guest) {
        view.showLoadingOfFunctions(R.id.btn_delete_guest);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, view.getInfoRoomFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();

                            documentSnapshots.sort((o1, o2) -> {
                                Timestamp timestamp1 = o1.getTimestamp(Constants.KEY_TIMESTAMP);
                                Timestamp timestamp2 = o2.getTimestamp(Constants.KEY_TIMESTAMP);
                                if (timestamp1 != null && timestamp2 != null) {
                                    return timestamp1.compareTo(timestamp2);
                                }
                                return 0;
                            });

                            position = 0;
                            boolean found = false;

                            for (DocumentSnapshot doc : documentSnapshots) {
                                position++;
                                String roomIdFromFirestore = doc.getId();

                                // Kiểm tra nếu roomId khớp
                                if (roomIdFromFirestore.equals(guest.getGuestId())) {
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                database.collection(Constants.KEY_COLLECTION_GUESTS)
                                        .document(guest.getGuestId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            getGuests(view.getInfoRoomFromGoogleAccount());
                                            view.hideLoadingOfFunctions(R.id.btn_delete_guest);
                                            view.dialogClose();
                                            view.openDialogSuccess(R.layout.layout_dialog_delete_guest_success);
                                        })
                                        .addOnFailureListener(e -> {
                                            view.hideLoadingOfFunctions(R.id.btn_delete_guest);
                                            view.showToast("Xoá khách thất bại");
                                        });
                            } else {
                                view.hideLoadingOfFunctions(R.id.btn_delete_guest);
                                view.showToast("Không tìm thấy homeId");
                            }
                        } else {
                            view.hideLoadingOfFunctions(R.id.btn_delete_guest);
                            view.showToast("Lỗi khi lấy tài liệu: " + task.getException());
                        }
                        });
                    }

    @Override
    public void updateGuestInFirebase(String guestId, Guest guest) {
        HashMap<String, Object> guestData = new HashMap<>();
        guestData.put(Constants.KEY_GUEST_NAME, guest.getNameGuest());
        guestData.put(Constants.KEY_GUEST_PHONE, guest.getPhoneGuest());
        guestData.put(Constants.KEY_CONTRACT_STATUS, guest.isFileStatus());
        guestData.put(Constants.KEY_GUEST_DATE_IN, guest.getDateIn());

        database.collection(Constants.KEY_COLLECTION_GUESTS).document(guestId)
                .update(guestData)
                .addOnSuccessListener(aVoid -> {
                    view.showToast("Cập nhật thông tin khách thành công");
                    getGuests(view.getInfoRoomFromGoogleAccount()); // Refresh the guest list
                })
                .addOnFailureListener(e -> view.showToast("Cập nhật thông tin khách thất bại"));
    }
}
