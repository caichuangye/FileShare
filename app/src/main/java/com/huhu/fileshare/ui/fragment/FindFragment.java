package com.huhu.fileshare.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huhu.fileshare.R;
import com.huhu.fileshare.com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.huhu.fileshare.com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.huhu.fileshare.de.greenrobot.event.EventBus;
import com.huhu.fileshare.model.WiFiItem;
import com.huhu.fileshare.ui.adapter.WiFiAdapter;
import com.huhu.fileshare.util.EventBusType;
import com.huhu.fileshare.util.GlobalParams;
import com.huhu.fileshare.util.HLog;
import com.huhu.fileshare.util.WiFiOperation;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FindFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FindFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FindFragment extends BaseFragment implements WiFiOperation.IOnWiFiListScanListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private WiFiAdapter mAdapter;

    private PullToRefreshListView mPullToRefreshListView;

    private ListView mListView;

    private ProgressBar mProgressBar;

    private WiFiOperation mWiFiOperation;

    public static FindFragment newInstance(String param1, String param2) {
        FindFragment fragment = new FindFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FindFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find, container, false);
        mWiFiOperation = WiFiOperation.getInstance(mContext);
        mWiFiOperation.setListener(this);
        mAdapter = new WiFiAdapter(mContext);
        mPullToRefreshListView = (PullToRefreshListView)view.findViewById(R.id.find_listview);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWiFiOperation.scanWiFi();
            }
        });
        mProgressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        RelativeLayout emptyView = (RelativeLayout)view.findViewById(R.id.emptyview);
        TextView setWiFiTextView = (TextView)emptyView.findViewById(R.id.set_wifi);
        setWiFiTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                startActivity(intent);
            }
        });
        mListView = mPullToRefreshListView.getRefreshableView();
        mListView.setEmptyView(emptyView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String bssid = WiFiOperation.getInstance(mContext).getConnectedWiFiBSSID();
                if (bssid.equals(mAdapter.getBSSID(position - 1))) {
                    EventBus.getDefault().post(new EventBusType.ChangeMainFragment(EventBusType.DEVICES_FRAGMENT, mAdapter.getSSID(position - 1)));
                } else {
                    showInputPasswordDialog(mAdapter.getSSID(position - 1),bssid);
                }
            }
        });
        mWiFiOperation.scanWiFi();
        return view;
    }

    private void showInputPasswordDialog(final String ssid,final String bssid){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.input_pwd_layout,null);
        final EditText editText = (EditText)view.findViewById(R.id.pwd);
        TextView title = (TextView)view.findViewById(R.id.title);
        title.setText(ssid);
        editText.setHint("请输入密码");
        builder.setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pwd = editText.getText().toString();
                        if(TextUtils.isEmpty(pwd.trim())){
                            Toast.makeText(mContext,"请输入密码", Toast.LENGTH_SHORT).show();
                        }else {
                            mProgressBar.setVisibility(View.VISIBLE);
                            boolean res = mWiFiOperation.joinWiFi(ssid, pwd, 1);
                            HLog.d("CCYW", "conn: " + res);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onChanged(List<WiFiItem> list) {
        mPullToRefreshListView.onRefreshComplete();
        mAdapter.setData(list);
    }

    public interface OnFragmentInteractionListener {
         void onFragmentInteraction(Uri uri);
    }

    public void onEventMainThread(EventBusType.ConnectInfo info){
        mProgressBar.setVisibility(View.GONE);
        if(info.getStatus() == GlobalParams.WIFI_CONNECTED){
            EventBus.getDefault().post(new EventBusType.ChangeMainFragment(EventBusType.DEVICES_FRAGMENT, info.getSSID()));
        }else{
            Toast.makeText(mContext,info.getSSID()+"连接失败",Toast.LENGTH_SHORT).show();
        }
    }

    public void onEventMainThread(EventBusType.WiFiStatus info){
       boolean connected = info.isConnected();
        if(!connected){
            HLog.d("cwi","wifi disconnect, clear data");
            mAdapter.clear();
        }else{
            HLog.d("cwi","wifi connect, refresh data");
            mWiFiOperation.scanWiFi();
        }
    }

}
