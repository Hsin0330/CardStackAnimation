package com.example.hsinhung.testcardstackview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by HsinHung on 1/19/16.
 */
public class AdaptedRecyclerView extends RecyclerView {

    static final String TAG = AdaptedRecyclerView.class.getSimpleName();


    private ScrollPosition scrollPosition = new ScrollPosition();

    private int visibleHold = 5;
    private boolean isLoadEnabled = false;
    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private OnLoadListener onLoadListener;

    public AdaptedRecyclerView(Context context) {
        this(context, null, 0);
    }

    public AdaptedRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdaptedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        super.addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recycler, int dx, int dy) {
                View view = recycler.getChildCount() > 0 ? recycler.getChildAt(0) : null;
                LinearLayoutManager layoutManager = (LinearLayoutManager) recycler.getLayoutManager();
                if (view != null) {
                    int position = layoutManager.getPosition(view);
                    int offset = layoutManager.getOrientation() == VERTICAL ? view.getTop() : view.getLeft();
                    scrollPosition.update(position, offset);
//                    Log.d("Position", "position : " + position + "  firstVisible : " + layoutManager.findFirstVisibleItemPosition());

                    if (isLoadEnabled && layoutManager.getItemCount() < layoutManager.findLastVisibleItemPosition() + visibleHold) {
                        if (onLoadListener != null) {
                            setIsLoading(true);
                        }
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recycler, int newState) {
            }
        });
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        super.setAdapter(adapter);
        ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(scrollPosition.position, scrollPosition.offset);
    }

    @Override
    public void swapAdapter(RecyclerView.Adapter adapter, boolean removeAndRecycleExistingViews) {
        super.swapAdapter(adapter, removeAndRecycleExistingViews);
        ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(scrollPosition.position, scrollPosition.offset);
    }

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position);
        scrollPosition.update(position);
    }

    @Override
    public void smoothScrollToPosition(int position) {
        super.smoothScrollToPosition(position);
        scrollPosition.update(position);
    }

    public void setVisibleHold(int visibleHold) {
        this.visibleHold = visibleHold;
    }

    public void setLoadEnabled(boolean loadEnabled) {
        isLoadEnabled = loadEnabled;
    }

    public void setOnLoadListener(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    public void setIsLoading(boolean isLoading) {
        if (isLoadEnabled && this.isLoading.compareAndSet(!isLoading, isLoading)) {
            if (onLoadListener != null && isLoading) {
                onLoadListener.onLoad();
            }
        }
    }

    public void setScrollPosition(ScrollPosition stable) {
        super.scrollToPosition(stable.position);
        scrollPosition = stable;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            itemView.setOnClickListener(listener == null ? null : view -> listener.onItemClick(view, getAdapterPosition()));
        }

        public void setOnItemLongClickListener(OnItemLongClickListener listener) {
            itemView.setOnLongClickListener(listener == null ? null : view -> listener.onItemLongClick(view, getAdapterPosition()));
        }
    }

    private static abstract class BaseAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

        private final Context context;
        private final Fragment parentFragment;

        public BaseAdapter(Fragment fragment) {
            this(fragment.getActivity(), fragment);
        }

        public BaseAdapter(Context context) {
            this(context, null);
        }

        BaseAdapter(Context context, Fragment fragment) {
            this.context = context;
            parentFragment = fragment;
        }

        public View inflateView(ViewGroup parent, int resource) {
            return inflateView(parent.getContext(), parent, resource);
        }

        public View inflateView(Context context, ViewGroup parent, int resource) {
            return LayoutInflater.from(context).inflate(resource, parent, false);
        }

        public String getString(int resId) {
            return context.getString(resId);
        }

        public String getString(int resId, Object... formatArgs) {
            return context.getString(resId, formatArgs);
        }

        @SuppressWarnings("unchecked")
        public <S extends Fragment> S getParentFragment() {
            return (S) parentFragment;
        }

        public Context getContext() {
            return context;
        }

        public Resources getResources() {
            return context.getResources();
        }
    }

    public static abstract class Adapter<T, VH extends ViewHolder> extends BaseAdapter<VH> {

        private final List<T> objects;

        public Adapter(Fragment fragment, List<T> objects) {
            this(fragment.getActivity(), fragment, objects);
        }

        public Adapter(Context context, List<T> objects) {
            this(context, null, objects);
        }

        Adapter(Context context, Fragment fragment, List<T> objects) {
            super(context, fragment);
            this.objects = objects;
        }

        @Override
        public int getItemCount() {
            return objects.size();
        }

        public int getPosition(T object) {
            return objects.indexOf(object);
        }

        public void add(int position, T object) {
            objects.add(position, object);
            notifyItemInserted(position);
            notifyItemRangeChanged(position + 1, objects.size() - position - 1);
        }

        public void add(T object) {
            int lastIndex = objects.size();
            objects.add(object);
            notifyItemInserted(lastIndex);
        }

        public void addAll(int position, Collection<? extends T> objects) {
            if (this.objects.addAll(position, objects)) notifyItemRangeInserted(position, objects.size());
        }

        public void addAll(Collection<? extends T> objects) {
            int lastIndex = this.objects.size();
            if (this.objects.addAll(objects)) notifyItemRangeInserted(lastIndex, objects.size());
        }

        public void remove(int position) {
            if (objects.remove(position) != null) notifyItemRemoved(position);
        }

        public void removeList(int start, int end) {
            List sublist = objects.subList(start, end);
            if (sublist != null) {
                sublist.clear();
                notifyItemRangeRemoved(start, end - start);
//                notifyDataSetChanged();
            }
        }

        public void remove(T object) {
            int index = objects.indexOf(object);
            if (index >= 0) remove(index);
        }

        public void clear() {
            objects.clear();
            notifyDataSetChanged();
        }

        public boolean isEmpty() {
            return objects.isEmpty();
        }

        public void sort(Comparator<? super T> comparator) {
            Collections.sort(objects, comparator);
//            notifyDataSetChanged();
        }

        public T getItem(int position) {
            return objects.get(position);
        }

        public List<T> getItems() {
            return objects;
        }
    }

    public static abstract class ExpandableAdapter<S, T, GVH extends RecyclerView.ViewHolder, VH extends RecyclerView.ViewHolder>
            extends BaseAdapter<RecyclerView.ViewHolder> {

        private static final int CHILD_VIEW_TYPE_OFFSET = 1024;

        public static final int GROUP_TYPE = 0;
        public static final int CHILD_TYPE = 1;

        private List<S> groups;
        private List<List<T>> objectsByGroup;
        private List<Integer> headerPositions = new ArrayList<>();

        public ExpandableAdapter(Fragment fragment, List<S> groups, List<List<T>> objectsByGroup) {
            this(fragment.getActivity(), fragment, groups, objectsByGroup);
        }

        public ExpandableAdapter(Context context, List<S> groups, List<List<T>> objectsByGroup) {
            this(context, null, groups, objectsByGroup);
        }

        ExpandableAdapter(Context context, Fragment fragment, List<S> groups, List<List<T>> objectsByGroup) {
            super(context, fragment);
            this.groups = groups;
            this.objectsByGroup = objectsByGroup;
            indexGroupPosition();

            registerAdapterDataObserver(new AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    indexGroupPosition();
                }
            });
        }

        private void indexGroupPosition() {
            headerPositions.clear();
            for (int group = 0, index = 0, size = groups.size(); group < size; group++) {
                headerPositions.add(index);
                index += objectsByGroup.get(group).size() + 1;
            }
        }

        public void addGroup(S group) {
            addGroup(group, new ArrayList<>());
        }

        public void addGroup(S group, List<T> objects) {
            if (! groups.contains(group)) {
                groups.add(group);
                objectsByGroup.add(objects);
                notifyDataSetChanged();
            }
        }

        public void add(int groupPosition, T object) {
            for (int group = 0, size = groups.size(), min = 0, max; group < size; group++, min = max) {
                max = min + objectsByGroup.get(group).size() + 1;
                if (group == groupPosition) {
                    objectsByGroup.get(groupPosition).add(object);
                    notifyItemInserted(max + 1);
                    break;
                }
            }
        }

        public void addAll(int groupPosition, List<T> objects) {
            for (int group = 0, size = groups.size(), min = 0, max; group < size; group++, min = max) {
                max = min + objectsByGroup.get(group).size() + 1;
                if (group == groupPosition) {
                    objectsByGroup.get(groupPosition).addAll(objects);
                    notifyItemRangeInserted(max, objects.size());
                    break;
                }
            }
        }

        public void clear() {
            groups.clear();
            objectsByGroup.clear();
            notifyDataSetChanged();
        }

        public boolean isGroup(int position) {
            return headerPositions.contains(position);
        }

        public S getGroup(int position) {
            return groups.get(position);
        }

        public int getGroupCount() {
            return groups.size();
        }

        public T getChild(int groupPosition, int position) {
            return objectsByGroup.get(groupPosition).get(position);
        }

        public int getChildrenCount(int position) {
            return objectsByGroup.get(position).size();
        }

        public abstract GVH onCreateGroupViewHolder(ViewGroup parent, int viewType);
        public abstract void onBindGroupViewHolder(GVH holder, int position);
        public int getGroupViewType(int position) { return GROUP_TYPE; }

        public abstract VH onCreateChildViewHolder(ViewGroup parent, int viewType);
        public abstract void onBindChildViewHolder(VH holder, int groupPosition, int position);
        public int getChildViewType(int position) { return CHILD_TYPE; }

        @Override
        public int getItemCount() {
            int count = groups.size();
            for (List<T> objects : objectsByGroup) count += objects.size();
            return count;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return viewType >= CHILD_VIEW_TYPE_OFFSET ?
                    onCreateChildViewHolder(parent, viewType - CHILD_VIEW_TYPE_OFFSET) :
                    onCreateGroupViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            for (int group = 0, size = groups.size(), min = 0, max; group < size; group++, min = max) {
                max = min + objectsByGroup.get(group).size() + 1;

                if (position == min) {
                    onBindGroupViewHolder((GVH) holder, group);
                    break;
                } else if (position > min && position < max) {
                    onBindChildViewHolder((VH) holder, group, position - min - 1);
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            int viewType = 0;
            for (int group = 0, size = groups.size(), min = 0, max; group < size; group++, min = max) {
                max = min + objectsByGroup.get(group).size() + 1;

                if (position == min) {
                    viewType = getGroupViewType(group);
                    break;
                } else if (position > min && position < max) {
                    viewType = CHILD_VIEW_TYPE_OFFSET + getChildViewType(position - min - 1);
                    break;
                }
            }
            return viewType;
        }

    }

    public static abstract class ScrollDetector extends OnScrollListener implements ScrollDirectionListener {

        public static final int DIRECTION_UP = 0;
        public static final int DIRECTION_DOWN = 1;

        @Override
        public void onScrolled(RecyclerView recycler, int dx, int dy) {
            if (Math.abs(dy) > 5) onScroll(recycler, dy > 0 ? DIRECTION_UP : DIRECTION_DOWN);
        }

    }

    public interface ScrollDirectionListener {
        void onScroll(RecyclerView recycler, int direction);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    public interface OnLoadListener {
        void onLoad();
    }

    public class ScrollPosition {

        public int position;
        public int offset;

        public ScrollPosition() {
            this.position = 0;
            this.offset = 0;
        }

        public ScrollPosition(int position, int offset) {
            this.position = position;
            this.offset = offset;
        }

        public void update(ScrollPosition stable) {
            this.position = stable.position;
            this.offset = stable.offset;
        }

        public void update(int position) {
            this.position = position;
        }

        public void update(int position, int offset) {
            this.position = position;
            this.offset = offset;
        }

        @Override
        public String toString() {
            return "{ position : " + position + " , offset : " + offset + " }";
        }


    }
}
