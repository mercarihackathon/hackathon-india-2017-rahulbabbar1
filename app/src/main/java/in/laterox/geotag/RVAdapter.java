package in.laterox.geotag;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        if(points.get(i).name!=null)
            pointViewHolder.pointName.setText(points.get(i).name);
        else
            pointViewHolder.pointName.setText(points.get(i).path);
        Log.d(TAG, "onBindViewHolder() called with:" + points.size() + " pointViewHolder = [" + points.get(i).name+ "], i = [" + i + "]");
        if(points.get(i).description!=null)
            pointViewHolder.pointName.setText(points.get(i).description);
        else
            pointViewHolder.pointDesc.setText(points.get(i).latitude + ", " +  points.get(i).longitude);
        //pointViewHolder.pointPhoto.setImageResource(points.get(i).photoId);
    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    public static class PointViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView pointName;
        TextView pointDesc;
//        ImageView pointPhoto;

        PointViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            pointName = (TextView) (itemView.findViewById(R.id.point_name));
            pointDesc = (TextView) (itemView.findViewById(R.id.point_desc));
//            pointPhoto = (ImageView)itemView.findViewById(R.id.point_photo);
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