package org.fisco.bcos.android.demo;

public class DeployContractResult {
    private String errorInfo;
    private String address;
    private String hash;

    public String getErrorInfo() {
        return errorInfo;
    }
    
    public String getAddress () {
        return address;
    }

    public String getHash() {
        return hash;
    }
}