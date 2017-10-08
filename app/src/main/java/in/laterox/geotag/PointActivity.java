package in.laterox.geotag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.w3c.dom.Text;

import java.util.List;

import static in.laterox.geotag.RVAdapter.startDownload;

/**
 * Created by rahul on 8/10/17.
 */

public class PointActivity extends AppCompatActivity {

    private TextView nameTV, descTV, typeTV, sizeTV, latlngTV;
    private Button downB;
    Point mPoint = new Point();
    private String TAG = "point";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point);

        Intent data = getIntent();
        mPoint.latitude = data.getDoubleExtra("lat", -1);
        mPoint.longitude = data.getDoubleExtra("long", -1);
        mPoint.path = data.getStringExtra("path");

        nameTV = (TextView) findViewById(R.id.filename);
        descTV = (TextView) findViewById(R.id.description);

        typeTV = (TextView) findViewById(R.id.spinner);

        downB = (Button) findViewById(R.id.button_down);
        downB.setEnabled(false);

        latlngTV =(TextView) findViewById(R.id.latlng);

        sizeTV =(TextView) findViewById(R.id.size);



        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("global");
        Query pathQuery = myRef.orderByChild("path").startAt(mPoint.path).endAt(mPoint.path);


        updateValues();


        pathQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
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

                updateValues();
                getSize(mPoint.path);
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

    private void getSize(String path){
        final FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReference();

        final StorageReference fileRef = storageRef.child(path);



        fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // Metadata now contains the metadata for 'images/forest.jpg'


                sizeTV.setText((((double)storageMetadata.getSizeBytes())/1000d)+"KB");
                final String filename = storageMetadata.getName();


                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess() called with: uri = [" + uri + "]");
                        enableDownload(uri, filename);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

                //Log.d(TAG, "onSuccess() called with: storageMetadata = [" + storageMetadata.getSizeBytes() + "]");
                //Log.d(TAG, "onSuccess() called with: storageMetadata = [" + storageMetadata + "]");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "onFailure() called with: exception = [" + exception + "]");
                // Uh-oh, an error occurred!
            }
        });



    }

    private void enableDownload(final Uri uri,final String filename){
        downB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDownload(uri,filename, PointActivity.this);
            }
        });
        downB.setEnabled(true);
    }

    void updateValues(){
        nameTV.setText(mPoint.name);
        descTV.setText(mPoint.description);
        typeTV.setText(mPoint.type);
        latlngTV.setText(( (double)Math.round(mPoint.latitude * 100000d) / 100000d)
                + ", " +  ( (double)Math.round(mPoint.longitude * 100000d) / 100000d) );
    }
}
