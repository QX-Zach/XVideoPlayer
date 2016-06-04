package ozner.device;

import ozner.device.OperateCallback;

/**
 * Created by xzyxd on 2015/11/3.
 */
public class OperateCallbackProxy<T> implements ozner.device.OperateCallback<T> {

    public ozner.device.OperateCallback<T> callback;
    public Object param;

    public OperateCallbackProxy(ozner.device.OperateCallback<T> callback, Object param) {
        this.param=param;
        this.callback = callback;
    }
    public OperateCallbackProxy(OperateCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void onSuccess(T var1) {
        if (callback != null)
            callback.onSuccess(var1);
    }

    @Override
    public void onFailure(Throwable var1) {

        if (callback != null)
            callback.onFailure(var1);
    }
}
