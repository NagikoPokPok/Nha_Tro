package edu.poly.nhtr.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ItemContainerGuestBinding;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.models.MainGuest;

public class GuestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> itemList; // List of either Guest or MainGuest

    public GuestAdapter(List<Object> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerGuestBinding binding = ItemContainerGuestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GuestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = itemList.get(position);
        if (holder instanceof GuestViewHolder) {
            ((GuestViewHolder) holder).bind((Guest) item);
        } else if (holder instanceof MainGuestViewHolder) {
            ((MainGuestViewHolder) holder).bind((MainGuest) item);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setGuestList(List<Object> guests) {
        // Sort the list to ensure MainGuest appears first
        List<Object> sortedList = new ArrayList<>();
        List<MainGuest> mainGuests = new ArrayList<>();
        List<Guest> regularGuests = new ArrayList<>();

        // Separate MainGuests and RegularGuests
        for (Object guest : guests) {
            if (guest instanceof MainGuest) {
                mainGuests.add((MainGuest) guest);
            } else if (guest instanceof Guest) {
                regularGuests.add((Guest) guest);
            }
        }

        // Add MainGuests first
        sortedList.addAll(mainGuests);
        sortedList.addAll(regularGuests);

        this.itemList = sortedList;
        notifyDataSetChanged();
    }

    public static class GuestViewHolder extends RecyclerView.ViewHolder {
        ItemContainerGuestBinding binding;

        GuestViewHolder(@NonNull ItemContainerGuestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Guest guest) {
            binding.txtNameGuest.setText(guest.getNameGuest());
            binding.txtPhoneNumber.setText(guest.getPhoneGuest());
            binding.txtEntryDate.setText(guest.getDateIn());

            boolean isProfileComplete = guest.isFileStatus();
            binding.txtProfileStatus.setText(isProfileComplete ? "Đã cập nhật đầy đủ" : "Chưa cập nhật đầy đủ");

            int color = isProfileComplete ? ContextCompat.getColor(binding.txtProfileStatus.getContext(), R.color.greenText)
                    : ContextCompat.getColor(binding.txtProfileStatus.getContext(), R.color.redText);
            binding.txtProfileStatus.setTextColor(color);
        }
    }

    public static class MainGuestViewHolder extends RecyclerView.ViewHolder {
        ItemContainerGuestBinding binding;

        MainGuestViewHolder(@NonNull ItemContainerGuestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MainGuest mainGuest) {
            binding.txtNameGuest.setText(mainGuest.getNameGuest());
            binding.txtPhoneNumber.setText(mainGuest.getPhoneGuest());
            binding.txtEntryDate.setText(mainGuest.getCreateDate());

            boolean isProfileComplete = mainGuest.isFileStatus();
            binding.txtProfileStatus.setText(isProfileComplete ? "Đã cập nhật đầy đủ" : "Chưa cập nhật đầy đủ");

            int color = isProfileComplete ? ContextCompat.getColor(binding.txtProfileStatus.getContext(), R.color.greenText)
                    : ContextCompat.getColor(binding.txtProfileStatus.getContext(), R.color.redText);
            binding.txtProfileStatus.setTextColor(color);

            // Additional fields specific to MainGuest can be set here
        }
    }
}
