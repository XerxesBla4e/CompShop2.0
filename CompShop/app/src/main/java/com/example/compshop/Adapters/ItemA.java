package com.example.compshop.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Interface.OnQuantityChangeListener;
import com.example.compshop.Models.Item;
import com.example.compshop.R;
import com.squareup.picasso.Picasso;

public class ItemA extends ListAdapter<Item, RecyclerView.ViewHolder> {
    int quantity;
    private OnQuantityChangeListener quantityChangeListener;
    private static final int VIEW_TYPE_NORMAL = 1;

    public ItemA() {
        super(CALLBACK);
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.quantityChangeListener = listener;
    }

    private static final DiffUtil.ItemCallback<Item> CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getName().equals(newItem.getName())
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.my_cart_row, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            Item item = getItem(position);
            itemViewHolder.bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public Item getItem(int position) {
        return super.getItem(position);
    }

    public void clearCart() {
        submitList(null);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textViewName;
        private TextView textViewDescription;
        private TextView textViewPrice;
        private ImageButton imageButtonAdd;
        private ImageButton imageButtonRemove;
        private TextView textViewQuantity;
        private ImageView cartItemImage;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            cartItemImage = itemView.findViewById(R.id.imageView7);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            imageButtonAdd = itemView.findViewById(R.id.imageButtonAdd);
            imageButtonRemove = itemView.findViewById(R.id.imageButtonRemove);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);

            imageButtonAdd.setOnClickListener(this);
            imageButtonRemove.setOnClickListener(this);
        }

        public void bind(Item item) {
            textViewName.setText(item.getName());
            textViewDescription.setText(item.getDescription());
            textViewPrice.setText(String.valueOf(item.getTotal()));
            textViewQuantity.setText(String.valueOf(item.getQuantity()));
            String ImagePath = item.getImage();
            try {
                if (ImagePath != null && !ImagePath.isEmpty()) {
                    Picasso.get().load(item.getImage()).into(cartItemImage);
                } else {
                    cartItemImage.setImageResource(R.mipmap.ic_launcher);
                }
            } catch (Exception e) {
                e.printStackTrace();
                cartItemImage.setImageResource(R.mipmap.ic_launcher);
            }

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Item item = getItem(position);
                if (quantityChangeListener != null) {
                    if (view.getId() == R.id.imageButtonAdd) {
                        quantityChangeListener.onAddButtonClick(item, position);
                    } else if (view.getId() == R.id.imageButtonRemove) {
                        quantityChangeListener.onRemoveButtonClick(item, position);
                    }
                }
            }
        }
    }
}
