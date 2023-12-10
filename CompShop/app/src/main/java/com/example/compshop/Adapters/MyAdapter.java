package com.example.compshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Interface.OnMoveToItemsListener;
import com.example.compshop.Models.Order;
import com.example.compshop.Models.category;
import com.example.compshop.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context context;
    private List<category> dataList;

    OnMoveToItemsListener onMoveToItemsListener;

    public MyAdapter(Context context, List<category> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    public void setOnMoveToItemsListener(OnMoveToItemsListener onMoveToItemsListener) {
        this.onMoveToItemsListener = onMoveToItemsListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardView;
        ImageView imageView;
        TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView4);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && onMoveToItemsListener != null) {
                category order = dataList.get(position);
                onMoveToItemsListener.onMoveToDets(order);
            }
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.categoryview, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        category data = dataList.get(position);

        // Bind data to views
        holder.textView.setText(data.getCategoryName());
        String imagePath = data.getImage();
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                Picasso.get().load(data.getImage()).into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.mipmap.ic_launcher);
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}

