// IDownloadListenerInterface.aidl
package com.huhu.fileshare;

// Declare any non-default types here with import statements

interface IDownloadListenerInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onProgress(String uuid,long total,long recv);
}
