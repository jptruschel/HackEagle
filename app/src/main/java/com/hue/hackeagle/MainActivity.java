package com.hue.hackeagle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.ValidationError;
import com.braintreepayments.api.dropin.BraintreePaymentActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    public static String ClientToken;
    final int REQUEST_CODE_PRIM = 1;
    ProgressBar pb;
    TextView wrong;

    public static final String URL_BASE = "http://192.168.176.242/public/api/";
    public static final String URL_LOGIN = "user.php";
    public static final String URL_REQUESTS = "requests.php";
    public static final String URL_BTTOKEN = "braintree/token.php";
    public static final String URL_BTPURCHASE= "braintree/index.php";

    public static String userEmail = "", userPassword = "";
    public static int userType = -1;

    public static List<Req> publicList = null;

    public Context context;

    public View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL_BASE + URL_BTTOKEN, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int n, Header[] h, String clientToken) {
                ClientToken = clientToken;
                Log.i("token", clientToken);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    // login
    public void OnLoginButton(View view) {
        pb = (ProgressBar) findViewById(R.id.main_pb);
        pb.setVisibility(View.VISIBLE);
        wrong = (TextView) findViewById(R.id.main_tv_wrong);
        wrong.setVisibility(View.INVISIBLE);
        this.view = view;
        // Send request
        EditText email = (EditText) findViewById(R.id.rinfo_et_desc);
        userEmail = email.getText().toString();
        EditText pass = (EditText) findViewById(R.id.main_et_pass);
        userPassword = pass.getText().toString();

        PostLoginTask task = new PostLoginTask();
        task.execute();
    }

    // after login
    public void OnLoginComplete() {
        // depending on the type of user, open the right activity
        switch (userType) {
            case 1: {
                Intent i = new Intent(getBaseContext(), DesignerActivity.class);
                i.putExtra("userEmail", userEmail);
                startActivity(i);
                break;
            }
            case 2: {
                Intent i = new Intent(getBaseContext(), DeveloperActivity.class);
                i.putExtra("userEmail", userEmail);
                startActivity(i);
                break;
            }
            default: {

            }
        }
        pb.setVisibility(View.INVISIBLE);
        if (userType == -1) {
            wrong.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);
        }
    }

    // asks for login
    public class PostLoginTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
        @Override
        protected void onPostExecute(Void result) {
            OnLoginComplete();
        }
        @Override
        protected Void doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post;
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();

            post = new HttpPost(URL_BASE + URL_LOGIN);
            pairs.add(new BasicNameValuePair("user", userEmail));
            pairs.add(new BasicNameValuePair("passwd", userPassword));

            userType = -1;

            // Make the POST
            try {
                post.setEntity(new UrlEncodedFormEntity(pairs));
            } catch (UnsupportedEncodingException e) {
                Log.i("login", "pair problem");
                cancel(true);
            }
            try {
                Log.i("login", "executing ... ");
                HttpResponse response = client.execute(post);
                Log.i("login", "executed. reading response ...");
                String responseBody = EntityUtils.toString(response.getEntity());
                Log.i("login", responseBody);

                JSONObject userInfo = new JSONObject(responseBody);
                String s = userInfo.getString("user_type");

                if (s.equals("designer"))
                    userType = 1;
                if (s.equals("developer"))
                    userType = 2;
            } catch (IOException e) {
                cancel(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onCancelled() {
            Log.i("login", "Failed");
        }
    }


}
