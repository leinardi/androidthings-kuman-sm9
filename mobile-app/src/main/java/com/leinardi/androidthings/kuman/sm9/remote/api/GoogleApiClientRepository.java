package com.leinardi.androidthings.kuman.sm9.remote.api;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
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
import com.leinardi.androidthings.kuman.sm9.common.api.BaseRepository;
import com.leinardi.androidthings.kuman.sm9.remote.BuildConfig;
import com.leinardi.androidthings.kuman.sm9.remote.R;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

import javax.inject.Inject;
import java.nio.charset.Charset;

import static com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository.ConnectionStatus.CONNECTED;
import static com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository.ConnectionStatus.CONNECTING;
import static com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository.ConnectionStatus.DISCONNECTED;

public class GoogleApiClientRepository extends BaseRepository {
    private final Application mApplication;
    private final BehaviorSubject<NearbyConnectionsStatusUpdate> mConnectionBehaviorPublishSubject;
    private GoogleApiClient mGoogleApiClient;
    private String mRemoteHostEndpoint;

    @Inject
    public GoogleApiClientRepository(Application application) {
        mApplication = application;
        mConnectionBehaviorPublishSubject = BehaviorSubject.create();
        setupGoogleApiClient();
    }

    public void clear() {
        disconnect();
        getCompositeDisposable().clear();
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mApplication)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle connectionHint) {
                        startNearbyConnectionsDiscovery();
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Timber.d("onConnectionSuspended: %s", cause);
                        // Try to re-connect
                        mGoogleApiClient.reconnect();
                    }
                })
                .addOnConnectionFailedListener(connectionResult -> {
                    Timber.d("onConnectionFailed: " + connectionResult.getErrorCode() + "\n" +
                            connectionResult.getErrorMessage());
                    updateConnectionStatus(DISCONNECTED, R.string.connection_info_google_api_connection_failed,
                            connectionResult.getErrorCode(), connectionResult.getErrorMessage());
                })
                .addApi(Nearby.CONNECTIONS_API)
                .build();
    }

    public void connect() {
        Timber.d("connect");
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        } else {
            startNearbyConnectionsDiscovery();
        }
    }

    public void disconnect() {
        Timber.d("disconnect");
        if (mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnected() || TextUtils.isEmpty(mRemoteHostEndpoint)) {
                Nearby.Connections.stopDiscovery(mGoogleApiClient);
                return;
            }
            sendMessage("Client disconnecting");
            Nearby.Connections.disconnectFromEndpoint(mGoogleApiClient, mRemoteHostEndpoint);
            mRemoteHostEndpoint = null;
            updateConnectionStatus(DISCONNECTED, R.string.connection_info_disconnected);

            mGoogleApiClient.disconnect();
        }
    }

    private void updateConnectionStatus(@ConnectionStatus int connectionStatus, @StringRes int resId, Object... formatArgs) {
        mConnectionBehaviorPublishSubject.onNext(new NearbyConnectionsStatusUpdate(connectionStatus, mApplication.getString(resId, formatArgs)));
    }

    public void subscribeToConnectionStatusUpdate(DisposableObserver<NearbyConnectionsStatusUpdate> observer) {
        getCompositeDisposable().add(mConnectionBehaviorPublishSubject.subscribeWith(observer));
    }

    private void startNearbyConnectionsDiscovery() {
        Timber.d("startNearbyConnectionsDiscovery");
        updateConnectionStatus(CONNECTING, R.string.connection_info_start_discovery);

        Nearby.Connections.startDiscovery(
                mGoogleApiClient,
                BuildConfig.NEARBY_SERVICE_ID, new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                        handleOnEndpointFound(endpointId, info);
                    }

                    @Override
                    public void onEndpointLost(String endpointId) {
                        handleOnEndpointLost(endpointId);

                    }
                },
                new DiscoveryOptions(Strategy.P2P_STAR)
        ).setResultCallback(this::handleStartDiscoveryResult);
    }

    private void handleOnEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
        Timber.d("onEndpointFound:" + endpointId + ":" + info.getEndpointName());
        updateConnectionStatus(CONNECTING, R.string.connection_info_endpoint_found, info.getEndpointName());
        Nearby.Connections.requestConnection(mGoogleApiClient, null, endpointId, new ConnectionLifecycleCallback() {
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
        }).setResultCallback(this::handleEndpointConnectionResult);
    }

    private void handleOnEndpointLost(String endpointId) {
        // An endpoint that was previously available for connection is no longer.
        // It may have stopped advertising, gone out of range, or lost connectivity.
        Timber.d("onEndpointLost: %s", endpointId);
        updateConnectionStatus(DISCONNECTED, R.string.connection_info_connection_lost);
    }

    private void handleStartDiscoveryResult(Status status) {
        if (status.isSuccess()) {
            Timber.d("Discovering...");
            updateConnectionStatus(CONNECTING, R.string.connection_info_discovering);
        } else {
            Timber.d("Discovery failed: " + status.getStatusMessage() + "(" + status.getStatusCode() + ")");
            updateConnectionStatus(DISCONNECTED, R.string.connection_info_discovery_failed, status.getStatusCode(), status
                    .getStatusMessage());
        }
    }

    private void handleOnConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
        Timber.d("onConnectionInitiated. Token: %s", connectionInfo.getAuthenticationToken());
        // Automatically accept the connection on both sides"
        Nearby.Connections.acceptConnection(mGoogleApiClient, endpointId, new PayloadCallback() {
            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {
                if (payload.getType() == Payload.Type.BYTES) {
                    Timber.d("onPayloadReceived: %s", new String(payload.asBytes()));
                }
            }

            @Override
            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                // Provides updates about the progress of both incoming and outgoing payloads
            }
        });
    }

    private void handleOnConnectionResult(String endpointId, ConnectionResolution resolution) {
        Timber.d("onConnectionResult:" + endpointId + ":" + resolution.getStatus());
        if (resolution.getStatus().isSuccess()) {
            Timber.d("Connected successfully");
            updateConnectionStatus(CONNECTED, R.string.connection_info_connected);
            Nearby.Connections.stopDiscovery(mGoogleApiClient);
            mRemoteHostEndpoint = endpointId;
        } else {
            if (resolution.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED) {
                Timber.d("The connection was rejected by one or both sides");
            } else {
                Timber.d("Connection to " + endpointId + " failed. Code: " + resolution.getStatus().getStatusCode());
            }
            updateConnectionStatus(DISCONNECTED, R.string.connection_info_discovery_failed, resolution.getStatus().getStatusCode(),
                    resolution.getStatus().getStatusMessage());
        }
    }

    private void handleOnDisconnected(String endpointId) {
        // We've been disconnected from this endpoint. No more data can be sent or received.
        Timber.d("onDisconnected: %s", endpointId);
        updateConnectionStatus(DISCONNECTED, R.string.connection_info_disconnected);
    }

    private void handleEndpointConnectionResult(Status status) {
        if (status.isSuccess()) {
            // We successfully requested a connection. Now both sides
            // must accept before the connection is established.
        } else {
            // Nearby Connections failed to request the connection.
        }
        Timber.d("onEndpointRequestConnection status: %s:%s ", status.getStatusCode(), status.getStatusMessage());
    }

    public void sendMessage(String message) {
        Timber.d("About to send message: %s", message);
        Nearby.Connections.sendPayload(mGoogleApiClient, mRemoteHostEndpoint, Payload.fromBytes(message.getBytes(Charset.forName("UTF-8"))));
    }

    @IntDef({DISCONNECTED, CONNECTING, CONNECTED})
    public @interface ConnectionStatus {
        int DISCONNECTED = R.drawable.ic_disconnected;
        int CONNECTING = R.drawable.ic_connecting;
        int CONNECTED = R.drawable.ic_connected;
    }

    public static class NearbyConnectionsStatusUpdate {
        @ConnectionStatus
        private final int mConnectionStatus;
        private final String mConnectionInfo;

        public NearbyConnectionsStatusUpdate(int connectionStatus, String connectionInfo) {
            mConnectionStatus = connectionStatus;
            mConnectionInfo = connectionInfo;
        }

        public int getConnectionStatus() {
            return mConnectionStatus;
        }

        public String getConnectionInfo() {
            return mConnectionInfo;
        }
    }
}
