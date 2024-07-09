package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.appbar.AppBarLayout;

import org.checkerframework.checker.units.qual.N;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.Adapter.HomeArrayAdapter;
import edu.poly.nhtr.Adapter.NotificationAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentNotificationBinding;
import edu.poly.nhtr.listeners.NotificationListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Index;
import edu.poly.nhtr.models.Notification;
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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentNotificationBinding.inflate(getLayoutInflater());
        dialog = new Dialog(requireActivity());

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

       notificationPresenter.getListHomes(new NotificationPresenter.OnGetHomeListCompleteListener() {
           @Override
           public void onComplete(List<Home> homeList) {
               setHomeList(homeList);
               notificationPresenter.getNotification(new NotificationPresenter.OnSetNotificationListCompleteListener() {
                   @Override
                   public void onComplete() {


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

        MenuItem deleteNotificationsItem = menu.findItem(R.id.deleteNotifications);
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
                adapter.isDeleteChecked(false);
                binding.layoutSelectAll.setVisibility(View.GONE);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
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
            }else{
                notificationPresenter.deleteSelectedNotifications(selectedNotifications, new NotificationPresenter.OnSetNotificationListCompleteListener() {
                    @Override
                    public void onComplete() {
                        //showToast("Delete successfully");
                    }
                }, homeList);
            }



            // Logic cho hành động icon delete notifications
            return true;
        } else if (itemId == R.id.markAllNotificationsAreRead) {
            // Logic cho hành động mark all notifications as read
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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


}
