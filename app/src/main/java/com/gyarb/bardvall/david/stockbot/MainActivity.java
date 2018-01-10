package com.gyarb.bardvall.david.stockbot;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.sheets.v4.SheetsScopes;

import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    ViewFlipper vf;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    vf.setDisplayedChild(0);
                    return true;
                case R.id.navigation_dashboard:
                    vf.setDisplayedChild(1);
                    return true;
                case R.id.navigation_notifications:
                    vf.setDisplayedChild(2);
                    return true;
            }
            return false;
        }

    };

    Brain[] GeneticPredict1 = new Brain[GenePred1ID.length];
    static String[] GenePred1ID = {
            "1-6UIogljURV6TLVcx5lCzqa-s2ce04lK1ypcdFj6h1Q",
            "1JcP1ChW4q3VUy2ir86UqrD3bUPQT0DQ7V5gzqftbp-U",
            "1DGFDNP0FycQnnZhJWkap9BDmX_2IVcP_qcOK7aWt8Mo",
            "1L6KU_dP8qE-Loxmt3WXzqe-jrXrs7q2CBiV5rXtDhZo",
            "1vx2gpgSlTBr7cwjYvQ7Z1c9MrK58acZRuZV7K_bhWJ8",
            "104FNcK0uW2441avkxGAM4jnBdPqoAqDLTHHTR2Y8G5w",
            "1-3rc1QnbIv4dc-X5fCntFgzvks6d0yFH63OwkHfkDQ8",
            "1SKEnNrRgiSWVhpU5JweCsW5P814aC-1fXD_5EcfCMNU",
            "1eP37Jvn3FjeLMzvyxjjG9yhjuwqq9b1Sp5sHwp64lYU",
            "1vbOamjAFaNx9hwP4u0Gtyln6oyulA62Uh4-gmaQlpaI",
            "1zLrWK-JnJWHExkubj7J2fSr2LziZDUu_FW063RRkc4E",
            "1bSTEQevr_wmf9tNWT6FZX9H7ZoJzFkLk8w6gLhvr-is",
            "1H_hHxhMlj_j5WHyi-u8Lkf02T5WF4qhVolqBPEzMBwE",
            "1KQoRAe1q8i3uWjHKE-5lUaZL-iOtM-JkhKaVZzYm-CE",
            "1t5BWe3NGPYZknwvyYt06GpXa4h-VBvSkyVPqeg46-A4",
            "1dTphuOKnbph3RRW8ofg44_EEzTRhNopmya6juWLsYYI"
    };
    int[] GenePred1Dimens = {7, 3, 3, 1};

    double[][][] defaultWeights; {defaultWeights = new double[][][]{{{0}}};}

    //0: no activation
    //1: ReLU
    //2: Leaky ReLU (leak = 0.1)
    //3: Sigmoid (beta = 1)
    int[] defaultActivations = {2, 2, 2, 3};

    static String[] Letter = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private TextView mOutputText;
    private TextView mOutputText2;
    private Button mCallApiButton;
    ProgressDialog mProgress;
    GoogleAccountCredential mCredential;

    boolean postORget = false;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String GET_BUTTON_TEXT = "Download Brains";
    private static final String POST_BUTTON_TEXT = "Post Brains";
    private static final String EDIT_BUTTON_TEXT = "Edit Brains";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vf = findViewById(R.id.vf);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mOutputText = findViewById(R.id.textView1);
        mOutputText2 = findViewById(R.id.textView2);
        mProgress = new ProgressDialog(this);

        final Button PostButton = findViewById(R.id.ButtonPost);
        PostButton.setText(POST_BUTTON_TEXT);
        PostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postORget = true;
                PostButton.setEnabled(false);
                mOutputText.setText("");
                getResultsFromApi();
                PostButton.setEnabled(true);
            }
        });

        mCallApiButton = findViewById(R.id.ButtonGet);
        mCallApiButton.setText(GET_BUTTON_TEXT);
        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postORget = false;
                mCallApiButton.setEnabled(false);
                mOutputText.setText("");
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });

        final Button EditButton = findViewById(R.id.ButtonEdit);
        EditButton.setText(EDIT_BUTTON_TEXT);
        EditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditButton.setEnabled(false);
                mProgress.show();
                EditText dimensInput = findViewById(R.id.editTextDimens);
                EditText randomInput = findViewById(R.id.editTextRand);
                String[] dimText = dimensInput.getText().toString().split(",");
                int[] dimensions = new int[dimText.length];
                try {
                    for (int i = 0; i < dimText.length; i++) {
                        dimensions[i] = Integer.parseInt(dimText[i]);
                    }
                    GeneticPredict1 = Edit(GeneticPredict1,
                            dimensions,
                            Double.parseDouble(randomInput.getText().toString()));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    mOutputText2.setText("Syntax Error");
                }
                EditButton.setEnabled(true);
                mProgress.hide();
            }
        });

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        for (int i = 0; i < GeneticPredict1.length; i++) {
            GeneticPredict1[i] = new Brain(GenePred1ID[i], GenePred1Dimens, defaultWeights, 0.0, defaultActivations);
        }
    }

    private void getResultsFromApi() {
        final Brain[] testpost = {GeneticPredict1[0]};
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else if (postORget){
            new PostBrains(mCredential, GeneticPredict1).execute();
        } else {
            new GetBrains(mCredential, GeneticPredict1, new GetBrainCallback() {
                @Override
                public void onResult(double[][][][] brain) {
                    for (int i = 0; i < GeneticPredict1.length; i++) {
                        GeneticPredict1[i].weights = brain[i];
                    }
                }
            }).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public void Train(Brain[] input, boolean GeneticLearn, int newKid, int dedOlds, double mutProb, double mutFact, double mutInc, int trialsPerGen){

        if(GeneticLearn){

            String[] IDS = new String[input.length];
            for (int i = 0; i < input.length; i++) {
                IDS[i] = input[i].id;
            }

            Brain[] trials = Reproduce(input, newKid, dedOlds, mutProb, mutFact, mutInc);

            for (int i = 0; i < trialsPerGen; i++) {
                for (Brain trial : trials) {
                    trial.error += Error(Think(trial, ), );
                }
            }

            Brain[] improved = KillOff(Sort(trials), input.length);

            for (int i = 0; i < improved.length; i++) {
                improved[i].error = 0.0;
                improved[i].id = IDS[i];
            }

        }
    }

    public static Brain[] Edit(Brain[] input, int[] dimens, double maxAbs){
        Random rand = new Random();
        for (int i = 0; i < input.length; i++) {
            input[i].dimens = dimens;

            double[][][] weights = new double[dimens.length - 1][][];
            for (int j = 0; j < weights.length; j++) {
                double[][] layer = new double[dimens[j] + 1][dimens[j + 1]];
                for (int k = 0; k < dimens[j] + 1; k++) {

                    for (int l = 0; l < dimens[j + 1]; l++) {

                        layer[k][l] = (rand.nextDouble() * 2.0 - 1.0) * maxAbs;
                    }
                }
                weights[j] = layer;
            }
            input[i].weights = weights;
        }
        return input;
    }

    public static Brain[] Sort(Brain[] input){
        int n = input.length;
        boolean kek = true;

        for (int i = 0; i < n - 1; i++) {
            if (kek){
                kek = false;
                for (int j = 0; j < n - i - 1; j++) {
                    if (input[j].error > input[j + 1].error) {
                        Brain temp = input[j];
                        input[j] = input[j + 1];
                        input[j + 1] = temp;
                        kek = true;
                    }
                }
            } else break;
        }
        return input;
    }

    public static Brain[] Reproduce(Brain[] parents, int newChildren, int deadParents, double mutationProbability, double mutationFactor, double mutationIncrement){
        int totalParents = parents.length;
        int aliveParents = totalParents - deadParents;
        Brain[] children = new Brain[newChildren];
        Random rand = new Random();

        if (deadParents > newChildren || deadParents > totalParents || mutationProbability < 0.0 || mutationProbability > 1.0 || mutationFactor < 1.0) throw new RuntimeException("Illegal repoduction arguments.");

        for (int i = 0; i < newChildren; i++) {
            Brain mom = parents[i % totalParents];
            double[][][] kid = mom.weights;
            for (int j = 0; j < kid.length; j++) {
                for (int k = 0; k < kid[j].length; k++) {
                    for (int l = 0; l < kid[j][k].length; l++) {
                        if (rand.nextDouble() < mutationProbability) {
                            kid[j][k][l] = rand.nextFloat() < 0.5 ?
                                    kid[j][k][l] * (1.0 + rand.nextDouble() * (mutationFactor - 1.0)) + (rand.nextDouble() * 2.0 - 1.0) * mutationIncrement
                                    :
                                    kid[j][k][l] / (1.0 + rand.nextDouble() * (mutationFactor - 1.0)) + (rand.nextDouble() * 2.0 - 1.0) * mutationIncrement;
                        }
                    }
                }
            }
            mom.weights = kid;
            children[i] = mom;
        }

        Brain[] newGen = new Brain[aliveParents + newChildren];
        for (int i = 0; i < newGen.length; i++) {
            if (i < aliveParents) {
                newGen[i] = parents[i];
            } else {
                newGen[i] = children[i - aliveParents];
            }
        }
        return newGen;
    }

    public static Brain[] KillOff(Brain[] population, int numSurvivors){
        if (numSurvivors > population.length) throw new RuntimeException("Illegal kill_off arguments.");
        Brain[] survivors = new Brain[numSurvivors];
        System.arraycopy(population, 0, survivors, 0, numSurvivors);
        return survivors;
    }

    public static double[] Think(Brain brain, double[] data){
        double[][] inputs = new double[1][data.length];
        inputs[0] = data;

        for (int i = 0; i < brain.weights.length; i++) {
            double[][] weightLayer = brain.weights[i];

            inputs = addBias(inputs);
            inputs = Activation(mult(inputs, weightLayer), brain.activations[i]);
        }

        return inputs[0];
    }

    public static double Error(double[] result, double[] expected){
        if (result.length != expected.length) throw new RuntimeException("Net outputs don't match expected");
        double error = 0.0;
        for (int i = 0; i < result.length; i++) {
            error += Math.pow((result[i] - expected[i]), 2.0);
        }
        return  error;
    }

    public static double[][] mult(double[][] a, double[][] b) {
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

    public static double[][] transpose(double[][] input) {
        double[][] output = new double[input[0].length][input.length];
        for (int i = 0; i < input.length; i++)
            for (int j = 0; j < input[0].length; j++)
                output[j][i] = input[i][j];
        return output;
    }

    public static double[][] addBias(double[][] input){
        double[][] output = new double[1][input.length + 1];
        for (int i = 0; i < input.length; i++) {
            output[0][i] = input[0][i];
        }
        output[0][input.length] = 1.0;
        return output;
    }

    public static double[][] Activation(double[][] input, int functionIndex){

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

    public static double ReLU(double input){
        if (input > 0) {
            return input;
        } else {
            return 0;
        }
    }

    public static double LeakyReLU(double input){
        if (input > 0) {
            return input;
        } else {
            return (input * 0.1);
        }
    }

    public static double Sigmoid(double input){
        return (1D / (1D + Math.pow(Math.E, -input)));
    }

    private class PostBrains extends AsyncTask<Void, Void, String>{

        private com.google.api.services.sheets.v4.Sheets mService = null;
        Brain[] brains;

        PostBrains(GoogleAccountCredential credential, Brain[] brains){
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();

            this.brains = brains;
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected String doInBackground(Void...params) {

            try {
                for (int net = 0; net < brains.length; net++) {

                    for (int i = 0; i < brains[net].weights.length; i++) {
                        int dimIn = brains[net].dimens[i] + 1;
                        int dimOut = brains[net].dimens[i + 1];

                        String range = "Layer" + (i+1) + "!A1:" + Letter[dimOut - 1] + dimIn;

                        double[][] post = brains[net].weights[i];

                        List<List<Object>> values = new ArrayList<>();
                        for (int j = 0; j < post.length; j++) {

                            List<Object> row = new ArrayList<>();
                            for (int k = 0; k < (post[j].length); k++) {
                                row.add((post[j][k]));
                            }
                            values.add(row);
                        }

                        ValueRange body = new ValueRange()
                                .setValues(values);
                        UpdateValuesResponse result =
                                mService.spreadsheets().values().update(brains[net].id, range, body)
                                        .setValueInputOption("RAW")
                                        .execute();

                    }
                }
                return "Post Successful";
            } catch (Exception e) {
                e.printStackTrace();
                return "Post Failed";
            }
        }

        @Override
        protected void onPostExecute(String response) {
            mProgress.hide();
            mOutputText.setText(response);
        }

    }

    private class GetBrains extends AsyncTask<Void, Void, double[][][][]> {
        GetBrainCallback callback;

        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        private String spreadsheetIds[];
        int[] dimens;

        GetBrains(GoogleAccountCredential credential, Brain[] brains, GetBrainCallback callback) {
            this.callback = callback;

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();

            spreadsheetIds = new String[brains.length];
            for (int i = 0; i < brains.length; i++) {
                spreadsheetIds[i] = brains[i].id;
            }
            dimens = brains[0].dimens;
        }

        @Override
        protected double[][][][] doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                e.printStackTrace();
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private double[][][][] getDataFromApi() throws IOException {

            double[][][][] results = new double[spreadsheetIds.length][dimens.length - 1][][];

            for (int net = 0; net < spreadsheetIds.length; net++) {
                for (int i = 0; i < dimens.length - 1; i++) {
                    int dimIn = dimens[i] + 1;
                    int dimOut = dimens[i + 1];
                    String range = "Layer" + (i+1) + "!A1:" + Letter[dimOut - 1] + dimIn;
                    ValueRange response = this.mService.spreadsheets().values()
                            .get(spreadsheetIds[net], range)
                            .execute();
                    List<List<Object>> values = response.getValues();
                    double[][] layer = new double[dimIn][dimOut];
                    if (values != null) {
                        for (int j = 0; j < dimIn; j++) {
                            for (int k = 0; k < dimOut; k++){
                                String value = values.get(j).get(k).toString();
                                if (value.equals("")) {
                                    layer[j][k] = 0.0;
                                } else {
                                    layer[j][k] = Double.parseDouble(value);
                                }
                            }
                        }
                    }
                    results[net][i] = layer;
                }
            }
            return results;
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(double[][][][] output) {
            mProgress.hide();
            if (output == null || output.length == 0) {
                mOutputText.setText("No results returned.");
            } else {
                callback.onResult(output);
                //mOutputText.setText(TextUtils.join("\n", output));
                mOutputText.setText("Download Successful");
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

}
