// ITimerService.aidl
package jp.malta_yamto.servicesontarget26.aidl;

// Declare any non-default types here with import statements
import jp.malta_yamto.servicesontarget26.aidl.ITimerServiceCallback;

interface ITimerService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
	oneway void registerCallback(ITimerServiceCallback callback);

	oneway void unregisterCallback(ITimerServiceCallback callback);

	oneway void startTimer();

	oneway void stopTimer();

	int getPid();

	boolean isTimerRunning();

	int getLatestValue();

}
