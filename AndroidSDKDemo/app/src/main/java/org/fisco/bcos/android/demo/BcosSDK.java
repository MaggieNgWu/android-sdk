package org.fisco.bcos.android.demo;

import android.util.Log;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;

import sdk.EventLog;
import sdk.BuildSDKResult;
import sdk.RPCResult;
import sdk.Sdk;
import sdk.DeployContractResult;
import sdk.SendTransactionReceipt;
import sdk.TxReceipt;

public class BcosSDK {

    private static final String TAG = "BcosSDK";

    public static BuildSDKResult buildSDK(String confPath) throws IOException {
        String initResult = Sdk.buildSDK(confPath);
        return ObjectMapperFactory.getObjectMapper().readValue(initResult, BuildSDKResult.class);
    }

    public static RPCResult getClientVersion() throws IOException {
        String result = Sdk.getClientVersion();
        return ObjectMapperFactory.getObjectMapper().readValue(result, RPCResult.class);
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

    public static EventLog[] getEventLog(TxReceipt txReceipt) throws IOException {
        String logs = txReceipt.getLogs();
        return ObjectMapperFactory.getObjectMapper().readValue(logs, EventLog[].class);
    }

    public static RPCResult getTransactionByHash(String txHash) throws IOException {
        String result = Sdk.getTransactionByHash(txHash);
        return ObjectMapperFactory.getObjectMapper().readValue(result, RPCResult.class);
    }

    public static RPCResult getTransactionReceipt(String txHash) throws IOException {
        String result = Sdk.getTransactionReceipt(txHash);
        return ObjectMapperFactory.getObjectMapper().readValue(result, RPCResult.class);
    }

    public static String call(String abi, String address, String method, String params) {
        return Sdk.call(abi, address, method, params);
    }
}