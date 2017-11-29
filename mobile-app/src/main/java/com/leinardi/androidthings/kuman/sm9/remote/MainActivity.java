package com.leinardi.androidthings.kuman.sm9.remote;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.erz.joysticklibrary.JoyStick;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.nio.charset.Charset;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1982;
    private GoogleApiClient mGoogleApiClient;
    private String mRemoteHostEndpoint;
    private boolean mIsConnected;
    private TextView mLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        JoyStick joyStick = findViewById(R.id.motor_joystick);
        joyStick.setType(JoyStick.TYPE_4_AXIS);
        joyStick.setListener(new JoyStick.JoyStickListener() {
            @Override
            public void onMove(JoyStick joyStick, double angle, double power, int direction) {
                if (mIsConnected) {
                    sendMessage("Direction " + direction);
                }
            }

            @Override
            public void onTap() {

            }

            @Override
            public void onDoubleTap() {

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Timber.d("onConnected: start discovering hosts to send connection requests");
                        startDiscovery();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Timber.d("onConnectionSuspended: " + i);
                        // Try to re-connect
                        mGoogleApiClient.reconnect();
                    }
                })
                .addOnConnectionFailedListener(connectionResult -> Timber.d("onConnectionFailed: " + connectionResult.getErrorCode() + "\n" + connectionResult.getErrorMessage()))
                .addApi(Nearby.CONNECTIONS_API)
                .build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //TODO properly handle permissions
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //TODO properly handle permissions
                }
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("onStart: connect");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.d("onStop: disconnect");

        if (mGoogleApiClient.isConnected()) {
            if (!mIsConnected || TextUtils.isEmpty(mRemoteHostEndpoint)) {
                Nearby.Connections.stopDiscovery(mGoogleApiClient);
                return;
            }
            sendMessage("Client disconnecting");
            Nearby.Connections.disconnectFromEndpoint(mGoogleApiClient, mRemoteHostEndpoint);
            mRemoteHostEndpoint = null;
            mIsConnected = false;

            mGoogleApiClient.disconnect();
        }
    }

    private void startDiscovery() {
        Timber.d("startDiscovery");

        Nearby.Connections.startDiscovery(mGoogleApiClient, BuildConfig.NEARBY_SERVICE_ID, new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                        Timber.d("onEndpointFound:" + endpointId + ":" + info.getEndpointName());

                        Nearby.Connections
                                .requestConnection(mGoogleApiClient, null, endpointId, new ConnectionLifecycleCallback() {
                                    @Override
                                    public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                                        Timber.d("onConnectionInitiated. Token: " + connectionInfo.getAuthenticationToken());
                                        // Automatically accept the connection on both sides"
                                        Nearby.Connections.acceptConnection(mGoogleApiClient, endpointId, new PayloadCallback() {
                                            @Override
                                            public void onPayloadReceived(String endpointId, Payload payload) {
                                                if (payload.getType() == Payload.Type.BYTES) {
                                                    Timber.d("onPayloadReceived: " + new String(payload.asBytes()));
                                                }
                                            }

                                            @Override
                                            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                                                // Provides updates about the progress of both incoming and outgoing payloads
                                            }
                                        });
                                    }

                                    @Override
                                    public void onConnectionResult(String endpointId, ConnectionResolution resolution) {
                                        Timber.d("onConnectionResult:" + endpointId + ":" + resolution.getStatus());
                                        if (resolution.getStatus().isSuccess()) {
                                            Timber.d("Connected successfully");
                                            Nearby.Connections.stopDiscovery(mGoogleApiClient);
                                            mRemoteHostEndpoint = endpointId;
                                            mIsConnected = true;
                                        } else {
                                            if (resolution.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED) {
                                                Timber.d("The connection was rejected by one or both sides");
                                            } else {
                                                Timber.d("Connection to " + endpointId + " failed. Code: " + resolution.getStatus().getStatusCode());
                                            }
                                            mIsConnected = false;
                                        }
                                    }

                                    @Override
                                    public void onDisconnected(String endpointId) {
                                        // We've been disconnected from this endpoint. No more data can be sent or received.
                                        Timber.d("onDisconnected: " + endpointId);
                                    }
                                })
                                .setResultCallback(status -> {
                                    if (status.isSuccess()) {
                                        // We successfully requested a connection. Now both sides
                                        // must accept before the connection is established.
                                    } else {
                                        // Nearby Connections failed to request the connection.
                                    }
                                });
                    }

                    @Override
                    public void onEndpointLost(String endpointId) {
                        // An endpoint that was previously available for connection is no longer.
                        // It may have stopped advertising, gone out of range, or lost connectivity.
                        Timber.d("onEndpointLost:" + endpointId);
                    }
                },
                new DiscoveryOptions(Strategy.P2P_STAR)
        )
                .setResultCallback(status -> {
                    if (status.isSuccess()) {
                        Timber.d("Discovering...");
                    } else {
                        Timber.d("Discovering failed: " + status.getStatusMessage() + "(" + status.getStatusCode() + ")");
                    }
                });
    }

    private void sendMessage(String message) {
        Timber.d("About to send message: " + message);
        Nearby.Connections.sendPayload(mGoogleApiClient, mRemoteHostEndpoint, Payload.fromBytes(message.getBytes(Charset.forName("UTF-8"))));
    }
//
//    private void initLayout() {
//        setContentView(R.layout.activity_main);
//        mLogs = (TextView) findViewById(R.id.nearby_logs);
//
//        findViewById(R.id.nearby_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!mGoogleApiClient.isConnected()) {
//                    Timber.d("Not connected");
//                    return;
//                }
//
//                sendMessage("Hello, Things!");
//            }
//        });
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
