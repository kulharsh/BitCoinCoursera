package com.harshad.bitCoinCoursera.week1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TxHandler {

    UTXOPool publicLedger;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        publicLedger = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        int index = 0;
        double inputSum = 0.0;
        Set<UTXO> uniqueUTXOs = new HashSet<>();
        for (Transaction.Input input : tx.getInputs()) {
            UTXO newUTXO = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output output = publicLedger.getTxOutput(newUTXO);
            if(output == null) return false;
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(index), input.signature)) {
                return false;
            }
            if(uniqueUTXOs.contains(newUTXO)) return false;
            uniqueUTXOs.add(newUTXO);
            inputSum += output.value;
            index++;
        }
        double outputValue = 0.0;
        for(Transaction.Output output: tx.getOutputs()){
            if(output.value < 0) return false;
            outputValue+=output.value;
        }
        if(inputSum < outputValue)  return false;
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        List<Transaction> valids = new ArrayList<>();
        for(int i = 0; i < possibleTxs.length; i++){
            if(isValidTx(possibleTxs[i])){
                valids.add(possibleTxs[i]);
                for (Transaction.Input in : possibleTxs[i].getInputs()) {
                    UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                    publicLedger.removeUTXO(utxo);
                }
                int index = 0;
                for (Transaction.Output output : possibleTxs[i].getOutputs()) {
                    this.publicLedger.addUTXO(new UTXO(possibleTxs[i].getHash(), index), output);
                    index++;
                }
            }
        }
        return valids.toArray(new Transaction[0]);
    }

}
