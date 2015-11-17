package com.example.hzy.traffic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextToSpeech textToSpeech;
    private Timer timer;
    private Handler handler;
    private Map<String, Marker> markerDictionary;
    private Map<String, MarkerInfo> markerInfos;
    private Map<String, String> markerDescriptions;
    private Map<String, String> chineseDescriptions;
    private Map<String, String> englishDescriptions;
    private int[] marker_image_size = new int[] {300, 250};

    public MapsActivity() {
        timer = new Timer();
        handler = new Handler();
        markerDictionary = new HashMap<>();

        markerInfos = new HashMap<>();
        chineseDescriptions = new HashMap<>();
        englishDescriptions = new HashMap<>();
        // Add marker here
        markerInfos.put("H201F", new MarkerInfo("H201F", new LatLng(22.277974, 114.168412)));
        chineseDescriptions.put("H201F", "軒尼詩道近軍器廠街");
        englishDescriptions.put("H201F", "Hennessy Road near Arsenal Street");

        markerInfos.put("ST703F", new MarkerInfo("ST703F", new LatLng(22.371, 114.1739)));
        chineseDescriptions.put("ST703F", "大埔公路近港鐵馬場站");
        englishDescriptions.put("ST703F", "Tai Po Road near MTR Racecourse Station");

        markerInfos.put("K101F", new MarkerInfo("K101F", new LatLng(22.294853, 114.172466)));
        chineseDescriptions.put("K101F", "梳士巴利道近彌敦道");
        englishDescriptions.put("K101F", "Salisbury Road near Nathan Road");

        // Set default description to english
        markerDescriptions = englishDescriptions;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create Android TextToSpeech Instance
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        // Update every 3 minutes
        timer.schedule(createTimeTask(), 10000, 180000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.english_voice:
                textToSpeech.setLanguage(Locale.US);
                markerDescriptions = englishDescriptions;
                return true;
            case R.id.chinese_voice:
                textToSpeech.setLanguage(Locale.CHINA);
                markerDescriptions = chineseDescriptions;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*// Setting a custom info window adapter for the google map
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View view = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                return view;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                new ImageFetchTask(marker).execute(marker.getSnippet());
                textToSpeech.speak(marker.getTitle(), TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });*/

        // add the marker
        for (Map.Entry<String, MarkerInfo> entry: markerInfos.entrySet()) {
            Marker marker = mMap.addMarker(new MarkerOptions().position(entry.getValue().getLatLng())
                .title(entry.getValue().getKey()).snippet(entry.getValue().getUrl()));
            markerDictionary.put(entry.getKey(), marker);
            new ImageFetchTask(marker).execute(marker.getSnippet());
        }

        // Move Camera to Tai Po Road
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerInfos.get("ST703F").getLatLng(), 15));


        // Disable toolbar at bottom right
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                new ImageFetchTask(marker).execute(marker.getSnippet());
                textToSpeech.speak(markerDescriptions.get(marker.getTitle()), TextToSpeech.QUEUE_FLUSH, null, null);
                return true;
            }
        });
    }

    private class ImageFetchTask extends AsyncTask<String, Bitmap, Void> {

        private Marker marker;

        public ImageFetchTask(Marker marker) {
            this.marker = marker;
        }

        @Override
        protected Void doInBackground(String... urls) {
            Bitmap original_image = downloadImage(urls[0]);
            if (original_image == null) {
                publishProgress();
            } else {
                Bitmap image = Bitmap.createScaledBitmap(original_image, marker_image_size[0], marker_image_size[1], false);
                publishProgress(image);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Bitmap... images) {
            if (!isCancelled()) {
                if (images.length != 0) {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(images[0]));
                } else {
                    Toast.makeText(getApplicationContext(), "Can't Download Image", Toast.LENGTH_SHORT).show();
                }
            }
            super.onProgressUpdate(images);
        }
    }

    private Bitmap downloadImage(String url) {
        Bitmap bitmap = null;
        try {
            // Download Image from URL
            InputStream input = new URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(input);
        }
        catch (IOException e) {
//            e.printStackTrace();
        }
        return bitmap;
    }

    private TimerTask createTimeTask() {
        return new TimerTask() {
            @Override
            public void run() {
                for (Map.Entry<String, MarkerInfo> entry: markerInfos.entrySet()) {
                    // Download Every image and set to the markerInfo map
                    Bitmap original_image = downloadImage(entry.getValue().getUrl());
                    if (original_image != null) {
                        entry.getValue().setImage(Bitmap.createScaledBitmap(original_image, marker_image_size[0], marker_image_size[1], false));
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Update Marker Icon in main thread
                        for (Map.Entry<String, MarkerInfo> entry: markerInfos.entrySet()) {
                            Bitmap image = entry.getValue().getImage();
                            if (image != null) {
                                markerDictionary.get(entry.getKey()).setIcon(BitmapDescriptorFactory.fromBitmap(image));
                            }
                        }
                    }
                });
            }
        };
    }

}
