package com.wonjin.dolphin.http.protocol;

public enum ProtocolVersion {
    TLS_1("TLSv1"),
    TLS_1_1("TLSv1.1"),
    TLS_1_2("TLSv1.2");

    private final String tlsVersion;

    ProtocolVersion(String tlsVersion) {
        this.tlsVersion = tlsVersion;
    }

    public String getTLSVersion() {
        return tlsVersion;
    }
}
