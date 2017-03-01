package com.example.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络状态监听器，单实例
 * @author Jack
 * @version 创建时间：2014-3-6 上午9:47:39
 */
public class ConnectivitySupport {

	public enum NetState {
		NET_NOT_CONNECT, // 没有网落连接

		NET_CONNECT_WIFI, // WIFI

		NET_CONNECT_MOBILE, // MOBILE.3GNET / MOBILE.CMNET / MOBILE.UNINET /
							// MOBILE.CTNET / MOBILE.INTERNET

		NET_CONNECT_WAP, // MOBILE.WAP

		NET_CONNECT_UNKNOW, // 未知的网络连接类型
	}

	public static final String APN_3G = "3GNET";

	public static final String APN_CMNET = "CMNET";

	public static final String APN_UNINET = "UNINET";

	public static final String APN_CTNET = "CTNET";

	public static final String APN_INTERNET = "INTERNET";

	public static final int DEFAULT_DELAY = 0;

	public static final int DEFAULT_PERIOD = 4 * 1000;

	public static NetState getConnectivityState(Context context) {
		NetState net_state = NetState.NET_NOT_CONNECT;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = connMgr.getActiveNetworkInfo();
		if (null != netinfo && netinfo.isAvailable() && netinfo.isConnected()) {
			int nettype = netinfo.getType();
			switch (nettype) {
			case ConnectivityManager.TYPE_WIFI:
				net_state = NetState.NET_CONNECT_WIFI;
				break;
			case ConnectivityManager.TYPE_MOBILE:
				String extrainfo = netinfo.getExtraInfo();
				if (null != extrainfo) {
					if (APN_3G.equalsIgnoreCase(extrainfo)
							|| APN_CMNET.equalsIgnoreCase(extrainfo)
							|| APN_UNINET.equalsIgnoreCase(extrainfo)
							|| APN_CTNET.equalsIgnoreCase(extrainfo)
							|| APN_INTERNET.equalsIgnoreCase(extrainfo)) {
						net_state = NetState.NET_CONNECT_MOBILE;
					} else {
						net_state = NetState.NET_CONNECT_UNKNOW;
					}
				} else {
					net_state = NetState.NET_CONNECT_WAP;
				}
				break;
			default:
				net_state = NetState.NET_CONNECT_UNKNOW;
				break;
			}
		} else {
			net_state = NetState.NET_NOT_CONNECT;
		}
		return net_state;
	}

	public interface IConnectivityListener {
		public void onConnectivityChanged(NetState oldstate, NetState newstate);
	}

	private NetState mNetState = NetState.NET_CONNECT_WIFI;

	private Timer mTimer;

	private List<IConnectivityListener> mListeners = new ArrayList<IConnectivityListener>(
			3);

	private static ConnectivitySupport s_intance;

	private ConnectivitySupport() {
	}

	public static ConnectivitySupport getInstance() {
		if (null == s_intance) {
			s_intance = new ConnectivitySupport();
		}
		return s_intance;
	}

	public NetState getNetState() {
		return mNetState;
	}

	public void registerConnectivityListener(Context context,
			IConnectivityListener listener) {
		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
			if (1 == mListeners.size()) {
				mTimer = startConnectivityMonitor(context);
			}
		}
	}

	public void unregisterConnectivityListener(IConnectivityListener listener) {
		mListeners.remove(listener);
		if (0 == mListeners.size()) {
			mTimer.cancel();
		}
	}

	private Timer startConnectivityMonitor(final Context context) {
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				NetState state = getConnectivityState(context);
				if (mNetState != state) {
					for (IConnectivityListener listener : mListeners) {
						listener.onConnectivityChanged(mNetState, state);
					}
					mNetState = state;
				}
			}
		}, DEFAULT_DELAY, DEFAULT_PERIOD);
		return timer;
	}
}
