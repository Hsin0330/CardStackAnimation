package com.example.hsinhung.testcardstackview.collapse;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.example.hsinhung.testcardstackview.AdaptedRecyclerView;
import com.example.hsinhung.testcardstackview.FlipAnimation;
import com.example.hsinhung.testcardstackview.R;

/**
 * Created by HsinHung on 16/9/22.
 */
public class ItemTouchCollapseHelper extends RecyclerView.ItemDecoration {

    private Context context;
    private int cardHeight;
    private int showHeight;

    private AdaptedRecyclerView collapseRecyclerView;
    private View childView;

    private GestureDetectorCompat gestureDetector;

    private boolean isEffectiveDown;
    private boolean isCollapse;
    private boolean isFlip;
    private boolean isFling;

    private int showPosition = -1;
    private int distanceY;
    private int slop;

    private OnItemClickListener onItemClickListener;
    private OnDragListener onDragListener;

    public ItemTouchCollapseHelper(Context context, int cardHeight, int showHeight) {
        this.context = context;
        this.cardHeight = cardHeight;
        this.showHeight = showHeight;
    }

    public void attachToRecyclerView(CollapseRecyclerView collapseRecyclerView) {
        if (this.collapseRecyclerView == collapseRecyclerView) {
            return; // nothing to do
        }
        if (this.collapseRecyclerView != null) {
            destroyCallbacks();
        }
        this.collapseRecyclerView = collapseRecyclerView;
        if (this.collapseRecyclerView != null) {
            setupCallbacks();
        }
    }

    private void setupCallbacks() {
        ViewConfiguration vc = ViewConfiguration.get(collapseRecyclerView.getContext());
        slop = vc.getScaledTouchSlop();
        collapseRecyclerView.addItemDecoration(this);
        collapseRecyclerView.addOnItemTouchListener(onItemTouchListener);
        initGestureDetector();
    }

    private void destroyCallbacks() {
        collapseRecyclerView.removeItemDecoration(this);
        collapseRecyclerView.removeOnItemTouchListener(onItemTouchListener);
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

    private final RecyclerView.OnItemTouchListener onItemTouchListener = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            float tranX = 0;
            if (isCollapse && childView != null) {
                tranX = ViewCompat.getTranslationX(childView);
            }

            childView = collapseRecyclerView.findChildViewUnder(tranX + e.getX(), e.getY());
            gestureDetector.onTouchEvent(e);

            if (childView != null && e.getAction() == MotionEvent.ACTION_UP && isEffectiveDown && isCollapse && !isFling) {
                if (Math.abs(distanceY) > (childView.getHeight() * 0.5)) {
                    revertCollapse(childView);
                } else {
                    resumeCollapse(childView);
                }
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    };

    private void initGestureDetector() {
        if (gestureDetector != null) {
            return;
        }
        gestureDetector = new GestureDetectorCompat(collapseRecyclerView.getContext(),
                new ItemTouchHelperGestureListener());
    }

    private class ItemTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            isEffectiveDown = !(showPosition != -1 && showPosition != collapseRecyclerView.indexOfChild(childView));
            return !isEffectiveDown;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!isEffectiveDown) {
                return super.onSingleTapConfirmed(e);
            }

            if (childView != null) {
                if (!isCollapse) {
                    collapse(collapseRecyclerView.indexOfChild(childView));
                } else {
                    flip(collapseRecyclerView.indexOfChild(childView));
                }
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(childView, collapseRecyclerView.getChildAdapterPosition(childView));
                }
                return true;
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX, float dY) {
            if (!isEffectiveDown || !isCollapse) {
                return super.onScroll(e1, e2, dX, dY);
            }

            if (childView != null) {
                if ((distanceY += dY) > 0) {
                    distanceY = 0;
                }

                int translationY = -distanceY - childView.getTop();
                childView.setTranslationY(translationY);
                if (onDragListener != null) {
                    onDragListener.onDrag(childView, distanceY);
                }
                return true;
            }
            return super.onScroll(e1, e2, dX, dY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!isEffectiveDown || !isCollapse) {
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            if (childView != null) {
                if (velocityY < 5000 && distanceY > childView.getHeight() * 0.5) {
                    resumeCollapse(childView);
                }
                if (velocityY > 5000) {
                    revertCollapse(childView);
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public void collapse(int position) {
        isCollapse = !isCollapse;
        distanceY = 0;

        int parentHeight = collapseRecyclerView.getHeight();
        int childCount = collapseRecyclerView.getChildCount();

        int collapseTop = (int) (parentHeight * 0.9);
        int collapseSpacing = (int) (parentHeight * 0.1);
        int collapseDevide = collapseSpacing / (childCount - 1);

        trySetScrollEnable();

        if (!isCollapse) {
            for (int i = 0; i < childCount; i++) {
                View view = collapseRecyclerView.getChildAt(i);
                view.animate().translationY(0).start();

                showPosition = -1;
            }
        } else {
            boolean draw = false;
            for (int i = 0; i < childCount; i++) {
                View view = collapseRecyclerView.getChildAt(i);

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

    public void flip(int position) {
        if (!isCollapse) {
            return;
        }

        childView.animate().cancel();
        if (isFlip) {
//            childView.animate().rotationY(0f).start();
//            runReverseFlipAnimation();
        } else {
//            runFlipAnimation();
        }

        isFlip = !isFlip;
    }

    private void runFlipAnimation(View frontView, View backView) {
        float centerX = childView.getWidth() / 2.0f;
        float centerY = childView.getHeight() / 2.0f;

        FlipAnimation flipAnimation = new FlipAnimation(0, 90, centerX, centerY, 640, true);

        flipAnimation.setDuration(600);
        flipAnimation.setInterpolator(new AccelerateInterpolator());
        flipAnimation.setFillAfter(true);
        flipAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                frontView.setVisibility(View.GONE);
                backView.setVisibility(View.VISIBLE);

                //从270到360度，顺时针旋转视图，此时reverse参数为false，达到360度动画结束时视图变得可见
                FlipAnimation rotateAnimation = new FlipAnimation(270, 360, centerX, centerY, 640, false);
                rotateAnimation.setDuration(600);
                rotateAnimation.setFillAfter(true);
                rotateAnimation.setInterpolator(new DecelerateInterpolator());
                backView.startAnimation(rotateAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        frontView.startAnimation(flipAnimation);
    }

    private void revertCollapse(View childView) {
        isFling = true;

        int parentHeight = collapseRecyclerView.getHeight();
        int childCount = collapseRecyclerView.getChildCount();

        int collapseTop = (int) (parentHeight * 0.9);
        int collapseSpacing = (int) (parentHeight * 0.1);
        int collapseDevide = collapseSpacing / (childCount - 1);

        childView.animate().setInterpolator(new DecelerateInterpolator(1.0f)).
                translationY(collapseTop - childView.getTop() + collapseRecyclerView.indexOfChild(childView) * collapseDevide).
                withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        isFling = false;
                        collapse(showPosition);
                    }
                }).start();
    }

    private void resumeCollapse(View childView) {
        distanceY = 0;
        childView.animate().setInterpolator(new DecelerateInterpolator(1.0f)).
                translationY(-childView.getTop()).
                withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        isFling = false;
                    }
                }).start();
    }

    private void trySetScrollEnable() {
        ((ScrollLinearLayoutManager) collapseRecyclerView.getLayoutManager()).setScrollEnabled(!isCollapse);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnDragListener {
        void onDrag(View view, int dy);
    }

    public boolean isCollapse() {
        return isCollapse;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnDragListener(OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
    }
}
