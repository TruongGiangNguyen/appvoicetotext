package com.rabiloo.appnote.network.utils;

public enum PCMEncoding {
    SIGNED("signed-integer"),
    UNSIGNED("unsigned-integer"),
    FLOAT("floating-point");

    private String encoding;

    PCMEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }
}
