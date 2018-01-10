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
 * modify by https://guofeng007.github.io remove request by  auto genereate requestcode 2017/1/10
 */

public class AvoidOnResultFragment extends Fragment {
    private Map<Integer, PublishSubject<ActivityResultInfo>> mSubjects = new HashMap<>();
    private Map<Integer, AvoidOnResult.Callback> mCallbacks = new HashMap<>();
    private volatile int  currentRequestCode;

    public AvoidOnResultFragment() {
    }

    /**
     * 自动为每个 activity 中发出的请求构造 requestcode
     * 循环使用
     * @return
     */
    private synchronized int generateAutoRequestCode(){
        if(currentRequestCode == Integer.MAX_VALUE){
            currentRequestCode =Integer.MIN_VALUE ;
        }
         currentRequestCode++;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public Observable<ActivityResultInfo> startForResult(final Intent intent) {
        PublishSubject<ActivityResultInfo> subject = PublishSubject.create();
        final int autoRequestCode = generateAutoRequestCode();
        mSubjects.put(autoRequestCode, subject);
        return subject.doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                startActivityForResult(intent, autoRequestCode);
            }
        });
    }

    public void startForResult(Intent intent, AvoidOnResult.Callback callback) {
        final int autoRequestCode = generateAutoRequestCode();
        mCallbacks.put(autoRequestCode, callback);
        startActivityForResult(intent, autoRequestCode);
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

}
