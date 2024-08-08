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
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomViewGuestBinding;
import edu.poly.nhtr.interfaces.RoomGuestViewInterface;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.presenters.RoomViewGuestPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class RoomViewGuestFragment extends Fragment implements RoomGuestViewInterface.View {

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

        // Set fields to read-only
        setFieldsEnabled(false);

        if (getArguments() != null) {
            guest = (Guest) getArguments().getSerializable("guest");
            if (guest != null) {
                presenter.fetchGuestDetails(guest.getGuestId());
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
        edtNgayVao.setClickable(enabled);
        edtNgayVao.setFocusable(enabled);
        edtSoCCCD.setClickable(enabled);
        edtSoCCCD.setFocusable(enabled);
        edtSoDienThoai.setClickable(enabled);
        edtSoDienThoai.setFocusable(enabled);
        edtTenKhach.setClickable(enabled);
        edtTenKhach.setFocusable(enabled);
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
        edtTenKhach.setText(guest.getNameGuest());
        edtNgayVao.setText(guest.getDateIn());
        edtSoDienThoai.setText(guest.getPhoneGuest());
        edtSoCCCD.setText(guest.getCccdNumber());
        binding.imgAddCccdFront.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_FRONT;
            pickImage.launch(presenter.prepareImageSelection());
        });
        binding.imgAddCccdBack.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_BACK;
            pickImage.launch(presenter.prepareImageSelection());
        });
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

    @Override
    public void showDeleteGuestDialog(Guest guest) {
        setUpDialog(R.layout.layout_dialog_delete_guest);

        TextView txtConfirmDeleteGuest = dialog.findViewById(R.id.txt_confirm_delete_guest);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_delete_guest);
        Button btnDeleteGuest = dialog.findViewById(R.id.btn_delete_guest);

        String text = " " + guest.getNameGuest() + " ?";
        txtConfirmDeleteGuest.append(text);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDeleteGuest.setOnClickListener(v -> {
            presenter.deleteGuest(guest);
            dialog.dismiss();
        });
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
        showToast("Chỉ có thể chỉnh sửa hoặc xóa MainGuest ở hợp đồng.");
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

        String text = " " + guest.getNameGuest() + " ?";
        txtConfirmDeleteGuest.append(text);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDeleteGuest.setOnClickListener(v -> {
            presenter.deleteGuest(guest);
            dialog.dismiss();
        });
    }

    private void openMenuForEachRoom(View view, Guest guest) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                setFieldsEnabled(true);
                return true;
            } else if (itemId == R.id.menu_delete_guest) {
                openDeleteGuestDialog(guest);
                return true;
            }
            return false;
        });

        popupMenu.inflate(R.menu.menu_edit_delete);
        popupMenu.show();
    }

}
