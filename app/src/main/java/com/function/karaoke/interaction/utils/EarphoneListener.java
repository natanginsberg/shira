package com.function.karaoke.interaction.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.function.karaoke.interaction.R;

public class EarphoneListener {

    private final Context context;
    private boolean bluetoothConnectionExists;
    private BroadcastReceiver bReceiver;
    private boolean bluetoothConnected;
    private BroadcastReceiver mReceiver;
    private boolean microphonePluggedIn;
    private boolean prompted;

    public EarphoneListener(Context context) {
        this.context = context;
        createEarphoneReceivers();
    }

    private void createEarphoneReceivers() {
        createHeadphoneReceiver();
//        createBluetoothReceiver();
//        checkIfHeadsetIsPairedAlready();
    }

    private void createBluetoothReceiver() {
        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                    if (isBluetoothHeadsetConnected(device))
                    bluetoothConnected = true;
                    //Device found
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

                    bluetoothConnected = true;
                    //Device is now connected
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    bluetoothConnected = false;
                    bluetoothConnectionExists = false;
                    //Device has disconnected
                }
            }
        };
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        receiverFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        receiverFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(bReceiver, receiverFilter);
    }

    private void createHeadphoneReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                int iii = 2;
                if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                    iii = intent.getIntExtra("state", -1);
                    if (Integer.valueOf(iii) == 0) {
                        microphonePluggedIn = false;
//                        Toast.makeText(getApplicationContext(), "microphone not plugged in", Toast.LENGTH_LONG).show();
                        if (!bluetoothConnectionExists && !bluetoothConnected && !prompted)
                            promptUserToConnectEarphones();
                    }
                    if (Integer.valueOf(iii) == 1) {
                        microphonePluggedIn = true;
//                        Toast.makeText(getApplicationContext(), "microphone plugged in", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        context.registerReceiver(mReceiver, receiverFilter);
    }

    private void promptUserToConnectEarphones() {

        Toast toast = Toast.makeText(context, context.getResources().getString(R.string.attach_earphones), Toast.LENGTH_LONG);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
        toast.show();

    }

    private void checkIfHeadsetIsPairedAlready() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            int[] profiles = {BluetoothProfile.A2DP, BluetoothProfile.HEADSET, BluetoothProfile.HEALTH};
            for (int profileId : profiles) {
                if (BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(profileId)) {
                    bluetoothConnectionExists = true;
                    break;
                }
            }
        }
    }

    public boolean getEarphonesUsed() {
        return microphonePluggedIn || bluetoothConnectionExists || bluetoothConnected;
    }
}
