package com.leinardi.androidthings.kuman.sm9.api;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.leinardi.androidthings.kuman.sm9.BuildConfig;
import com.leinardi.androidthings.kuman.sm9.common.api.BaseRepository;
import timber.log.Timber;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class GoogleApiClientRepository extends BaseRepository {
    private final Application mApplication;
    private GoogleApiClient mGoogleApiClient;
    private List<String> mRemotePeerEndpoints = new ArrayList<>();

    @Inject
    public GoogleApiClientRepository(Application application) {
        mApplication = application;
        setupGoogleApiClient();
        connectGoogleApiClient();
    }

    @Override
    public void clear() {
        super.clear();
        disconnectGoogleApiClient();
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mApplication)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Timber.d("onConnected: advertises on the network as the host");
                        startNearbyConnectionsAdvertising();
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Timber.d("onConnectionSuspended: %s", cause);
                        mGoogleApiClient.reconnect();
                    }
                })
                .addOnConnectionFailedListener(connectionResult -> Timber.e("onConnectionFailed: %s", connectionResult))
                .addApi(Nearby.CONNECTIONS_API)
                .build();
    }

    public void connectGoogleApiClient() {
        Timber.d("connectGoogleApiClient");
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnectGoogleApiClient() {
        Timber.d("disconnectGoogleApiClient");
        if (mGoogleApiClient.isConnected()) {
            Nearby.Connections.stopAdvertising(mGoogleApiClient);

            if (!mRemotePeerEndpoints.isEmpty()) {
                Nearby.Connections.sendPayload(mGoogleApiClient, mRemotePeerEndpoints, Payload.fromBytes("Shutting down host".getBytes(Charset
                        .forName("UTF-8"))));
                Nearby.Connections.stopAllEndpoints(mGoogleApiClient);
                mRemotePeerEndpoints.clear();
            }

            mGoogleApiClient.disconnect();
        }
    }

    private void startNearbyConnectionsAdvertising() {
        Nearby.Connections.startAdvertising(mGoogleApiClient, null, BuildConfig.NEARBY_SERVICE_ID, new ConnectionLifecycleCallback() {
                    @Override
                    public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                        handleOnConnectionInitiated(endpointId, connectionInfo);
                    }

                    @Override
                    public void onConnectionResult(String endpointId, ConnectionResolution resolution) {
                        handleOnConnectionResult(endpointId, resolution);
                    }

                    @Override
                    public void onDisconnected(String endpointId) {
                        handleOnDisconnected(endpointId);

                    }
                },
                new AdvertisingOptions(Strategy.P2P_STAR)
        ).setResultCallback(this::handleStartAdvertisingResult);
    }

    private void handleOnConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
        Timber.d("onConnectionInitiated. Token: %s", connectionInfo.getAuthenticationToken());
        // Automatically accept the connection on both sides"
        Nearby.Connections.acceptConnection(mGoogleApiClient, endpointId, new PayloadCallback() {
            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {
                handleOnPayloadReceived(endpointId, payload);
            }

            @Override
            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                // Provides updates about the progress of both incoming and outgoing payloads
            }
        });
    }

    private void handleOnConnectionResult(String endpointId, ConnectionResolution resolution) {
        Timber.d("onConnectionResult");
        if (resolution.getStatus().isSuccess()) {
            if (!mRemotePeerEndpoints.contains(endpointId)) {
                mRemotePeerEndpoints.add(endpointId);
            }
            Timber.d("Connected! (endpointId=" + endpointId + ")");
        } else {
            Timber.w("Connection to " + endpointId + " failed. Code: " + resolution.getStatus().getStatusCode());
        }
    }

    private void handleOnDisconnected(String endpointId) {
        // We've been disconnected from this endpoint. No more data can be sent or received.
        Timber.i("onDisconnected: %s", endpointId);
    }

    private void handleStartAdvertisingResult(Connections.StartAdvertisingResult result) {
        Timber.d("startNearbyConnectionsAdvertising:onResult:%s", result);
        if (result.getStatus().isSuccess()) {
            Timber.d("Advertising...");
        }
    }

    private void handleOnPayloadReceived(String endpointId, Payload payload) {
        if (payload.getType() == Payload.Type.BYTES) {
            Timber.d("onPayloadReceived: " + new String(payload.asBytes()));
            Nearby.Connections.sendPayload(mGoogleApiClient, endpointId, Payload.fromBytes("ACK".getBytes(Charset
                    .forName("UTF-8"))));
        }
    }
}
