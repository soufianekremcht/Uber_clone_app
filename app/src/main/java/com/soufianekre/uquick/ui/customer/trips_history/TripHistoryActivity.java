package com.soufianekre.uquick.ui.customer.trips_history;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.soufianekre.uquick.R;
import com.soufianekre.uquick.helpers.FirebaseConstant;
import com.soufianekre.uquick.ui.base.BaseActivity;

import java.math.BigDecimal;

import butterknife.ButterKnife;

class TripHistoryActivity extends BaseActivity {

    private final int PAYPAL_REQUEST_CODE = 1454;
    private String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DatabaseReference customerDbRef = FirebaseDatabase.getInstance().getReference()
            .child(FirebaseConstant.CUSTOMERS_PATH).child(currentUserUID);
    private DatabaseReference customerTripHistoryDbRef;

    private PayPalConfiguration paypalConfiguration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_trip_history);
        ButterKnife.bind(this);

        paypalConfiguration = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                .clientId(PaypalHelper.CLIENT_ID);
        startPayPalService();
        customerTripHistoryDbRef = customerDbRef.child("trip_history");


    }


    private void payPayments(){
        PayPalPayment payPalPayment = new PayPalPayment(BigDecimal.valueOf(34.44),"USD","Uber Ride",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent paypalActivityIntent = new Intent(this, PaymentActivity.class);
        paypalActivityIntent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
        paypalActivityIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfiguration);
        startActivity(paypalActivityIntent);
    }

    private void startPayPalService(){
        Intent intent = new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfiguration);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE){
            showMessage("The Payment is successful.");
        }else{
            showMessage("The Payment wasn't successful.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
}
