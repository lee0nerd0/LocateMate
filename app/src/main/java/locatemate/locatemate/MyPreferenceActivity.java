package locatemate.locatemate;

import android.preference.PreferenceActivity;

import java.util.List;

/**
 * Created by lee0nerd0 on 9/14/2016.
 */
public class MyPreferenceActivity extends PreferenceActivity{
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.headers_preference, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return MyPreferenceFragment.class.getName().equals(fragmentName);
    }
}
