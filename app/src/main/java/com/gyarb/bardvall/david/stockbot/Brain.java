package com.gyarb.bardvall.david.stockbot;


public class Brain {
    String id;
    int[] dimens;
    double[][][] weights;
    double[][] preActivationValues;
    double error;
    int[] activations;

    Brain(String ID, int[] DIMENS, double[][][] WEIGHTS, double[][] PREACTIVATIONVALUES, double ERROR, int[] ACTIVATIONS){
        id = ID;
        dimens = DIMENS;
        weights = WEIGHTS;
        preActivationValues = PREACTIVATIONVALUES;
        error = ERROR;
        activations = ACTIVATIONS;
    }
}
