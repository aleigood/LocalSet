package com.lee.smart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lee.smart.comm.Constants;
import com.lee.smart.comm.LocationUtils;
import com.lee.smart.comm.SettingUtils;
import com.lee.smart.comm.Utils;
import com.lee.smart.comm.cache.Cachable;
import com.lee.smart.comm.cache.ImageCache;
import com.lee.smart.comm.cache.ImageCache.ImageCacheParams;
import com.lee.smart.comm.cache.ImageFetcher;
import com.lee.smart.data.DatabaseOper;
import com.lee.smart.data.LocationEntity;
import com.lee.smart.provider.DefaultProvider;
import com.lee.smart.provider.GooglePlayProvider;
import com.lee.smart.provider.ILocationProvider;
import com.lee.smart.provider.ILocationProvider.LocationChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements Cachable {
    public static final String REFRESH_ACTION = "com.lee.smart.action.REFRESH_ACTION";
    private static final String IMAGE_CACHE_DIR = "cache";
    private ListView mListView;
    private DatabaseOper mDBOper;
    private List<LocationEntity> mLocations;
    private ImageFetcher mImageFetcher;
    private ImageCache mCache;
    private LocationAdapter mAdapter;
    private RefreshReceiver mReceiver;
    private PopupWindow mPopupWindow;
    private TextView mTxtInfo;
    private Point mScreenSize;
    private int mThumbHeight;
    private int mThumbWidth;
    private ILocationProvider mProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDBOper = MyApplication.getInstance().getDataOper();
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        mScreenSize = new Point();
        display.getSize(mScreenSize);
        mThumbHeight = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_height);
        mThumbWidth = (mScreenSize.x - getResources().getDimensionPixelSize(R.dimen.list_padding) * 2);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstLaunch = pref.getBoolean(Constants.PREF_FIRST_LAUNCH, true);

        if (isFirstLaunch) {
            SettingUtils.saveCurSettings(this);
            pref.edit().putBoolean(Constants.PREF_FIRST_LAUNCH, false).commit();
        }

        mListView = (ListView) findViewById(R.id.list);
        mTxtInfo = (TextView) findViewById(R.id.info);
        mLocations = mDBOper.getAllLocation();
        mAdapter = new LocationAdapter(this);
        mListView.setAdapter(mAdapter);

        mPopupWindow = new PopupWindow(new View(this));
        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        mPopupWindow.setWidth(getResources().getDimensionPixelSize(R.dimen.popup_window_width));
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_menu_dropdown));
        mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        mReceiver = new RefreshReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(REFRESH_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);

        ImageCacheParams cacheParams = new ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f);
        mImageFetcher = new ImageFetcher(this, mThumbWidth, mThumbHeight);
        mImageFetcher.addImageCache(this, cacheParams);

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startSettingsActivity(mLocations.get(position));
            }
        });
    }

    private void startSettingsActivity(LocationEntity entity) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.putExtra("data", entity);
        startActivity(intent);
    }

    private void addLocation() {
        if (!Utils.checkConnection(this)) {
            Toast.makeText(this, R.string.no_network_conn, Toast.LENGTH_LONG).show();
            return;
        }

        boolean isPlayServicesAvailable = LocationUtils.isPlayServicesAvailable(this);

        if (isPlayServicesAvailable) {
            mProvider = new GooglePlayProvider(this);
        } else {
            mProvider = new DefaultProvider(this);
        }

        mProvider.setLocationChangeListener(new LocationChangeListener() {
            @Override
            public void onLocationChanged(final Location location) {
                final View view = getLayoutInflater().inflate(R.layout.view_add_location, null);
                final EditText name = (EditText) view.findViewById(R.id.nameEdit);
                final EditText range = (EditText) view.findViewById(R.id.rangeEdit);
                final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
                mImageFetcher.loadImage(LocationUtils.getMapUrl(location.getLatitude(), location.getLongitude(),
                        mThumbWidth, mThumbHeight), imageView);

                new AlertDialog.Builder(MainActivity.this).setView(view).setTitle(R.string.add_location)
                        .setNeutralButton(android.R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (name.getText().toString().equals("") || range.getText().toString().equals("")) {
                                    return;
                                }

                                LocationEntity entity = new LocationEntity();
                                entity.setName(name.getText().toString());
                                entity.setLatitude(location.getLatitude());
                                entity.setLongitude(location.getLongitude());
                                entity.setRange(Integer.parseInt(range.getText().toString()));
                                entity.setStatus(LocationEntity.STATE_ENABLE);
                                mDBOper.insertLocation(entity);
                                refreshData();
                            }
                        }).setNegativeButton(android.R.string.cancel, null).create().show();

                // 只获取一次
                mProvider.stopPeriodicUpdates();
            }
        });

        mProvider.startPeriodicUpdates();
        Toast.makeText(this, R.string.get_location, Toast.LENGTH_LONG).show();
    }

    private void deleteLocation(LocationEntity entity) {
        mDBOper.deleteLocation(entity.getId());
        refreshData();
    }

    private void modLocation(LocationEntity entity) {
        mDBOper.updateLocation(entity);
        refreshData();
    }

    private void disableLocation(LocationEntity entity) {
        entity.setStatus(LocationEntity.STATE_DISABLE);
        mDBOper.updateLocation(entity);
        refreshData();
    }

    private void enableLocation(LocationEntity entity) {
        entity.setStatus(LocationEntity.STATE_ENABLE);
        mDBOper.updateLocation(entity);
        refreshData();
    }

    private void refreshData() {
        mLocations = mDBOper.getAllLocation();
        mAdapter.setData(mLocations);
        boolean hasEnabledLocation = false;

        for (LocationEntity entity : mLocations) {
            if (entity.getStatus() == LocationEntity.STATE_ACTIVE || entity.getStatus() == LocationEntity.STATE_ENABLE) {
                hasEnabledLocation = true;
            }
        }

        Intent service = new Intent(this, LocationService.class);

        if (hasEnabledLocation) {
            startService(service);
        } else {
            stopService(service);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_location:
                addLocation();
                break;
            case R.id.default_settings:
                Intent intent = new Intent(this, DefaultSettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        refreshData();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
        unregisterReceiver(mReceiver);
    }

    @Override
    public ImageCache getCache() {
        return mCache;
    }

    @Override
    public void setCache(ImageCache cache) {
        this.mCache = cache;
    }

    class LocationAdapter extends BaseAdapter {
        List<LocationEntity> mList;
        private Activity mContext;

        public LocationAdapter(Activity context) {
            mContext = context;
            mList = new ArrayList<LocationEntity>();
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
            final LocationEntity entity = mList.get(position);
            ViewHolder viewHolder = null;

            if (convertView == null) {
                convertView = mContext.getLayoutInflater().inflate(R.layout.view_item_location, null);
                viewHolder = new ViewHolder();
                viewHolder.image = (ImageView) convertView.findViewById(R.id.imageView);
                viewHolder.status = (ImageView) convertView.findViewById(R.id.status);
                viewHolder.txt1 = (TextView) convertView.findViewById(R.id.text1);
                viewHolder.txt2 = (TextView) convertView.findViewById(R.id.text2);
                viewHolder.setting = convertView.findViewById(R.id.settings);
                viewHolder.pop = convertView.findViewById(R.id.bottom);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.txt1.setText(entity.getName());
            viewHolder.txt2.setText(entity.getRange() + " M");
            mImageFetcher.loadImage(
                    LocationUtils.getMapUrl(entity.getLatitude(), entity.getLongitude(), mThumbWidth, mThumbHeight),
                    viewHolder.image);
            viewHolder.status.setImageResource(getStatusRes(entity.getStatus()));
            viewHolder.pop.setTag(entity.getId());

            viewHolder.setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListView popListView = new ListView(MainActivity.this);
                    final ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();

                    if (entity.getStatus() == LocationEntity.STATE_DISABLE) {
                        Map<String, String> map2 = new HashMap<String, String>();
                        map2.put("name", "Enable");
                        data.add(map2);
                    } else {
                        Map<String, String> map2 = new HashMap<String, String>();
                        map2.put("name", "Disable");
                        data.add(map2);
                    }

                    Map<String, String> map1 = new HashMap<String, String>();
                    map1.put("name", "Edit");
                    data.add(map1);

                    Map<String, String> map3 = new HashMap<String, String>();
                    map3.put("name", "Delete");
                    data.add(map3);

                    popListView.setAdapter(new SimpleAdapter(MainActivity.this, data,
                            android.R.layout.simple_list_item_1, new String[]{"name"},
                            new int[]{android.R.id.text1}));
                    popListView.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            switch (position) {
                                case 0:
                                    if (entity.getStatus() == LocationEntity.STATE_DISABLE) {
                                        enableLocation(entity);
                                    } else {
                                        disableLocation(entity);
                                    }
                                    break;
                                case 1:
                                    final View modView = getLayoutInflater().inflate(R.layout.view_add_location, null);
                                    final EditText name = (EditText) modView.findViewById(R.id.nameEdit);
                                    name.setText(entity.getName());
                                    final EditText range = (EditText) modView.findViewById(R.id.rangeEdit);
                                    range.setText(String.valueOf(entity.getRange()));
                                    final ImageView imageView = (ImageView) modView.findViewById(R.id.imageView);
                                    mImageFetcher.loadImage(LocationUtils.getMapUrl(entity.getLatitude(),
                                            entity.getLongitude(), mThumbWidth, mThumbHeight), imageView);

                                    new AlertDialog.Builder(MainActivity.this).setView(modView)
                                            .setTitle(R.string.eidt_location)
                                            .setNeutralButton(android.R.string.ok, new OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    entity.setName(name.getText().toString());
                                                    entity.setRange(Integer.parseInt(range.getText().toString()));
                                                    modLocation(entity);
                                                }
                                            }).setNegativeButton(android.R.string.cancel, null).create().show();
                                    break;
                                case 2:
                                    new AlertDialog.Builder(MainActivity.this).setTitle(R.string.delete_location)
                                            .setMessage(R.string.delete_location_msg)
                                            .setNegativeButton(android.R.string.cancel, null)
                                            .setNeutralButton(android.R.string.ok, new OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    deleteLocation(entity);
                                                }
                                            }).show();
                                    break;
                                default:
                                    break;
                            }

                            mPopupWindow.dismiss();
                        }
                    });

                    mPopupWindow.setContentView(popListView);
                    mPopupWindow.showAsDropDown(v, 0, -17);
                }
            });

            return convertView;
        }

        public List<LocationEntity> getData() {
            return mList;
        }

        public void setData(List<LocationEntity> list) {
            mList = list;
            notifyDataSetChanged();
        }

        private int getStatusRes(int status) {
            switch (status) {
                case LocationEntity.STATE_DISABLE:
                    return R.drawable.ic_status_disable;
                case LocationEntity.STATE_ENABLE:
                    return R.drawable.ic_status_inactive;
                case LocationEntity.STATE_ACTIVE:
                    return R.drawable.ic_status_active;
                default:
                    return R.drawable.ic_status_disable;
            }
        }

        class ViewHolder {
            private ImageView image;
            private ImageView status;
            private TextView txt1;
            private TextView txt2;
            private View setting;
            private View pop;
        }
    }

    class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                return;
            }

            if (action.equals(REFRESH_ACTION)) {
                refreshData();
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (Utils.checkConnection(MainActivity.this)) {
                    mTxtInfo.setVisibility(View.GONE);
                } else {
                    mTxtInfo.setVisibility(View.VISIBLE);
                    mTxtInfo.setText(R.string.no_network_conn_msg);
                }
            }
        }
    }
}
