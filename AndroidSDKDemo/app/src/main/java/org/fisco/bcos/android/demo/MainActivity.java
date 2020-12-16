package org.fisco.bcos.android.demo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import sdk.Sdk;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String ipPort = "127.0.0.1:20200";
        String sdkPath = "/sdcard/fiscobcossdk/";                // mkdir by user
        String confPath = sdkPath + "conf";
        String groupId = "1";
        String keyFile = "sdk.key";
        String contractName = "HelloWorld";

        try {
            // init sdk
            Log.i(TAG, "init sdk result : " + BcosSDK.buildSDK(confPath, groupId, ipPort, keyFile));
            // getClientVersion
            Log.i(TAG, "get node version : " + BcosSDK.getClientVersion());
            // deploy contract
            DeployContractResult deployContractResult = BcosSDK.deployContract(sdkPath, contractName);
            String address = deployContractResult.getAddress();
            Log.i(TAG, "get contract address : " + address);
            // sendTransaction
            String abiFile = sdkPath + "contract/abi/" + contractName + ".abi";
            String abi = FileUtils.readFileToString(new File(abiFile));
            TxReceipt txReceipt = BcosSDK.sendTransaction(abi, address, "set", "[{\"type\":\"string\", \"value\":\"Hello, FISCO 3 周年!\"}]");
            Log.i(TAG, "get transaction hash : " + txReceipt.getTransactionHash());
            Log.i(TAG, "get block number : " + txReceipt.getBlockNumber());
            Log.i(TAG, "get event log : " + txReceipt.getLogs());
            // call
            Log.i(TAG, "call transaction result : " + BcosSDK.call(abi, address, "get", "[]"));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}