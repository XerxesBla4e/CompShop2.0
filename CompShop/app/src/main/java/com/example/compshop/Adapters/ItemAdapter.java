package com.example.compshop.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Interface.ActionType;
import com.example.compshop.Interface.OnItemClickListener;
import com.example.compshop.Models.Item;
import com.example.compshop.R;
import com.example.compshop.databinding.ActivityHomeDetailsBinding;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ItemAdapter extends ListAdapter<Item, ItemAdapter.ItemViewHolder> {
    private final Context context;
    private OnItemClickListener onItemClickListener;

    public ItemAdapter(Context context) {
        super(CALLBACK);
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<Item> CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return Objects.equals(oldItem.getItem_Id(), newItem.getItem_Id());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getCategory().equals(newItem.getCategory())
                    && oldItem.getDescription().equals(newItem.getDescription())
                    && oldItem.getPrice().equals(newItem.getPrice())
                    && oldItem.getDiscount().equals(newItem.getDiscount())
                    && oldItem.getDiscountdescription().equals(newItem.getDiscountdescription())
                    && oldItem.getImage().equals(newItem.getImage())
                    && oldItem.getQuantity() == newItem.getQuantity()
                    && oldItem.getTotal() == newItem.getTotal();
        }
    };

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ActivityHomeDetailsBinding binding = ActivityHomeDetailsBinding.inflate(inflater, parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = getItem(position);
        holder.bind(item);
    }

    public void updateItemList(List<Item> itemList) {
        submitList(itemList);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ActivityHomeDetailsBinding binding;

        public ItemViewHolder(ActivityHomeDetailsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.imageButton2.setOnClickListener(this);
            binding.fabAdd.setOnClickListener(this);
        }

        public void bind(Item item) {
            if (item.isFavorite()) {
                // Item is a favorite, set red color
                binding.imageButton2.setColorFilter(ContextCompat.getColor(context, R.color.colorRed));
            } else {
                // Item is not a favorite, set default color
                binding.imageButton2.setColorFilter(ContextCompat.getColor(context, R.color.defaultHeartColor));
            }

            binding.itemName.setText(item.getName());
            binding.itemPrice.setText(String.format("Price: Shs %s", item.getPrice()));
            if (item.getDiscount() != null && !item.getDiscount().isEmpty() && item.getDiscountdescription() != null && !item.getDiscountdescription().isEmpty()) {
                int discount = Integer.parseInt(item.getDiscount());
                if (discount > 0 && item.getDiscountdescription().contains("%")) {
                    double newPrice = Double.parseDouble(item.getPrice()) * (1 - discount / 100.0);
                    binding.newPrice.setVisibility(View.VISIBLE);
                    binding.newPrice.setText(String.format(Locale.getDefault(), "Price: Shs %.2f", newPrice));
                    // Add crossline through old price
                    binding.itemPrice.setPaintFlags(binding.itemPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    // Remove crossline if discount condition is not met
                    binding.itemPrice.setPaintFlags(binding.itemPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    binding.newPrice.setVisibility(View.GONE);
                }
            } else {
                // Remove crossline and clear new price if discount conditions are not met
                binding.itemPrice.setPaintFlags(binding.itemPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.newPrice.setVisibility(View.GONE);
            }
            String imagePath = item.getImage();
            try {
                if (imagePath != null && !imagePath.isEmpty()) {
                    Picasso.get().load(item.getImage()).into(binding.movieImg22);
                } else {
                    binding.movieImg22.setImageResource(R.mipmap.ic_launcher);
                }
            } catch (Exception e) {
                e.printStackTrace();
                binding.movieImg22.setImageResource(R.mipmap.ic_launcher);
            }
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && onItemClickListener != null) {
                Item item = getItem(pos);
                ActionType actionType = view.getId() == R.id.fabAdd
                        ? ActionType.ADD_TO_CART
                        : ActionType.ADD_TO_FAVORITES;

                onItemClickListener.onItemClick(item, pos, actionType);
            }
        }

    }
}
