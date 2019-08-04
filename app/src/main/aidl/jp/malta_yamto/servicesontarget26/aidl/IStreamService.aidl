// IStreamService.aidl
package jp.malta_yamto.servicesontarget26.aidl;

// Declare any non-default types here with import statements
import jp.malta_yamto.servicesontarget26.aidl.IStreamServiceCallback;

interface IStreamService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     oneway void registerCallback(IStreamServiceCallback callback);

     oneway void unregisterCallback(IStreamServiceCallback callback);

     int getPid();

     ParcelFileDescriptor getInputStream();

     ParcelFileDescriptor getOutputStream();

     boolean checkDigest(in byte[] md5);

     oneway void interruptTransmittingThread();

     oneway void interruptReceivingThread();
}
