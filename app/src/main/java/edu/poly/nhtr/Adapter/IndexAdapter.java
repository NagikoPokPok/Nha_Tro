package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.poly.nhtr.databinding.LayoutItemRowIndexBinding;
import edu.poly.nhtr.interfaces.IndexInterface;
import edu.poly.nhtr.models.Index;
import edu.poly.nhtr.presenters.IndexPresenter;

public class IndexAdapter extends RecyclerView.Adapter<IndexAdapter.ViewHolder> {

    Context context;
    List<Index> index_list;
    private boolean isNextClicked = false;
    private boolean isDeleteClicked = false;
    private boolean isCheckBoxClicked = false;
    private boolean multiSelectMode = false;
    private List<Index> selectedIndexes = new ArrayList<>();

    private final IndexInterface indexInterface;
    private IndexPresenter indexPresenter;

    public IndexAdapter(Context context, List<Index> index_list, IndexInterface indexInterface, IndexPresenter indexPresenter) {
        this.context = context;
        this.index_list = index_list;
        this.indexInterface = indexInterface;
        this.indexPresenter = indexPresenter;
    }

    public void setNextClicked(boolean nextClicked) {
        isNextClicked = nextClicked;
        notifyDataSetChanged();
    }

    public void setMultiSelectMode(boolean multiSelectMode) {
        this.multiSelectMode = multiSelectMode;
        notifyDataSetChanged();
    }

    public List<Index> getSelectedIndexes() {
        return selectedIndexes;
    }

    @NonNull
    @Override
    public IndexAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LayoutItemRowIndexBinding binding = LayoutItemRowIndexBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }



    @Override
    public void onBindViewHolder(@NonNull IndexAdapter.ViewHolder holder, int position) {
        Index indexModel = index_list.get(position);

        holder.binding.txtNameRoomIndex.setText(indexModel.getNameRoom());

        // Kiểm tra dữ liệu và cập nhật biểu tượng
        indexPresenter.checkIndexes(indexModel.getIndexID(), indexModel.getMonth(), indexModel.getYear(), (isElectricityNewFilled, isWaterNewFilled) -> {
            // Kiểm tra lại vị trí của ViewHolder để tránh cập nhật nhầm
            if (holder.getAdapterPosition() == position) {
                if (isElectricityNewFilled && isWaterNewFilled) {
                    holder.binding.imgIsFilled.setVisibility(View.VISIBLE);
                    holder.binding.imgIsNotFilled.setVisibility(View.GONE);
                } else {
                    holder.binding.imgIsNotFilled.setVisibility(View.VISIBLE);
                    holder.binding.imgIsFilled.setVisibility(View.GONE);
                }
            }
        });

        if (!isNextClicked) {
            holder.binding.txtElectricityIndexOld.setVisibility(View.VISIBLE);
            holder.binding.txtElectricityIndexNew.setVisibility(View.VISIBLE);
            holder.binding.txtWaterIndexOld.setVisibility(View.GONE);
            holder.binding.txtWaterIndexNew.setVisibility(View.GONE);

            holder.binding.txtElectricityIndexOld.setText(indexModel.getElectricityIndexOld());
            holder.binding.txtElectricityIndexNew.setText(String.valueOf(indexModel.getElectricityIndexNew()));
        } else {
            holder.binding.txtElectricityIndexOld.setVisibility(View.GONE);
            holder.binding.txtElectricityIndexNew.setVisibility(View.GONE);
            holder.binding.txtWaterIndexOld.setVisibility(View.VISIBLE);
            holder.binding.txtWaterIndexNew.setVisibility(View.VISIBLE);

            holder.binding.txtWaterIndexOld.setText(String.valueOf(indexModel.getWaterIndexOld()));
            holder.binding.txtWaterIndexNew.setText(String.valueOf(indexModel.getWaterIndexNew()));
        }

        if (isDeleteClicked) {
            holder.binding.btnDeleteIndex.setVisibility(View.GONE);
            holder.binding.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.binding.btnDeleteIndex.setVisibility(View.VISIBLE);
            holder.binding.checkBox.setVisibility(View.GONE);
        }

        if (isCheckBoxClicked) {
            holder.binding.checkBox.setChecked(true);
            selectedIndexes.clear();
            selectedIndexes.addAll(index_list);
        } else {
            holder.binding.checkBox.setChecked(false);
            selectedIndexes.clear();
        }

        // Đặt sự kiện click cho nút chỉnh sửa
        holder.binding.btnEditIndex.setOnClickListener(v -> indexInterface.showDialogDetailedIndex(indexModel));
        holder.binding.btnDeleteIndex.setOnClickListener(v -> indexInterface.showDialogConfirmDeleteIndex(indexModel));

        holder.binding.imgIsFilled.setOnClickListener(v -> indexInterface.showDialogNoteIndexStatus());
        holder.binding.imgIsNotFilled.setOnClickListener(v -> indexInterface.showDialogNoteIndexStatus());

        //Delete many indexes
        holder.itemView.setOnClickListener(v -> {
            if (multiSelectMode) {
                if (holder.binding.checkBox.isChecked()) {
                    holder.binding.checkBox.setChecked(false);
                    selectedIndexes.remove(indexModel);
                } else {
                    holder.binding.checkBox.setChecked(true);
                    selectedIndexes.add(indexModel);
                }
            }
        });

        holder.binding.checkBox.setOnClickListener(v -> {
            if (holder.binding.checkBox.isChecked()) {
                selectedIndexes.add(indexModel);
            } else {
                selectedIndexes.remove(indexModel);
            }
        });


    }

    @Override
    public int getItemCount() {
        return index_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LayoutItemRowIndexBinding binding;

        public ViewHolder(@NonNull LayoutItemRowIndexBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    public void isDeleteClicked(boolean isClicked) {
        isDeleteClicked = isClicked;
        notifyDataSetChanged();
    }

    public void isCheckBoxClicked(boolean isClicked) {
        isCheckBoxClicked = isClicked;
        notifyDataSetChanged();
    }

    public void setIndexList(List<Index> indexList) {
        this.index_list = indexList;
        if (this.index_list.isEmpty()) {
            indexInterface.showLayoutNoData();
        } else {
            indexInterface.hideLayoutNoData();
            notifyDataSetChanged();
        }
        indexInterface.hideLoading();
    }
}
