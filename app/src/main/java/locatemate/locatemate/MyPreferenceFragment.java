package locatemate.locatemate;

import android.os.Bundle;

/**
 * Created by lee0nerd0 on 9/14/2016.
 */
public class MyPreferenceFragment extends android.preference.PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.fragment_preference);
    }
}
