package edu.poly.nhtr.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ItemContainerGuestBinding;
import edu.poly.nhtr.models.MainGuest;

public class MainGuestAdapter extends RecyclerView.Adapter<MainGuestAdapter.MainGuestViewHolder> {

    private final List<MainGuest> mainGuestList;

    public MainGuestAdapter(List<MainGuest> mainGuestList) {
        this.mainGuestList = mainGuestList;
    }

    @NonNull
    @Override
    public MainGuestAdapter.MainGuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerGuestBinding itemContainerGuestBinding = ItemContainerGuestBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new MainGuestViewHolder(itemContainerGuestBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MainGuestAdapter.MainGuestViewHolder holder, int position) {
        holder.setMainGuestData(mainGuestList.get(position));

    }

    @Override
    public int getItemCount() {
        return mainGuestList.size();
    }

    public static class MainGuestViewHolder extends RecyclerView.ViewHolder {
        ItemContainerGuestBinding binding;

        MainGuestViewHolder(@NonNull ItemContainerGuestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setMainGuestData(MainGuest mainGuest) {
            binding.txtNameGuest.setText(mainGuest.getNameGuest());
            binding.txtPhoneNumber.setText(mainGuest.getPhoneGuest());
            binding.txtEntryDate.setText(mainGuest.getDateIn());

            boolean isProfileComplete = mainGuest.getFileStatus();
            binding.txtProfileStatus.setText(mainGuest.getFileStatus() ? "Đã cập nhật đầy đủ" : "Chưa cập nhật đầy đủ");

            int color = isProfileComplete ? ContextCompat.getColor(binding.txtProfileStatus.getContext(), R.color.greenText)
                    : ContextCompat.getColor(binding.txtProfileStatus.getContext(), R.color.redText);
            binding.txtProfileStatus.setTextColor(color);

        }
    }
}