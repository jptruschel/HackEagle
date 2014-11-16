package com.hue.hackeagle;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hue.hackeagle.R;
import com.hue.hackeagle.Req;

import java.util.List;

/**
 * Created by John on 01/11/2014.
 */
public class ReqListAdapter  extends BaseAdapter {

    private Activity activity;
    private static LayoutInflater inflater = null;
    public List<Req> dataList;
    public Boolean showEmailOnFoot = true;

    public ReqListAdapter(Activity a, List<Req> dataList, boolean ShowEmailOnFoot) {
        activity = a;
        this.dataList = dataList;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        showEmailOnFoot = ShowEmailOnFoot;
    }

    public int getCount() {
        return dataList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.req_list_item, null);

        TextView tvName = (TextView) vi.findViewById(R.id.req_li_name);
        TextView tvTags = (TextView) vi.findViewById(R.id.req_li_tag);
        ImageView imageView = (ImageView) vi.findViewById(R.id.req_li_icon);

        Req reqs = dataList.get(position);

        tvName.setText(String.format("%s ($%.2f)", reqs.description, reqs.price));
        tvName.setTextColor(activity.getResources().getColor(R.color.bt_black));
        if (reqs.status.equals("posted"))
            tvName.setTextColor(activity.getResources().getColor(R.color.req_pending));
        if (reqs.status.equals("waiting"))
            tvName.setTextColor(activity.getResources().getColor(R.color.req_waiting));
        if (reqs.status.equals("fulfilled"))
            tvName.setTextColor(activity.getResources().getColor(R.color.req_fullfiled));

        tvTags.setText((showEmailOnFoot)?String.format("(%s) %s", reqs.date, reqs.user):reqs.date);
        switch (reqs.category) {
            case 1: {
                imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.req_t));
                break;
            }
            case 2: {
                imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.req_d));
                break;
            }
            case 3: {
                imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.req_s));
                break;
            }
            default: {
                imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.req_o));
                break;
            }
        }

        return vi;
    }
}
