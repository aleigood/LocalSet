package com.lee.smart;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Switch;

import com.lee.smart.comm.SettingUtils;
import com.lee.smart.data.DatabaseOper;
import com.lee.smart.data.LocationEntity;
import com.lee.smart.data.SettingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends Activity implements SettingAdapter.OnSwitchClickListener {
    private DatabaseOper dbOper;
    private int locationId;
    private List<SettingEntity> settings;
    private LocationEntity location;
    private ListView listView;
    private SettingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar bar = getActionBar();
        bar.setDisplayOptions(bar.getDisplayOptions() ^ ActionBar.DISPLAY_HOME_AS_UP);
        dbOper = MyApplication.getInstance().getDataOper();
        location = (LocationEntity) getIntent().getSerializableExtra("data");
        locationId = location.getId();
        settings = location.getSettings();

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
                        new AlertDialog.Builder(SettingsActivity.this)
                                .setView(brightnessView)
                                .setTitle(
                                        SettingUtils.getSettingStrById(SettingsActivity.this,
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

                                        dbOper.updateSetting(entity);
                                        refreshData();
                                    }
                                }).show();
                        break;
                    case SettingUtils.SETTING_VOLUME:
                        View volumeView = getLayoutInflater().inflate(R.layout.view_setting_volume, null);
                        final SeekBar volumeSeekBar = (SeekBar) volumeView.findViewById(R.id.seekBar);
                        volumeSeekBar.setMax(SettingUtils.getMaxVolume(SettingsActivity.this));
                        volumeSeekBar.setProgress(Integer.parseInt(entity.getValue()));
                        new AlertDialog.Builder(SettingsActivity.this)
                                .setView(volumeView)
                                .setTitle(
                                        SettingUtils.getSettingStrById(SettingsActivity.this,
                                                SettingUtils.SETTING_VOLUME))
                                .setNegativeButton(android.R.string.cancel, null)
                                .setNeutralButton(android.R.string.ok, new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        entity.setValue(String.valueOf(volumeSeekBar.getProgress()));
                                        dbOper.updateSetting(entity);
                                        refreshData();
                                    }
                                }).show();
                        break;
                    case SettingUtils.SETTING_RINGER_MODE:
                        final ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
                        Map<String, String> map1 = new HashMap<String, String>();
                        map1.put("name",
                                SettingUtils.getRingerModeStr(SettingsActivity.this, SettingUtils.RINGER_MODE_NORMAL)
                                        .toString());
                        map1.put("value", String.valueOf(SettingUtils.RINGER_MODE_NORMAL));
                        data.add(map1);
                        Map<String, String> map2 = new HashMap<String, String>();
                        map2.put("name",
                                SettingUtils.getRingerModeStr(SettingsActivity.this, SettingUtils.RINGER_MODE_SILENT)
                                        .toString());
                        map2.put("value", String.valueOf(SettingUtils.RINGER_MODE_SILENT));
                        data.add(map2);
                        Map<String, String> map3 = new HashMap<String, String>();
                        map3.put("name",
                                SettingUtils.getRingerModeStr(SettingsActivity.this, SettingUtils.RINGER_MODE_VIBRATE)
                                        .toString());
                        map3.put("value", String.valueOf(SettingUtils.RINGER_MODE_VIBRATE));
                        data.add(map3);
                        new AlertDialog.Builder(SettingsActivity.this)
                                .setAdapter(
                                        new SimpleAdapter(SettingsActivity.this, data,
                                                android.R.layout.simple_list_item_1, new String[]{"name"},
                                                new int[]{android.R.id.text1}),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                entity.setValue(data.get(which).get("value"));
                                                dbOper.updateSetting(entity);
                                                refreshData();
                                            }
                                        })
                                .setTitle(
                                        SettingUtils.getSettingStrById(SettingsActivity.this,
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
        location = dbOper.getLocation(locationId);
        settings = location.getSettings();
        adapter.setData(settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.selection:
                final ListView popListView = new ListView(SettingsActivity.this);
                final ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();

                Map<String, String> map1 = new HashMap<String, String>();
                map1.put("param", String.valueOf(SettingUtils.SETTING_WIFI));
                map1.put("name", SettingUtils.getSettingStrById(SettingsActivity.this, SettingUtils.SETTING_WIFI)
                        .toString());
                data.add(map1);
                Map<String, String> map2 = new HashMap<String, String>();
                map2.put("param", String.valueOf(SettingUtils.SETTING_DATA));
                map2.put("name", SettingUtils.getSettingStrById(SettingsActivity.this, SettingUtils.SETTING_DATA)
                        .toString());
                data.add(map2);
                Map<String, String> map3 = new HashMap<String, String>();
                map3.put("param", String.valueOf(SettingUtils.SETTING_BLUETOOTH));
                map3.put("name", SettingUtils.getSettingStrById(SettingsActivity.this, SettingUtils.SETTING_BLUETOOTH)
                        .toString());
                data.add(map3);
                Map<String, String> map4 = new HashMap<String, String>();
                map4.put("param", String.valueOf(SettingUtils.SETTING_SYNC));
                map4.put("name", SettingUtils.getSettingStrById(SettingsActivity.this, SettingUtils.SETTING_SYNC)
                        .toString());
                data.add(map4);
                Map<String, String> map5 = new HashMap<String, String>();
                map5.put("param", String.valueOf(SettingUtils.SETTING_VIBRATE));
                map5.put("name", SettingUtils.getSettingStrById(SettingsActivity.this, SettingUtils.SETTING_VIBRATE)
                        .toString());
                data.add(map5);
                Map<String, String> map6 = new HashMap<String, String>();
                map6.put("param", String.valueOf(SettingUtils.SETTING_BRIGHTNESS));
                map6.put("name", SettingUtils.getSettingStrById(SettingsActivity.this, SettingUtils.SETTING_BRIGHTNESS)
                        .toString());
                data.add(map6);
                Map<String, String> map7 = new HashMap<String, String>();
                map7.put("param", String.valueOf(SettingUtils.SETTING_VOLUME));
                map7.put("name", SettingUtils.getSettingStrById(SettingsActivity.this, SettingUtils.SETTING_VOLUME)
                        .toString());
                data.add(map7);
                Map<String, String> map8 = new HashMap<String, String>();
                map8.put("param", String.valueOf(SettingUtils.SETTING_RINGER_MODE));
                map8.put("name", SettingUtils
                        .getSettingStrById(SettingsActivity.this, SettingUtils.SETTING_RINGER_MODE).toString());
                data.add(map8);
                Map<String, String> map9 = new HashMap<String, String>();
                map9.put("param", String.valueOf(SettingUtils.SETTING_RINGTONE));
                map9.put("name", SettingUtils.getSettingStrById(SettingsActivity.this, SettingUtils.SETTING_RINGTONE)
                        .toString());
                data.add(map9);

                popListView.setAdapter(new SimpleAdapter(SettingsActivity.this, data,
                        android.R.layout.simple_list_item_multiple_choice, new String[]{"name"},
                        new int[]{android.R.id.text1}));

                popListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                for (SettingEntity setting : settings) {
                    switch (setting.getParam()) {
                        case SettingUtils.SETTING_WIFI:
                            popListView.setItemChecked(0, true);
                            break;
                        case SettingUtils.SETTING_DATA:
                            popListView.setItemChecked(1, true);
                            break;
                        case SettingUtils.SETTING_BLUETOOTH:
                            popListView.setItemChecked(2, true);
                            break;
                        case SettingUtils.SETTING_SYNC:
                            popListView.setItemChecked(3, true);
                            break;
                        case SettingUtils.SETTING_VIBRATE:
                            popListView.setItemChecked(4, true);
                            break;
                        case SettingUtils.SETTING_BRIGHTNESS:
                            popListView.setItemChecked(5, true);
                            break;
                        case SettingUtils.SETTING_VOLUME:
                            popListView.setItemChecked(6, true);
                            break;
                        case SettingUtils.SETTING_RINGER_MODE:
                            popListView.setItemChecked(7, true);
                            break;
                        case SettingUtils.SETTING_RINGTONE:
                            popListView.setItemChecked(8, true);
                            break;
                        default:
                            break;
                    }
                }
                final List<SettingEntity> settingsClone = cloneSettings();
                popListView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        CheckedTextView checkedVew = (CheckedTextView) view;
                        int paramId = Integer.valueOf(data.get(position).get("param"));

                        if (checkedVew.isChecked()) {
                            SettingEntity entity = new SettingEntity();
                            entity.setLocationId(locationId);
                            entity.setParam(paramId);
                            entity.setValue(SettingUtils.getDefaultValue(SettingsActivity.this, paramId));
                            settingsClone.add(entity);
                        } else {
                            for (Iterator<SettingEntity> iterator = settingsClone.iterator(); iterator.hasNext(); ) {
                                SettingEntity settingEntity = (SettingEntity) iterator.next();

                                // 如果找到删除，直接返回
                                if (settingEntity.getParam() == paramId) {
                                    settingsClone.remove(settingEntity);
                                    return;
                                }
                            }
                        }
                    }
                });

                new AlertDialog.Builder(SettingsActivity.this).setView(popListView).setTitle(R.string.select_settings)
                        .setNeutralButton(android.R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbOper.deleteSettingsByLocation(locationId);

                                for (SettingEntity settingEntity : settingsClone) {
                                    dbOper.insertSetting(settingEntity);
                                }

                                refreshData();
                            }
                        }).setNegativeButton(android.R.string.cancel, null).create().show();
                break;
            default:
                break;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private List<SettingEntity> cloneSettings() {
        List<SettingEntity> list = new ArrayList<SettingEntity>();

        for (SettingEntity setting : settings) {
            try {
                list.add((SettingEntity) setting.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SettingUtils.SETTING_RINGTONE) {
            if (data != null) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                for (SettingEntity entity : settings) {
                    if (entity.getParam() == SettingUtils.SETTING_RINGTONE) {
                        entity.setValue(uri.toString());
                        dbOper.updateSetting(entity);
                        refreshData();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v, SettingEntity entity) {
        Switch switch1 = (Switch) v;

        if (switch1.isChecked()) {
            entity.setValue(String.valueOf(SettingUtils.STATE_ENABLED));
            dbOper.updateSetting(entity);
        } else {
            entity.setValue(String.valueOf(SettingUtils.STATE_DISABLED));
            dbOper.updateSetting(entity);
        }
    }

}
