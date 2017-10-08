package in.laterox.geotag;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by rahul on 8/10/17.
 */

public class UploadActivity extends AppCompatActivity {

    private double latitude,longitude;
    private String name = "" , description = "";
    private TextView nameTV, descTV;
    private String type = "Official";

    ProgressDialog progress ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Intent data = getIntent();
        latitude = data.getDoubleExtra("lat", -1);
        longitude = data.getDoubleExtra("long", -1);

        nameTV = (TextView) findViewById(R.id.filename);
        descTV = (TextView) findViewById(R.id.description);

        findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchFile();
            }
        });
        final Button uploadButton = (Button)findViewById(R.id.button_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadButton.setEnabled(false);
                description = descTV.getText().toString();
                name = nameTV.getText().toString();
                uploadFile(uri, new LatLng(latitude, longitude));
            }
        });



        MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.spinner);
        spinner.setItems("Official", "Study", "Transit", "Infrastructure", "Entertainment", "Historic", "Offers" ,"Food");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();
                type = item;
            }
        });
    }

    private static final int READ_REQUEST_CODE = 42;

    public void fetchFile(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    Uri uri = null;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                uri = resultData.getData();
                Button button =(Button)findViewById(R.id.button_add);
                button.setText(uri.toString());
                button.setEnabled(false);
            }
        }
    }

    void uploadFile(Uri uri,final LatLng latLng){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        StorageReference riversRef = storageRef.child(user.getUid()+"/"+uri.getLastPathSegment());

        UploadTask uploadTask = riversRef.putFile(uri);

        progress = new ProgressDialog(this);
        progress.setMessage("Uploading :) ");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progress.cancel();
                Toast.makeText(UploadActivity.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String path = taskSnapshot.getMetadata().getPath();
                updateDatabase(latLng, path);
                progress.cancel();
                Toast.makeText(UploadActivity.this, "Upload Successfull!", Toast.LENGTH_SHORT).show();
                finish();
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
        myRef.child(key).child("name").setValue(name);
        myRef.child(key).child("desc").setValue(description);
        myRef.child(key).child("type").setValue(type);

    }

}
