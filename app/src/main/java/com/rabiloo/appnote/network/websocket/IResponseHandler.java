package com.rabiloo.appnote.network.websocket;

public interface IResponseHandler<T> {
    void onMessage(T msg);

    void onFailure(Throwable cause);

    void onComplete();
}
