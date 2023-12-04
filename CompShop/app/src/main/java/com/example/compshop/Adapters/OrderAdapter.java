package com.example.compshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.compshop.Interface.OnMoveToDetsListener;
import com.example.compshop.Models.Order;
import com.example.compshop.R;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private Context context;

    OnMoveToDetsListener onMoveToDetsListener;

    public void setOnMoveToDetsListener(OnMoveToDetsListener listener){
        this.onMoveToDetsListener = listener;
    }
    public OrderAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_order_item, parent, false);
        return new OrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Set data to the views
        holder.studentNameTextView.setText(order.getOrderBy());
        holder.orderIdTextView.setText(order.getOrderID());
        // You can set other data here as well, such as order status, order time, etc.
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // Define the ViewHolder class
    class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView patientImageView;
        TextView studentNameTextView;
        TextView orderIdTextView;
        // Add other views here

        OrderViewHolder(View view) {
            super(view);
            patientImageView = view.findViewById(R.id.patientImage);
            studentNameTextView = view.findViewById(R.id.studentName);
            orderIdTextView = view.findViewById(R.id.orderId);
            view.setOnClickListener(this);
            // Initialize other views here if necessary
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && onMoveToDetsListener != null) {
                Order order = orderList.get(position);
                onMoveToDetsListener.onMoveToDets(order);
            }

        }
    }
}
