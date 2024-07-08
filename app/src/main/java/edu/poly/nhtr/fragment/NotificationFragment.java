package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentNotificationBinding;

public class NotificationFragment extends Fragment {
    private FragmentNotificationBinding binding;
    private Dialog dialog;

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

        // Inflate the layout for this fragment
        return binding.getRoot();
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
        if(id == R.id.deleteNotifications)
        {
            Toast.makeText(requireContext(), "New group", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.markAllNotificationsAreRead)
        {
            Toast.makeText(requireContext(), "New broadcast", Toast.LENGTH_SHORT).show();
        }

        return true;
    }
}
