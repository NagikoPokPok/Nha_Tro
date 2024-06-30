package edu.poly.nhtr.Class;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CustomRecyclerView extends RecyclerView {
    private boolean isScrollEnabled = true;
    public CustomRecyclerView(@NonNull Context context) {
        super(context);
    }

    public CustomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return isScrollEnabled && super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return isScrollEnabled && super.onTouchEvent(e);
    }

    public void setScrollEnabled(boolean enabled) {
        isScrollEnabled = enabled;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (!isScrollEnabled) {
            int height = 0;
            RecyclerView.Adapter adapter = getAdapter();
            int itemCount = adapter.getItemCount();

            int spanCount = 3;  // Số cột
            int rowCount = (int) Math.ceil((double) itemCount / spanCount);  // Số hàng

            for (int i = 0; i < rowCount; i++) {
                int maxHeightInRow = 0;
                for (int j = 0; j < spanCount; j++) {
                    int position = i * spanCount + j;
                    if (position < itemCount) {
                        ViewHolder holder = adapter.createViewHolder(this, adapter.getItemViewType(position));
                        adapter.onBindViewHolder(holder, position);
                        holder.itemView.measure(widthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                        int itemHeight = holder.itemView.getMeasuredHeight();
                        if (itemHeight > maxHeightInRow) {
                            maxHeightInRow = itemHeight;
                        }
                    }
                }
                height += maxHeightInRow;
            }

            heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthSpec, heightSpec);
    }
}
