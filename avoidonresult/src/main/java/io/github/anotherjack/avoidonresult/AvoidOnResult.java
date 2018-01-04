package io.github.anotherjack.avoidonresult;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;

import io.reactivex.Observable;

/**
 * Created by jack on 2017/12/27.
 */

public class AvoidOnResult {
    private static final String TAG = "AvoidOnResult";
    private AvoidOnResultFragment mAvoidOnResultFragment;

    /**
     * Builder模式构造实例
     *
     * @param activity
     * @return
     */
    public static AvoidOnResult init(Activity activity) {
        return new AvoidOnResult(activity);
    }

    /**
     * Builder模式构造实例
     *
     * @param fragment
     * @return
     */
    public static AvoidOnResult init(Fragment fragment) {
        return new AvoidOnResult(fragment);
    }

    public AvoidOnResult(Activity activity) {
        mAvoidOnResultFragment = getAvoidOnResultFragment(activity);
    }

    public AvoidOnResult(Fragment fragment) {
        this(fragment.getActivity());
    }

    private AvoidOnResultFragment getAvoidOnResultFragment(Activity activity) {
        AvoidOnResultFragment avoidOnResultFragment = findAvoidOnResultFragment(activity);
        if (avoidOnResultFragment == null) {
            avoidOnResultFragment = new AvoidOnResultFragment();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .add(avoidOnResultFragment, TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return avoidOnResultFragment;
    }

    private AvoidOnResultFragment findAvoidOnResultFragment(Activity activity) {
        return (AvoidOnResultFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }

    public Observable<ActivityResultInfo> startForResult(Intent intent, int requestCode) {
        return mAvoidOnResultFragment.startForResult(intent, requestCode);
    }

    public Observable<ActivityResultInfo> startForResult(Class<?> clazz, int requestCode) {
        Intent intent = new Intent(mAvoidOnResultFragment.getActivity(), clazz);
        return startForResult(intent, requestCode);
    }

    public void startForResult(Intent intent, int requestCode, Callback callback) {
        mAvoidOnResultFragment.startForResult(intent, requestCode, callback);
    }

    public void startForResult(Class<?> clazz, int requestCode, Callback callback) {
        Intent intent = new Intent(mAvoidOnResultFragment.getActivity(), clazz);
        startForResult(intent, requestCode, callback);
    }

    public interface Callback {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }
}
