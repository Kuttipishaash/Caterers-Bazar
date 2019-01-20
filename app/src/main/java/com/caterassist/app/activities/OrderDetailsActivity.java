package com.caterassist.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.OrderItemsAdapter;
import com.caterassist.app.dialogs.LoadingDialog;
import com.caterassist.app.models.CartItem;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class OrderDetailsActivity extends Activity implements View.OnClickListener {

    private RecyclerView orderItemsRecyclerView;
    private TextView userTypeTxtView;
    private TextView userNameTxtView;
    private TextView orderDateTxtView;
    private TextView orderTimeTxtView;
    private TextView orderIDTxtView;
    private TextView orderTotalAmtTxtView;
    private TextView orderStatusTxtView;
    private LinearLayout noItemsView;
    private LoadingDialog loadingDialog;
    private ImageButton deleteOrderBtn;
    private ImageButton viewVendorBtn;


    private ArrayList<CartItem> cartItemArrayList;
    private OrderItemsAdapter orderItemsAdapter;
    private String orderBranchName;
    private String orderId;
    private OrderDetails orderDetails;
    private Handler handler;
    private Runnable runnable;
    private LinearLayoutManager orderItemsLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        Intent intent = getIntent();
        orderBranchName = intent.getStringExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH);
        orderId = intent.getStringExtra(Constants.IntentExtrasKeys.ORDER_ID);
        orderDetails = (OrderDetails) intent.getSerializableExtra(Constants.IntentExtrasKeys.ORDER_INFO);
        if (orderBranchName != null && orderId != null) {
            initViews();
            setOrderInfo();
            fetchItems();
        } else {
            Toasty.error(this, "Some error occured! Try again...", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void checkItems() {
        if (cartItemArrayList.size() > 0) {
            //TODO: Show the top card
            orderItemsRecyclerView.setVisibility(VISIBLE);
            noItemsView.setVisibility(GONE);
        } else {
            noItemsView.setVisibility(VISIBLE);
            orderItemsRecyclerView.setVisibility(GONE);
            //TODO: Hide the top card
        }
    }

    private void setOrderInfo() {
        if (AppUtils.isCurrentUserVendor(this)) {
            userTypeTxtView.setText("Caterer Name: ");
            userTypeTxtView.setText("Caterer");
            //TODO:Null pointer to fix
            userNameTxtView.setText(orderDetails.getCatererName());
        } else {
            viewVendorBtn.setVisibility(VISIBLE);
            userTypeTxtView.setText("Vendor Name: ");
            viewVendorBtn.setVisibility(View.VISIBLE);
            userTypeTxtView.setText("Vendor");
            userNameTxtView.setText(orderDetails.getVendorName());
        }
        orderIDTxtView.setText(orderId);

        String totalAmount = "₹" + String.valueOf(orderDetails.getOrderTotalAmount());
        orderTotalAmtTxtView.setText(totalAmount);
        String timeStamp[] = String.valueOf(orderDetails.getOrderTime()).split(" ");
        orderDateTxtView.setText(timeStamp[0]);
        orderTimeTxtView.setText(timeStamp[1]);
        String status;
        //TODO Set color in this switch case
        switch (orderDetails.getOrderStatus()) {
            case 0:
                status = "Awaiting approval";
                break;
            case 1:
                status = "Approved and Processing";
                break;
            case 2:
                status = "Completed";
                break;
            case 3:
                status = "Rejected";
                break;
            default:
                status = "Status Unavailable";
                break;
        }
        orderStatusTxtView.setText(status);
    }

    @Override
    public void onBackPressed() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        super.onBackPressed();
    }

    private void fetchItems() {
        loadingDialog = new LoadingDialog(this, "Loading order details...");
        loadingDialog.show();
        final int interval = 10000; // 1 Second
        handler = new Handler();
        runnable = () -> {
            if (loadingDialog != null)
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    Toast.makeText(OrderDetailsActivity.this, "Please check your internet connection and try again!", Toast.LENGTH_SHORT).show();
                    checkItems();
                }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + interval);
        handler.postDelayed(runnable, interval);
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + orderBranchName +
                FirebaseAuth.getInstance().getUid() + "/" + orderId + "/" + FirebaseUtils.ORDER_ITEMS_BRANCH;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CartItem cartItem = snapshot.getValue(CartItem.class);
                    cartItemArrayList.add(cartItem);
                    orderItemsAdapter.notifyDataSetChanged();
                }
                checkItems();
                loadingDialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loadingDialog.dismiss();
                Toasty.error(OrderDetailsActivity.this, "Error occured while fetching items!", Toast.LENGTH_SHORT).show();
                checkItems();
            }
        });
    }

    private void initViews() {
        noItemsView = findViewById(R.id.error_items_list_empty);
        userTypeTxtView = findViewById(R.id.li_caterer_order_info_user_type);
        userNameTxtView = findViewById(R.id.li_caterer_order_info_vendor_name);
        orderIDTxtView = findViewById(R.id.li_caterer_order_info_id);
        orderDateTxtView = findViewById(R.id.li_caterer_order_info_timestamp);
        orderTimeTxtView = findViewById(R.id.li_caterer_order_info_timestamp_time);
        orderTotalAmtTxtView = findViewById(R.id.li_caterer_order_info_order_total);
        orderStatusTxtView = findViewById(R.id.li_caterer_order_info_status);
        deleteOrderBtn = findViewById(R.id.li_caterer_order_info_delete);
        orderItemsRecyclerView = findViewById(R.id.act_ord_det_order_items_recyc_view);
        viewVendorBtn = findViewById(R.id.li_caterer_order_info_view_vendor);

        deleteOrderBtn.setOnClickListener(this);
        viewVendorBtn.setOnClickListener(this);

        cartItemArrayList = new ArrayList<>();
        orderItemsAdapter = new OrderItemsAdapter();
        orderItemsAdapter.setCartItemArrayList(cartItemArrayList);
        orderItemsAdapter.setActivity(this);
        orderItemsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        orderItemsRecyclerView.setLayoutManager(orderItemsLayoutManager);
        orderItemsRecyclerView.setAdapter(orderItemsAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == deleteOrderBtn.getId()) {
            if (orderDetails.getOrderStatus() > 1) {
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_title_delete_order_history))
                        .setMessage(
                                getResources().getString(R.string.dialog_message_delete_order_history))
                        .setPositiveButton(
                                getResources().getString(R.string.dialog_btn_yes),
                                (dialog, which) -> deleteOrder())
                        .setNegativeButton((getResources().getString(R.string.dialog_btn_no))
                                , (dialog, which) -> dialog.dismiss()).show();
            } else {
                Toasty.warning(this, "You cannot delete an unfulfilled order!").show();
            }
        } else if (v.getId() == viewVendorBtn.getId()) {
            if (AppUtils.isCurrentUserVendor(this)) {
                Intent viewCatererIntent = new Intent(OrderDetailsActivity.this, CatererProfileActivity.class);
                viewCatererIntent.putExtra(Constants.IntentExtrasKeys.USER_ID, orderDetails.getCatererID());
                startActivity(viewCatererIntent);
            } else {
                Intent viewVendorIntent = new Intent(OrderDetailsActivity.this, ViewVendorItemsActivity.class);
                viewVendorIntent.putExtra(Constants.IntentExtrasKeys.VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID, orderDetails.getVendorId());
                startActivity(viewVendorIntent);
                finish();
            }
        }
    }

    private void deleteOrder() {
        String orderID = orderDetails.getOrderId();
        String branch = AppUtils.isCurrentUserVendor(this) ? FirebaseUtils.ORDERS_VENDOR_BRANCH : FirebaseUtils.ORDERS_CATERER_BRANCH;
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + branch +
                FirebaseAuth.getInstance().getUid() + "/" + orderID;
        DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference(databasePath);
        orderReference.setValue(null)
                .addOnSuccessListener(aVoid -> {
                    Toasty.success(this, "Order deleted from history.").show();
                    finish();
                })
                .addOnFailureListener(e -> Toasty.error(this, "Failed to delete order from history!").show());
    }
}
