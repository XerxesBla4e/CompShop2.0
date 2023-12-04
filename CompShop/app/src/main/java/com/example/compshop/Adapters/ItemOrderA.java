package com.example.compshop.Adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Models.ItemOrder;
import com.example.compshop.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ItemOrderA extends RecyclerView.Adapter<ItemOrderA.ItemOrderViewHolder> {
    private Context context;
    private List<ItemOrder> itemOrderList; // Update type

    public ItemOrderA(Context context, List<ItemOrder> itemOrderList) {
        this.context = context;
        this.itemOrderList = itemOrderList;
    }

    public void setItemOrderList(List<ItemOrder> itemOrderList) {
        this.itemOrderList = itemOrderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.orderlist1, parent, false);
        return new ItemOrderViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ItemOrderViewHolder holder, int position) {
        ItemOrder itemOrder = itemOrderList.get(position);

        holder.textViewName.setText(itemOrder.getItemName());
        holder.textViewDescription.setText(itemOrder.getItemDescription());
        holder.textViewPrice.setText(String.format("Price: $%s", itemOrder.getItemTotal()));
        holder.textViewQuantity.setText(String.format("Quantity: %d", itemOrder.getItemQuantity()));

        String imagePath = itemOrder.getItemImage();
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                Picasso.get().load(itemOrder.getItemImage()).into(holder.imageView);
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
        return itemOrderList.size();
    }

    public static class ItemOrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName;
        TextView textViewDescription;
        TextView textViewPrice;
        TextView textViewQuantity;

        public ItemOrderViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView7);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
        }
    }
}
