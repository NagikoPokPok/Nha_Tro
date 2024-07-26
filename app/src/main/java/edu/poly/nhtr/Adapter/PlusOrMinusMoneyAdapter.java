package edu.poly.nhtr.Adapter;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.databinding.ItemPlusOrMinusMoneyBinding;
import edu.poly.nhtr.models.PlusOrMinusMoney;

public class PlusOrMinusMoneyAdapter extends RecyclerView.Adapter<PlusOrMinusMoneyAdapter.ViewHolder> {
    private final List<PlusOrMinusMoney> plusOrMinusMoneyList;
    private final OnItemValueChangeListener callback;
    private final Handler handler = new Handler();
    private Runnable runnable;

    public List<PlusOrMinusMoney> getPlusOrMinusMoneyList(){
        return plusOrMinusMoneyList;
    }

    public interface OnItemValueChangeListener {
        void onItemValueChange();
    }

    public PlusOrMinusMoneyAdapter(List<PlusOrMinusMoney> plusOrMinusMoneyList, OnItemValueChangeListener callback) {
        this.plusOrMinusMoneyList = plusOrMinusMoneyList;
        this.callback = callback;
    }

    @NonNull
    @Override
    public PlusOrMinusMoneyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlusOrMinusMoneyBinding binding = ItemPlusOrMinusMoneyBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlusOrMinusMoneyAdapter.ViewHolder holder, int position) {
        PlusOrMinusMoney plusOrMinusMoney = plusOrMinusMoneyList.get(position);

        holder.binding.txtTitle.setText(plusOrMinusMoney.getTitle());


        holder.binding.imgExit.setOnClickListener(v -> {
            plusOrMinusMoneyList.remove(holder.getAdapterPosition());
            notifyDataSetChanged();
            if (callback != null) {
                callback.onItemValueChange();
            }
        });
        holder.binding.edtMoney.setText(String.valueOf(plusOrMinusMoney.getMoney()));
        holder.binding.edtMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        int position = holder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            try {
                                int newValue = Integer.parseInt(s.toString());
                                plusOrMinusMoneyList.get(position).setMoney(newValue);
                                if (callback != null) {
                                    callback.onItemValueChange();
                                }
                            } catch (NumberFormatException e) {
                                // Xử lý lỗi nếu giá trị không phải là số
                                plusOrMinusMoneyList.get(position).setMoney(0); // Hoặc giá trị mặc định nào đó
                                if (callback != null) {
                                    callback.onItemValueChange();
                                }
                            }
                        }
                    }
                };
                handler.postDelayed(runnable, 1000); // Chờ 1 giây sau khi người dùng ngừng nhập
            }
        });
        holder.binding.edtMoney.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && Objects.requireNonNull(holder.binding.edtMoney.getText()).toString().equals("0"))
                    holder.binding.edtMoney.setText("");
                if(!hasFocus && Objects.requireNonNull(holder.binding.edtMoney.getText()).toString().isEmpty())
                    holder.binding.edtMoney.setText("0");
            }
        });

        holder.binding.edtReason.setText(plusOrMinusMoney.getReason());
        holder.binding.edtReason.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        int position = holder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            try {
                                String newValue = s.toString();
                                plusOrMinusMoneyList.get(position).setReason(newValue);
                            } catch (NumberFormatException e) {
                                // Xử lý lỗi nếu giá trị không phải là số
                                plusOrMinusMoneyList.get(position).setReason(""); // Hoặc giá trị mặc định nào đó
                            }
                        }
                    }
                };
                handler.postDelayed(runnable, 1000); // Chờ 1 giây sau khi người dùng ngừng nhập
            }
        });
    }

    @Override
    public int getItemCount() {
        if (plusOrMinusMoneyList == null || plusOrMinusMoneyList.isEmpty()) return 0;
        return plusOrMinusMoneyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ItemPlusOrMinusMoneyBinding binding;
        public ViewHolder(@NonNull ItemPlusOrMinusMoneyBinding itemBinding) {
            super(itemBinding.getRoot());
            binding = itemBinding;
        }

    }

    public void addPlusOrMinusMoney(Boolean isPlus){
        String title = "Số tiền cần ";
        if (isPlus) title += "cộng";
        else title +="trừ";
        PlusOrMinusMoney plusOrMinusMoney = new PlusOrMinusMoney(title, isPlus, getItemCount());
        plusOrMinusMoneyList.add(plusOrMinusMoney);
        notifyDataSetChanged();
    }

    public int getTotalMoney(){
        int totalMoney = 0;
        for (PlusOrMinusMoney item : plusOrMinusMoneyList){
            if (item.getPlus())
                totalMoney += item.getMoney();
            else
                totalMoney -= item.getMoney();
        }
        return totalMoney;
    }

}
