package locatemate.locatemate;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by lee0nerd0 on 9/28/2016.
 */

public class SharedRef {
    SharedPreferences sharedPreferences;

    public SharedRef (Context context) {
        sharedPreferences = context.getSharedPreferences("myRef", Context.MODE_PRIVATE);
    }

    public void saveData(String groupName, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("groupName", groupName);
        editor.putString("password", password);
        editor.commit();
    }

    public String loadData(){
        String data = sharedPreferences.getString("groupName", "default group");
        data += sharedPreferences.getString("password", "");
        return data;
    }
}
