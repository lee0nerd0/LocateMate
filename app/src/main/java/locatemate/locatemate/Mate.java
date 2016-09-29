package locatemate.locatemate;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Timestamp;
import java.util.UUID;

/**
 * Created by lee0nerd0 on 9/26/2016.
 */

public class Mate {
    private UUID id;
    private String userName;
    private String password;
    private String groupName;
    private Integer iconid;
    private LatLng latLng;
    private Timestamp timestamp;

    // getters and setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public static String toJSON(Mate mate) {

        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", mate.getId());
            jsonObject.put("userName", mate.getUserName());
            jsonObject.put("password", mate.getPassword());
            jsonObject.put("groupName", mate.getGroupName());
            jsonObject.put("iconid", mate.getIconid());
            jsonObject.put("latlng", mate.getLatLng());
            jsonObject.put("timestamp", mate.getTimestamp());

            return jsonObject.toString();

        } catch (JSONException e){

            e.printStackTrace();

        }

        return null;

    }
}
