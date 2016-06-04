package ozner.device;

/**
 * Created by xzyxd on 2015/11/1.
 */
public interface OperateCallback<T> {
    void onSuccess(T var1);

    void onFailure(Throwable var1);
}
