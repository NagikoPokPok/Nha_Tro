package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.Adapter.IndexAdapter;
import edu.poly.nhtr.Adapter.NotificationAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentNotificationBinding;
import edu.poly.nhtr.listeners.NotificationListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Notification;
import edu.poly.nhtr.presenters.IndexPresenter;
import edu.poly.nhtr.presenters.NotificationPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class NotificationFragment extends Fragment implements NotificationListener {
    private FragmentNotificationBinding binding;
    private Dialog dialog;
    private List<Notification> notificationList;
    private NotificationAdapter adapter;
    private NotificationPresenter notificationPresenter;
    private String homeID;
    private PreferenceManager preferenceManager;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        notificationPresenter = new NotificationPresenter(this, homeID);
        notificationList = new ArrayList<>();
        preferenceManager = new PreferenceManager(requireContext());

       notificationPresenter.getListHomes(new NotificationPresenter.OnGetHomeListCompleteListener() {
           @Override
           public void onComplete(List<Home> homeList) {
               notificationPresenter.getNotification(new NotificationPresenter.OnSetNotificationListCompleteListener() {
                   @Override
                   public void onComplete() {
                       setupRecyclerView();
                   }
               }, homeList);
           }
       });



        // Inflate the layout for this fragment
        return binding.getRoot();
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

        MenuItem menuItem = menu.findItem(R.id.Notification);
        TextView textView = (TextView) menuItem.getActionView();
        if (textView != null) {
            textView.setText("Lọc thông báo theo nhà trọ");
            textView.setTextSize(18); // optional: set text size
            textView.setTextColor(getResources().getColor(android.R.color.white));
            Typeface typeface = Typeface.create("preloaded_fonts", Typeface.BOLD); // Thay "sans-serif-medium" bằng font bạn muốn
            textView.setTypeface(typeface);// optional: set text color
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.deleteNotifications) {
            Toast.makeText(requireContext(), "New group", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.markAllNotificationsAreRead) {
            Toast.makeText(requireContext(), "New broadcast", Toast.LENGTH_SHORT).show();
        }

        return true;
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


}
