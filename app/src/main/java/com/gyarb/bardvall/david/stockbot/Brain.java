package com.gyarb.bardvall.david.stockbot;


public class Brain {
    String id;
    int[] dimens;
    double[][][] weights;

    Brain(String ID, int[] DIMENS, double[][][] WEIGHTS){
        id = ID;
        dimens = DIMENS;
        weights = WEIGHTS;
    }
}
