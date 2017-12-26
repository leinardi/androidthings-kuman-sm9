/*
 * Copyright 2017 Roberto Leinardi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.leinardi.androidthings.kuman.sm9.common.api.car.ThingsMessage;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.BehaviorSubject;
import org.apache.commons.lang3.SerializationUtils;
import timber.log.Timber;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class GoogleApiClientRepository extends BaseRepository {
    private final Application mApplication;
    private GoogleApiClient mGoogleApiClient;
    private List<String> mRemotePeerEndpoints = new ArrayList<>();
    private final BehaviorSubject<ThingsMessage> mThingsMessageBehaviorSubject;

    public GoogleApiClientRepository(Application application) {
        mApplication = application;
        mThingsMessageBehaviorSubject = BehaviorSubject.create();
        setupGoogleApiClient();
    }

    @Override
    public void clear() {
        super.clear();
        disconnect();
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

    public void connect() {
        Timber.d("connect");
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        Timber.d("disconnect");
        if (mGoogleApiClient.isConnected()) {
            Nearby.Connections.stopAdvertising(mGoogleApiClient);

            if (!mRemotePeerEndpoints.isEmpty()) {
                Nearby.Connections.sendPayload(mGoogleApiClient,
                        mRemotePeerEndpoints,
                        Payload.fromBytes("Shutting down host".getBytes(Charset.forName("UTF-8"))));
                Nearby.Connections.stopAllEndpoints(mGoogleApiClient);
                mRemotePeerEndpoints.clear();
            }

            mGoogleApiClient.disconnect();
        }
    }

    private void startNearbyConnectionsAdvertising() {
        Nearby.Connections.startAdvertising(mGoogleApiClient,
                null,
                BuildConfig.NEARBY_SERVICE_ID,
                new ConnectionLifecycleCallback() {
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
            mThingsMessageBehaviorSubject.onNext(SerializationUtils.deserialize(payload.asBytes()));
        }
    }

    public void subscribeToThingsMessage(DisposableObserver<ThingsMessage> observer) {
        getCompositeDisposable().add(mThingsMessageBehaviorSubject.subscribeWith(observer));
    }
}
