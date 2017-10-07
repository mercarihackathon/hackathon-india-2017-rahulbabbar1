package in.laterox.geotag;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Created by rahul on 8/10/17.
 */

public class UploadActivity extends AppCompatActivity {

    private double latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Intent data = getIntent();
        latitude = data.getDoubleExtra("lat", -1);
        longitude = data.getDoubleExtra("long", -1);

    }

    private static final int READ_REQUEST_CODE = 42;

    public void fetchFile(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                uploadFile(uri, new LatLng(latitude, longitude));
            }
        }
    }

    void uploadFile(Uri uri,final LatLng latLng){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        StorageReference riversRef = storageRef.child(user.getUid()+"/"+uri.getLastPathSegment());

        UploadTask uploadTask = riversRef.putFile(uri);


        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String path = taskSnapshot.getMetadata().getPath();
                updateDatabase(latLng, path);
            }
        });
    }

    void updateDatabase(final LatLng latLng, String path){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("global");
        String key = myRef.push().getKey();
        myRef.child(key).child("lat").setValue(latLng.latitude);
        myRef.child(key).child("long").setValue(latLng.longitude);
        myRef.child(key).child("path").setValue(path);

    }

}
