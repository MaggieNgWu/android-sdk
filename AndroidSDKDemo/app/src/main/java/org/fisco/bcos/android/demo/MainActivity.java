package org.fisco.bcos.android.demo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
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
        Log.i(TAG, "init sdk result : " + Sdk.buildSDK(confPath, groupId, ipPort, keyFile));
        Log.i(TAG, "get node version : " + Sdk.getClientVersion());

        String contranctName = "HelloWorld";
        String abiFile = sdkPath + "contract/abi/" + contranctName + ".abi";
        String binFile = sdkPath + "contract/bin/" + contranctName + ".bin";
        try {
            String abi = FileUtils.readFileToString(new File(abiFile));
            String bin = FileUtils.readFileToString(new File(binFile));
            Log.i(TAG, "abi:" + abi);
            Log.i(TAG, "bin:" + bin);
            String deployResult = Sdk.deployContract(abi, bin);
            DeployContractResult deployContractResult =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(deployResult, DeployContractResult.class);
            String address = deployContractResult.getAddress();
            Log.i(TAG, "deploy contract result : " + deployResult);
            Log.i(TAG, "get contract address : " + address);
            String sendResult = Sdk.sendTransaction(abi, address, "set", "[{\"type\":\"string\", \"value\":\"Maggie\"}]");
            SendTransactionReceipt sendTransactionReceipt =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(sendResult, SendTransactionReceipt.class);
            String receiptReceipt = sendTransactionReceipt.getReceipt();
            TxReceipt txReceipt =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(receiptReceipt, TxReceipt.class);
            Log.i(TAG, "send transaction result : " + sendResult);
            Log.i(TAG, "get transaction hash : " + txReceipt.getTransactionHash());
            Log.i(TAG, "get block number : " + txReceipt.getBlockNumber());
            Log.i(TAG, "call transaction result : " + Sdk.call(abi, address, "get", "[]"));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}