// IStreamServiceCallback.aidl
package jp.malta_yamto.servicesontarget26.aidl;

// Declare any non-default types here with import statements

interface IStreamServiceCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onReplyDigest(in byte[] md5);
}
