package xh.xshare;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by G1494458 on 2017/7/20.
 */

public abstract class BaseFragment extends Fragment {

    protected Activity parentActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            parentActivity = (Activity) context;
        } else {
            throw new IllegalArgumentException("Fragment must attached to Activity");
        }
    }
}
