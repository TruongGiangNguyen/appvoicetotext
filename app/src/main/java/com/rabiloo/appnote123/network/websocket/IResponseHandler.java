package com.rabiloo.appnote123.network.websocket;

public interface IResponseHandler<T> {
    void onMessage(T msg);
    void onFailure(Throwable cause);
    void onComplete();
}
