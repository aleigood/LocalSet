package com.lee.smart;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Switch;

import com.lee.smart.comm.SettingUtils;
import com.lee.smart.data.SettingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultSettingsActivity extends Activity implements SettingAdapter.OnSwitchClickListener {
    private List<SettingEntity> settings;
    private ListView listView;
    private SettingAdapter adapter;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        ActionBar bar = getActionBar();
        bar.setDisplayOptions(bar.getDisplayOptions() ^ ActionBar.DISPLAY_HOME_AS_UP);

        setContentView(R.layout.activity_setting);
        listView = (ListView) findViewById(R.id.list);
        adapter = new SettingAdapter(this, this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SettingEntity entity = settings.get(position);

                switch (entity.getParam()) {
                    case SettingUtils.SETTING_BRIGHTNESS:
                        View brightnessView = getLayoutInflater().inflate(R.layout.view_setting_brightness, null);
                        final CheckBox toggleAuto = (CheckBox) brightnessView.findViewById(R.id.toggleAuto);
                        final SeekBar seekBar = (SeekBar) brightnessView.findViewById(R.id.seekBar);
                        seekBar.setMax(255);

                        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                                if (toggleAuto.isChecked()) {
                                    toggleAuto.setChecked(false);
                                }
                            }

                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            }
                        });

                        int val = Integer.parseInt(entity.getValue());

                        if (val == -1) {
                            toggleAuto.setChecked(true);
                        } else {
                            toggleAuto.setChecked(false);
                            seekBar.setProgress(val);
                        }
                        new AlertDialog.Builder(DefaultSettingsActivity.this)
                                .setView(brightnessView)
                                .setTitle(
                                        SettingUtils.getSettingStrById(DefaultSettingsActivity.this,
                                                SettingUtils.SETTING_BRIGHTNESS))
                                .setNegativeButton(android.R.string.cancel, null)
                                .setNeutralButton(android.R.string.ok, new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (toggleAuto.isChecked()) {
                                            entity.setValue(String.valueOf(-1));
                                        } else {
                                            entity.setValue(String.valueOf(seekBar.getProgress()));
                                        }

                                        saveSetting(entity);
                                        refreshData();
                                    }
                                }).show();
                        break;
                    case SettingUtils.SETTING_VOLUME:
                        View volumeView = getLayoutInflater().inflate(R.layout.view_setting_volume, null);
                        final SeekBar volumeSeekBar = (SeekBar) volumeView.findViewById(R.id.seekBar);
                        volumeSeekBar.setMax(SettingUtils.getMaxVolume(DefaultSettingsActivity.this));
                        volumeSeekBar.setProgress(Integer.parseInt(entity.getValue()));
                        new AlertDialog.Builder(DefaultSettingsActivity.this)
                                .setView(volumeView)
                                .setTitle(
                                        SettingUtils.getSettingStrById(DefaultSettingsActivity.this,
                                                SettingUtils.SETTING_VOLUME))
                                .setNegativeButton(android.R.string.cancel, null)
                                .setNeutralButton(android.R.string.ok, new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        entity.setValue(String.valueOf(volumeSeekBar.getProgress()));
                                        saveSetting(entity);
                                        refreshData();
                                    }
                                }).show();
                        break;
                    case SettingUtils.SETTING_RINGER_MODE:
                        final ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
                        Map<String, String> map1 = new HashMap<String, String>();
                        map1.put(
                                "name",
                                SettingUtils.getRingerModeStr(DefaultSettingsActivity.this,
                                        SettingUtils.RINGER_MODE_NORMAL).toString());
                        map1.put("value", String.valueOf(SettingUtils.RINGER_MODE_NORMAL));
                        data.add(map1);
                        Map<String, String> map2 = new HashMap<String, String>();
                        map2.put(
                                "name",
                                SettingUtils.getRingerModeStr(DefaultSettingsActivity.this,
                                        SettingUtils.RINGER_MODE_SILENT).toString());
                        map2.put("value", String.valueOf(SettingUtils.RINGER_MODE_SILENT));
                        data.add(map2);
                        Map<String, String> map3 = new HashMap<String, String>();
                        map3.put(
                                "name",
                                SettingUtils.getRingerModeStr(DefaultSettingsActivity.this,
                                        SettingUtils.RINGER_MODE_VIBRATE).toString());
                        map3.put("value", String.valueOf(SettingUtils.RINGER_MODE_VIBRATE));
                        data.add(map3);
                        new AlertDialog.Builder(DefaultSettingsActivity.this)
                                .setAdapter(
                                        new SimpleAdapter(DefaultSettingsActivity.this, data,
                                                android.R.layout.simple_list_item_1, new String[]{"name"},
                                                new int[]{android.R.id.text1}),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                entity.setValue(data.get(which).get("value"));
                                                saveSetting(entity);
                                                refreshData();
                                            }
                                        })
                                .setTitle(
                                        SettingUtils.getSettingStrById(DefaultSettingsActivity.this,
                                                SettingUtils.SETTING_RINGER_MODE)).show();
                        break;
                    case SettingUtils.SETTING_RINGTONE:
                        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                !TextUtils.isEmpty(entity.getValue()) ? Uri.parse(entity.getValue()) : null);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getTitle());
                        startActivityForResult(intent, SettingUtils.SETTING_RINGTONE);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void refreshData() {
        settings = new ArrayList<SettingEntity>();
        settings.add(new SettingEntity(SettingUtils.SETTING_WIFI, pref.getString(SettingUtils.PREF_PARAM
                + SettingUtils.SETTING_WIFI, String.valueOf(SettingUtils.getWifiState(this)))));
        settings.add(new SettingEntity(SettingUtils.SETTING_DATA, pref.getString(SettingUtils.PREF_PARAM
                + SettingUtils.SETTING_DATA, String.valueOf(SettingUtils.getDataConnState(this)))));
        settings.add(new SettingEntity(SettingUtils.SETTING_BLUETOOTH, pref.getString(SettingUtils.PREF_PARAM
                + SettingUtils.SETTING_BLUETOOTH, String.valueOf(SettingUtils.getBluetoothState(this)))));
        settings.add(new SettingEntity(SettingUtils.SETTING_SYNC, pref.getString(SettingUtils.PREF_PARAM
                + SettingUtils.SETTING_SYNC, String.valueOf(SettingUtils.getSyncState()))));
        settings.add(new SettingEntity(SettingUtils.SETTING_VIBRATE, pref.getString(SettingUtils.PREF_PARAM
                + SettingUtils.SETTING_VIBRATE, String.valueOf(SettingUtils.getVibrateState(this)))));
        settings.add(new SettingEntity(SettingUtils.SETTING_BRIGHTNESS, pref.getString(SettingUtils.PREF_PARAM
                + SettingUtils.SETTING_BRIGHTNESS, String.valueOf(SettingUtils.getBrightness(this)))));
        settings.add(new SettingEntity(SettingUtils.SETTING_VOLUME, pref.getString(SettingUtils.PREF_PARAM
                + SettingUtils.SETTING_VOLUME, String.valueOf(SettingUtils.getVolume(this)))));
        settings.add(new SettingEntity(SettingUtils.SETTING_RINGER_MODE, pref.getString(SettingUtils.PREF_PARAM
                + SettingUtils.SETTING_RINGER_MODE, String.valueOf(SettingUtils.getRingerMode(this)))));
        settings.add(new SettingEntity(SettingUtils.SETTING_RINGTONE, String.valueOf(pref.getString(
                SettingUtils.PREF_PARAM + SettingUtils.SETTING_RINGTONE, SettingUtils.getRingtone(this)))));
        adapter.setData(settings);
    }

    private void saveSetting(SettingEntity entity) {
        pref.edit().putString(SettingUtils.PREF_PARAM + entity.getParam(), entity.getValue()).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onClick(View v, SettingEntity entity) {
        Switch switch1 = (Switch) v;

        if (switch1.isChecked()) {
            entity.setValue(String.valueOf(SettingUtils.STATE_ENABLED));
            saveSetting(entity);
        } else {
            entity.setValue(String.valueOf(SettingUtils.STATE_DISABLED));
            saveSetting(entity);
        }
    }

}
