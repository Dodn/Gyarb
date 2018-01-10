package com.gyarb.bardvall.david.stockbot;


public class Brain {
    String id;
    int[] dimens;
    double[][][] weights;
    double error;
    int[] activations;

    Brain(String ID, int[] DIMENS, double[][][] WEIGHTS, double ERROR, int[] ACTIVATIONS){
        id = ID;
        dimens = DIMENS;
        weights = WEIGHTS;
        error = ERROR;
        activations = ACTIVATIONS;
    }
}
