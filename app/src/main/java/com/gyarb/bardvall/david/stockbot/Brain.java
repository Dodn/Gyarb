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

    Brain(String ID, int[] DIMENS, double[][][] WEIGHTS, int[] ACTIVATIONS) {

        if (DIMENS.length != WEIGHTS.length + 1 || ACTIVATIONS.length != WEIGHTS.length)
            throw new RuntimeException("Illegal constructor arguments.");

        id = ID;
        dimens = DIMENS;
        weights = WEIGHTS;
        activations = ACTIVATIONS;
    }

    public Brain ctrlCctrlV() {

        double[][][] newNet = new double[weights.length][][];

        for (int i = 0; i < weights.length; i++) {

            double[][] newLayer = new double[weights[i].length][];

            for (int j = 0; j < weights[i].length; j++) {

                double[] newNode = new double[weights[i][j].length];

                for (int k = 0; k < weights[i][j].length; k++) {

                    newNode[k] = weights[i][j][k];
                }
                newLayer[j] = newNode;
            }
            newNet[i] = newLayer;
        }
        int[] newDims = new int[dimens.length];
        for (int i = 0; i < dimens.length; i++) {
            newDims[i] = dimens[i];
        }
        int[] newAct = new int[activations.length];
        for (int i = 0; i < activations.length; i++) {
            newAct[i] = activations[i];
        }

        return new Brain("", newDims, newNet, newAct);
    }

    public void ResetWeights(int[] dimens, int[] activations, double maxAbs) {

        if (dimens.length != activations.length + 1) throw new RuntimeException("Illegal reset arguments.");

        Random rand = new Random();
        this.dimens = dimens;
        this.activations = activations;

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

    public double[] Think(double[] data, double[] expectedResults) {

        if (dimens[0] != data.length) throw new RuntimeException("Illegal think dimens.");

        double[][] inputs = {data};
        preActivationValues = new double[this.dimens.length][];
        postActivationValues = new double[this.dimens.length][];
        preActivationValues[0] = data;
        postActivationValues[0] = data;

        for (int i = 0; i < weights.length; i++) {
            postActivationValues[i] = addBias(inputs)[0];
            inputs = mult(addBias(inputs), weights[i]);
            preActivationValues[i + 1] = inputs[0];
            inputs = Activation(inputs, activations[i]);
        }
        postActivationValues[weights.length] = inputs[0];
        this.error += Error(inputs[0], expectedResults);
        return inputs[0];
    }

    public double Error(double[] result, double[] expected) {
        if (result.length != expected.length) throw new RuntimeException("Illegal error dimens");
        double err = 0.0;
        for (int i = 0; i < result.length; i++) {
            err += 0.5 * (result[i] - expected[i]) * (result[i] - expected[i]);
        }
        return err;
    }

    public void ResetError() {
        this.error = 0.0;
    }

    public void Mutate(double mutationProbability, double mutationFactor, double mutationIncrement) {

        Random rand = new Random();

        for (int j = 0; j < weights.length; j++) {
            for (int k = 0; k < weights[j].length; k++) {
                for (int l = 0; l < weights[j][k].length; l++) {
                    if (rand.nextDouble() < mutationProbability) {
                        if (rand.nextBoolean()) {
                            weights[j][k][l] *= (1.0 + rand.nextDouble() * (mutationFactor - 1.0));
                        } else {
                            weights[j][k][l] /= (1.0 + rand.nextDouble() * (mutationFactor - 1.0));
                        }
                        weights[j][k][l] += (rand.nextDouble() * 2.0 - 1.0) * mutationIncrement;
                    }
                }
            }
        }
    }

    public void AddGradient(double[][][] gradient){

        if (weights.length != gradient.length) throw new RuntimeException("Illegal matrix sizes.");

        for (int i = 0; i < weights.length; i++) {

            if (weights[i].length != gradient[i].length) throw new RuntimeException("Illegal matrix sizes.");

            for (int j = 0; j < weights[i].length; j++) {

                if (weights[i][j].length != gradient[i][j].length) throw new RuntimeException("Illegal matrix sizes.");

                for (int k = 0; k < weights[i][j].length; k++) {

                    weights[i][j][k] += gradient[i][j][k];
                }
            }
        }
    }

    public double[][][] BackPropogate(double increment, double[] expectedResults) {
        double[][][] result = this.ctrlCctrlV().weights;
        double[][] chainDelta = new double[result.length][];
        for (int i = 0; i < chainDelta.length; i++) {
            int inv = chainDelta.length - 1 - i;
            chainDelta[inv] = new double[dimens[inv + 1]];
            if (i == 0) {
                for (int j = 0; j < chainDelta[inv].length; j++) {
                    chainDelta[inv][j] = derivedActivation(preActivationValues[inv + 1][j], activations[inv]) * (expectedResults[j] - postActivationValues[inv + 1][j]);
                }
            } else {
                for (int j = 0; j < chainDelta[inv].length; j++) {
                    double sum = 0;
                    for (int k = 0; k < chainDelta[inv + 1].length; k++) {
                        sum += chainDelta[inv + 1][k] * weights[inv][j][k];
                    }
                    chainDelta[inv][j] = derivedActivation(preActivationValues[inv + 1][j], activations[inv]) * sum;
                }
            }
        }

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                for (int k = 0; k < result[i][j].length; k++) {
                    result[i][j][k] = chainDelta[i][k] * postActivationValues[i][j] * increment;
                }
            }
        }

        return result;
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

    public double[][] addBias(double[][] input) {
        double[][] output = new double[1][input[0].length + 1];
        for (int i = 0; i < input[0].length; i++) {
            output[0][i] = input[0][i];
        }
        output[0][input[0].length] = 1.0;
        return output;
    }

    public double[][] Activation(double[][] input, int functionIndex) {

        double[][] result = new double[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                switch (functionIndex) {
                    case 1:
                        result[i][j] = ReLU(input[i][j]);
                        break;
                    case 2:
                        result[i][j] = LeakyReLU(input[i][j]);
                        break;
                    case 3:
                        result[i][j] = Sigmoid(input[i][j]);
                        break;
                    default:
                        result[i][j] = input[i][j];
                }
            }
        }
        return result;
    }

    public double ReLU(double input) {
        if (input > 0) {
            return input;
        } else {
            return 0;
        }
    }

    public double LeakyReLU(double input) {
        if (input > 0) {
            return input;
        } else {
            return (input * 0.1);
        }
    }

    public double Sigmoid(double input) {
        return (1D / (1D + Math.pow(Math.E, -input)));
    }

    public double derivedActivation(double input, int functionIndex) {
        double result = input;
        switch (functionIndex) {
            case 1:
                result = derivedReLU(input);
                break;
            case 2:
                result = derivedLeakyReLU(input);
                break;
            case 3:
                result = derivedSigmoid(input);
                break;
        }

        return result;
    }

    public double derivedReLU(double input) {
        if (input > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public double derivedLeakyReLU(double input) {
        if (input > 0) {
            return 1;
        } else {
            return 0.1;
        }
    }

    public double derivedSigmoid(double input) {
        double o = Sigmoid(input);
        return o * (1 - o);
    }
}
