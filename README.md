原理讲解文章：[避免使用onActivityResult，以提高代码可读性](https://juejin.im/post/5a4611786fb9a0451a76b565)

配合AOP的实践文章：[一个注解搞定登录注册、实名认证及购买vip等业务流程](https://juejin.im/post/5b29cfb3518825748e54595b)

# 优雅地封装和使用onActivityResult
* 无需重写onActivityResult，以回调的方式拿到startActivityForResult的结果，解决了以往跳转页面和处理结果分离的痛点。

* 同时无需重写onActivityResult也意味着只要你可以拿到一个Activity实例，就可以通过它startActivityForResult，甚至它是一个第三方库中的Activity也一样。

* 支持RxJava方式调用

# Setup
1. in your root build.gradle

	```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	```
2. add to dependencies

	```
	dependencies {
	        implementation 'com.github.AnotherJack:AvoidOnResult:1.0.0'
	}
	```
	
# Usage
* callback方式

	```
	new AvoidOnResult(activity).startForResult(XXActivity.class, new AvoidOnResult.Callback() {
            @Override
            public void onActivityResult(int resultCode, Intent data) {
                if (resultCode == Activity.RESULT_OK){
                    // get your data from intent
                    
                }else {
                    
                }
            }
        });
	```
	
* rxjava方式

	```
	new AvoidOnResult(activity).startForResult(MainActivity.class)
                //可自由变换
                .filter(new Predicate<ActivityResultInfo>() {
                    @Override
                    public boolean test(ActivityResultInfo activityResultInfo) throws Exception {
                        return activityResultInfo.getResultCode() == Activity.RESULT_OK;
                    }
                })
                .subscribe(new Observer<ActivityResultInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        
                    }

                    @Override
                    public void onNext(ActivityResultInfo activityResultInfo) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
	```

# 如果对你有所帮助，给个star吧
