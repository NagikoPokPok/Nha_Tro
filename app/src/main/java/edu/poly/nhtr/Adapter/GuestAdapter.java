package edu.poly.nhtr.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ItemContainerGuestBinding;
import edu.poly.nhtr.interfaces.RoomGuestInterface;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.models.MainGuest;

public class GuestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> itemList; // Danh sách khách thuê phòng gồm MainGuest và Guest
    private static RoomGuestInterface.View view;
    boolean[] isVisible;
    private boolean isEnabled = false;
    private boolean isSelectAll = false;
    private List<Guest> selectList = new ArrayList<>();

    public GuestAdapter(List<Object> itemList, RoomGuestInterface.View view) {
        this.itemList = itemList;
        GuestAdapter.view = view;
        isVisible = new boolean[itemList.size()];
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof MainGuest) {
            return 1; // MainGuest
        } else {
            return 0; // Guest
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerGuestBinding binding = ItemContainerGuestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        if (viewType == 1) {
            return new MainGuestViewHolder(binding);
        } else {
            return new GuestViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = itemList.get(position);
        if (holder instanceof GuestViewHolder) {
            ((GuestViewHolder) holder).bind((Guest) item);
        } else if (holder instanceof MainGuestViewHolder) {
            ((MainGuestViewHolder) holder).bind((MainGuest) item);
        }

        CheckBox checkBoxSelectAll = view.getRootView().findViewById(R.id.checkbox_select_all);

        checkBoxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                for (int i = 0; i < getItemCount(); i++) {
                    if (!(itemList.get(i) instanceof MainGuest)) { // Exclude MainGuest
                        isVisible[i] = true;
                        if (itemList.get(i) instanceof Guest) {
                            selectList.add((Guest) itemList.get(i));
                        }
                    }
                }
                notifyDataSetChanged();
            } else {
                Arrays.fill(isVisible, false);
                selectList.clear();
                notifyDataSetChanged();
            }
        });

        view.deleteListAll(selectList);

        if (holder instanceof GuestViewHolder) {
            holder.itemView.setOnLongClickListener(v -> {
                isEnabled = true;
                view.setDeleteAllUI();
                clickItem(holder);
                toggleAllCheckboxes(true);
                notifyDataSetChanged();
                return false;
            });
        }

        performCheckBoxes(holder, position);
    }

    public void performCheckBoxes(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GuestViewHolder) {
            GuestViewHolder guestHolder = (GuestViewHolder) holder;
            guestHolder.binding.ivCheckBox.setOnClickListener(v -> {
                if (guestHolder.binding.ivCheckBox.isChecked()) {
                    selectList.add((Guest) itemList.get(holder.getAdapterPosition()));
                } else {
                    selectList.remove(itemList.get(holder.getAdapterPosition()));
                }
            });

            holder.itemView.setOnClickListener(v -> {
                if (isEnabled) {
                    clickItem(holder);
                } else {
                    view.onGuestClick((Guest) itemList.get(holder.getAdapterPosition()));
                }
            });

            if (isVisible[position]) {
                guestHolder.binding.frmImage.setVisibility(View.INVISIBLE);
                guestHolder.binding.ivCheckBox.setVisibility(View.VISIBLE);
                guestHolder.binding.ivCheckBox.setChecked(selectList.contains(itemList.get(position)));
            } else {
                guestHolder.binding.ivCheckBox.setChecked(false);
                guestHolder.binding.ivCheckBox.setVisibility(View.GONE);
                guestHolder.binding.frmImage.setVisibility(View.VISIBLE);
            }
        }
    }

    public void cancelDeleteAll() {
        if (itemList.size() == 0) {
            view.cancelDeleteAll();
            view.noGuestData();
        } else {
            isEnabled = false;
            isSelectAll = false;
            selectList.clear();
            toggleAllCheckboxes(false);
            notifyDataSetChanged();
            view.cancelDeleteAll();
        }
    }

    public void clickItem(RecyclerView.ViewHolder holder) {
        Guest guest = (Guest) itemList.get(holder.getAdapterPosition());
        if (!((GuestViewHolder) holder).binding.ivCheckBox.isChecked()) {
            ((GuestViewHolder) holder).binding.ivCheckBox.setChecked(true);
            if (!selectList.contains(guest)) {
                selectList.add(guest);
            }
        } else {
            ((GuestViewHolder) holder).binding.ivCheckBox.setChecked(false);
            selectList.remove(guest);
        }
    }

    // Hàm chọn hết check box trừ Main Guest
    private void toggleAllCheckboxes(boolean show) {
        for (int i = 0; i < isVisible.length; i++) {
            if (!((itemList.get(i)) instanceof MainGuest))
                isVisible[i] = show;
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setGuestList(List<Object> guests) {
        List<Object> sortedList = new ArrayList<>();
        List<MainGuest> mainGuests = new ArrayList<>();
        List<Guest> regularGuests = new ArrayList<>();

        for (Object guest : guests) {
            if (guest instanceof MainGuest) {
                mainGuests.add((MainGuest) guest);
            } else if (guest instanceof Guest) {
                regularGuests.add((Guest) guest);
            }
        }

        sortedList.addAll(mainGuests);
        sortedList.addAll(regularGuests);

        this.itemList = sortedList;
        this.isVisible = new boolean[itemList.size()];
        notifyDataSetChanged();
    }

    public int getGuestPosition(Guest guest) {
        int position = itemList.indexOf(guest);
        return position != -1 ? position + 1 : -1; // Trả về vị trí cộng thêm 1 để khớp với ordinal number
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

            binding.txtOrdinalNumber.setText(String.valueOf(getAdapterPosition() + 1));
            binding.imgStar.setVisibility(View.GONE);
            binding.txtOrdinalNumber.setVisibility(View.VISIBLE);

            int color = isProfileComplete ? ContextCompat.getColor(binding.txtProfileStatus.getContext(), R.color.colorGreen)
                    : ContextCompat.getColor(binding.txtProfileStatus.getContext(), R.color.colorRed);
            binding.txtProfileStatus.setTextColor(color);

            binding.imgMenu.setOnClickListener(v -> {
                binding.frmImage2.setVisibility(View.VISIBLE);
                binding.frmImage.setVisibility(View.GONE);
                view.openPopup(v, guest, binding);
            });
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
            binding.txtEntryDate.setText(mainGuest.getDateIn());

            boolean isProfileComplete = mainGuest.isFileStatus();
            binding.txtProfileStatus.setText(isProfileComplete ? "Đã cập nhật đầy đủ" : "Chưa cập nhật đầy đủ");


            int color = isProfileComplete ? ContextCompat.getColor(binding.txtProfileStatus.getContext(), R.color.colorGreen)
                    : ContextCompat.getColor(binding.txtProfileStatus.getContext(), R.color.colorRed);
            binding.txtProfileStatus.setTextColor(color);
            binding.layoutContractOwner.setVisibility(View.VISIBLE);
            binding.imgStar.setVisibility(View.VISIBLE);
            binding.txtOrdinalNumber.setVisibility(View.GONE);

            binding.imgMenu.setOnClickListener(v -> {
                binding.frmImage2.setVisibility(View.VISIBLE);
                binding.frmImage.setVisibility(View.GONE);
                view.openPopupMainGuest(v, mainGuest, binding);
            });
        }
    }
}
