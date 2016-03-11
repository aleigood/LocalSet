package com.lee.smart;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.lee.smart.comm.SettingUtils;
import com.lee.smart.data.SettingEntity;

import java.util.ArrayList;
import java.util.List;

public class SettingAdapter extends BaseAdapter {
    List<SettingEntity> mList;
    private Activity mContext;
    private OnSwitchClickListener mListener;

    public SettingAdapter(Activity context, OnSwitchClickListener listener) {
        mContext = context;
        mList = new ArrayList<SettingEntity>();
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SettingEntity entity = mList.get(position);
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = mContext.getLayoutInflater().inflate(R.layout.view_item_setting, null);
            viewHolder = new ViewHolder();
            viewHolder.txt1 = (TextView) convertView.findViewById(R.id.text1);
            viewHolder.txt2 = (TextView) convertView.findViewById(R.id.text2);
            viewHolder.switch1 = (Switch) convertView.findViewById(R.id.switch1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txt1.setText(SettingUtils.getSettingStrById(mContext, entity.getParam()));

        switch (entity.getParam()) {
            case SettingUtils.SETTING_WIFI:
            case SettingUtils.SETTING_DATA:
            case SettingUtils.SETTING_BLUETOOTH:
            case SettingUtils.SETTING_SYNC:
            case SettingUtils.SETTING_VIBRATE:
                viewHolder.txt2.setVisibility(View.GONE);
                viewHolder.switch1.setVisibility(View.VISIBLE);
                viewHolder.switch1.setChecked(Integer.parseInt(entity.getValue()) == SettingUtils.STATE_ENABLED ? true
                        : false);
                viewHolder.switch1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onClick(v, entity);
                    }
                });
                break;
            case SettingUtils.SETTING_BRIGHTNESS:
            case SettingUtils.SETTING_VOLUME:
            case SettingUtils.SETTING_RINGER_MODE:
            case SettingUtils.SETTING_RINGTONE:
                viewHolder.txt2.setText(SettingUtils.getValueStr(mContext, entity.getParam(), entity.getValue()));
                viewHolder.txt2.setVisibility(View.VISIBLE);
                viewHolder.switch1.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        return convertView;
    }

    public List<SettingEntity> getData() {
        return mList;
    }

    public void setData(List<SettingEntity> list) {
        mList = list;
        notifyDataSetChanged();
    }

    interface OnSwitchClickListener {
        public void onClick(View v, SettingEntity entity);
    }

    class ViewHolder {
        private TextView txt1;
        private TextView txt2;
        private Switch switch1;
    }
}
