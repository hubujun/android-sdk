package com.qiniu.android.http.request;

import com.qiniu.android.http.dns.IDnsNetworkAddress;

import java.net.InetAddress;

public interface UploadServerInterface {

    String getServerId();

    String getHost();

    String getIp();

    String getSource();

    Long getIpPrefetchedTime();
}
