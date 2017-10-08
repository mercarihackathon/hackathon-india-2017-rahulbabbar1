package in.laterox.geotag;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 51;
    LocationService locationService;
    boolean mBound = false;
    Marker currentLoc, selected;
    boolean mLocationPermissionGranted = false;
    boolean listenLoc = true;
    double rad = 0.01;

    static Set<Point> queued = new HashSet<>();
    static Set<Point> added = new HashSet<>();
    private GoogleMap mMap;
    private double latitude, longitude;
    private String TAG = "MapsActivity";
    private BottomSheetBehavior bottomSheetBehavior;

    ProgressDialog progress ;
    TextView leftSubtitle;


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            latitude = intent.getDoubleExtra("latitude", -1);
            longitude = intent.getDoubleExtra("longitude", -1);
            updateLocation();
            fetchList(new LatLng(latitude, longitude));
            progress.cancel();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        progress = new ProgressDialog(this);
        progress.setMessage("Initializing :) ");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        initRecyclerView();

        getLocationPermission();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchFile();
            }
        });

        leftSubtitle = (TextView) findViewById(R.id.left_subtitle);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        Intent locationServiceIntent = new Intent(MapsActivity.this, LocationService.class);
        bindService(locationServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
//        startService(locationServiceIntent);

        currentLoc = mMap.addMarker(new MarkerOptions().position(new LatLng(-31, 54)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_loc)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));

        updateLocation();

        setMapListener();

    }

    private void setMapListener() {
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver,
                        new IntentFilter("locationFetch"));
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    private void updateLocation() {
        currentLoc.setPosition(new LatLng(latitude, longitude));
        if (listenLoc)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
    }

    public void fetchFile() {
        Intent uploadActivity = new Intent(MapsActivity.this, UploadActivity.class);
        if (selected == null) {
            selected = mMap.addMarker(new MarkerOptions().position(currentLoc.getPosition()).title(currentLoc.getPosition().toString()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc.getPosition()));
        }
        uploadActivity.putExtra("lat", selected.getPosition().latitude);
        uploadActivity.putExtra("lat", selected.getPosition().longitude);
        startActivity(uploadActivity);
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            progress.setMessage("Getting Location :) ");
            progress.setMessage("Getting Location :) ");
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    progress.setMessage("Getting Location :) ");
                }
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        listenLoc = false;
        if (selected == null) {
            selected = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(latLng.toString()).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.ic_add_loc)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        } else {
            selected.setPosition(latLng);
            selected.setTitle(latLng.toString());
        }
    }

    void addListener(Query query){
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Point mPoint = new Point();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    switch (postSnapshot.getKey()) {
                        case "lat":
                            mPoint.latitude = (Double) postSnapshot.getValue();
                            break;
                        case "long":
                            mPoint.longitude = (Double) postSnapshot.getValue();
                            break;

                        case "path":
                            mPoint.path = (String) postSnapshot.getValue();
                            break;
                        case "name":
                            mPoint.name = (String) postSnapshot.getValue();
                            break;
                        case "type":
                            mPoint.type = (String) postSnapshot.getValue();
                            break;
                    }
                }
                if (!queued.contains(mPoint)) {
                    queued.add(mPoint);
                    Log.d(TAG, "onChildAdded() called with: queud = [" + queued.size() + "], s = [" + s + "]");
                    addToMap();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    void fetchList(LatLng latLng) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("global");
        Query latQuery = myRef.orderByChild("lat").startAt(latLng.latitude - rad).endAt(latLng.latitude + rad);
        Query longQuery = myRef.orderByChild("long").startAt(latLng.longitude - rad).endAt(latLng.longitude + rad);

        addListener(latQuery);
        addListener(longQuery);
    }

    void addToMap() {
        Iterator it = queued.iterator();
        while (it.hasNext()) {
            Point mp = (Point) it.next();
            Log.d(TAG, "addToMap() called" + mp.latitude + mp.longitude );
            if (!added.contains(mp)) {
                added.add(mp);
                mMap.addMarker(new MarkerOptions().position(new LatLng(mp.latitude, mp.longitude)).title("Title"));
                points.add(mp);
                Log.d(TAG, "test " + points.size());

                if(adapter!=null){
//                    adapter.swap(points);
                    adapter.add(added);
                    leftSubtitle.setText(added.size() + " tags found nearby.");
                    //Log.d(TAG, "test() called");
                    //Log.d(TAG, "test " + points.size());
                }
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker){
        Intent pointActivity = new Intent(MapsActivity.this, PointActivity.class);
        startActivity(pointActivity);
        return false;
    }



    private List<Point> points = new ArrayList<>();

    RVAdapter adapter;

    private void initRecyclerView(){

        RecyclerView rv = (RecyclerView)findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(MapsActivity.this);
        rv.setLayoutManager(llm);

        adapter = new RVAdapter(points);
        rv.setAdapter(adapter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

}

