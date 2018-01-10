---
layout: post
title: 优雅地封装和使用 onActivityResult 
categories: Blog
description:  onActivityResult
keywords:   onActivityResult

---



# 优雅地封装和使用 onActivityResult 

 

**作者：国风**

## [原文](https://guofeng007.github.io/2018/01/05/onActivityResult/)

# 更新
调用时去掉 requestcode，更加简单清晰，内部使用 callback.hashcode 作为 requestcode 

```java
AvoidOnResult(this).startForResult(FetchDataActivity::class.java, object : AvoidOnResult.Callback {
                override fun onActivityResult(resultCode: Int, data: Intent?) =
                        if (resultCode == Activity.RESULT_OK) {
                            val text = data?.getStringExtra("text")
                            Toast.makeText(this@MainActivity, "callback -> " + text, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "callback canceled", Toast.LENGTH_SHORT).show()
                        }

            })
```

# 一、背景
在日常Android开发中，通过startActivityForResult跳转页面获取数据，然后在onActivityResult中处理的返回数据。这种方式想必大家早就习以为常了，我想大家再用的时候多少都会有些疑问？：为啥统一个逻辑分散在两个地方，而且 onActivityResult 经常会堆积着各种 if else。有没有办法能够像 setOnClickListener 或者 RxJava 那样，直接在一个地方处理调用和回调？答案是肯定的，这个问题以前我也想过，无奈之前不怎么善于真正发现问题、提出问题，而且解决问题的能力也不够。


[问题的提出者，和部分解决者](https://juejin.im/post/5a4611786fb9a0451a76b565?utm_source=gold_browser_extension#heading-5)，文中作者有一种思路，就是想通过 hook 系统 onActivityResult 回调，统一分发，但是当时并未找到思路。而我之前做过插件框架，热修复，hybrid,Router。对 Hook 自己认为还是驾轻就熟的，而且已经 hook 过作者所找寻的点，所以就在该基础上写了 Hook版本[github](https://github.com/guofeng007/OnResultManagerHook)。

接下来介绍下 onActivityResult实现是三种思路，以及各自的优缺点。

# 二、三种 onActivityResult 方案

## 2.1 Lifecycle add Fragment 方案
Lifecycle 监听 activity 的声明周期原理是，它内部持有一个Fragment，这个fragment没有视图，只负责请求权限和返回结果，相当于一个桥梁的作用，在这个 Fragment 中，会把所有的回调转发出去，实现监听。

这里直接借用 anotherJack 的demo

```java
public class AvoidOnResult {
    private static final String TAG = "AvoidOnResult";
    private AvoidOnResultFragment mAvoidOnResultFragment;

    public AvoidOnResult(Activity activity) {
        mAvoidOnResultFragment = getAvoidOnResultFragment(activity);
    }

    public AvoidOnResult(Fragment fragment){
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
}```


监听的 Fragment 如下：
 ```java
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

    public Observable<ActivityResultInfo> startForResult(final Intent intent, final int requestCode) {
        PublishSubject<ActivityResultInfo> subject = PublishSubject.create();
        mSubjects.put(requestCode, subject);
        return subject.doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                startActivityForResult(intent, requestCode);
            }
        });
    }

    public void startForResult(Intent intent, int requestCode, AvoidOnResult.Callback callback) {
        mCallbacks.put(requestCode, callback);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //rxjava方式的处理
        PublishSubject<ActivityResultInfo> subject = mSubjects.remove(requestCode);
        if (subject != null) {
            subject.onNext(new ActivityResultInfo(requestCode, resultCode, data));
            subject.onComplete();
        }

        //callback方式的处理
        AvoidOnResult.Callback callback = mCallbacks.remove(requestCode);
        if (callback != null) {
            callback.onActivityResult(requestCode, resultCode, data);
        }
    }
}
```

扩展rxjava调用
```java
//callback方式
callback.setOnClickListener {
    AvoidOnResult(this).startForResult(FetchDataActivity::class.java, REQUEST_CODE_CALLBACK, object : AvoidOnResult.Callback {
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
                if (resultCode == Activity.RESULT_OK) {
                    val text = data?.getStringExtra("text")
                    Toast.makeText(this@MainActivity, "callback -> " + text, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "callback canceled", Toast.LENGTH_SHORT).show()
                }

    })
}

//rxjava方式
rxjava.setOnClickListener {
    AvoidOnResult(this)
            .startForResult(FetchDataActivity::class.java, REQUEST_CODE_RXJAVA)
            //下面可自由变换
            .filter { it.resultCode == Activity.RESULT_OK }
            .flatMap {
                val text = it.data.getStringExtra("text")
                Observable.fromIterable(text.asIterable())
            }
            .subscribe({
                Log.d("-------> ", it.toString())
            }, {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
            }, {
                Toast.makeText(this, "complete", Toast.LENGTH_SHORT).show()
            })
}

```


## 2.2 BaseActivity +  ResultManager 方案
在 BaseActivity 的 startActivityForResult和 onActivityResult中分别主动触发
OnResultManager.startForResult和 trigger 回调
```java
public class OnResultManager {
    private static final String TAG = "OnResultManager";
    //HashMap的key Integer为requestCode
    private static WeakHashMap<Activity,HashMap<Integer,Callback>> mCallbacks = new WeakHashMap<>();
    private WeakReference<Activity> mActivity;

    public OnResultManager(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
    }

    public void startForResult(Intent intent, int requestCode, Callback callback){
        Activity activity = getActivity();
        if(activity == null){
            return;
        }

        addCallback(activity,requestCode,callback);
        activity.startActivityForResult(intent,requestCode);
    }

    public void trigger(int requestCode, int resultCode, Intent data){
        Log.d(TAG,"----------- trigger");
        Activity activity = getActivity();
        if(activity == null){
            return;
        }

        Callback callback = findCallback(activity,requestCode);
        if(callback != null){
            callback.onActivityResult(requestCode,resultCode,data);
        }
    }

    //获取该activity、该requestCode对应的callback
    private Callback findCallback(Activity activity,int requestCode){
        HashMap<Integer,Callback> map = mCallbacks.get(activity);
        if(map != null){
            return map.remove(requestCode);
        }
        return null;
    }

    private void addCallback(Activity activity,int requestCode,Callback callback){
        HashMap<Integer,Callback> map = mCallbacks.get(activity);
        if(map == null){
            map = new HashMap<>();
            mCallbacks.put(activity,map);
        }
        map.put(requestCode,callback);
    }

    private Activity getActivity(){
        return mActivity.get();
    }

    public interface Callback{
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }
}
```
## 2.3 Hook 方案
本质上也是用 2.2 的 OnResultManager ,只不过是不用在 BaseActivity中转发了，而是直接 Hook 系统的 onActivityResult
Hook onActivityResult 方法如下：
```java
/**
 * Created by guofeng05 on 2018/1/4.
 */

public class ActivityThreadCallbackHook {
    // Copy from ActivityThread.mH Handler
    public static final int SEND_RESULT = 108;

    public static void hook() {
        try {
            ActivityThread activityThread = ActivityThread.currentActivityThread();
            // 由于ActivityThread一个进程只有一个,我们获取这个对象的mH
            Field mHField;
            mHField = ActivityThread.class.getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler mH = (Handler) mHField.get(activityThread);

            // 设置它的回调, 根据源码:
            // 我们自己给他设置一个回调,就会替代之前的回调;

            //        public void dispatchMessage(Message msg) {
            //            if (msg.callback != null) {
            //                handleCallback(msg);
            //            } else {
            //                if (mCallback != null) {
            //                    if (mCallback.handleMessage(msg)) {
            //                        return;
            //                    }
            //                }
            //                handleMessage(msg);
            //            }
            //        }

            Field mCallBackField = Handler.class.getDeclaredField("mCallback");
            mCallBackField.setAccessible(true);
            // 塞入我们的 hook 对象
            mCallBackField.set(mH, new MyHandlerCallback(mH));
            Log.d("hook","success");
        } catch (Exception e) {
            // hook 失败，整个 callback 就 gg了
            e.printStackTrace();
        }
    }

    private static class MyHandlerCallback implements Handler.Callback {

        private Handler mOldHandler;
        public MyHandlerCallback(Handler mOldHandler) {
            this.mOldHandler = mOldHandler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            // 不干扰系统分发逻辑
            mOldHandler.handleMessage(msg);

            // 通知 ResultManager
            if (msg.what == SEND_RESULT) {
                Object obj = msg.obj;
                try {
                    // step 1 reflect to get activity
                    Object token = ReflectUtils.on(obj).get("token");
                    ArrayMap mActivities = (ArrayMap) ReflectUtils.on(ActivityThread.currentActivityThread()).get("mActivities");
                    Object activityClientRecord =  mActivities.get(token);
                    Activity activity = (Activity) ReflectUtils.on(activityClientRecord).get("activity");

                    // step2 reflect to get ResultInfo
                    // 注意这里的分发，无法分发到 Fragment 内部，所以采用动态塞入一个 Fragment 是最稳定的方案
                    ArrayList<ResultInfo> results = (ArrayList<ResultInfo>) ReflectUtils.on(obj).get("results");
                    for (ResultInfo result : results) {
                        OnResultManager.getInstance().trigger(activity, result.mRequestCode, result.mResultCode, result.mData);
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }

            //  default
            return true;
        }

    }
}
```
# 3.总结

1. fragment 版本是稳定性，易用性最好的
2. BaseActivity 那种其次
3. Hook 不建议线上使用，一是兼容性，二是各种插件化等 hook 相互影响，问题排插难度大