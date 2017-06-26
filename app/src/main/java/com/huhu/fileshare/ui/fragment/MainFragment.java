package com.huhu.fileshare.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huhu.fileshare.R;
import com.huhu.fileshare.ShareApplication;
import com.huhu.fileshare.ui.activity.BrowserLocalFilesActivity;
import com.huhu.fileshare.ui.activity.JoinWiFiActivity;
import com.huhu.fileshare.ui.activity.UsersListActivity;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.HLog;
import com.huhu.fileshare.util.SystemSetting;
import com.huhu.fileshare.util.WiFiOperation;

/**
 */
public class MainFragment extends BaseFragment {


    private TextView mWiFiTextView;

    public MainFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mWiFiTextView = (TextView)view.findViewById(R.id.wifi);

        view.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BrowserLocalFilesActivity.class);
                intent.putExtra("type", "mine");
                startActivity(intent);
            }
        });

        view.findViewById(R.id.get).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WiFiOperation.getInstance(getActivity().getApplicationContext()).isWiFiConnected()) {
                    startActivity(new Intent(getActivity(), UsersListActivity.class));
                } else {
                    startActivity(new Intent(getActivity(), JoinWiFiActivity.class));
                }
            }
        });

        return view;
    }

    private void showJoinWiFiNotice() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        builder.setTitle(R.string.join_wifi_title);
        builder.setPositiveButton("确    定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getActivity(), JoinWiFiActivity.class));
            }
        });
        builder.setNegativeButton("取    消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void onEventMainThread(EventBusType.ConnectInfo info) {
        if (info.wifiAvailvle()) {
            if (!TextUtils.isEmpty(info.getSSID())) {
                mWiFiTextView.setText("WiFi: " + info.getSSID());
            } else {
                mWiFiTextView.setText("WiFi未连接");
            }
        } else {
            mWiFiTextView.setText("WiFi已关闭");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

}
