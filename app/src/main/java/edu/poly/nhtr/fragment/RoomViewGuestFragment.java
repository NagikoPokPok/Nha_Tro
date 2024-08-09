package edu.poly.nhtr.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomViewGuestBinding;
import edu.poly.nhtr.interfaces.RoomGuestViewInterface;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.presenters.RoomViewGuestPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class RoomViewGuestFragment extends Fragment implements RoomGuestViewInterface.View  {

    private RoomViewGuestPresenter presenter;
    private Dialog dialog;
    private TextInputEditText edtTenKhach, edtNgayVao, edtSoDienThoai, edtSoCCCD;
    private FrameLayout frmMenu;
    private ImageView imgCircleMenu;
    private PreferenceManager preferenceManager;
    private FragmentRoomViewGuestBinding binding;
    private static final int IMAGE_SELECTION_CCCD_FRONT = 1;
    private static final int IMAGE_SELECTION_CCCD_BACK = 2;
    public String encodedCCCDFrontImage;
    public String encodedCCCDBackImage;
    private int currentImageSelection;
    private RoomGuestFragment.OnFragmentInteractionListener mListener;
    private ImageView btnBack;
    private Guest guest;
    private static final int REQUIRED_DATE_LENGTH = 8; // Độ dài chuỗi ngày tháng năm yêu cầu


    // Hàm truy cập thư viện để lấy ảnh
    public final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = requireContext().getContentResolver().openInputStream(Objects.requireNonNull(imageUri));
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            if (bitmap != null) {
                                switch (currentImageSelection) {
                                    case IMAGE_SELECTION_CCCD_FRONT:
                                        binding.imgCccdFront.setImageBitmap(bitmap);
                                        binding.imgAddCccdFront.setVisibility(View.GONE);
                                        encodedCCCDFrontImage = encodeImage(bitmap);
                                        break;
                                    case IMAGE_SELECTION_CCCD_BACK:
                                        binding.imgCccdBack.setImageBitmap(bitmap);
                                        binding.imgAddCccdBack.setVisibility(View.GONE);
                                        encodedCCCDBackImage = encodeImage(bitmap);
                                        break;
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RoomGuestFragment.OnFragmentInteractionListener) {
            mListener = (RoomGuestFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new RoomViewGuestPresenter(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRoomViewGuestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireActivity());

        // Initialize dialog
        dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Initialize UI elements
        edtTenKhach = view.findViewById(R.id.edt_ten_khach);
        edtNgayVao = view.findViewById(R.id.edt_ngay_vao);
        edtSoDienThoai = view.findViewById(R.id.edt_so_dien_thoai);
        edtSoCCCD = view.findViewById(R.id.edt_so_cccd);
        frmMenu = view.findViewById(R.id.frm_menu);
        imgCircleMenu = view.findViewById(R.id.img_circle_menu);

        setFieldsEnabled(false);

        if (getArguments() != null) {
            guest = (Guest) getArguments().getSerializable("guest");
            int guestPosition = getArguments().getInt("guest_position", -1);

            if (guestPosition != -1) {
                binding.txtOrdinalNumber.setText(String.valueOf(guestPosition));
            }
            if (guest != null) {
                presenter.fetchGuestDetails(guest.getGuestId());
                presenter.checkMainGuest(guest.getGuestId(), new RoomViewGuestPresenter.OnMainGuestCheckListener() {
                    @Override
                    public void onCheckCompleted(boolean isMainGuest) {
                        updateContractOwnerSection(isMainGuest);
                    }

                    @Override
                    public void onCheckFailed(Exception e) {
                        showToast("Lỗi khi kiểm tra khách chính: " + e.getMessage());
                    }
                });
            }
        }

        btnBack = binding.btnBack;
        btnBack.setOnClickListener(v -> returnToPreviousFragment());

        frmMenu.setOnClickListener(v -> {
            if (guest != null) {
                presenter.checkMainGuest(guest.getGuestId(), new RoomViewGuestPresenter.OnMainGuestCheckListener() {
                    @Override
                    public void onCheckCompleted(boolean isMainGuest) {
                        if (isMainGuest) {
                            disableMenuForMainGuest();
                        } else {
                            openMenuForEachRoom(v, guest);
                        }
                    }

                    @Override
                    public void onCheckFailed(Exception e) {
                        showToast("Lỗi khi kiểm tra khách chính: " + e.getMessage());
                    }
                });
            }
        });

        binding.btnSave.setOnClickListener(v -> {
            if (guest != null) {
                String name = edtTenKhach.getText().toString().trim();
                String dateIn = edtNgayVao.getText().toString().trim();
                String phone = edtSoDienThoai.getText().toString().trim();
                String cccd = edtSoCCCD.getText().toString().trim();

                boolean isNameEmpty = binding.tilTenKhach.getError() != null;
                boolean isDateInEmpty = binding.tilNgayVao.getError() != null;
                boolean isPhoneEmpty = binding.tilSoDienThoai.getError() != null;
                boolean isCccdEmpty = cccd.isEmpty();
                boolean isCccdFrontEmpty = encodedCCCDFrontImage == null;
                boolean isCccdBackEmpty = encodedCCCDBackImage == null;

                boolean allFieldsComplete = !isNameEmpty && !isDateInEmpty && !isPhoneEmpty && !isCccdEmpty && !isCccdFrontEmpty && !isCccdBackEmpty;
                guest.setFileStatus(allFieldsComplete);

                if (!isNameEmpty) guest.setNameGuest(name);
                if (!isDateInEmpty) guest.setDateIn(dateIn);
                if (!isPhoneEmpty) guest.setPhoneGuest(phone);
                if (!isCccdEmpty) guest.setCccdNumber(cccd);
                if (!isCccdFrontEmpty) guest.setCccdImageFront(encodedCCCDFrontImage);
                if (!isCccdBackEmpty) guest.setCccdImageBack(encodedCCCDBackImage);

                if (!isNameEmpty && !isDateInEmpty && !isPhoneEmpty && !isCccdEmpty) {
                    presenter.updateGuestInFirebase(guest);
                    returnToPreviousFragment();
                } else {
                    showToast("Vui lòng hoàn thành các trường thông tin bắt buộc.");
                }
            }
        });

        binding.btnCancel.setOnClickListener(v -> {
            setFieldsEnabled(false);
            setVisibilityOfButtons(View.GONE);
        });
    }

    private void setFieldsLogic() {

        binding.imgAddCccdFront.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_FRONT;
            pickImage.launch(presenter.prepareImageSelection());
        });
        binding.imgAddCccdBack.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_BACK;
            pickImage.launch(presenter.prepareImageSelection());
        });
        binding.imgCccdFront.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_FRONT;
            pickImage.launch(presenter.prepareImageSelection());
        });
        binding.imgCccdBack.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_BACK;
            pickImage.launch(presenter.prepareImageSelection());
        });

        int boxStrokeColor = getResources().getColor(R.color.colorPrimary);

        binding.edtTenKhach.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.handleNameChanged(s.toString(), binding.tilTenKhach, boxStrokeColor);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        binding.edtSoDienThoai.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.handlePhoneChanged(s.toString(), binding.tilSoDienThoai, boxStrokeColor, guest.getGuestId());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        binding.edtNgayVao.addTextChangedListener(new TextWatcher() {
            private final Calendar cal = Calendar.getInstance();
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("\\D", "");
                    String cleanCurr = current.replaceAll("\\D", "");

                    int c1 = clean.length();
                    int sel = c1;

                    final int MAX_DAY_MONTH_FORMAT_LENGTH = 6;

                    for (int i = 2; i < c1 && i < MAX_DAY_MONTH_FORMAT_LENGTH; i += 2) {
                        sel++;
                    }

                    if (clean.equals(cleanCurr)) sel--;

                    if (clean.length() < 8) {
                        String ddmmyyyy = "DDMMYYYY";
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int month = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        if (month > 12) month = 12;
                        cal.set(Calendar.MONTH, month - 1);
                        year = Math.min(Math.max(year, 2000), 2100);
                        cal.set(Calendar.YEAR, year);

                        day = Math.min(day, cal.getActualMaximum(Calendar.DATE));
                        clean = String.format(Locale.getDefault(), "%02d%02d%04d", day, month, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = Math.max(sel, 0);
                    current = clean;
                    binding.edtNgayVao.setText(current);
                    binding.edtNgayVao.setSelection(Math.min(sel, current.length()));
                }

                if (s.length() == REQUIRED_DATE_LENGTH) {
                    presenter.handleCheckInDateChanged(s.toString(), guest.getRoomId() , binding.tilNgayVao, boxStrokeColor);
                } else {
                    binding.tilNgayVao.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState(edtTenKhach, edtSoDienThoai, binding.btnSave);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        binding.edtTenKhach.addTextChangedListener(textWatcher);
        binding.edtSoDienThoai.addTextChangedListener(textWatcher);
    }

    private void updateContractOwnerSection(boolean isMainGuest) {
        TextView txtOrdinalNumber = binding.txtOrdinalNumber;
        ImageView imgStar = binding.imgStar;
        TextView txtNguoiDaiDien = binding.txtNguoiDaiDien;

        if (isMainGuest) {
            txtOrdinalNumber.setVisibility(View.GONE);
            imgStar.setVisibility(View.VISIBLE);
            txtNguoiDaiDien.setVisibility(View.VISIBLE);
        } else {
            txtOrdinalNumber.setVisibility(View.VISIBLE);
            imgStar.setVisibility(View.GONE);
            txtNguoiDaiDien.setVisibility(View.GONE);
        }
    }

    private void returnToPreviousFragment() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
        mListener.showTabLayoutAndViewPager();
    }

    private void setFieldsEnabled(boolean enabled) {
        edtTenKhach.setClickable(enabled);
        edtTenKhach.setFocusable(enabled);
        edtTenKhach.setFocusableInTouchMode(enabled);
        edtNgayVao.setClickable(enabled);
        edtNgayVao.setFocusable(enabled);
        edtNgayVao.setFocusableInTouchMode(enabled);
        edtSoCCCD.setClickable(enabled);
        edtSoCCCD.setFocusable(enabled);
        edtSoCCCD.setFocusableInTouchMode(enabled);
        edtSoDienThoai.setClickable(enabled);
        edtSoDienThoai.setFocusable(enabled);
        edtSoDienThoai.setFocusableInTouchMode(enabled);
    }


    private void setVisibilityOfButtons(int visibility) {
        binding.btnCancel.setVisibility(visibility);
        binding.btnSave.setVisibility(visibility);
    }
    @Override
    public void dialogClose() {
        dialog.dismiss();
    }

    @Override
    public boolean isAdded2() {
        return isAdded();
    }

    @Override
    public void openDialogSuccess(int id) {
        setUpDialog(id);

        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public void showLoadingOfFunctions(int id) {
        dialog.findViewById(id).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingOfFunctions(int id) {
        dialog.findViewById(id).setVisibility(View.VISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
    }

    @Override
    public void showGuestDetails(Guest guest) {
        if (!isAdded()) return;

        edtTenKhach.setText(guest.getNameGuest());
        edtNgayVao.setText(guest.getDateIn());
        edtSoDienThoai.setText(guest.getPhoneGuest());
        edtSoCCCD.setText(guest.getCccdNumber());

        Bitmap frontImageBitmap = getConversionImageForCCCD(guest.getCccdImageFront());
        if (frontImageBitmap != null) {
            binding.imgAddCccdFront.setVisibility(View.GONE);
            binding.imgCccdFront.setImageBitmap(frontImageBitmap);
        } else {
            binding.imgAddCccdFront.setVisibility(View.VISIBLE);
        }

        Bitmap backImageBitmap = getConversionImageForCCCD(guest.getCccdImageBack());
        if (backImageBitmap != null) {
            binding.imgAddCccdBack.setVisibility(View.GONE);
            binding.imgCccdBack.setImageBitmap(backImageBitmap);
        } else {
            binding.imgAddCccdBack.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public String getInfoRoomFromGoogleAccount() {
        return preferenceManager.getString(Constants.KEY_ROOM_ID);
    }

    @Override
    public String getInfoHomeFromGoogleAccount() {
        return preferenceManager.getString(Constants.KEY_HOME_ID);
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.INVISIBLE);
    }

    private void updateButtonState(EditText edtNameGuest, EditText edtPhoneGuest, AppCompatButton btnAdd) {
        if (isAdded()) {
            String name = edtNameGuest.getText().toString().trim();
            String phone = edtPhoneGuest.getText().toString().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                btnAdd.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.custom_button_clicked, null));
            } else {
                btnAdd.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.custom_button_add, null));
            }
        }
    }


    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 250;
        int previewHeight = bitmap.getHeight() + previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private Bitmap getConversionImageForCCCD(String encodedImage) {
        if (encodedImage == null || encodedImage.isEmpty()) {
            // Trả về null nếu encodedImage rỗng
            return null;
        }

        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        int heightDp = 250;
        int heightPx = (int) (heightDp * getResources().getDisplayMetrics().density);

        int widthPx = bitmap.getWidth() * heightPx / bitmap.getHeight();

        return Bitmap.createScaledBitmap(bitmap, widthPx, heightPx, true);
    }


    @Override
    public void showSuccessDialog(int layoutId) {
        setUpDialog(layoutId);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public void disableMenuForMainGuest() {
        frmMenu.setEnabled(false);
        frmMenu.setAlpha(0.5f);
        showToast("Bạn chỉ có thể chỉnh sửa hoặc xóa chủ hợp đồng ở trang hợp đồng.");
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void setUpDialog(int layoutId) {
        dialog.setContentView(layoutId);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    private void openDeleteGuestDialog(Guest guest) {
        setUpDialog(R.layout.layout_dialog_delete_guest);

        TextView txtConfirmDeleteGuest = dialog.findViewById(R.id.txt_confirm_delete_guest);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_delete_guest);
        Button btnDeleteGuest = dialog.findViewById(R.id.btn_delete_guest);

        String text = " " + guest.getNameGuest() + " không?";
        txtConfirmDeleteGuest.append(text);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDeleteGuest.setOnClickListener(v -> {
            presenter.deleteGuest(guest, new RoomViewGuestPresenter.DeleteGuestCallback() {
                @Override
                public void onSuccess() {
                    dialog.dismiss();
                    if (getFragmentManager() != null) {
                        getFragmentManager().popBackStack();
                    }
                    mListener.showTabLayoutAndViewPager();
                }

                @Override
                public void onFailure(Exception e) {
                    dialog.dismiss();
                    showToast("Xóa khách thất bại: " + e.getMessage());
                }
            });
        });
    }

    private void openMenuForEachRoom(View view, Guest guest) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit_guest) {
                setFieldsEnabled(true);
                setFieldsLogic();
                setVisibilityOfButtons(View.VISIBLE);
                return true;
            } else if (itemId == R.id.menu_delete_guest) {
                openDeleteGuestDialog(guest);
                return true;
            }
            return false;
        });

        popupMenu.inflate(R.menu.menu_edit_delete_guest);
        popupMenu.show();
    }

}
