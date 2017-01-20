package com.example.hsinhung.testcardstackview.collapse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.hsinhung.testcardstackview.AdaptedRecyclerView;

/**
 * Created by HsinHung on 16/9/21.
 */
public class CollapseRecyclerView extends AdaptedRecyclerView {

    private boolean isScrollable;
    private boolean isCollapse;

    private int showPosition;

    private ScrollLinearLayoutManager scrollLinearLayoutManager;

    public CollapseRecyclerView(Context context) {
        this(context, null, 0);
    }

    public CollapseRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollapseRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayoutManager(scrollLinearLayoutManager = new ScrollLinearLayoutManager(getContext()));
    }

    public boolean isScrollable() {
        return isScrollable;
    }

    public void setScrollable(boolean scrollable) {
        isScrollable = scrollable;
        ((ScrollLinearLayoutManager) getLayoutManager()).setScrollEnabled(scrollable);
    }

    public boolean isCollapse() {
        return isCollapse;
    }

    public void collapse(int position) {
        isCollapse = !isCollapse;
        int collapseTop = (int) (getHeight() * 0.9);
        int collapseSpacing = (int) (getHeight() * 0.1);
        int collapseDevide = collapseSpacing / (getChildCount() - 1);

        if (!isCollapse) {
            for(int i = 0; i < getChildCount(); i ++) {
                View view = getChildAt(i);
                view.animate().translationY(0).start();
            }
        } else {
            boolean draw = false;
            for(int i = 0; i < getChildCount(); i ++) {
                View view = getChildAt(i);

                if (i == position) {
                    showPosition = i;
                    view.animate().translationY(-view.getTop()).start();
                    draw = true;
                    continue;
                }

                int afterDraw = draw ? i - 1 : i;
                view.animate().translationY(collapseTop - view.getTop() + afterDraw * collapseDevide).start();
            }
        }
    }

    public void dragShowCard(int distance) {
        if (!isCollapse) {
            return;
        }

        View view = getChildAt(showPosition);
        view.animate().translationY(distance - view.getTop()).start();
    }

    public static class CollapseDecoration extends RecyclerView.ItemDecoration {

        private Context context;
        private int cardHeight;
        private int showHeight;

        public CollapseDecoration(Context context, int cardHeight, int showHeight) {
            this.context = context;
            this.cardHeight = cardHeight;
            this.showHeight = showHeight;
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            int childPosition = parent.getChildAdapterPosition(view);
            int totalCount = parent.getAdapter().getItemCount();

            if (childPosition == totalCount - 1) {
                outRect.set(0, 0, 0, 0);
            } else {
                outRect.set(0, 0, 0, showHeight - cardHeight);
            }

        }
    }

    public static class CollapseViewHolder extends ViewHolder {

        private OnDragClickListener onDragClickListener;

        public CollapseViewHolder(View view) {
            super(view);
        }

        public void setOnTouchListener(OnDragClickListener onDragClickListener) {
            itemView.setOnTouchListener(new OnTouchListener() {

                private int previousY;

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (!isDragEnable()) {
                        return false;
                    }

                    int y = (int) motionEvent.getY();

                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            previousY = y;
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            view.animate().translationY(y - previousY).start();
                            return true;
                        case MotionEvent.ACTION_UP:

                            return true;
                    }

                    return false;
                }
            });
        }

        public boolean isDragEnable() {
            return false;
        }
    }

    public interface OnDragClickListener {
        void onTouchDrag(View view, int distance);
    }
}
