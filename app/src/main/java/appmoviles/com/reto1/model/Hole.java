package appmoviles.com.reto1.model;

public class Hole {
    private String id;
    private double lat;
    private double lng;
    private boolean isConfirmed;

    public Hole() {
    }

    public Hole(String id, double lat, double lng, boolean isConfirmed) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.isConfirmed = isConfirmed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }
}
