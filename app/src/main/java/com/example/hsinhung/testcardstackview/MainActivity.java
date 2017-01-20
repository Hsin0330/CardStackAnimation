package com.example.hsinhung.testcardstackview;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hsinhung.testcardstackview.collapse.CollapseRecyclerView;
import com.example.hsinhung.testcardstackview.collapse.ItemTouchCollapseHelper;
import com.example.hsinhung.testcardstackview.collapse.ScrollLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CollapseRecyclerView recyclerView;
    private AdaptedRecyclerViewAdapter adaptedRecyclerViewAdapter;
    private ScrollLinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (CollapseRecyclerView) findViewById(R.id.main_recyclerview);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(linearLayoutManager = new ScrollLinearLayoutManager(this));
//        recyclerView.addItemDecoration(new CollapseRecyclerView.CollapseDecoration(this, getResources().getDimensionPixelOffset(R.dimen.card_height), getResources().getDimensionPixelOffset(R.dimen.card_header_height)));
        ItemTouchCollapseHelper itemTouchCollapseHelper = new ItemTouchCollapseHelper(this, getResources().getDimensionPixelOffset(R.dimen.card_height), getResources().getDimensionPixelOffset(R.dimen.card_header_height));
        itemTouchCollapseHelper.attachToRecyclerView(recyclerView);

//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
//            @Override
//            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
//                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
//                return makeMovementFlags(dragFlags, swipeFlags);
//            }
//
//            @Override
//            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                Collections.swap(adaptedRecyclerViewAdapter.getItems(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
//                adaptedRecyclerViewAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
//                return true;
//            }
//
//            @Override
//            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//                adaptedRecyclerViewAdapter.remove(viewHolder.getAdapterPosition());
//                adaptedRecyclerViewAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
//            }
//        });
//        itemTouchHelper.attachToRecyclerView(recyclerView);


        List<Integer> integers = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            integers.add(i);
        }

        recyclerView.setAdapter(adaptedRecyclerViewAdapter = new AdaptedRecyclerViewAdapter(this, integers));
    }


    public static class AdaptedRecyclerViewAdapter extends AdaptedRecyclerView.Adapter<Integer, BaseViewHolder> {

        private Context context;

        private AdaptedRecyclerView.OnItemClickListener collapseClickListener;
        private CollapseRecyclerView.OnDragClickListener collapseOnTouchDragListner;

        private AdaptedRecyclerView.OnItemClickListener onClickListener = new AdaptedRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (collapseClickListener != null) {
                    collapseClickListener.onItemClick(view, position);
                }
            }
        };

        private CollapseRecyclerView.OnDragClickListener onTouchDragListener = new CollapseRecyclerView.OnDragClickListener() {
            @Override
            public void onTouchDrag(View view, int distance) {
                if (collapseOnTouchDragListner != null) {
                    collapseOnTouchDragListner.onTouchDrag(view, distance);
                }
            }
        };

        public AdaptedRecyclerViewAdapter(Context context, List<Integer> integerList) {
            super(context, integerList);
            this.context = context;
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            BaseViewHolder viewHolder = new BaseViewHolder(LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false));
            viewHolder.setOnItemClickListener(onClickListener);
            viewHolder.setOnTouchListener(onTouchDragListener);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {

        }

        public void setCollapseClickListener(AdaptedRecyclerView.OnItemClickListener collapseClickListener) {
            this.collapseClickListener = collapseClickListener;
        }

        public void setCollapseOnTouchDragListner(CollapseRecyclerView.OnDragClickListener collapseOnTouchDragListner) {
            this.collapseOnTouchDragListner = collapseOnTouchDragListner;
        }
    }

    public static class BaseViewHolder extends CollapseRecyclerView.CollapseViewHolder {

        View frontView;
        View backView;


        public BaseViewHolder(View itemView) {
            super(itemView);

            frontView = itemView.findViewById(R.id.list_item);
            backView = itemView.findViewById(R.id.list_item_background);
        }

        @Override
        public boolean isDragEnable() {
            return false;
        }
    }
}
