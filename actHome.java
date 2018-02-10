package com.nomenta.newpro.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.nomenta.newpro.BLE.BleManager;
import com.nomenta.newpro.BLE.BleManager.BleStatusInterface;
import no.jonkjenn.BT.TesterManager;
import no.jonkjenn.BT.TesterManager.TesterManagerInterface;
import com.nomenta.newpro.C0609R;
import com.nomenta.newpro.entity.clsYahoo;
import com.nomenta.newpro.entity.ent_DeviceDetail;
import com.nomenta.newpro.entity.ent_Yahoo;
import com.nomenta.newpro.util.clsGeneral;
import com.nomenta.newpro.util.clsGeneral.FontStyle;
import com.nomenta.newpro.util.clsPref;
import com.nomenta.newpro.util.dbHelperOperations;
import io.fabric.sdk.android.Fabric;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class actHome extends Activity implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnGestureListener, BleStatusInterface, TesterManagerInterface {
    public static ArrayList<String> ARR_HISTORY = null;
    public static ArrayList<ent_Yahoo> ARR_YAHOO_FORCAST = null;
    private static int DISPLACEMENT = 10;
    private static int FATEST_INTERVAL = 500;
    public static boolean IsActive = false;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int REQUEST_ENABLE_BT = 1;
    private static int UPDATE_INTERVAL = 1000;
    dbHelperOperations DB_HELPER_OPERATION;
    ProgressDialog DIALOG;
    boolean IS_LOCATION_FIND = false;
    boolean IS_SEARCH_DEVICE = false;
    ImageView IV_ALERT;
    ImageView IV_SENSORIMAGE;
    ImageView IV_SETTING;
    ImageView IV_SHARE;
    ImageView IV_WEATHER_BACK;
    ImageView IV_WEATHER_ICON;
    RelativeLayout LAY_PARRENT;
    private LocationManager LOCATION_MANAGER;
    int POSITION_ALERT;
    ProgressBar PROGRESSBAR;
    TextView TV_CELCEUS;
    TextView TV_CITY_DATE;
    TextView TV_HUMIDITY;
    TextView TV_MONTH_DATE;
    TextView TV_SENSORNAME;
    TextView TV_SENSOR_NAME;
    TextView TV_TEMPRATURE;
    clsPref _pref;
    private GestureDetector gDetector;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private TesterManager testerManager;

    class C05921 implements OnLongClickListener {

        class C05912 implements DialogInterface.OnClickListener {
            C05912() {
            }

            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }

        C05921() {
        }

        public boolean onLongClick(View v) {
            if (actHome.this.TV_SENSOR_NAME.getCurrentTextColor() == -16711936) {
                Builder alertDialog = new Builder(actHome.this);
                alertDialog.setTitle((CharSequence) "");
                alertDialog.setMessage(actHome.this.getResources().getString(C0609R.string.Enter_device_name));
                final EditText EDT_NEW_NAME = new EditText(actHome.this);
                EDT_NEW_NAME.setText(actHome.this.TV_SENSOR_NAME.getText().toString());
                EDT_NEW_NAME.setSelection(EDT_NEW_NAME.getText().length());
                LayoutParams buttonLayoutParams = new LayoutParams(-1, -2);
                buttonLayoutParams.setMargins(30, 0, 30, 0);
                EDT_NEW_NAME.setLayoutParams(buttonLayoutParams);
                View layout = new LinearLayout(actHome.this);
                layout.setOrientation(1);
                layout.addView(EDT_NEW_NAME);
                alertDialog.setView(layout);
                alertDialog.setPositiveButton((int) C0609R.string.Save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (EDT_NEW_NAME.getText().toString().trim().equalsIgnoreCase("")) {
                            Toast.makeText(actHome.this.getApplicationContext(), C0609R.string.Enter_device_name, 0).show();
                            return;
                        }
                        try {
                            if (actHome.this.DB_HELPER_OPERATION.InsertDeviceDetail(EDT_NEW_NAME.getText().toString().trim(), clsGeneral.SELECTED_SENSOR_UUID, Boolean.valueOf(true)) != 0) {
                                clsGeneral.ShowToast(actHome.this, actHome.this.getResources().getString(C0609R.string.Device_name_updated_successfully));
                                actHome.this.TV_SENSOR_NAME.setText(EDT_NEW_NAME.getText().toString().trim());
                                return;
                            }
                            clsGeneral.ShowToast(actHome.this, actHome.this.getResources().getString(C0609R.string.TryAgain));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                alertDialog.setNegativeButton((int) C0609R.string.Cancel, new C05912());
                alertDialog.show();
            }
            return false;
        }
    }

    class C05953 extends TimerTask {
        C05953() {
        }

        public void run() {
            actHome.this.DIALOG.dismiss();
        }
    }

    class C05995 implements Runnable {

        class C05981 implements Runnable {
            C05981() {
            }

            public void run() {
                actHome.this.IV_SENSORIMAGE.setVisibility(0);
                actHome.this.TV_SENSOR_NAME.setText("NewPro");
                actHome.this.TV_SENSOR_NAME.setTextColor(-1);
                clsGeneral.ShowToast(actHome.this, actHome.this.getResources().getString(C0609R.string.disconnected));
            }
        }

        C05995() {
        }

        public void run() {
            new Handler().postDelayed(new C05981(), 2000);
        }
    }

    class C06006 implements DialogInterface.OnClickListener {
        C06006() {
        }

        public void onClick(DialogInterface dialog, int id) {
            actHome.this.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
        }
    }

    class C06017 implements DialogInterface.OnClickListener {
        C06017() {
        }

        public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
        }
    }

    private class asyncToYahoo extends AsyncTask<Void, Void, Void> {
        Exception EX;
        String LATITUDE;
        String LONGITUDE;

        public asyncToYahoo(String latitude, String longitude) {
            this.LATITUDE = latitude;
            this.LONGITUDE = longitude;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            actHome.this.PROGRESSBAR.setVisibility(0);
        }

        protected Void doInBackground(Void... params) {
            try {
                actHome.ARR_YAHOO_FORCAST = new ArrayList();
                List<Address> addresses = new Geocoder(actHome.this, Locale.getDefault()).getFromLocation(Double.parseDouble(this.LATITUDE), Double.parseDouble(this.LONGITUDE), 1);
                String cityName = ((Address) addresses.get(0)).getAddressLine(0);
                String stateName = ((Address) addresses.get(0)).getAddressLine(1);
                actHome.ARR_YAHOO_FORCAST = new clsYahoo(actHome.this).getYahooDetail(cityName + " , " + stateName + " , " + ((Address) addresses.get(0)).getAddressLine(2));
                Log.e("SIZE->", "" + actHome.ARR_YAHOO_FORCAST.size());
            } catch (Exception ex) {
                this.EX = ex;
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            actHome.this.PROGRESSBAR.setVisibility(8);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
            String dayOfTheWeek = new SimpleDateFormat("EEEE").format(new Date());
            String currentDateandTime = sdf.format(new Date());
            actHome.this.TV_CITY_DATE.setText(clsGeneral.CITY_NAME + " - " + dayOfTheWeek + ",");
            actHome.this.TV_MONTH_DATE.setText(currentDateandTime + " - " + clsGeneral.FORCAST_TEXT);
            if (actHome.ARR_YAHOO_FORCAST.size() == 0) {
                clsGeneral com_nomenta_newpro_util_clsGeneral = new clsGeneral();
                clsGeneral.ShowToast(actHome.this, actHome.this.getResources().getString(C0609R.string.Can_not_find_local_weather_data));
                return;
            }
            String _code = ((ent_Yahoo) actHome.ARR_YAHOO_FORCAST.get(0)).getCODE();
            if (!clsGeneral.IS_DEVICE_CONNECTED) {
                if (actHome.this._pref.getTempType().equalsIgnoreCase("C")) {
                    actHome.this.TV_TEMPRATURE.setText(clsGeneral.convertFahrenheCelcius_For_Pref(Float.parseFloat(clsGeneral.YAHOOTEMPRATURE)));
                    actHome.this.TV_CELCEUS.setText(C0609R.string.setting_celsius);
                } else {
                    actHome.this.TV_TEMPRATURE.setText(clsGeneral.YAHOOTEMPRATURE);
                    actHome.this.TV_CELCEUS.setText(C0609R.string.setting_ferenhit);
                }
                actHome.this.TV_HUMIDITY.setText(clsGeneral.YAHOOHUMIDITY);
            }
            if (_code.equalsIgnoreCase("31") || _code.equalsIgnoreCase("32") || _code.equalsIgnoreCase("36") || _code.equalsIgnoreCase("34")) {
                actHome.this.IV_WEATHER_BACK.setImageResource(C0609R.drawable.img_back_1);
                actHome.this.IV_WEATHER_ICON.setImageResource(C0609R.drawable.img_icon_1);
            } else if (_code.equalsIgnoreCase("26") || _code.equalsIgnoreCase("28") || _code.equalsIgnoreCase("30") || _code.equalsIgnoreCase("44")) {
                actHome.this.IV_WEATHER_BACK.setImageResource(C0609R.drawable.img_back_2);
                actHome.this.IV_WEATHER_ICON.setImageResource(C0609R.drawable.img_icon_2);
            } else if (_code.equalsIgnoreCase("29") || _code.equalsIgnoreCase("27") || _code.equalsIgnoreCase("33")) {
                actHome.this.IV_WEATHER_BACK.setImageResource(C0609R.drawable.img_back_3);
                actHome.this.IV_WEATHER_ICON.setImageResource(C0609R.drawable.img_icon_3);
            } else if (_code.equalsIgnoreCase("13") || _code.equalsIgnoreCase("14") || _code.equalsIgnoreCase("15") || _code.equalsIgnoreCase("16") || _code.equalsIgnoreCase("17") || _code.equalsIgnoreCase("25") || _code.equalsIgnoreCase("41") || _code.equalsIgnoreCase("42") || _code.equalsIgnoreCase("43") || _code.equalsIgnoreCase("46")) {
                actHome.this.IV_WEATHER_BACK.setImageResource(C0609R.drawable.img_back_4);
                actHome.this.IV_WEATHER_ICON.setImageResource(C0609R.drawable.img_icon_4);
            } else if (_code.equalsIgnoreCase("5") || _code.equalsIgnoreCase("6") || _code.equalsIgnoreCase("7") || _code.equalsIgnoreCase("18")) {
                actHome.this.IV_WEATHER_BACK.setImageResource(C0609R.drawable.img_back_5);
                actHome.this.IV_WEATHER_ICON.setImageResource(C0609R.drawable.img_icon_5);
            } else if (_code.equalsIgnoreCase("20") || _code.equalsIgnoreCase("22")) {
                actHome.this.IV_WEATHER_BACK.setImageResource(C0609R.drawable.img_back_6);
                actHome.this.IV_WEATHER_ICON.setImageResource(C0609R.drawable.img_icon_6);
            } else if (_code.equalsIgnoreCase("23") || _code.equalsIgnoreCase("24")) {
                actHome.this.IV_WEATHER_BACK.setImageResource(C0609R.drawable.img_back_7);
                actHome.this.IV_WEATHER_ICON.setImageResource(C0609R.drawable.img_icon_7);
            } else if (_code.equalsIgnoreCase("11") || _code.equalsIgnoreCase("12") || _code.equalsIgnoreCase("8") || _code.equalsIgnoreCase("9") || _code.equalsIgnoreCase("10") || _code.equalsIgnoreCase("35") || _code.equalsIgnoreCase("40")) {
                actHome.this.IV_WEATHER_BACK.setImageResource(C0609R.drawable.img_back_8);
                actHome.this.IV_WEATHER_ICON.setImageResource(C0609R.drawable.img_icon_8);
            } else if (_code.equalsIgnoreCase("19") || _code.equalsIgnoreCase("21")) {
                actHome.this.IV_WEATHER_BACK.setImageResource(C0609R.drawable.img_back_9);
                actHome.this.IV_WEATHER_ICON.setImageResource(C0609R.drawable.img_icon_10);
            } else if (_code.equalsIgnoreCase("1") || _code.equalsIgnoreCase("2") || _code.equalsIgnoreCase("3") || _code.equalsIgnoreCase("4") || _code.equalsIgnoreCase("37") || _code.equalsIgnoreCase("38") || _code.equalsIgnoreCase("39") || _code.equalsIgnoreCase("45") || _code.equalsIgnoreCase("47")) {
                actHome.this.IV_WEATHER_BACK.setImageResource(C0609R.drawable.img_back_10);
                actHome.this.IV_WEATHER_ICON.setImageResource(C0609R.drawable.img_icon_10);
            } else {
                actHome.this.IV_WEATHER_BACK.setImageResource(C0609R.drawable.img_back_11);
                actHome.this.IV_WEATHER_ICON.setImageResource(C0609R.drawable.img_icon_11);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(C0609R.layout.act_home);
        this.gDetector = new GestureDetector(this);
        initpagecontrol();
        IsActive = true;
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
            if (this.mGoogleApiClient.isConnected()) {
                startLocationUpdates();
            }
        }
        boolean b = isLocationEnabled(this);
        if (!clsGeneral.IS_DEVICE_CONNECTED) {
            this.DIALOG.setMessage(getResources().getString(C0609R.string.Connecting_device));
            this.DIALOG.show();
            startBle();
        } else if (clsGeneral.TEMPRATURE.equalsIgnoreCase("")) {
            sendCommand(clsGeneral.setCurrentDataCommand());
        }
        if (b) {
            getYahooDetail();
        } else {
            showGPSDisabledAlertToUser();
        }
    }

    protected void onDestroy() {
        IsActive = false;
        super.onDestroy();
    }

    public boolean onTouchEvent(MotionEvent event) {
        return this.gDetector.onTouchEvent(event);
    }

    private void getYahooDetail() {
        String _latitude = this._pref.getLatitude();
        String _longitude = this._pref.getLongitude();
        if (_latitude.equalsIgnoreCase("") && _longitude.equalsIgnoreCase("")) {
            this.IS_LOCATION_FIND = false;
            clsGeneral com_nomenta_newpro_util_clsGeneral = new clsGeneral();
            clsGeneral.ShowToast(getApplicationContext(), getResources().getString(C0609R.string.err_no_locationfound));
            return;
        }
        new asyncToYahoo(_latitude, _longitude).execute(new Void[0]);
    }

    private void initpagecontrol() {
        new clsGeneral().changeLang(this, new clsPref(this).getLanguage());
        ARR_HISTORY = new ArrayList();
        this.LAY_PARRENT = (RelativeLayout) findViewById(C0609R.id.layMain);
        this.PROGRESSBAR = (ProgressBar) findViewById(C0609R.id.progress);
        this.IV_SETTING = (ImageView) findViewById(C0609R.id.ivSetting);
        this.IV_ALERT = (ImageView) findViewById(C0609R.id.ivAlert);
        this.IV_SHARE = (ImageView) findViewById(C0609R.id.ivShare);
        this.IV_WEATHER_BACK = (ImageView) findViewById(C0609R.id.ivWeatherback);
        this.IV_WEATHER_ICON = (ImageView) findViewById(C0609R.id.ivWeathericon);
        this.IV_SENSORIMAGE = (ImageView) findViewById(C0609R.id.img_sensor);
        this.TV_SENSORNAME = (TextView) findViewById(C0609R.id.tv_sensor);
        this.TV_SENSOR_NAME = (TextView) findViewById(C0609R.id.tvSensorName);
        this.IS_SEARCH_DEVICE = false;
        this.TV_HUMIDITY = (TextView) findViewById(C0609R.id.tvHumidity);
        this.TV_TEMPRATURE = (TextView) findViewById(C0609R.id.tvDegree);
        this.TV_CELCEUS = (TextView) findViewById(C0609R.id.tv_celceus);
        this.IV_SETTING.setOnClickListener(this);
        this.IV_ALERT.setOnClickListener(this);
        this.IV_SHARE.setOnClickListener(this);
        this.TV_CITY_DATE = (TextView) findViewById(C0609R.id.tvCityDate);
        this.TV_MONTH_DATE = (TextView) findViewById(C0609R.id.tvMonthDate);
        String _sensorname = new clsPref(this).getActiveSensor();
        if (_sensorname.equalsIgnoreCase("")) {
            _sensorname = "Sensor 1";
        }
        this.TV_SENSORNAME.setText(_sensorname);
        this.DIALOG = new ProgressDialog(this);
        this.DIALOG.setCanceledOnTouchOutside(false);
        this._pref = new clsPref(this);
        this.ARR_DEVICE_LIST = new ArrayList();
        this.DB_HELPER_OPERATION = new dbHelperOperations(this);
        if (this._pref.getSoundMode().booleanValue()) {
            this.IV_ALERT.setImageResource(C0609R.drawable.ic_notifications_white_48dp);
        } else {
            this.IV_ALERT.setImageResource(C0609R.drawable.ic_notifications_off_white_48dp);
        }
        new clsGeneral().changeFonts(this, this.LAY_PARRENT, FontStyle.light);
        this.TV_SENSOR_NAME.setOnLongClickListener(new C05921());
    }

    protected void onResume() {
        if (!clsGeneral.IS_DEVICE_CONNECTED && clsGeneral.YAHOOTEMPRATURE.equalsIgnoreCase("")) {
            if (this._pref.getTempType().equalsIgnoreCase("C")) {
                this.TV_TEMPRATURE.setText("");
                this.TV_CELCEUS.setText(C0609R.string.setting_celsius);
            } else {
                this.TV_TEMPRATURE.setText(clsGeneral.YAHOOTEMPRATURE);
                this.TV_CELCEUS.setText(C0609R.string.setting_ferenhit);
            }
            this.TV_HUMIDITY.setText(clsGeneral.YAHOOHUMIDITY);
        }
        if (!(clsGeneral.IS_DEVICE_CONNECTED || clsGeneral.YAHOOTEMPRATURE.equalsIgnoreCase(""))) {
            if (this._pref.getTempType().equalsIgnoreCase("C")) {
                this.TV_TEMPRATURE.setText(clsGeneral.convertFahrenheCelcius_For_Pref(Float.parseFloat(clsGeneral.YAHOOTEMPRATURE)));
                this.TV_CELCEUS.setText(C0609R.string.setting_celsius);
            } else {
                this.TV_TEMPRATURE.setText(clsGeneral.YAHOOTEMPRATURE);
                this.TV_CELCEUS.setText(C0609R.string.setting_ferenhit);
            }
            this.TV_HUMIDITY.setText(clsGeneral.YAHOOHUMIDITY);
        }
        super.onResume();
    }

    public void onClick(View v) {
        ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
        switch (v.getId()) {
            case C0609R.id.ivShare:
                if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
                    ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, 1);
                    return;
                }
                startActivity(new Intent(this, actCamera.class));
                overridePendingTransition(C0609R.anim.anim_up, C0609R.anim.anim_down);
                return;
            case C0609R.id.ivSetting:
                this.IS_SEARCH_DEVICE = true;
                this.DIALOG.setMessage("Connecting device...");
                this.DIALOG.show();
                startBle();
                return;
            case C0609R.id.ivAlert:
                if (this._pref.getSoundMode().booleanValue()) {
                    this._pref.putSoundMode(Boolean.valueOf(false));
                    this.IV_ALERT.setImageResource(C0609R.drawable.ic_notifications_off_white_48dp);
                    return;
                }
                this._pref.putSoundMode(Boolean.valueOf(true));
                this.IV_ALERT.setImageResource(C0609R.drawable.img_bell);
                return;
            default:
                return;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == 0) {
                    startActivity(new Intent(this, actCamera.class));
                    overridePendingTransition(C0609R.anim.anim_up, C0609R.anim.anim_down);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onLocationChanged(Location location) {
        try {
            this.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
            if (this.mLastLocation != null) {
                double _lat = this.mLastLocation.getLatitude();
                double _long = this.mLastLocation.getLongitude();
                if (!(_lat == 0.0d && _long == 0.0d)) {
                    this._pref.putLongitude(String.valueOf(_long));
                    this._pref.putLatitude(String.valueOf(_lat));
                    if (!this.IS_LOCATION_FIND) {
                        this.IS_LOCATION_FIND = true;
                        getYahooDetail();
                    }
                }
                Log.e("LOCATION0--->", "" + _lat + "," + _long);
                stopLocationUpdates();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTesterNotified(String data) {
        final String DATA = data;
        if (this.DIALOG.isShowing()) {
            this.DIALOG.dismiss();
        }
        if (ARR_HISTORY.size() > 5) {
            ARR_HISTORY = new ArrayList();
        }
        runOnUiThread(new Runnable() {

            class C05931 implements Runnable {
                C05931() {
                }

                public void run() {
                    int nLen = DATA.length();
                    if (nLen >= 16) {
                        actHome.ARR_HISTORY.add(DATA);
                    } else if (nLen == 12 && DATA.substring(0, 2).equalsIgnoreCase("FF") && DATA.substring(2, 4).equalsIgnoreCase("7f")) {
                        int Temprature = clsGeneral.HexToDecimal(DATA.substring(4, 6)).intValue();
                        int Humidity = clsGeneral.HexToDecimal(DATA.substring(6, 8)).intValue();
                        clsGeneral.CURRENT_TEMPRATURE = String.valueOf(Temprature);
                        clsGeneral.CURRENT_HUMIDITY = String.valueOf(Humidity);
                        clsGeneral.CURRENT_DATA = DATA.toString().trim();
                    }
                    try {
                        int _maxHumidity = Integer.parseInt(actHome.this._pref.getMaxHumidity());
                        int _minHumidity = Integer.parseInt(actHome.this._pref.getMinHumidity());
                        int _maxTemprature = Integer.parseInt(clsGeneral.convertCelciusToFahrenheit(Float.parseFloat(actHome.this._pref.getMaxTemprature()), actHome.this));
                        int _minTemprature = Integer.parseInt(clsGeneral.convertCelciusToFahrenheit(Float.parseFloat(actHome.this._pref.getMinTemprature()), actHome.this));
                        int _currentTemprature = Integer.parseInt(clsGeneral.convertCelciusToFahrenheit(Float.parseFloat(clsGeneral.CURRENT_TEMPRATURE), actHome.this));
                        int _currentHumidity = Integer.parseInt(clsGeneral.convertCelciusToFahrenheit(Float.parseFloat(clsGeneral.CURRENT_HUMIDITY), actHome.this));
                        Log.e("-----v--->>>", _currentTemprature + "," + _currentHumidity + "===" + _maxHumidity + "," + _minHumidity);
                        if (!(clsGeneral.IS_ALERT_SHOWN || _currentTemprature == 0)) {
                            clsGeneral.IS_ALERT_SHOWN = true;
                            Intent iv;
                            if (_currentHumidity >= _minHumidity || clsGeneral.IS_HUMIDITY_ALERT_SHOWN) {
                                if (_currentHumidity > _maxHumidity) {
                                    if (!clsGeneral.IS_HUMIDITY_ALERT_SHOWN) {
                                        clsGeneral.IS_HUMIDITY_ALERT_SHOWN = true;
                                        iv = new Intent(actHome.this, act_AlarmDialog.class);
                                        iv.addFlags(DriveFile.MODE_READ_ONLY);
                                        actHome.this.startActivity(iv);
                                    }
                                }
                                if (_currentTemprature < _minTemprature) {
                                    if (!clsGeneral.IS_TEMPRATURE_ALERT_SHOWN) {
                                        Log.e("-------->", "" + _currentTemprature);
                                        clsGeneral.IS_TEMPRATURE_ALERT_SHOWN = true;
                                        iv = new Intent(actHome.this, act_AlarmDialog.class);
                                        iv.addFlags(DriveFile.MODE_READ_ONLY);
                                        iv.putExtra("key", "Low temprature: " + _currentTemprature);
                                        actHome.this.startActivity(iv);
                                    }
                                }
                                if (_currentTemprature > _maxTemprature && !clsGeneral.IS_TEMPRATURE_ALERT_SHOWN) {
                                    Log.e("-------->", "" + _currentTemprature);
                                    clsGeneral.IS_TEMPRATURE_ALERT_SHOWN = true;
                                    iv = new Intent(actHome.this, act_AlarmDialog.class);
                                    iv.addFlags(DriveFile.MODE_READ_ONLY);
                                    iv.putExtra("key", "High temprature: " + _currentTemprature);
                                    actHome.this.startActivity(iv);
                                }
                            } else {
                                clsGeneral.IS_HUMIDITY_ALERT_SHOWN = true;
                                iv = new Intent(actHome.this, act_AlarmDialog.class);
                                iv.addFlags(DriveFile.MODE_READ_ONLY);
                                actHome.this.startActivity(iv);
                            }
                        }
                    } catch (Exception e) {
                        clsGeneral.IS_ALERT_SHOWN = false;
                    }
                    actHome.this.TV_TEMPRATURE.setText(clsGeneral.convertCelciusToFahrenheit(Float.parseFloat(clsGeneral.CURRENT_TEMPRATURE), actHome.this));
                    actHome.this.TV_HUMIDITY.setText(clsGeneral.CURRENT_HUMIDITY);
                    actHome.this.TV_CELCEUS = (TextView) actHome.this.findViewById(C0609R.id.tv_celceus);
                    if (actHome.this._pref.getTempType().equalsIgnoreCase("C")) {
                        actHome.this.TV_CELCEUS.setText(C0609R.string.setting_celsius);
                    } else {
                        actHome.this.TV_CELCEUS.setText(C0609R.string.setting_ferenhit);
                    }
                }
            }

            public void run() {
                new Handler().postDelayed(new C05931(), 100);
            }
        });
    }

    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    public void onConnectionSuspended(int i) {
        this.mGoogleApiClient.connect();
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onFling(MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
        if (start.getRawY() >= finish.getRawY() && this.PROGRESSBAR.getVisibility() != 0) {
            try {
                startActivity(new Intent(this, act_Forcast.class));
                overridePendingTransition(C0609R.anim.anim_up, C0609R.anim.anim_down);
            } catch (Exception e) {
            }
        }
        return false;
    }

    private void startBle() {
        this.BLE_MANAGER.initBleAdapter();
        this.BLE_MANAGER.setBleStatusInterface(this);
        if (this.BLE_MANAGER.isBleOn()) {
            this.BLE_MANAGER.startAutoConnect();
            this.DIALOG.show();
            dailogDismissCountDown();
            return;
        }
        startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 1) {
            return;
        }
        if (resultCode != 0) {
            this.BLE_MANAGER.startAutoConnect();
            this.DIALOG.show();
            dailogDismissCountDown();
            return;
        }
        this.DIALOG.dismiss();
    }

    public void dailogDismissCountDown() {
        new Timer().schedule(new C05953(), 15000);
        if (this.IS_SEARCH_DEVICE) {
            this.IS_SEARCH_DEVICE = false;
            DeviceListAlert();
        }
    }

    public void onBleDeviceFound(String deviceName, String deviceAddress) {
        Log.e("onBleDeviceFound", "" + deviceName + "," + deviceAddress);
    }

    public void onBleConnected(final BluetoothDevice bluetoothDevice) {
        Log.e("ACTHOME", "Connected");
        clsGeneral.IS_TEMPRATURE_ALERT_SHOWN = false;
        clsGeneral.IS_HUMIDITY_ALERT_SHOWN = false;
        runOnUiThread(new Runnable() {

            class C05961 implements Runnable {
                C05961() {
                }

                public void run() {
                    clsGeneral.SELECTED_SENSOR_UUID = bluetoothDevice.getAddress();
                    actHome.this.IV_SENSORIMAGE.setVisibility(4);
                    try {
                        String DeviceName_DB = actHome.this.DB_HELPER_OPERATION.getDeviceName(bluetoothDevice.getAddress());
                        if (DeviceName_DB.equalsIgnoreCase("")) {
                            actHome.this.TV_SENSOR_NAME.setText(bluetoothDevice.getName());
                        } else {
                            actHome.this.TV_SENSOR_NAME.setText(DeviceName_DB);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    actHome.this.TV_SENSOR_NAME.setTextColor(-16711936);
                    clsGeneral.ShowToast(actHome.this, actHome.this.getResources().getString(C0609R.string.connected));
                    actHome.this.sendCommand(clsGeneral.setCurrentDataCommand());
                }
            }

            public void run() {
                new Handler().postDelayed(new C05961(), 2000);
            }
        });
    }

    public void onBleDisconnected() {
        Log.e("ACTHOME", "Disconnected");
        runOnUiThread(new C05995());
    }

    public void writableCharacteristicPrepared() {
        startTester();
    }

    public void onRssiChanged(int rssi) {
    }

    private void startTester() {
        if (this.testerManager == null) {
            this.testerManager = TesterManager.getInstance();
            this.testerManager.setTesterManagerInterface(this);
            this.BLE_MANAGER.setBleStatusInterface(this);
        }
    }

    private void sendCommand(String command) {
        if (this.testerManager == null) {
            this.testerManager = TesterManager.getInstance();
            this.testerManager.setTesterManagerInterface(this);
            this.BLE_MANAGER.setBleStatusInterface(this);
        }
        this.testerManager.sendCommand(command);
    }

    public String TimeDeference() {
        String DefernceTime = "";
        String currentDateandTime = new SimpleDateFormat("HH:mm").format(new Date());
        return DefernceTime;
    }

    public double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        long factor = (long) Math.pow(10.0d, (double) places);
        return ((double) Math.round(value * ((double) factor))) / ((double) factor);
    }

    private void showGPSDisabledAlertToUser() {
        Builder alertDialogBuilder = new Builder(this);
        alertDialogBuilder.setMessage((CharSequence) "GPS is disabled in your device. Would you like to enable it?").setCancelable(false).setPositiveButton((CharSequence) "Enable Location", new C06006());
        alertDialogBuilder.setNegativeButton((CharSequence) "Cancel", new C06017());
        alertDialogBuilder.create().show();
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        if (VERSION.SDK_INT >= 19) {
            try {
                locationMode = Secure.getInt(context.getContentResolver(), "location_mode");
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
            }
            if (locationMode != 0) {
                return true;
            }
            return false;
        } else if (TextUtils.isEmpty(Secure.getString(context.getContentResolver(), "location_providers_allowed"))) {
            return false;
        } else {
            return true;
        }
    }

    public void DeviceListAlert() {
        this.ARR_DEVICE_LIST = new ArrayList();
        LinearLayout LAY_CONTAINT = null;
        View deviceDialogView = LayoutInflater.from(this).inflate(C0609R.layout.custom_dialog_device, null);
        Builder dialogBuilder = new Builder(this);
        dialogBuilder.setView(deviceDialogView);
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout ll_main = (LinearLayout) deviceDialogView.findViewById(C0609R.id.layDevice);
        try {
            this.ARR_DEVICE_LIST = this.DB_HELPER_OPERATION.getDeviceList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < this.ARR_DEVICE_LIST.size(); i++) {
            this.POSITION_ALERT = i;
            View dialogView = inflater.inflate(C0609R.layout.row_device_selection, null);
            ((TextView) dialogView.findViewById(C0609R.id.tvSensorName)).setText(((ent_DeviceDetail) this.ARR_DEVICE_LIST.get(i)).getDEVICE_NAME());
            LAY_CONTAINT = (LinearLayout) dialogView.findViewById(C0609R.id.LAY_DEVICE_DETAIL);
            ll_main.addView(dialogView);
        }
        final AlertDialog alertDialog = dialogBuilder.create();
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        alertDialog.getWindow().addFlags(2);
        wlp.gravity = 80;
        window.setAttributes(wlp);
        try {
            LAY_CONTAINT.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    alertDialog.dismiss();
                    actHome.this.BLE_MANAGER.connect(((ent_DeviceDetail) actHome.this.ARR_DEVICE_LIST.get(actHome.this.POSITION_ALERT)).getDEVICE_UUID());
                    actHome.this.DIALOG.show();
                    actHome.this.dailogDismissCountDown();
                }
            });
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(17170445));
        alertDialog.show();
    }

    protected void onStart() {
        super.onStart();
        if (this.mGoogleApiClient != null) {
            this.mGoogleApiClient.connect();
        }
    }

    protected void onStop() {
        super.onStop();
        if (this.mGoogleApiClient.isConnected()) {
            this.mGoogleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == 0) {
            return true;
        }
        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1000).show();
        } else {
            Toast.makeText(getApplicationContext(), "This device is not supported.", 1).show();
            finish();
        }
        return false;
    }

    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() {
        this.mLocationRequest = new LocationRequest();
        this.mLocationRequest.setInterval((long) UPDATE_INTERVAL);
        this.mLocationRequest.setFastestInterval((long) FATEST_INTERVAL);
        this.mLocationRequest.setPriority(100);
        this.mLocationRequest.setSmallestDisplacement((float) DISPLACEMENT);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, this.mLocationRequest, (LocationListener) this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, (LocationListener) this);
    }
}
