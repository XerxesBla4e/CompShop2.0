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

import com.example.compshop.Models.Item;
import com.example.compshop.Models.ItemOrder;
import com.example.compshop.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ItemOrderAdapter extends RecyclerView.Adapter<ItemOrderAdapter.ItemOrderViewHolder> {
    private Context context;
    private List<ItemOrder> itemModelList;

    public ItemOrderAdapter(Context context, List<ItemOrder> itemModelList) {
        this.context = context;
        this.itemModelList = itemModelList;
    }

    public void setItemModelList(List<ItemOrder> itemList) {
        this.itemModelList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.orderlist, parent, false);
        return new ItemOrderViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ItemOrderViewHolder holder, int position) {
        ItemOrder itemModel = itemModelList.get(position);

        holder.textViewName.setText(itemModel.getItemName());
        holder.textViewDescription.setText(itemModel.getItemDescription());
        holder.textViewPrice.setText(String.format("Price: Shs:%s", itemModel.getItemTotal()));
        holder.textViewQuantity.setText(String.format("Quantity: %d", itemModel.getItemQuantity()));

        String imagePath = itemModel.getItemImage();
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                Picasso.get().load(itemModel.getItemImage()).into(holder.imageView);
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
        return itemModelList.size();
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
