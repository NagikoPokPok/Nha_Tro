package edu.poly.nhtr.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.poly.nhtr.R;
import edu.poly.nhtr.models.MainGuest;

public class MainGuestAdapter extends RecyclerView.Adapter<MainGuestAdapter.MainGuestViewHolder> {

    private List<MainGuest> mainGuests;

    public MainGuestAdapter(List<MainGuest> mainGuests) {
        this.mainGuests = mainGuests;
    }

    @NonNull
    @Override
    public MainGuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_guest, parent, false);
        return new MainGuestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MainGuestViewHolder holder, int position) {
        MainGuest mainGuest = mainGuests.get(position);

        // Bind data to views in your MaterialCardView
        holder.txtNameGuest.setText(mainGuest.getNameGuest());
        holder.txtPhoneNumber.setText(mainGuest.getPhoneGuest());
        holder.txtProfileStatus.setText(mainGuest.getFileStatus() ? "Đã cập nhật đầy đủ" : "Chưa cập nhật đầy đủ");
        holder.txtEntryDate.setText(mainGuest.getCreateDate());

        // Set other views as needed
        // Example: holder.txtEntryDate.setText(mainGuest.getEntryDate());
    }

    @Override
    public int getItemCount() {
        return mainGuests.size();
    }

    public static class MainGuestViewHolder extends RecyclerView.ViewHolder {
        TextView txtNameGuest, txtPhoneNumber, txtProfileStatus, txtEntryDate;

        public MainGuestViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNameGuest = itemView.findViewById(R.id.txt_name_guest);
            txtPhoneNumber = itemView.findViewById(R.id.txt_phone_number);
            txtProfileStatus = itemView.findViewById(R.id.txt_profile_status);
            txtEntryDate = itemView.findViewById(R.id.txt_entry_date);

        }
    }
}

