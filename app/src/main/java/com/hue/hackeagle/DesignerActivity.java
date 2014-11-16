package com.hue.hackeagle;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DesignerActivity extends Activity {

    public String userEmail;
    public ListView listView;
    public List<Req> dataList;
    ArrayList<Req> wholeDatalist;
    private ProgressBar pb;
    private CheckBox cbView;

    public DesignerActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designer);

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            userEmail = extras.getString("userEmail");
        }

        pb = (ProgressBar) findViewById(R.id.ides_pb);
        TextView tv = (TextView) findViewById(R.id.ides_tv_name);
        tv.setText(userEmail);
        cbView = (CheckBox) findViewById(R.id.ides_cbme);
        listView = (ListView) findViewById(R.id.ides_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(getBaseContext(), RequestInfo.class);
                i.putExtra("userEmail", userEmail);
                i.putExtra("index", position);
                i.putExtra("canPay", false);
                MainActivity.publicList = dataList;
                startActivity(i);
            }
        });
        pb.setVisibility(View.VISIBLE);
        listView.setAdapter(null);
        RequestListUpdate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.designer, menu);
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
        if (id == R.id.action_refresh) {
            RequestListUpdate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UpdateListWithData(List<Req> dataList) {
        this.dataList = dataList;
        pb.setVisibility(View.GONE);
        if (dataList != null)
            listView.setAdapter(new ReqListAdapter(this, dataList, true));
    }

    public void RequestListUpdate() {
        pb.setVisibility(View.VISIBLE);
        DownloadRequests downloadRequests = new DownloadRequests();
        downloadRequests.execute();
    }

    public void OnDesignViewMyRequests(View view) {
        UpdateListView();
    }

    public void UpdateListView() {
        dataList = new ArrayList<Req>();
        if (cbView.isChecked()) {
            for (Req r : wholeDatalist) {
                if (r.finemail.equals(userEmail) && r.status.equals("waiting"))
                    dataList.add(r);
            }
        } else {
            for (Req r : wholeDatalist) {
                if (r.status.equals("posted") || (r.finemail.equals(userEmail) && r.status.equals("waiting")))
                    dataList.add(r);
            }
        }
        UpdateListWithData(dataList);
    }

    /** Async task to download all requests
     */
    public class DownloadRequests extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onProgressUpdate(Integer... values) {
        }
        @Override
        protected void onPostExecute(Void result) {
            if (!(isCancelled())) {
                UpdateListView();
                //UpdateListWithData(dataList);
            }
        }
        @Override
        protected Void doInBackground(Void... params) {
            dataList = new ArrayList<Req>();

            // getting JSON string from URL
            JSONObject json = null;
            if (!isCancelled())
                json = NetHelper.getRequestsJSONFromUrl(MainActivity.URL_BASE + MainActivity.URL_REQUESTS);
            //parsing json data
            if (!isCancelled()) {
                wholeDatalist = NetHelper.parseRequestsJson(json);
            }

            return null;
        }
        @Override
        protected void onCancelled() {

        }
    }
}
