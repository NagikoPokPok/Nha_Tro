package edu.poly.nhtr.presenters;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    private static @NonNull List<DocumentSnapshot> getDocumentSnapshots(Task<QuerySnapshot> task) {
        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();

        documentSnapshots.sort((o1, o2) -> {
            Timestamp timestamp1 = o1.getTimestamp(Constants.KEY_TIMESTAMP);
            Timestamp timestamp2 = o2.getTimestamp(Constants.KEY_TIMESTAMP);
            if (timestamp1 != null && timestamp2 != null) {
                return timestamp1.compareTo(timestamp2);
            }
                return 0;
            });
            return documentSnapshots;
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
                        if (view.isAdded2()) {
                            view.hideLoading();
                            if (task.isSuccessful() && task.getResult() != null) {
                                List<Object> guests = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.contains(Constants.KEY_CONTRACT_CREATED_DATE)) {
                                        // This document is a MainGuest
                                        MainGuest mainGuest = new MainGuest();
                                        mainGuest.setNameGuest(document.getString(Constants.KEY_GUEST_NAME));
                                        mainGuest.setPhoneGuest(document.getString(Constants.KEY_GUEST_PHONE));
                                        mainGuest.setDateIn(document.getString(Constants.KEY_GUEST_DATE_IN));
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
            guests.put(Constants.KEY_HOME_ID, view.getInfoHomeFromGoogleAccount());
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
                                view.getInfoHomeFromGoogleAccount(),
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
    public void updateGuestInFirebase(Guest guest) {
        view.showLoadingOfFunctions(R.id.btn_add_new_guest);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, view.getInfoRoomFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> documentSnapshots = getDocumentSnapshots(task);

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
                            HashMap<String, Object> updateInfo = new HashMap<>();
                            updateInfo.put(Constants.KEY_GUEST_NAME, guest.getNameGuest());
                            updateInfo.put(Constants.KEY_GUEST_PHONE, guest.getPhoneGuest());
                            updateInfo.put(Constants.KEY_GUEST_DATE_IN, guest.getDateIn());
                            updateInfo.put(Constants.KEY_CONTRACT_STATUS, guest.isFileStatus());

                            database.collection(Constants.KEY_COLLECTION_GUESTS)
                                    .document(guest.getGuestId())
                                    .update(updateInfo)
                                    .addOnSuccessListener(aVoid -> {
                                        getGuests(view.getInfoRoomFromGoogleAccount());
                                        view.hideLoadingOfFunctions(R.id.btn_add_new_guest);
                                        view.dialogClose();
                                        view.openDialogSuccess(R.layout.layout_dialog_update_guest_success);
                                    })
                                    .addOnFailureListener(e -> {
                                        view.hideLoadingOfFunctions(R.id.btn_add_new_guest);
                                        view.showToast("Cập nhật thông tin khách thất bại");
                                    });
                        } else {
                            view.hideLoadingOfFunctions(R.id.btn_add_new_guest);
                            view.showToast("Không tìm thấy khách");
                        }
                    } else {
                        view.hideLoadingOfFunctions(R.id.btn_add_new_guest);
                        view.showToast("Lỗi khi lấy tài liệu: " + task.getException());
                    }
                });
    }

    @Override
    public void handleNameChanged(String name, TextInputLayout textInputLayout, int boxStrokeColor) {
        if (textInputLayout == null) {
            return;
        }

        if (TextUtils.isEmpty(name)) {
            textInputLayout.setError("Không được bỏ trống");
        } else if (!isValidName(name)) {
            textInputLayout.setError("Tên không được chứa số hoặc ký tự đặc biệt");
        } else {
            textInputLayout.setErrorEnabled(false);
            textInputLayout.setBoxStrokeColor(boxStrokeColor);
        }
    }


    // Kiểm tra tên có chứa kí tự đặc biệt hoặc số không
    private boolean isValidName(String name) {
        String regex = "^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỂưạảấầẩẫậắằẳẵặẹẻẽềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễếệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵỷỹ\\s]+$";
        return name.matches(regex);
    }

    @Override
    public void handlePhoneChanged(String phone, TextInputLayout textInputLayout, int boxStrokeColor) {
        if (textInputLayout == null) {
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            textInputLayout.setError("Không được bỏ trống");
        } else if (!isValidPhoneNumber(phone)) {
            textInputLayout.setError("Số điện thoại không hợp lệ");
        } else {
            checkDuplicatePhoneNumber(phone, textInputLayout, boxStrokeColor);
        }
    }


    // Kiểm tra số điện thoại có hợp lệ không
    private boolean isValidPhoneNumber(CharSequence target) {
        if (target.length() != 10) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }

    private void checkDuplicatePhoneNumber(String phone, TextInputLayout textInputLayout, int boxStrokeColor) {
        database.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_HOME_ID, view.getInfoHomeFromGoogleAccount())
                .whereEqualTo(Constants.KEY_GUEST_PHONE, phone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            textInputLayout.setError("Số điện thoại đã tồn tại trong nhà trọ này");
                        } else {
                            textInputLayout.setError(null);
                            textInputLayout.setBoxStrokeColor(boxStrokeColor);
                        }
                    } else {
                        textInputLayout.setError("Có lỗi xảy ra khi kiểm tra số điện thoại");
                    }
                });
    }

    @Override
    public void handleCheckInDateChanged(String checkInDate, String roomId, TextInputLayout textInputLayout, int boxStrokeColor) {
        if (textInputLayout == null) {
            return;
        }

        if (TextUtils.isEmpty(checkInDate)) {
            textInputLayout.setError("Không được bỏ trống");
        } else {
            isCheckInDateValid(checkInDate, roomId, isValid -> {
                if (!isValid) {
                    textInputLayout.setError("Ngày vào ở không hợp lệ");
                } else {
                    textInputLayout.setErrorEnabled(false);
                    textInputLayout.setBoxStrokeColor(boxStrokeColor);
                }
            });
        }
    }

    private void isCheckInDateValid(String inputDate, String roomId, final OnDateValidationResult callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .whereEqualTo(Constants.KEY_IS_MAIN_GUEST, true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String mainGuestDate = document.getString("checkInDate");

                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date inputDateParsed = sdf.parse(inputDate);
                                Date mainGuestDateParsed = sdf.parse(mainGuestDate);

                                if (inputDateParsed != null && inputDateParsed.before(mainGuestDateParsed)) {
                                    callback.onResult(false);
                                } else {
                                    callback.onResult(true);
                                }
                            } catch (ParseException e) {
                                callback.onResult(false);
                            }
                        }
                    } else {
                        callback.onResult(false);
                    }
                });
    }

    // Callback interface for date validation
    public interface OnDateValidationResult {
        void onResult(boolean isValid);
    }

}
