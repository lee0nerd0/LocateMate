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
    private Integer iconid;
    private LatLng latLng;
    private String timestamp;

    Mate (String id, String uN, String pw, String gN, Integer i, LatLng loc, String ts) {
        setId(id);
        setUserName(uN);
        setPassword(pw);
        setGroupName(gN);
        setIconid(i);
        setLatLng(loc);
        setTimestamp(ts);
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

    public Integer getIconid() {
        return iconid;
    }

    public void setIconid(Integer iconid) {
        this.iconid = iconid;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
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
            jsonObject.put("icon", getIconid());
            jsonObject.put("lat", getLatLng().latitude);
            jsonObject.put("lon", getLatLng().longitude);
            jsonObject.put("timestamp", getTimestamp());

            return jsonObject.toString();

        } catch (JSONException e){

            e.printStackTrace();

        }

        return null;

    }
}
