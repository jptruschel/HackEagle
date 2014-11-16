package com.hue.hackeagle;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.braintreepayments.api.dropin.BraintreePaymentActivity;
import com.braintreepayments.api.dropin.Customization;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class RequestInfo extends Activity {

    public String userEmail;
    public Req req;
    private int PAY_CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_info);

        // bundle
        Bundle extras = getIntent().getExtras();
        if (extras !=null) {
            userEmail = extras.getString("userEmail");
            req = MainActivity.publicList.get(extras.getInt("index"));
                Button paybtn = (Button) findViewById(R.id.rinfo_confirm);
                paybtn.setVisibility((extras.getBoolean("canPay"))?View.VISIBLE:View.GONE);
        }

        // setup
        ImageView imageView = (ImageView) findViewById(R.id.rinfo_img_ctgry);
        switch (req.category) {
            case 1: {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.req_t));
                break;
            }
            case 2: {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.req_d));
                break;
            }
            case 3: {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.req_s));
                break;
            }
            default: {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.req_o));
                break;
            }
        }
        TextView tv_category = (TextView) findViewById(R.id.rinfo_tv_category);
        tv_category.setText(NetHelper.CategoryTranslate(req.category));
        TextView tv_price = (TextView) findViewById(R.id.rinfo_tv_price);
        tv_price.setText(String.format("$%.2f", req.price));
        EditText et_desc = (EditText) findViewById(R.id.rinfo_et_desc);
        et_desc.setText(req.description);
        TextView tv_user = (TextView) findViewById(R.id.rinfo_tv_req);
        tv_user.setText(req.user);
        TextView tv_accby = (TextView) findViewById(R.id.rinfo_tv_accby);
        tv_accby.setText((req.finemail.length() > 0)?req.finemail:"N/A");

        Button btn_action = (Button) findViewById(R.id.rinfo_btn_action);
        btn_action.setText("OK!");
        final Activity act = this;
        btn_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.request_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PAY_CODE) {
            switch (resultCode) {
                case BraintreePaymentActivity.RESULT_OK:
                    String paymentMethodNonce = data
                            .getStringExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);
                    // create task
                    PostBraintreePurchase task = new PostBraintreePurchase();
                    task.execute(paymentMethodNonce);
                    break;
                case BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR:
                case BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR:
                case BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_UNAVAILABLE:
                    // handle errors here, a throwable may be available in
                    // data.getSerializableExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE)
                    break;
                default:
                    break;
            }
        }
    }

    public void OnConfirmPayment(View view) {
        Intent intent = new Intent(this, BraintreePaymentActivity.class);
        intent.putExtra(BraintreePaymentActivity.EXTRA_CLIENT_TOKEN, MainActivity.ClientToken);
        // REQUEST_CODE is arbitrary and is only used within this activity.
        Customization customization = new Customization.CustomizationBuilder()
                .primaryDescription("Asset Purchase")
                .secondaryDescription("1 Item")
                .amount("$" + req.price)
                .submitButtonText("Purchase")
                .build();
        intent.putExtra(BraintreePaymentActivity.EXTRA_CUSTOMIZATION, customization);
        startActivityForResult(intent, PAY_CODE);
    }

    // task for purchase
    public class PostBraintreePurchase extends AsyncTask<String, Integer, Void> {

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
        @Override
        protected void onPostExecute(Void result) {
        }
        @Override
        protected Void doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post;
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();

            post = new HttpPost(MainActivity.URL_BASE + MainActivity.URL_BTPURCHASE);
            pairs.add(new BasicNameValuePair("nounce", params[0]));
            pairs.add(new BasicNameValuePair("amount", String.format("%.2f", req.price)));
            pairs.add(new BasicNameValuePair("mail", userEmail));

            // Make the POST
            try {
                post.setEntity(new UrlEncodedFormEntity(pairs));
            } catch (UnsupportedEncodingException e) {
                cancel(true);
            }
            try {
                HttpResponse response = client.execute(post);
                String responseBody = EntityUtils.toString(response.getEntity());
                Log.i("purchase", responseBody);

            } catch (IOException e) {
                cancel(true);
            }

            return null;
        }
        @Override
        protected void onCancelled() {
            Log.i("purchase", "Failed");
        }
    }

}
