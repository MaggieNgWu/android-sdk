package org.fisco.bcos.android.demo;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import sdk.Sdk;

public class BcosSDK {

    private static final String TAG = "BcosSDK";

    public static String buildSDK(String confPath, String groupId, String ipPort, String keyFile) {
        return Sdk.buildSDK(confPath, groupId, ipPort, keyFile);
    }

    public static String getClientVersion() {
        return Sdk.getClientVersion();
    }

    public static DeployContractResult deployContract(String contractPath, String contractName) throws IOException {
        String abiFile = contractPath + "contract/abi/" + contractName + ".abi";
        String binFile = contractPath + "contract/bin/" + contractName + ".bin";
        String abi = FileUtils.readFileToString(new File(abiFile));
        String bin = FileUtils.readFileToString(new File(binFile));
        Log.i(TAG, contractName + ".abi:" + abi);
        Log.i(TAG, contractName + ".bin:" + bin);
        String deployResult = Sdk.deployContract(abi, bin);
        return ObjectMapperFactory.getObjectMapper().readValue(deployResult, DeployContractResult.class);
    }

    public static TxReceipt sendTransaction(String abi, String address, String method, String params) throws IOException {
        String sendResult = Sdk.sendTransaction(abi, address, method, params);
        SendTransactionReceipt sendTransactionReceipt =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(sendResult, SendTransactionReceipt.class);
        String receiptReceipt = sendTransactionReceipt.getReceipt();
        return ObjectMapperFactory.getObjectMapper().readValue(receiptReceipt, TxReceipt.class);
    }

    public static String call(String abi, String address, String method, String params) {
        return Sdk.call(abi, address, method, params);
    }
}