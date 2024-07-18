package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.appbar.AppBarLayout;

import org.checkerframework.checker.units.qual.N;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.Activity.MainActivity;
import edu.poly.nhtr.Activity.MainDetailedRoomActivity;
import edu.poly.nhtr.Activity.MainRoomActivity;
import edu.poly.nhtr.Adapter.HomeArrayAdapter;
import edu.poly.nhtr.Adapter.NotificationAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentNotificationBinding;
import edu.poly.nhtr.databinding.LayoutDialogDeleteHomeSuccessBinding;
import edu.poly.nhtr.databinding.LayoutDialogDeleteIndexBinding;
import edu.poly.nhtr.listeners.NotificationListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Index;
import edu.poly.nhtr.models.Notification;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.NotificationPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class NotificationFragment extends Fragment implements NotificationListener {
    private FragmentNotificationBinding binding;
    private Dialog dialog;
    private List<Notification> notificationList;
    private List<Home> homeList;
    private NotificationAdapter adapter;
    private NotificationPresenter notificationPresenter;
    private String homeID;
    private PreferenceManager preferenceManager;
    private boolean isSelectAllChecked = false;
    private MainActivity mainActivity;
    private OnNotificationReadListener listener;
    private MenuItem deleteNotificationsItem;

    public interface OnNotificationReadListener {
        void onNotificationsRead();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentNotificationBinding.inflate(getLayoutInflater());
        dialog = new Dialog(requireActivity());

        // Kiểm tra nếu activity có implement interface OnNotificationReadListener
        if (getActivity() instanceof OnNotificationReadListener) {
            listener = (OnNotificationReadListener) getActivity();
        }

        // Set toolbar
        Toolbar toolbar = binding.toolbar;
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    public List<Home> getHomeList() {
        return homeList;
    }

    public void setHomeList(List<Home> homeList) {
        this.homeList = homeList;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        notificationPresenter = new NotificationPresenter(this, homeID);
        notificationList = new ArrayList<>();
        preferenceManager = new PreferenceManager(requireContext());
        adapter = new NotificationAdapter(requireActivity(), new ArrayList<>(), notificationPresenter, this);
        homeList = new ArrayList<>();
        mainActivity = new MainActivity();

        notificationPresenter.getListHomes(new NotificationPresenter.OnGetHomeListCompleteListener() {
            @Override
            public void onComplete(List<Home> homeList) {
                setHomeList(homeList);
                notificationPresenter.getNotification(new NotificationPresenter.OnSetNotificationListCompleteListener() {
                    @Override
                    public void onComplete() {

                        // Gửi broadcast để cập nhật badge
                        Intent intent = new Intent("edu.poly.nhtr.ACTION_UPDATE_BADGE");
                        requireActivity().sendBroadcast(intent);

                        setupDropDownMenu(homeList);
                        setupRecyclerView();
                        setupDeleteNotifications();
                    }
                }, homeList);
            }
        });


        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void setupDeleteNotifications() {
        binding.checkboxSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelectAllChecked = !isSelectAllChecked;
                adapter.isSelectAllChecked(isSelectAllChecked);
            }
        });
    }

    private void setupDropDownMenu(List<Home> homeList) {
        HomeArrayAdapter arrayAdapter = new HomeArrayAdapter(requireContext(), homeList);
        binding.autoCompleteTxt.setAdapter(arrayAdapter);

        binding.autoCompleteTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Home home = (Home) parent.getItemAtPosition(position);
                if (home != null) {
                    notificationPresenter.getNotificationByHome(home);
                }
            }
        });
    }


    private void setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new NotificationAdapter(requireActivity(), new ArrayList<>(), notificationPresenter, this);
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_notification_toolbar, menu);

        deleteNotificationsItem = menu.findItem(R.id.deleteNotifications); // Lưu tham chiếu
        TextView textView = (TextView) deleteNotificationsItem.getActionView();
        if (textView != null) {
            textView.setText("Xoá nhà trọ");
            textView.setTextSize(18); // optional: set text size
            textView.setTextColor(getResources().getColor(android.R.color.white));
            Typeface typeface = Typeface.create("sans-serif-medium", Typeface.BOLD); // Thay "sans-serif-medium" bằng font bạn muốn
            textView.setTypeface(typeface); // optional: set text color
        }

        MenuItem deleteIconItem = menu.findItem(R.id.icon_delete_notifications);
        boolean showDelete = false;
        deleteIconItem.setVisible(showDelete);
        deleteNotificationsItem.setVisible(!showDelete);

        // Set up listener to detect when the action view is expanded or collapsed
        deleteNotificationsItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                // Khi mở rộng, ẩn nút icon_delete_notifications
                deleteIconItem.setVisible(true);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                // Khi thu gọn, hiện nút icon_delete_notifications
                deleteIconItem.setVisible(false);
                handleCollapseAction();
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void handleCollapseAction() {
        adapter.isDeleteChecked(false);
        binding.layoutSelectAll.setVisibility(View.GONE);
    }

    @Override
    public void closeLayoutDeleteNotification() {
        // Gửi broadcast để cập nhật badge
        Intent intent = new Intent("edu.poly.nhtr.ACTION_UPDATE_BADGE");
        requireActivity().sendBroadcast(intent);
        handleCollapseAction();
        if (deleteNotificationsItem != null) {
            deleteNotificationsItem.collapseActionView(); // Thu gọn action view
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Menu menu = binding.toolbar.getMenu();

        if (itemId == R.id.deleteNotifications) {
            // Expand the action view
            item.expandActionView();
            adapter.isDeleteChecked(true);
            binding.layoutSelectAll.setVisibility(View.VISIBLE);

            return true;
        } else if (itemId == R.id.icon_delete_notifications) {
            List<Notification> selectedNotifications = adapter.getSelectedNotifications();

            if (selectedNotifications.isEmpty()) {
                showToast("Không có thông báo nào được chọn");
            } else {
                openDialogConfirmDelete(selectedNotifications);

            }

            // Logic cho hành động icon delete notifications
            return true;
        } else if (itemId == R.id.markAllNotificationsAreRead) {
            notificationPresenter.getNotificationList(getInfoUserFromGoogleAccount(), new NotificationPresenter.OnReturnNotificationListCompleteListener() {
                @Override
                public void onComplete(List<Notification> notificationList) {
                    notificationPresenter.updateListNotificationIsRead(notificationList, homeList, new NotificationPresenter.OnSetNotificationListCompleteListener() {
                        @Override
                        public void onComplete() {
                            // Gửi broadcast để cập nhật badge
                            Intent intent = new Intent("edu.poly.nhtr.ACTION_UPDATE_BADGE");
                            requireActivity().sendBroadcast(intent);
                        }
                    });
                }
            });
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void openDialogConfirmDelete(List<Notification> selectedNotifications) {
        LayoutDialogDeleteIndexBinding binding = LayoutDialogDeleteIndexBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());


        binding.txtConfirmDelete.setText("Bạn chắc chắn muốn xóa các thông báo này? ");

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        binding.btnConfirmDeleteIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationPresenter.deleteSelectedNotifications(selectedNotifications, new NotificationPresenter.OnSetNotificationListCompleteListener() {
                    @Override
                    public void onComplete() {

                    }
                }, homeList);
            }
        });

        setUpDialogConfirmation();
    }


    private void setUpDialogConfirmation() {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);
        }
        dialog.setCancelable(true);
        dialog.show();
    }


    @Override
    public void setNotificationList(List<Notification> notificationList) {
        this.notificationList = notificationList;
        if (adapter != null) {
            adapter.setIndexList(notificationList);
        }
    }

    @Override
    public String getInfoUserFromGoogleAccount() {
        // Lấy thông tin người dùng từ tài khoản Google
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        String currentUserId = "";
        if (account != null) {
            currentUserId = account.getId();
        } else {
            currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        }
        return currentUserId;
    }

    @Override
    public boolean isAdded2() {
        return isAdded();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLayoutNoData() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.layoutNoData.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLayoutNoData() {
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.layoutNoData.setVisibility(View.GONE);
    }

    @Override
    public void setNotificationIsRead(int position) {
        adapter.notificationIsRead(position);
    }

    @Override
    public void closeDialog() {
        dialog.dismiss();
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutNoData.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDialogActionSuccess(String message) {
        LayoutDialogDeleteHomeSuccessBinding binding = LayoutDialogDeleteHomeSuccessBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        binding.txtDeleteHomeSuccess.setText(message);

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        setUpDialogConfirmation();
    }


    @Override
    public void returnNotificationList(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @Override
    public void onNotificationClicked(Notification notification) {
        notificationPresenter.getHomeByNotification(notification, new NotificationPresenter.OnGetHomeIDByNotificationListener() {
            @Override
            public void onComplete(List<Home> homeList, List<Room> roomList) {
                Home home = homeList.get(0);
                if (roomList.isEmpty()) {
                    Intent intent = new Intent(requireContext(), MainRoomActivity.class);
                    intent.putExtra("FRAGMENT_TO_LOAD", "IndexFragment");
                    intent.putExtra("home", home);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(requireContext());
                    stackBuilder.addParentStack(MainRoomActivity.class);
                    stackBuilder.addNextIntent(intent);

                    stackBuilder.startActivities();
                } else {
                    Room room = roomList.get(0);
                    Intent intentMainRoom = new Intent(requireContext(), MainRoomActivity.class);
                    intentMainRoom.putExtra("FRAGMENT_TO_LOAD", "HomeFragment");
                    intentMainRoom.putExtra("home", home);

                    Intent intentDetailedRoom = new Intent(requireContext(), MainDetailedRoomActivity.class);
                    intentDetailedRoom.putExtra("target_fragment_index", 2);
                    intentDetailedRoom.putExtra("room", room);
                    intentDetailedRoom.putExtra("home", home);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(requireContext());
                    stackBuilder.addParentStack(MainDetailedRoomActivity.class);
                    stackBuilder.addNextIntent(intentMainRoom);
                    stackBuilder.addNextIntent(intentDetailedRoom);

                    stackBuilder.startActivities();
                }
            }
        });
    }


    @Override
    public void showButtonLoading(int id) {
        dialog.findViewById(id).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideButtonLoading(int id) {
        dialog.findViewById(id).setVisibility(View.VISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
    }


}
