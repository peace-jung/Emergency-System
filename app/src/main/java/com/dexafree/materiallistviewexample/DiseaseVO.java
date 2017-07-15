package com.dexafree.materiallistviewexample;

/**
 * Created by peace-win10 on 2017-05-31.
 */

public class DiseaseVO{
    private int disease_number;
    private String operation_status;
    private String period;


    public DiseaseVO(){   }


    public int getDisease_number() {
        return disease_number;
    }


    public void setDisease_number(int disease_number) {
        this.disease_number = disease_number;
    }


    public String getOperation_status() {
        return operation_status;
    }


    public void setOperation_status(String operation_status) {
        this.operation_status = operation_status;
    }


    public String getPeriod() {
        return period;
    }


    public void setPeriod(String period) {
        this.period = period;
    }

    @Override
    public String toString() {
        return "asdfasdf";
    }
}