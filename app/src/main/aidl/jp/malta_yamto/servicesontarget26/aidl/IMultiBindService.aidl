// IMultiBindService.aidl
package jp.malta_yamto.servicesontarget26.aidl;

// Declare any non-default types here with import statements
import jp.malta_yamto.servicesontarget26.aidl.IMultiBindServiceCallback;

interface IMultiBindService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

     int send(in int value, IMultiBindServiceCallback callback);

     oneway void interrupt(in int id);
}
