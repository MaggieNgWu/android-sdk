package org.fisco.bcos.android.demo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import sdk.EventLog;
import sdk.BuildSDKResult;
import sdk.DeployContractResult;
import sdk.RPCResult;
import sdk.TxReceipt;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String sdkPath = "/sdcard/fiscobcossdk/";                // mkdir by user
        String contractName = "HelloWorld";
        try {
            // init sdk
            BuildSDKResult buildResult = BcosSDK.buildSDK(sdkPath);
            if (buildResult.getErrorId() == 0) {
                Log.i(TAG, "init sdk successfully");
            } else {
                Log.e(TAG, "init sdk error : " + buildResult.getErrorInfo());
                return;
            }
            // getClientVersion
            RPCResult result = BcosSDK.getClientVersion();
            if (result.getErrorInfo().isEmpty()) {
                Log.i(TAG, "node version: " + result.getQueryResult());
            } else {
                Log.i(TAG, "get node version error: " + result.getErrorInfo());
            }
            // deploy contract
            DeployContractResult deployContractResult = BcosSDK.deployContract(sdkPath, contractName);
            String address = deployContractResult.getAddress();
            Log.i(TAG, "contract address : " + address);
            // sendTransaction
            String abiFile = sdkPath + "contract/abi/" + contractName + ".abi";
            String abi = FileUtils.readFileToString(new File(abiFile));
            TxReceipt txReceipt = BcosSDK.sendTransaction(abi, address, "set", "[{\"type\":\"string\", \"value\":\"Hello, FISCO 3 周年!\"}]");
            String transactionHash = txReceipt.getTransactionHash();
            Log.i(TAG, "transaction hash : " + transactionHash);
            Log.i(TAG, "block number : " + txReceipt.getBlockNumber());
            EventLog[] eventLogs = BcosSDK.getEventLog(txReceipt);
            Log.i(TAG, "event log , size: " + eventLogs.length);
            for (EventLog eventLog : eventLogs) {
                Log.i(TAG, "event log content: " + eventLog);
            }
            // getTransactionByHash
            result = BcosSDK.getTransactionByHash(transactionHash);
            if (result.getErrorInfo().isEmpty()) {
                Log.i(TAG, "transaction content: " + result.getQueryResult());
            } else {
                Log.i(TAG, "getTransactionByHash error: " + result.getErrorInfo());
            }
            // getTransactionReceipt
            result = BcosSDK.getTransactionReceipt(transactionHash);
            if (result.getErrorInfo().isEmpty()) {
                Log.i(TAG, "transaction receipt content: " + result.getQueryResult());
            } else {
                Log.i(TAG, "getTransactionReceipt error: " + result.getErrorInfo());
            }
            // call
            Log.i(TAG, "call transaction result : " + BcosSDK.call(abi, address, "get", "[]"));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}