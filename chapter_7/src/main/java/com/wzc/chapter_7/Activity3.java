package com.wzc.chapter_7;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author wzc
 * @date 2018/10/14
 */
public class Activity3 extends ActionBarActivity implements ReplaceFragmentListener {
    public static void start(Context context) {
        Intent starter = new Intent(context, Activity3.class);
        context.startActivity(starter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);
        Fragment fragment1 = getSupportFragmentManager().findFragmentByTag(Fragment1.TAG);
        if (fragment1 == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_content, Fragment1.newInstance(), Fragment1.TAG)
                    .commit();
        }
    }

    @Override
    public void replaceFragment() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_bottom_in, R.anim.exit_top_out)
                .replace(R.id.fl_content, Fragment2.newInstance())
                .commit();
    }

    public static class Fragment1 extends Fragment {
        public static final String TAG = Fragment1.class.getSimpleName();
        public static Fragment1 newInstance() {

            Bundle args = new Bundle();

            Fragment1 fragment = new Fragment1();
            fragment.setArguments(args);
            return fragment;
        }

        private ReplaceFragmentListener mReplaceFragmentListener;
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mReplaceFragmentListener = (ReplaceFragmentListener) activity;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_1, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            view.findViewById(R.id.btn_replace).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mReplaceFragmentListener != null) {
                        mReplaceFragmentListener.replaceFragment();
                    }
                }
            });
        }


    }

    public static class Fragment2 extends Fragment {
        public static Fragment2 newInstance() {

            Bundle args = new Bundle();

            Fragment2 fragment = new Fragment2();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_2, container, false);
        }
    }


}
