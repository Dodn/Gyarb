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

    Brain[] Genetic1 = new Brain[Gene1ID.length];
    static String[] Gene1ID = {
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
    int[] Gene1Dimens = {7, 3, 3, 1};

    Brain[] BackProp1 = new Brain[Prop1ID.length];
    static String[] Prop1ID = {
            "1nDgK8TGpDPYoaHmG0fwOARUO_dOOdNne5dc-pqDFq-k"
    };
    int[] Prop1Dimens = {7, 3, 3, 1};

    double[][][] defaultWeights; {defaultWeights = new double[][][]{{{0}}, {{0}}, {{0}}};}
    //double[][] defaultNodeValues; {defaultNodeValues = new double[][]{{0}};}

    //0: no activation
    //1: ReLU
    //2: Leaky ReLU (leak = 0.1)
    //3: Sigmoid (beta = 1)
    int[] defaultActivations = {2, 2, 3};

    static String[] Letter = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private TextView mOutputText;
    private TextView mOutputText2;
    private TextView mOutputText3;
    private Button mCallApiButton;
    ProgressDialog mProgress;
    GoogleAccountCredential mCredential;

    boolean postORget = false;
    boolean geneORprop = true;

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
        mOutputText3 = findViewById(R.id.textView3);
        mProgress = new ProgressDialog(this);

        final Button ToggleButton = findViewById(R.id.ButtonToggle);
        ToggleButton.setText("Switch to Backprop");
        ToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (geneORprop){
                    mOutputText.setText("Current mode: Backprop");
                    ToggleButton.setText("Switch to Genetic");
                } else{
                    mOutputText.setText("Current mode: Genetic");
                    ToggleButton.setText("Switch to Backprop");
                }
                geneORprop = !geneORprop;
            }
        });

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
                mOutputText2.setText("");
                EditText dimensInput = findViewById(R.id.editTextDimens);
                EditText randomInput = findViewById(R.id.editTextRand);
                String[] dimText = dimensInput.getText().toString().split(",");
                int[] dimensions = new int[dimText.length];
                double maxAbs;
                try {
                    for (int i = 0; i < dimText.length; i++) {
                        dimensions[i] = Integer.parseInt(dimText[i]);
                    }
                    maxAbs = Double.parseDouble(randomInput.getText().toString());

                    if (geneORprop){
                        for (int i = 0; i < Genetic1.length; i++) {
                            Genetic1[i].ResetWeights(dimensions, defaultActivations, maxAbs);
                        }
                    } else{
                        for (int i = 0; i < BackProp1.length; i++) {
                            BackProp1[i].ResetWeights(dimensions, defaultActivations, maxAbs);
                        }
                    }
                    mOutputText2.setText("Reset successful");
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    mOutputText2.setText("Syntax Error");
                }
                EditButton.setEnabled(true);
                mProgress.hide();
            }
        });

        final Button TrainButton = findViewById(R.id.ButtonTrain);
        TrainButton.setText("test training");
        TrainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TrainButton.setEnabled(false);
                mProgress.show();
                double[][] testInput = {{1.0, 0.34, 0.74, 0.06, 0.53, 0.12, 0.81}};
                double[][] testExpected = {{0.5}};
                String results = "";

                if (geneORprop){
                    Genetic1 = TrainOneGeneration(Genetic1, true, 0.05, 1.005, 0.01, testInput, testExpected);

                    for (int i = 0; i < Genetic1.length; i++) {
                        results += String.valueOf(Genetic1[i].error);
                        if(i != Genetic1.length - 1) results += "\n";
                    }
                } else{
                    BackProp1 = TrainOneGeneration(BackProp1, false, 0,0, 0.1, testInput, testExpected);
                    results = String.valueOf(BackProp1[0].error);
                }

                mOutputText3.setText(results);
                TrainButton.setEnabled(true);
                mProgress.hide();
            }
        });

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        for (int i = 0; i < Genetic1.length; i++) {
            Genetic1[i] = new Brain(Gene1ID[i], Gene1Dimens, defaultWeights, defaultActivations);
        }
        for (int i = 0; i < BackProp1.length; i++) {
            BackProp1[i] = new Brain(Prop1ID[i], Prop1Dimens, defaultWeights, defaultActivations);
        }
    }

    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else if (postORget){
            if (geneORprop){
                new PostBrains(mCredential, Genetic1).execute();
            } else {
                new PostBrains(mCredential, BackProp1).execute();
            }
        } else {
            if (geneORprop){
                new GetBrains(mCredential, Genetic1, new GetBrainCallback() {
                    @Override
                    public void onResult(double[][][][] brain) {
                        for (int i = 0; i < Genetic1.length; i++) {
                            Genetic1[i].weights = brain[i];
                        }
                    }
                }).execute();
            } else {
                new GetBrains(mCredential, BackProp1, new GetBrainCallback() {
                    @Override
                    public void onResult(double[][][][] brain) {
                        for (int i = 0; i < BackProp1.length; i++) {
                            BackProp1[i].weights = brain[i];
                        }
                    }
                }).execute();
            }
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

    public Brain[] TrainOneGeneration(Brain[] input, boolean GeneticLearn, double mutProb, double mutFact, double mutInc, double[][] trainInputs, double[][] trainExpected){

        //if (trainInputs.length != trainExpected.length) throw new RuntimeException("Illegal matrix dimensions.");
        int batchSize = trainInputs.length;
        int populationSize = input.length;

        if(GeneticLearn){

            String[] IDS = new String[populationSize];
            for (int i = 0; i < populationSize; i++) {
                IDS[i] = input[i].id;
            }

            input = Reproduce(input, mutProb, mutFact, mutInc);

            for (int i = 0; i < input.length; i++) {
                input[i].ResetError();
                for (int j = 0; j < batchSize; j++) {
                    double[] resultClassification = input[i].Think(trainInputs[j], trainExpected[j]); //image data, correct classification
                }
            }

            input = KillOff(Sort(input), populationSize);

            for (int i = 0; i < populationSize; i++) {
                input[i].id = IDS[i];
            }

        } else {

            double[][][] gradient = input[0].ctrlCctrlV().weights;
            gradient = fill3DMatrix(gradient, 0);
            for (int i = 0; i < batchSize; i++) {
                input[0].ResetError();
                double[] resultClassification = input[0].Think(trainInputs[i], trainExpected[i]); //image data, expected result
                gradient = add3DMatrix(gradient, input[0].BackPropogate(mutInc, trainExpected[i])); //expected results
            }
            input[0].AddGradient(gradient);
        }

        return input;
    }

    public Brain[] Sort(Brain[] input){
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

    public Brain[] Reproduce(Brain[] parents, double mutationProbability, double mutationFactor, double mutationIncrement){

        if (mutationProbability < 0.0 || mutationProbability > 1.0 || mutationFactor < 1.0) throw new RuntimeException("Illegal repoduction arguments.");
        int popSize = parents.length;
        //Brain[] kids = parents.clone();
        Brain[] newGen = new Brain[2 * popSize];
        for (int i = 0; i < newGen.length; i++) {
            if (i < popSize) {
                newGen[i] = parents[i];
            } else {
                newGen[i] = parents[i - popSize].ctrlCctrlV();
                //newGen[i] = parents[i - popSize].;
                newGen[i].Mutate(mutationProbability, mutationFactor, mutationIncrement);
            }
        }
        return newGen;
    }

    public Brain[] KillOff(Brain[] population, int numSurvivors){
        if (numSurvivors > population.length) throw new RuntimeException("Illegal kill_off arguments.");
        List<Brain> net = new ArrayList<>(numSurvivors);
        Brain[] survivors = new Brain[numSurvivors];
        System.arraycopy(population, 0, survivors, 0, numSurvivors);
        return survivors;
    }

    double[][][] add3DMatrix(double[][][] matrix1, double[][][] matrix2){

        if (matrix1.length != matrix2.length) throw new RuntimeException("Illegal matrix sizes.");

        double[][][] newZ = new double[matrix1.length][][];
        for (int i = 0; i < matrix1.length; i++) {

            if (matrix1[i].length != matrix2[i].length) throw new RuntimeException("Illegal matrix sizes.");

            double[][] newY = new double[matrix1[i].length][];
            for (int j = 0; j < matrix1[i].length; j++) {

                if (matrix1[i][j].length != matrix2[i][j].length) throw new RuntimeException("Illegal matrix sizes.");

                double[] newX = new double[matrix1[i][j].length];
                for (int k = 0; k < matrix1[i][j].length; k++) {

                    newX [k] = matrix1[i][j][k] + matrix2[i][j][k];
                }
                newY[j] = newX;
            }
            newZ[i] = newY;
        }

        return newZ;
    }

    double[][][] fill3DMatrix(double[][][] input, double value){

        for (int i = 0; i < input.length; i++) {

            for (int j = 0; j < input[i].length; j++) {

                for (int k = 0; k < input[i][j].length; k++) {

                    input[i][j][k] = value;
                }
            }
        }
        return input;
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
