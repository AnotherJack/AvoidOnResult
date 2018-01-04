package io.github.anotherjack.avoidonresultdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import io.github.anotherjack.avoidonresult.AvoidOnResult;

import static io.github.anotherjack.avoidonresultdemo.MultiFragmentActiivty.REQUEST_CODE_CALLBACK;

/**
 * @author zhengkaituo
 * @date 2018/1/4
 */
public class SecondTestFragment extends Fragment {

    LinearLayout rootLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootLayout = new LinearLayout(getActivity());
        initView();
        return rootLayout;
    }

    private void initView() {
        Button button = new Button(getActivity());
        button.setText("second button");
        rootLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //callback方式
                AvoidOnResult.init(getActivity()).startForResult(FetchDataActivity.class, REQUEST_CODE_CALLBACK, new AvoidOnResult.Callback() {
                    @Override
                    public void onActivityResult(int requestCode, int resultCode, Intent data) {
                        if (resultCode == Activity.RESULT_OK) {
                            if (resultCode == Activity.RESULT_OK) {
                                String text = data.getStringExtra("text");
                                Toast.makeText(getActivity(), "callback -> " + text, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
            }
        });
    }
}
