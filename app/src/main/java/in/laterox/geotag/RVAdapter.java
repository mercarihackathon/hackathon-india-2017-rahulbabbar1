package in.laterox.geotag;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by rahul on 8/10/17.
 */



public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PointViewHolder>{

    private String TAG = "RVAdapter";
    @Override
    public PointViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragmant_card, viewGroup, false);
        PointViewHolder pvh = new PointViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PointViewHolder pointViewHolder, int i) {
        pointViewHolder.pointDown.setEnabled(false);
        if(points.get(i).name!=null)
            pointViewHolder.pointName.setText(points.get(i).name);
        else
            pointViewHolder.pointName.setText(points.get(i).path);
        Log.d(TAG, "onBindViewHolder() called with:" + points.size() + " pointViewHolder = [" + points.get(i).name+ "], i = [" + i + "]");
        if(points.get(i).description!=null)
            pointViewHolder.pointName.setText(points.get(i).description);
        else
            pointViewHolder.pointDesc.setText( ( (double)Math.round(points.get(i).latitude * 100000d) / 100000d)
                    + ", " +  ( (double)Math.round(points.get(i).longitude * 100000d) / 100000d) );
        //pointViewHolder.pointPhoto.setImageResource(points.get(i).photoId);

        pointViewHolder.pointLatlng.setText(( (double)Math.round(points.get(i).latitude * 100000d) / 100000d)
                + ", " +  ( (double)Math.round(points.get(i).longitude * 100000d) / 100000d) );
        getSize(points.get(i).path, pointViewHolder);
    }

    private void getSize(String path, final PointViewHolder pointViewHolder){
        final FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReference();

// Get reference to the file
        if(path==null)
            return;

        final StorageReference fileRef = storageRef.child(path);
        


        fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // Metadata now contains the metadata for 'images/forest.jpg'


                pointViewHolder.pointsize.setText((((double)storageMetadata.getSizeBytes())/1000d)+"KB");
                final String filename = storageMetadata.getName();


                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess() called with: uri = [" + uri + "]");
                        enableDownload(uri, pointViewHolder, filename, pointViewHolder.pointDown.getContext());
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

    private void enableDownload(final Uri uri, PointViewHolder pointViewHolder, final String filename, final Context context){
        pointViewHolder.pointDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    startDownload(uri,filename, context);
            }
        });
        pointViewHolder.pointDown.setEnabled(true);
    }

    public static void startDownload(Uri uri, String filename, Context context){
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription(uri.toString());
        request.setTitle(filename);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    public static class PointViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView pointDesc,pointLatlng,pointsize,pointName;
        ImageView pointPhoto, pointDown;

        PointViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            pointName = (TextView) (itemView.findViewById(R.id.point_name));
            pointDesc = (TextView) (itemView.findViewById(R.id.point_desc));
            pointLatlng = (TextView) (itemView.findViewById(R.id.point_latlng));
            pointsize = (TextView) (itemView.findViewById(R.id.point_size));
            pointPhoto = (ImageView)itemView.findViewById(R.id.point_photo);
            pointDown = (ImageView) itemView.findViewById(R.id.point_download);
        }
    }

    List<Point> points = new ArrayList<>();

    RVAdapter(List<Point> points){
        this.points = points;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void swap(List list){
        if (points != null) {
            points.clear();
            points = new ArrayList<Point>(list);
        }
        else {
            points = new ArrayList<Point>(list);
        }
        notifyDataSetChanged();
    }

    public void add(Set<Point> set){
        if (points == null) {
            points = new ArrayList<Point>();
        }
        points.clear();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Point mp = (Point) it.next();
            points.add(mp);
        }
        notifyDataSetChanged();
    }


}