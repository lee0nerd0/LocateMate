package locatemate.locatemate;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by lee0nerd0 on 9/26/2016.
 */

public class Mate {
    private String id;
    private String userName;
    private String password;
    private String groupName;
    private Integer icon;
    private String lat;
    private String lon;
    private String timestamp;
    private LatLng loc;

    Mate (String id, String uN, String pw, String gN, Integer i, String lat, String lon, String ts) {
        setId(id);
        setUserName(uN);
        setPassword(pw);
        setGroupName(gN);
        setIcon(i);
        setLat(lat);
        setLon(lon);
        setTimestamp(ts);
        setLatLng();
    }

    // getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }

    public LatLng getLatLng() {
        return loc;
    }

    public void setLatLng() {
        this.loc = new LatLng(Double.valueOf(this.lat), Double.valueOf(this.lon));
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String toJSON() {

        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", getId());
            jsonObject.put("userName", getUserName());
            jsonObject.put("password", getPassword());
            jsonObject.put("groupName", getGroupName());
            jsonObject.put("icon", getIcon());
            jsonObject.put("lat", getLat());
            jsonObject.put("lon", getLon());
            jsonObject.put("timestamp", getTimestamp());

            return jsonObject.toString();

        } catch (JSONException e){

            e.printStackTrace();

        }

        return null;

    }
}
