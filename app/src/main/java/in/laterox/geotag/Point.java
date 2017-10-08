package in.laterox.geotag;

import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by rahul on 8/10/17.
 */

public class Point {
    double latitude;
    double longitude;
    String path;
    String name, description,type;
    int size;
    String url;

    public Point(double latitude, double longitude, String path) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.path = path;
    }

    public Point(){    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(latitude*1000000);
        result = prime * result + (int)(longitude*100000);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        Log.d("test", "equals() this"+ this.toString() +" with: obj = [" + obj.toString() + "]");
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Point other = (Point) obj;
        if (latitude != other.latitude)
            return false;

        Log.d("TAG", "" + longitude + " & " + other.longitude + "");

        if (longitude != other.longitude){
            return false;
        }


        Log.d("TAG2", "" + path + " & " + other.path + "");
        if(path!=null && other.path!=null && !path.equals(other.path))
            return false;


        if(name!=null && other.name!=null && !name.equals(other.name))
            return false;
        if(type!=null && other.type!=null && !type.equals(other.type))
            return false;
        if(description!=null && other.description!=null && !description.equals(other.description))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return latitude + ", " + longitude + ", " + path+ ", " +  name + ", " + description + ", " + type;
    }
}

