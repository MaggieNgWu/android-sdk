package org.fisco.bcos.android.demo;

public class TxReceipt {
    private String transactionHash;
    private String transactionIndex;
    private String blockHash;
    private String blockNumber;
    private String gasUsed;
    private String contractAddress;
    private String root;
    private int status;
    private String from;
    private String to;
    private String input;
    private String output;
    private String logs;
    private String logsBloom;

    public String getTransactionHash() {
        return transactionHash;
    }

    public String getTransactionIndex() {
        return transactionIndex;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public String getRoot() {
        return root;
    }

    public int getStatus() {
        return status;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public String getLogs() {
        return logs;
    }

    public String getLogsBloom() {
        return logsBloom;
    }
}