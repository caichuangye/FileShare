// IDownloadServicelInterface.aidl
package com.huhu.fileshare;

// Declare any non-default types here with import statements

interface IDownloadServicelInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void addDownloadItem(String uuid,String ip,String fromPath,long size,String fromUser,String type);

    void deleteDownloadItem(String uuid);
}
