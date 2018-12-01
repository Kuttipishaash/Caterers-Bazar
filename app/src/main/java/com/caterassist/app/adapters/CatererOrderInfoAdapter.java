package com.caterassist.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caterassist.app.R;
import com.caterassist.app.models.OrderDetails;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CatererOrderInfoAdapter extends RecyclerView.Adapter<CatererOrderInfoAdapter.ViewHolder> {
    ArrayList<OrderDetails> orderDetailsArrayList;

    public void setOrderDetailsArrayList(ArrayList<OrderDetails> orderDetailsArrayList) {
        this.orderDetailsArrayList = orderDetailsArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_caterer_order_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetails orderDetails = orderDetailsArrayList.get(position);
        holder.orderIDTxtView.setText(orderDetails.getOrderId());
        holder.orderStatusTxtView.setText(String.valueOf(orderDetails.getOrderStatus()));
        holder.orderTimeStampTxtView.setText(orderDetails.getOrderTime());
        holder.vendorNameTxtView.setText(orderDetails.getVendorName());
        holder.orderTotalAmtTxtView.setText(String.valueOf(orderDetails.getOrderTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return orderDetailsArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout parentLayout;
        TextView orderIDTxtView;
        TextView orderStatusTxtView;
        TextView orderTimeStampTxtView;
        TextView vendorNameTxtView;
        TextView orderTotalAmtTxtView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.caterer_order_info_parent);
            orderIDTxtView = itemView.findViewById(R.id.li_caterer_order_info_id);
            orderStatusTxtView = itemView.findViewById(R.id.li_caterer_order_info_status);
            vendorNameTxtView = itemView.findViewById(R.id.li_caterer_order_info_vendor_name);
            orderTimeStampTxtView = itemView.findViewById(R.id.li_caterer_order_info_timestamp);
            orderTotalAmtTxtView = itemView.findViewById(R.id.li_caterer_order_info_order_total);
        }
    }
}