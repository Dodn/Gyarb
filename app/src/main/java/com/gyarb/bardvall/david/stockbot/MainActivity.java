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
    String exampleRange = "Sheet1!A1:G";

    static String[] Letter = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private TextView mOutputText;
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

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        for (int i = 0; i < GeneticPredict1.length; i++) {
            GeneticPredict1[i] = new Brain(GenePred1ID[i], GenePred1Dimens, defaultWeights);
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

    public static double[] Think(double[][][] brain, double[] data){
        double[][] inputs = new double[data.length][1];
        for (int i = 0; i < data.length; i++) {
            inputs[i][0] = data[i];
        }

        for (double[][] weightLayer : brain) {
            inputs = Activation(mult(weightLayer, inputs));
        }

        double[] results = new double[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            results[i] = inputs[i][0];
        }

        return results;
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

    public static double[][] Activation(double[][] input){

        double[][] result = new double[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                result[i][j] = LeakyReLU(input[i][j]);
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
                for (int i = 0; i < brains.length; i++) {

                    for (int j = 0; j < brains[i].weights.length; j++) {

                        String range = "Layer" + (j+1) + "!A1:" + Letter[brains[i].dimens[j] - 1] + brains[i].dimens[j + 1];

                        List<List<Object>> values = new ArrayList<>();
                        for (int k = 0; k < brains[i].weights[j].length; k++) {

                            List<Object> row = new ArrayList<>();
                            for (int l = 0; l < (brains[i].weights[j][k].length); l++) {
                                row.add((brains[i].weights[j][k][l]));
                            }
                            values.add(row);
                        }

                        ValueRange body = new ValueRange()
                                .setValues(values);
                        UpdateValuesResponse result =
                                mService.spreadsheets().values().update(brains[i].id, range, body)
                                        .setValueInputOption("USER_ENTERED")
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

                    String range = "Layer" + (i+1) + "!A1:" + Letter[dimens[i] - 1] + dimens[i + 1];
                    ValueRange response = this.mService.spreadsheets().values()
                            .get(spreadsheetIds[net], range)
                            .execute();
                    List<List<Object>> values = response.getValues();
                    double[][] layer = new double[dimens[i + 1]][dimens[i]];
                    if (values != null) {
                        for (int j = 0; j < dimens[i + 1]; j++) {
                            for (int k = 0; k < dimens[i]; k++){
                                layer[j][k] = Double.parseDouble(values.get(j).get(k).toString());
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
