package in.laterox.geotag;

/**
 * Created by rahul on 8/10/17.
 */

public class Point {
    double latitude;
    double longitude;
    String path;
    public Point(double latitude, double longitude, String path) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.path = path;
    }

    public Point(){    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Point other = (Point) obj;
        if (latitude != other.latitude)
            return false;
        if (longitude != other.longitude)
            return false;
        if(path != other.path)
            return false;
        return true;
    }
}

