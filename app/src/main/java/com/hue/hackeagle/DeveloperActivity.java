package com.hue.hackeagle;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DeveloperActivity extends Activity {

    public String userEmail;
    public ListView listView;
    public List<Req> dataList;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        Bundle extras = getIntent().getExtras();
        if (extras !=null) {
            userEmail = extras.getString("userEmail");
        }

        pb = (ProgressBar) findViewById(R.id.idev_pb);
        TextView tv = (TextView) findViewById(R.id.ides_tv_name);
        tv.setText(userEmail);
        listView = (ListView) findViewById(R.id.idev_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Intent i = new Intent(getBaseContext(), RequestInfo.class);
                i.putExtra("userEmail", userEmail);
                i.putExtra("index", position);
                i.putExtra("canPay", !(dataList.get(position).status.equals("posted")));
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
        getMenuInflater().inflate(R.menu.developer, menu);
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
            listView.setAdapter(new ReqListAdapter(this, dataList, false));
    }

    public void RequestListUpdate() {
        pb.setVisibility(View.VISIBLE);
        DownloadAndFilterRequests downloadAndFilterRequests = new DownloadAndFilterRequests();
        downloadAndFilterRequests.execute();
    }

    public void OnNewRequestButton(View view) {
        Intent i = new Intent(getBaseContext(), NewRequest.class);
        i.putExtra("userEmail", userEmail);
        MainActivity.publicList = dataList;
        startActivity(i);
    }

    /** Async task to download all requests and filter
     */
    public class DownloadAndFilterRequests extends AsyncTask<Void, Integer, Void> {
        private List<Req> dataList;
        @Override
        protected void onProgressUpdate(Integer... values) {
        }
        @Override
        protected void onPostExecute(Void result) {
            if (!(isCancelled())) {
                UpdateListWithData(dataList);
            }
        }
        @Override
        protected Void doInBackground(Void... params) {
            dataList = new ArrayList<Req>();
            ArrayList<Req> wholeDatalist;

            // getting JSON string from URL
            JSONObject json = null;
            if (!isCancelled())
                json = NetHelper.getRequestsJSONFromUrl(MainActivity.URL_BASE + MainActivity.URL_REQUESTS);
            //parsing json data
            if (!isCancelled()) {
                wholeDatalist = NetHelper.parseRequestsJson(json);

                // now go through all the elements, deleting all of those which are not this user's
                for (Req r : wholeDatalist) {
                    if (r.user.equals(userEmail))
                        dataList.add(r);
                }
            }

            return null;
        }
        @Override
        protected void onCancelled() {

        }
    }

}
