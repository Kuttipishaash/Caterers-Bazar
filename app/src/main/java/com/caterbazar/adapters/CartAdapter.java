package com.caterbazar.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterbazar.R;
import com.caterbazar.models.CartItem;
import com.caterbazar.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartItemViewHolder> {
    private ArrayList<CartItem> cartItemsArrayList;
    private Activity activity;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setCartItemsArrayList(ArrayList<CartItem> cartItemsArrayList) {
        this.cartItemsArrayList = cartItemsArrayList;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cart_item, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem cartItem = cartItemsArrayList.get(position);
        String imageUrl = cartItem.getImageURL();
        if (imageUrl != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.placeholder);
                requestOptions.error(R.drawable.ic_error_placeholder);
                Glide.with(activity.getApplicationContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri).thumbnail(0.1f)
                        .into(holder.itemImageView);
            }).addOnFailureListener(exception -> holder.itemImageView.setImageResource(R.drawable.ic_error_placeholder));
        }
        holder.itemNameTextView.setText(cartItem.getName());
        holder.itemRateTextView.setText(String.valueOf(cartItem.getRate()));

        String totalPrice = "₹"+String.valueOf(cartItem.getTotalAmount());
        holder.itemTotalTextView.setText(totalPrice);
        holder.itemQtyTxtView.setText(String.valueOf(cartItem.getQuantity()));
        holder.tempQuantity = cartItem.getQuantity();
    }

    @Override
    public int getItemCount() {
        return cartItemsArrayList.size();
    }

    class CartItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView itemImageView;
        TextView itemNameTextView;
        TextView itemRateTextView;
        TextView itemTotalTextView;
        TextView itemQtyTxtView;
        ImageButton removeButton;
        double tempQuantity;

        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.li_item_cart_item_image);
            itemNameTextView = itemView.findViewById(R.id.li_item_cart_item_name);
            itemRateTextView = itemView.findViewById(R.id.li_item_cart_item_rate);
            itemTotalTextView = itemView.findViewById(R.id.li_item_cart_item_total);
            itemQtyTxtView = itemView.findViewById(R.id.li_item_cart_item_qty);
            removeButton = itemView.findViewById(R.id.li_item_cart_remove_btn);
            tempQuantity = 1;

            removeButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.li_item_cart_remove_btn:
                    removeItem();

                    break;
            }
        }

        private void removeItem() {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(itemView.getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
            } else {
                builder = new AlertDialog.Builder(itemView.getContext());
            }
            builder.setTitle("Remove from cart")
                    .setMessage("Are you sure you want to remove " + cartItemsArrayList.get(getAdapterPosition()).getName() + " from cart?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.CART_BRANCH_NAME +
                                    FirebaseAuth.getInstance().getUid() + "/" +
                                    FirebaseUtils.CART_ITEMS_BRANCH +
                                    cartItemsArrayList.get(getAdapterPosition()).getId();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
                            databaseReference.setValue(null);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }


    }
}
