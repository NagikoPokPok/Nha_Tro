package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;
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

        Date date = notification.getDateObject();

        // Tính toán relative time
        String relativeTime = DateUtils.getRelativeTimeSpanString(
                date.getTime(), // Chuyển đổi Date thành milliseconds
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ).toString();

        // Hiển thị relative time
        holder.binding.timeGetNotificationUntilNow.setText(relativeTime);


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
            //indexInterface.showLayoutNoData();
        } else {
            //indexInterface.hideLayoutNoData();
            notifyDataSetChanged();
        }
        //indexInterface.hideLoading();
    }
}
