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
            for (int i = 0; i < getAdapter().getItemCount()/3 + getAdapter().getItemCount()%3 +1; i++) {
                ViewHolder holder = getAdapter().createViewHolder(this, getAdapter().getItemViewType(i));
                getAdapter().onBindViewHolder(holder, i);
                holder.itemView.measure(widthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                height += holder.itemView.getMeasuredHeight();
            }
            heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthSpec, heightSpec);
    }
}
