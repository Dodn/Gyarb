package com.gyarb.bardvall.david.stockbot;


import java.util.Random;

public class Brain {
    String id;
    int[] dimens;
    double[][][] weights;
    double[][] preActivationValues = new double[][]{{0}};
    double[][] postActivationValues = new double[][]{{0}};
    double error = 0;
    int[] activations;

    Brain(String ID, int[] DIMENS, double[][][] WEIGHTS, int[] ACTIVATIONS){

        if (DIMENS.length != WEIGHTS.length + 1 || ACTIVATIONS.length != WEIGHTS.length) throw new RuntimeException("Illegal constructor arguments.");

        id = ID;
        dimens = DIMENS;
        weights = WEIGHTS;
        activations = ACTIVATIONS;
    }

    public void ResetWeights(int[] dimens, int[] activations, double maxAbs){

        if (dimens.length != activations.length + 1 ) throw new RuntimeException("Illegal reset arguments.");

        Random rand = new Random();
        this.dimens = dimens;

        weights = new double[dimens.length - 1][][];
        for (int i = 0; i < weights.length; i++) {

            weights[i] = new double[dimens[i] + 1][dimens[i + 1]];

            for (int j = 0; j < dimens[i] + 1; j++) {

                for (int k = 0; k < dimens[i + 1]; k++) {

                    weights[i][j][k] = (rand.nextDouble() * 2.0 - 1.0) * maxAbs;
                }
            }
        }
    }

    public double[] Think(double[] data, double[] expectedResults){

        if (dimens[0] != data.length) throw new RuntimeException("Illegal reset arguments.");

        double[][] inputs = new double[1][data.length];
        inputs[0] = data;
        preActivationValues = new double[this.dimens.length][];
        postActivationValues = new double[this.dimens.length][];
        preActivationValues[0] = data;
        postActivationValues[0] = data;

        for (int i = 0; i < weights.length; i++) {
            inputs = mult(addBias(inputs), weights[i]);
            preActivationValues[i + 1] = inputs[0];
            inputs = Activation(inputs, activations[i]);
            postActivationValues[i + 1] = inputs[0];
        }
        error += Error(inputs[0], expectedResults);
        return inputs[0];
    }

    public double Error(double[] result, double[] expected){
        if (result.length != expected.length) throw new RuntimeException("Net outputs don't match expected");
        double error = 0.0;
        for (int i = 0; i < result.length; i++) {
            error += Math.pow((result[i] - expected[i]), 2.0) / 2.0;
        }
        return  error;
    }

    public void ResetError() {
        error = 0.0;
    }

    public double[][] mult(double[][] a, double[][] b) {
        int m1 = a.length;
        int n1 = a[0].length;
        int m2 = b.length;
        int n2 = b[0].length;
        if (n1 != m2) throw new RuntimeException("Illegal matrix dimensions.");
        double[][] c = new double[m1][n2];
        for (int i = 0; i < m1; i++)
            for (int j = 0; j < n2; j++)
                for (int k = 0; k < n1; k++)
                    c[i][j] += a[i][k] * b[k][j];
        return c;
    }

    public double[][] transpose(double[][] input) {
        double[][] output = new double[input[0].length][input.length];
        for (int i = 0; i < input.length; i++)
            for (int j = 0; j < input[0].length; j++)
                output[j][i] = input[i][j];
        return output;
    }

    public double[][] addBias(double[][] input){
        double[][] output = new double[1][input.length + 1];
        for (int i = 0; i < input.length; i++) {
            output[0][i] = input[0][i];
        }
        output[0][input.length] = 1.0;
        return output;
    }

    public double[][] Activation(double[][] input, int functionIndex){

        double[][] result = new double[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                switch (functionIndex){
                    case 1: result[i][j] = ReLU(input[i][j]);
                        break;
                    case 2: result[i][j] = LeakyReLU(input[i][j]);
                        break;
                    case 3: result[i][j] = Sigmoid(input[i][j]);
                    default: result[i][j] = input[i][j];
                }
            }
        }
        return result;
    }

    public double ReLU(double input){
        if (input > 0) {
            return input;
        } else {
            return 0;
        }
    }

    public double LeakyReLU(double input){
        if (input > 0) {
            return input;
        } else {
            return (input * 0.1);
        }
    }

    public double Sigmoid(double input){
        return (1D / (1D + Math.pow(Math.E, -input)));
    }
}
