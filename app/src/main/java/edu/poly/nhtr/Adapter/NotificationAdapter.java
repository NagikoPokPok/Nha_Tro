package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ItemContainerNotificationBinding;
import edu.poly.nhtr.listeners.NotificationListener;
import edu.poly.nhtr.models.Index;
import edu.poly.nhtr.models.Notification;
import edu.poly.nhtr.presenters.NotificationPresenter;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    Context context;
    List<Notification> notificationList;
    NotificationPresenter notificationPresenter;
    NotificationListener notificationListener;
    private boolean isDeleteClicked = false;
    private boolean isSelectAllClicked = false;
    private boolean multiSelectMode = false;

    public List<Notification> getSelectedNotifications() {
        return selectedNotifications;
    }

    private final List<Notification> selectedNotifications = new ArrayList<>();

    public NotificationAdapter(Context context, List<Notification> notificationList, NotificationPresenter notificationPresenter, NotificationListener notificationListener) {
        this.context = context;
        this.notificationList = notificationList;
        this.notificationPresenter = notificationPresenter;
        this.notificationListener = notificationListener;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemContainerNotificationBinding binding = ItemContainerNotificationBinding.inflate(inflater, parent, false);
        return new NotificationAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {

        Notification notification = notificationList.get(position);

        holder.binding.txtNotificationHeader.setText(notification.getHeader());
        holder.binding.txtNotificationBody.setText(notification.getBody());

        if(notification.getRead())
        {
            holder.binding.mainLayout.setBackground(null);
            holder.binding.layoutNotification.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.colorPrimary));
            holder.binding.imgNotification.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.colorGray));
        }else{
            holder.binding.mainLayout.setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.background_item_notification));
            holder.binding.layoutNotification.setBackground(holder.itemView.getContext().getDrawable(R.drawable.background_date_time_index));
            holder.binding.imgNotification.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.colorPrimary));
        }

        Date date = notification.getDateObject();

        // Tính toán relative time
        String relativeTime = DateUtils.getRelativeTimeSpanString(
                date.getTime(), // Chuyển đổi Date thành milliseconds
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ).toString();

        // Hiển thị relative time
        holder.binding.timeGetNotificationUntilNow.setText(relativeTime);

        if(isDeleteClicked)
        {
            holder.binding.checkBox.setVisibility(View.VISIBLE);
        }else{
            holder.binding.checkBox.setVisibility(View.GONE);
        }

        if(isSelectAllClicked)
        {
            holder.binding.checkBox.setChecked(true);
            selectedNotifications.clear();
            selectedNotifications.addAll(notificationList);
        }else{
            holder.binding.checkBox.setChecked(false);
            selectedNotifications.clear();
        }

        holder.itemView.setOnClickListener(v -> {
            if (multiSelectMode) {
                if (holder.binding.checkBox.isChecked()) {
                    holder.binding.checkBox.setChecked(false);
                    selectedNotifications.remove(notification);
                } else {
                    holder.binding.checkBox.setChecked(true);
                    selectedNotifications.add(notification);
                }
            }else{
                notificationPresenter.updateNotificationIsRead(position,notification);
                notificationListener.onNotificationClicked(notificationList.get(holder.getAdapterPosition()));
            }
        });

        holder.binding.checkBox.setOnClickListener(v -> {
            if (holder.binding.checkBox.isChecked()) {
                selectedNotifications.add(notification);
            } else {
                selectedNotifications.remove(notification);
            }
        });




    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerNotificationBinding binding;

        public ViewHolder(@NonNull ItemContainerNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    public void setIndexList(List<Notification> notificationList) {
        this.notificationList = notificationList;
        if (this.notificationList.isEmpty()) {
            notificationListener.showLayoutNoData();
        } else {
            notificationListener.hideLayoutNoData();
            notifyDataSetChanged();
        }
        notificationListener.hideLoading();
    }

    public void isDeleteChecked(boolean isDeleteChecked)
    {
        this.isDeleteClicked = isDeleteChecked;
        this.multiSelectMode = isDeleteChecked;
        notifyDataSetChanged();
    }

    public void isSelectAllChecked(boolean isSelectAllClicked)
    {
        this.isSelectAllClicked = isSelectAllClicked;
        notifyDataSetChanged();
    }


    public void notificationIsRead(int position)
    {
        notifyItemChanged(position);
    }
}
