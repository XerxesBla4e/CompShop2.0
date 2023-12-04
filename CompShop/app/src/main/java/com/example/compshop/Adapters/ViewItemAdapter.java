package com.example.compshop.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Models.Item;
import com.example.compshop.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class ViewItemAdapter extends RecyclerView.Adapter<ViewItemAdapter.ViewHolder> {

    private Context context;
    private List<Item> itemList; // Updated to Item

    public ViewItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_item_my, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position); // Updated to Item
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemImage;
        private TextView itemName;
        private TextView itemDescription;
        private TextView price;
        private TextView newPrice;
        private TextView discountPercentage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.img_item); // Updated to img_food
            itemName = itemView.findViewById(R.id.food_name); // Updated to food_name
            itemDescription = itemView.findViewById(R.id.descp);
            price = itemView.findViewById(R.id.prices);
            newPrice = itemView.findViewById(R.id.new_price); // Updated to new_price
            discountPercentage = itemView.findViewById(R.id.discount_percentage);
        }

        public void bind(Item item) {
            itemName.setText(item.getName());
            itemDescription.setText(item.getDescription());
            price.setText(String.format("Price: %s", item.getPrice()));

            if (item.getDiscount() != null && !item.getDiscount().isEmpty() && item.getDiscountdescription() != null && !item.getDiscountdescription().isEmpty()) {
                int discount = Integer.parseInt(item.getDiscount());
                if (discount > 0 && item.getDiscountdescription().contains("%")) {
                    double newPriceValue = Double.parseDouble(item.getPrice()) * (1 - discount / 100.0);
                    newPrice.setVisibility(View.VISIBLE);
                    newPrice.setText(String.format(Locale.getDefault(), "Price: Shs %.2f", newPriceValue));
                    discountPercentage.setVisibility(View.VISIBLE);
                    discountPercentage.setText(item.getDiscountdescription());
                    // Add crossline through old price
                    price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    // Remove crossline if discount condition is not met
                    price.setPaintFlags(price.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    newPrice.setVisibility(View.GONE);
                    discountPercentage.setVisibility(View.GONE);
                }
            } else {
                // Remove crossline and clear new price if discount conditions are not met
                price.setPaintFlags(price.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                newPrice.setVisibility(View.GONE);
            }

            String imagePath = item.getImage(); // Updated to getImage
            try {
                if (imagePath != null && !imagePath.isEmpty()) {
                    Picasso.get().load(item.getImage()).into(itemImage); // Updated to getImage
                } else {
                    itemImage.setImageResource(R.mipmap.ic_launcher);
                }
            } catch (Exception e) {
                e.printStackTrace();
                itemImage.setImageResource(R.mipmap.ic_launcher);
            }
        }
    }
}
