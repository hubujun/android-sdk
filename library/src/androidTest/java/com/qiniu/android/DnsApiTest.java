package com.qiniu.android;


import android.test.InstrumentationTestCase;
import android.util.Log;

import com.qiniu.android.common.ZoneInfo;
import com.qiniu.android.http.dns.DnsPrefetcher;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.http.dns.DnsCacheKey;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.utils.AndroidNetwork;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by jemy on 2019/8/20.
 */

public class DnsApiTest extends InstrumentationTestCase {
    public void notestDns() throws Throwable {
        List<InetAddress> inetAddresses = null;
        DnsPrefetcher dnsPrefetcher;
//        try {
//            inetAddresses = DnsPrefetcher.getDnsBySystem().lookup("upload.qiniup.com");
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
        Log.e("qiniutest", "InetAddress: " + inetAddresses.size());
        //超耗时过程
//        for (int i = 0; i < inetAddresses.size(); i++) {
//            Log.e("qiniutest", "InetAddress.getCanonicalHostName: " + inetAddresses.get(i).getCanonicalHostName());
//
//        }
        for (int i = 0; i < inetAddresses.size(); i++) {
            Log.e("qiniutest", "InetAddress.getHostAddress: " + inetAddresses.get(i).getHostAddress());
        }
    }


    public void notestQueryDomain() {
        ZoneInfo info = null;

        DnsPrefetcher dnsPrefetcher = DnsPrefetcher.getDnsPrefetcher();
        try {
            info = dnsPrefetcher.init(TestConfig.uptoken_prefetch, null).getPreQueryZone();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (info == null) {
            Log.e("qiniutest: ", "null");
        }
        Log.e("qiniutest: ", info.toString());
        Log.e("qiniutest: ", info.upDomainsList.get(0));
    }


    public void notestLocalDomain() {
        List<ZoneInfo> info = null;
        DnsPrefetcher dnsPrefetcher = DnsPrefetcher.getDnsPrefetcher();
        try {
            info = dnsPrefetcher.init(TestConfig.uptoken_prefetch, null).getLocalZone();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (info == null) {
            Log.e("qiniutest: ", "null");
        }
        for (int i = 0; i < info.size(); i++) {
            Log.e("qiniutest: ", info.get(i).toString());
        }

    }


    public void notestLocalIp() {
        String s = AndroidNetwork.getHostIP();
        Log.e("qiniutest", s);
    }

    /*
    public void testDnsPreAndcache() {
        Configuration config = new Configuration.Builder().build();
        boolean needPrefetch = DnsPrefetcher.checkRePrefetchDns(TestConfig.uptoken_prefetch, config);
        Log.e("qiniutest", "check:" + needPrefetch);
        if (needPrefetch) {
            DnsPrefetcher.startPrefetchDns(TestConfig.uptoken_prefetch, config);
        } else {
            testRecoverCache();
            return;
        }
        //预取或者recover success
        List<String> list = DnsPrefetcher.getDnsPrefetcher().getHosts();
        ConcurrentHashMap<String, List<InetAddress>> map = DnsPrefetcher.getDnsPrefetcher().getConcurrentHashMap();
        Log.e("qiniutest: ", "list size: " + list.size());
        for (String s : list) {
            Log.e("qiniutest: ", "uphost: " + s);
            List<InetAddress> list1 = map.get(s);
            for (InetAddress inetAddress :
                    list1) {
                Log.e("qiniutest: ", "ip: " + inetAddress.getHostAddress());
            }
        }

    }

    //test recover
    public void notestRecoverCache() {

        Recorder recorder = null;
        try {
            recorder = new DnsCacheFile(Config.dnscacheDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileName = recorder.getFileName();
        if (fileName == null) {
            Log.e("qiniutest: ", "recover file is null ");
            return;
        }
        byte[] data = recorder.get(recorder.getFileName());
        if (data == null) {
            Log.e("qiniutest: ", "recover data is null ");
            return;
        }
        DnsPrefetcher.recoverDnsCache(data);


        ConcurrentHashMap<String, List<InetAddress>> map1 = DnsPrefetcher.getDnsPrefetcher().getConcurrentHashMap();
        List<String> list = DnsPrefetcher.getDnsPrefetcher().getHosts();
        Log.e("qiniutest: ", "size for cache: " + list.size());
        for (String s : list) {
            Log.e("qiniutest: ", "uphost for cache: " + s);
            List<InetAddress> list1 = map1.get(s);
            for (InetAddress inetAddress :
                    list1) {
                Log.e("qiniutest: ", "ip for cache: " + inetAddress.getHostAddress());
            }
        }
    }
*/
    int time = 0;
    final Object lock = new Object();

    public void notestAtomic() {
        final int size = 6 * 1024;
        for (int i = 0; i < 3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Configuration config = new Configuration.Builder()
                                .useConcurrentResumeUpload(true).concurrentTaskCount(3)
                                .build();
                        final UploadManager uploadManager = new UploadManager(config);
                        final String expectKey = "r=" + size + "k";
                        final File f;
                        f = TempFile.createFile(size);
                        final UploadOptions uploadoption = new UploadOptions(null, null, false, new UpProgressHandler() {
                            public void progress(String key, double percent) {
                                Log.e("qiniutest", percent + "");
                            }
                        }, null);

                        uploadManager.put(f, expectKey, TestConfig.token_z0, new UpCompletionHandler() {
                            public void complete(String k, ResponseInfo rinfo, JSONObject response) {
                                Log.e("qiniutest", k + rinfo);
                                time += 1;
                                if (time == 3) {
                                    lock.notify();
                                }
                            }
                        }, uploadoption);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void notestSerializable() {
        DnsCacheKey key = new DnsCacheKey("12321", "127.0.0.1", "akscope");
        Log.e("qiniutest", key.toString());
        DnsCacheKey key1 = DnsCacheKey.toCacheKey(key.toString());
        if (key1 == null) {
            return;
        }
        Log.e("qiniutest", key1.getCurrentTime() + ":" + key1.getLocalIp() + ":" + key1.getAkScope());

    }


}
