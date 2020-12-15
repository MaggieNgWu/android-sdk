/*
 * Copyright 2014-2020. [fisco-dev]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 *  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package sdk

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"strconv"
	"strings"
	"github.com/FISCO-BCOS/go-sdk/abi"
	"github.com/FISCO-BCOS/go-sdk/abi/bind"
	"github.com/FISCO-BCOS/go-sdk/client"
	"github.com/FISCO-BCOS/go-sdk/conf"
	"github.com/FISCO-BCOS/go-sdk/core/types"
	"github.com/ethereum/go-ethereum/common"
)

type InitSDK struct {
	Connected			bool		`json:"connected"`
	ErrorInfo			string		`json:"errorInfo"`
}

type ContractParams struct {
	ValueType			string		`json:"type"`
	Value				string		`json:"value"`
}

type DeployContractResult struct {
	ErrorInfo			string		`json:"errorInfo"`
	Address				string		`json:"address"`
	Hash				string		`json:"hash"`
}

type SendTransactionReceipt struct {
	Receipt				string		`json:"receipt"`
	ErrorInfo			string		`json:"errorInfo"`
}

type TxReceipt struct {
	TransactionHash		string		`json:"transactionHash"`
	TransactionIndex	string		`json:"transactionIndex"`
	BlockHash			string		`json:"blockHash"`
	BlockNumber			string		`json:"blockNumber"`
	GasUsed				string		`json:"gasUsed"`
	ContractAddress		string		`json:"contractAddress"`
	Root				string		`json:"root"`
	Status				int			`json:"status"`
	From				string		`json:"from"`
	To					string		`json:"to"`
	Input				string		`json:"input"`
	Output				string		`json:"output"`
	Logs				string		`json:"logs"`
	LogsBloom			string		`json:"logsBloom"`
}

var clientSdk *client.Client

func BuildSDK(basePath string, groupId string, ipPort string, keyFile string) string {
	// set config content
	var config = "[Network]\n" +
		"Type=\"channel\"\n" +
		"CAFile=\"" + basePath + "/ca.crt\"\n" +
		"Cert=\"" + basePath + "/sdk.crt\"\n" +
		"Key=\"" + basePath + "/sdk.key\"\n" +
		"[[Network.Connection]]\n" +
		"NodeURL=\"" + ipPort + "\"\n" +
		"GroupID=" + groupId + "\n\n" +
		"[Account]\n" +
		"KeyFile= \"" + basePath + "/" + keyFile + "\"\n\n" +
		"[Chain]\n" +
		"ChainID=1\n" +
		"SMCrypto=false"
	// connect node
	configs, err := conf.ParseConfig([]byte(config))
	if err != nil {
		buildResult := InitSDK{Connected: false, ErrorInfo: err.Error()}
		ret, _ := json.Marshal(buildResult)
		return string(ret)
	}
	clientSdk, err = client.Dial(&configs[0])
	if err != nil {
		buildResult := InitSDK{Connected: false, ErrorInfo: err.Error()}
		ret, _ := json.Marshal(buildResult)
		return string(ret)
	}
	buildResult := InitSDK{Connected: true, ErrorInfo: ""}
	ret, _ := json.Marshal(buildResult)
	return string(ret)
}

func GetClientVersion() string {
	cv, err := clientSdk.GetClientVersion(context.Background())
	if err != nil {
		return err.Error()
	}
	return string(cv)
}

func DeployContract(abiContract string, binContract string) string {
	ops := clientSdk.GetTransactOpts()
	parsed, err := abi.JSON(strings.NewReader(abiContract))
	if err != nil {
		return err.Error()
	}
	address, transaction, _, err := bind.DeployContract(ops, parsed, common.FromHex(binContract), clientSdk)
	var txResult DeployContractResult
	if err != nil {
		txResult.ErrorInfo = err.Error()
	} else  {
		txResult.Address = address.Hex()
	}
	if transaction != nil {
		txResult.Hash = transaction.Hash().Hex()
	}
	ret, _ := json.Marshal(txResult)
	return string(ret)
}

func SendTransaction(abiContract string, address string, method string, params string) string {
	parsed, err := abi.JSON(strings.NewReader(abiContract))
	if err != nil {
		sendResult := SendTransactionReceipt{Receipt: "", ErrorInfo: err.Error()}
		ret, _ := json.Marshal(sendResult)
		return string(ret)
	}
	goParams, err := toGoParams(params)
	if err != nil {
		sendResult := SendTransactionReceipt{Receipt: "", ErrorInfo: err.Error()}
		ret, _ := json.Marshal(sendResult)
		return string(ret)
	}
	addr := common.HexToAddress(address)
	c := bind.NewBoundContract(addr, parsed, clientSdk, clientSdk, clientSdk)
	var receipt *types.Receipt
	if len(goParams) == 0 {
		_, receipt, err = c.Transact(clientSdk.GetTransactOpts(), method)
	} else {
		_, receipt, err = c.Transact(clientSdk.GetTransactOpts(), method, goParams...)
	}
	if err != nil {
		sendResult := SendTransactionReceipt{Receipt: "", ErrorInfo: err.Error()}
		ret, _ := json.Marshal(sendResult)
		return string(ret)
	}
	var rec TxReceipt
	rec.TransactionHash = receipt.TransactionHash
	rec.TransactionIndex = receipt.TransactionIndex
	rec.BlockHash = receipt.BlockHash
	rec.BlockNumber = receipt.BlockNumber
	rec.GasUsed = receipt.GasUsed
	rec.ContractAddress = receipt.ContractAddress.Hex()
	rec.Root = receipt.Root
	rec.Status = receipt.Status
	rec.From = receipt.From
	rec.To = receipt.To
	rec.Input = receipt.Input
	rec.Output = receipt.Output
	logs, _ := json.MarshalIndent(receipt.Logs, "", "\t")
	rec.Logs = string(logs)
	rec.LogsBloom = receipt.LogsBloom
	str, _ := json.Marshal(rec)
	sendResult := SendTransactionReceipt{Receipt: string(str), ErrorInfo: ""}
	ret, _ := json.Marshal(sendResult)
	return string(ret)
}

func Call(abiContract string, address string, method string, params string) string {
	parsed, err := abi.JSON(strings.NewReader(abiContract))
    if err != nil {
        return err.Error()
    }
    goParams, err := toGoParams(params)
	if err != nil {
		return err.Error()
	}
	addr := common.HexToAddress(address)
	c := bind.NewBoundContract(addr, parsed, clientSdk, clientSdk, clientSdk)
	var result interface{}
	if len(goParams) == 0 {
		err = c.Call(clientSdk.GetCallOpts(), &result, method)
	} else {
		err = c.Call(clientSdk.GetCallOpts(), result, method, goParams...)
	}
	if err != nil {
		return err.Error()
	}
	resultBytes, err := json.MarshalIndent(result, "", "\t")
	return string(resultBytes)
}

func toGoParams(param string) ([]interface{}, error) {
	var objs []ContractParams
	if err := json.Unmarshal([]byte(param), &objs); err != nil {
		fmt.Println(err.Error())
		return nil, err
	}
	var par []interface{}
	for _, t := range objs {
		switch t.ValueType {
		case "int":
			i, err := strconv.ParseInt(t.Value, 10, 32)
			if err != nil {
				return nil, err
			}
			par = append(par, i)
		case "uint":
			i, err := strconv.ParseUint(t.Value, 10, 32)
			if err != nil {
				return nil, err
			}
			par = append(par, i)
		case "bool":
			b, err := strconv.ParseBool(t.Value)
			if err != nil {
				return nil, err
			}
			par = append(par, b)
		case "string":
			par = append(par, t.Value)
		case "address":
			addr := common.HexToAddress(t.Value)
			par = append(par, addr)
		case "bytes":
			par = append(par, []byte(t.Value))
		default:
			return nil, errors.New("unsupport param type(" + t.ValueType + ")")
		}
	}
	return par, nil
}

func toReceipt(_r *types.Receipt) (string, error) {
	if _r == nil {
		return "", errors.New("receipt is null")
	}
	var rec TxReceipt
	rec.TransactionHash = _r.TransactionHash
	rec.TransactionIndex = _r.TransactionIndex
	rec.BlockHash = _r.BlockHash
	rec.BlockNumber = _r.BlockNumber
	rec.GasUsed = _r.GasUsed
	rec.ContractAddress = _r.ContractAddress.Hex()
	rec.Root = _r.Root
	rec.Status = _r.Status
	rec.From = _r.From
	rec.To = _r.To
	rec.Input = _r.Input
	rec.Output = _r.Output
	logs, err := json.MarshalIndent(_r.Logs, "", "\t")
	if err != nil {
		return "", err
	} else {
		rec.Logs = string(logs)
	}
	rec.LogsBloom = _r.LogsBloom
	ret, _ := json.Marshal(rec)
	return string(ret), nil
}