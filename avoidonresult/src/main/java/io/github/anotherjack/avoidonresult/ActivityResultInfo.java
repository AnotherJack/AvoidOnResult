package io.github.anotherjack.avoidonresult;

import android.content.Intent;

/**
 * Created by jack on 2017/12/27.
 */

public class ActivityResultInfo {
    private int resultCode;
    private Intent data;

    public ActivityResultInfo(int resultCode, Intent data) {
        this.resultCode = resultCode;
        this.data = data;
    }


    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public Intent getData() {

        return data;
    }

    public void setData(Intent data) {
        this.data = data;
    }
}
