package io.github.anotherjack.avoidonresult;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by jack on 2017/12/27.
 * modify by https://guofeng007.github.io remove request code ,instead use callback.hashcode as requestcode 2017/1/10
 */

public class AvoidOnResultFragment extends Fragment {
    private Map<Integer, PublishSubject<ActivityResultInfo>> mSubjects = new HashMap<>();
    private Map<Integer, AvoidOnResult.Callback> mCallbacks = new HashMap<>();

    public AvoidOnResultFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public Observable<ActivityResultInfo> startForResult(final Intent intent) {
        final PublishSubject<ActivityResultInfo> subject = PublishSubject.create();
        return subject.doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                mSubjects.put(shorthashCode(subject), subject);
                startActivityForResult(intent, shorthashCode(subject));
            }
        });
    }

    public void startForResult(Intent intent, AvoidOnResult.Callback callback) {

        mCallbacks.put(shorthashCode(callback), callback);
        startActivityForResult(intent, shorthashCode(callback));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //rxjava方式的处理
        PublishSubject<ActivityResultInfo> subject = mSubjects.remove(requestCode);
        if (subject != null) {
            subject.onNext(new ActivityResultInfo(resultCode, data));
            subject.onComplete();
        }

        //callback方式的处理
        AvoidOnResult.Callback callback = mCallbacks.remove(requestCode);
        if (callback != null) {
            callback.onActivityResult( resultCode, data);
        }
    }

    /**
     * 这个方法用于解决下面这个异常抛出
     * java.lang.IllegalArgumentException: Can only use lower 16 bits for requestCode
     * @param callback
     * @return
     */
    private int shorthashCode(Object callback) {
        return callback.hashCode() & 0x0000ffff;
    }

}
