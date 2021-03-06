package com.caterbazar.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterbazar.R;
import com.caterbazar.activities.ViewVendorItemsActivity;
import com.caterbazar.models.UserDetails;
import com.caterbazar.utils.Constants;
import com.caterbazar.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class VendorListAdapter extends RecyclerView.Adapter<VendorListAdapter.ViewHolder> implements Filterable {
    private ArrayList<UserDetails> vendorsList = new ArrayList<>();
    private ArrayList<UserDetails> filteredVendorsList = new ArrayList<>();
    private Activity activity;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setVendorsList(ArrayList<UserDetails> vendorsList) {
        this.vendorsList = vendorsList;
        this.filteredVendorsList = vendorsList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vendors, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserDetails vendorDetails = filteredVendorsList.get(position);
        holder.vendorNameTextView.setText(vendorDetails.getUserName());
        holder.vendorLocationTextView.setText(vendorDetails.getUserLocationName());
        String imageUrl = vendorDetails.getUserImageUrl();
        if (imageUrl != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_error_placeholder)
                        .override(150, 150);
                Glide.with(activity.getApplicationContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri).thumbnail(0.1f)
                        .into(holder.vendorImageView);
            }).addOnFailureListener(exception -> holder.vendorImageView.setImageResource(R.drawable.ic_error_placeholder));
        } else {
            holder.vendorImageView.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return filteredVendorsList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredVendorsList = vendorsList;
                } else {
                    ArrayList<UserDetails> filteredList = new ArrayList<>();
                    for (UserDetails vendor : vendorsList) {
                        if (vendor.getUserName().toLowerCase().contains(charString.toLowerCase()) || vendor.getUserLocationName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(vendor);
                        }
                    }
                    filteredVendorsList = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredVendorsList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredVendorsList = (ArrayList<UserDetails>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView vendorImageView;
        TextView vendorNameTextView;
        TextView vendorLocationTextView;
        ImageButton callVendorBtn;
        LinearLayout parentLayout;
        ImageButton addFavouriteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            vendorImageView = itemView.findViewById(R.id.li_vendors_image);
            vendorNameTextView = itemView.findViewById(R.id.li_vendor_name);
            vendorLocationTextView = itemView.findViewById(R.id.li_vendor_loc);
            parentLayout = itemView.findViewById(R.id.li_vendor_parent_layout);
            callVendorBtn = itemView.findViewById(R.id.li_vendor_call);
            addFavouriteButton = itemView.findViewById(R.id.li_vendor_add_favourite);
            callVendorBtn.setOnClickListener(this);
            parentLayout.setOnClickListener(this);
            addFavouriteButton.setOnClickListener(this);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.li_vendor_parent_layout) {
                String uID = filteredVendorsList.get(getAdapterPosition()).getUserID();
                Intent viewVendorItemsIntent = new Intent(itemView.getContext(), ViewVendorItemsActivity.class);
                viewVendorItemsIntent.putExtra(Constants.IntentExtrasKeys.VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID, uID);
                itemView.getContext().startActivity(viewVendorItemsIntent);
            } else if (v.getId() == R.id.li_vendor_call) {
                String phoneNumber = filteredVendorsList.get(getAdapterPosition()).getUserPhone();
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                itemView.getContext().startActivity(callIntent);
            } else if (v.getId() == R.id.li_vendor_add_favourite) {
                addFavouriteButton.setImageResource(R.drawable.ic_favorite);
                addFavouriteButton.setMinimumWidth(40);
                addFavouriteButton.setMinimumHeight(40);
                String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.FAVOURITE_VENDORS_BRANCH_NAME + FirebaseAuth.getInstance().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
                databaseReference.child(filteredVendorsList.get(getAdapterPosition()).getUserID()).setValue(filteredVendorsList.get(getAdapterPosition()))
                        .addOnSuccessListener(aVoid -> Toasty.success(itemView.getContext(), "Added to favourites.").show())
                        .addOnFailureListener(e -> Toasty.error(itemView.getContext(), "Couldn't add vendor to favourites").show());
            }
        }
    }
}
