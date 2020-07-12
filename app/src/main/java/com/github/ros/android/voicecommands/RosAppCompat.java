package com.github.ros.android.voicecommands;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;

import com.google.common.base.Preconditions;

import org.ros.address.InetAddressFactory;
import org.ros.android.MasterChooser;
import org.ros.android.NodeMainExecutorService;
import org.ros.android.NodeMainExecutorServiceListener;
import org.ros.exception.RosRuntimeException;
import org.ros.node.NodeMainExecutor;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;

/** ROS Activity that supports AppCompatActivity members. */
public abstract class RosAppCompat extends AppCompatActivity {
    protected static final int MASTER_CHOOSER_REQUEST_CODE = 0;
    private final RosAppCompat.NodeMainExecutorServiceConnection nodeMainExecutorServiceConnection;
    private final String notificationTicker;
    private final String notificationTitle;
    private Class<?> masterChooserActivity;
    private int masterChooserRequestCode;
    protected NodeMainExecutorService nodeMainExecutorService;
    private RosAppCompat.OnActivityResultCallback onActivityResultCallback;

    protected RosAppCompat(String notificationTicker, String notificationTitle) {
        this(notificationTicker, notificationTitle, (URI)null);
    }

    protected RosAppCompat(String notificationTicker, String notificationTitle, URI customMasterUri) {
        this.masterChooserActivity = MasterChooser.class;
        this.masterChooserRequestCode = 0;
        this.onActivityResultCallback = new RosAppCompat.OnActivityResultCallback() {
            public void execute(int requestCode, int resultCode, Intent data) {
                if (resultCode == -1) {
                    if (requestCode == 0) {
                        String networkInterfaceName = data.getStringExtra("ROS_MASTER_NETWORK_INTERFACE");
                        String host;
                        if (networkInterfaceName != null && !networkInterfaceName.equals("")) {
                            try {
                                NetworkInterface networkInterface = NetworkInterface.getByName(networkInterfaceName);
                                host = InetAddressFactory.newNonLoopbackForNetworkInterface(networkInterface).getHostAddress();
                            } catch (SocketException var9) {
                                throw new RosRuntimeException(var9);
                            }
                        } else {
                            host = RosAppCompat.this.getDefaultHostAddress();
                        }

                        RosAppCompat.this.nodeMainExecutorService.setRosHostname(host);
                        if (data.getBooleanExtra("ROS_MASTER_CREATE_NEW", false)) {
                            RosAppCompat.this.nodeMainExecutorService.startMaster(data.getBooleanExtra("ROS_MASTER_PRIVATE", true));
                        } else {
                            URI uri;
                            try {
                                uri = new URI(data.getStringExtra("ROS_MASTER_URI"));
                            } catch (URISyntaxException var8) {
                                throw new RosRuntimeException(var8);
                            }

                            RosAppCompat.this.nodeMainExecutorService.setMasterUri(uri);
                        }

                        (new AsyncTask<Void, Void, Void>() {
                            protected Void doInBackground(Void... params) {
                                RosAppCompat.this.init(RosAppCompat.this.nodeMainExecutorService);
                                return null;
                            }
                        }).execute(new Void[0]);
                    } else {
                        RosAppCompat.this.nodeMainExecutorService.forceShutdown();
                    }
                }

            }
        };
        this.notificationTicker = notificationTicker;
        this.notificationTitle = notificationTitle;
        this.nodeMainExecutorServiceConnection = new RosAppCompat.NodeMainExecutorServiceConnection(customMasterUri);
    }

    protected RosAppCompat(String notificationTicker, String notificationTitle, Class<?> activity, int requestCode) {
        this(notificationTicker, notificationTitle);
        this.masterChooserActivity = activity;
        this.masterChooserRequestCode = requestCode;
    }

    protected void onStart() {
        super.onStart();
        this.bindNodeMainExecutorService();
    }

    protected void bindNodeMainExecutorService() {
        Intent intent = new Intent(this, NodeMainExecutorService.class);
        intent.setAction("org.ros.android.ACTION_START_NODE_RUNNER_SERVICE");
        intent.putExtra("org.ros.android.EXTRA_NOTIFICATION_TICKER", this.notificationTicker);
        intent.putExtra("org.ros.android.EXTRA_NOTIFICATION_TITLE", this.notificationTitle);
        this.startService(intent);
        Preconditions.checkState(this.bindService(intent, this.nodeMainExecutorServiceConnection, 1), "Failed to bind NodeMainExecutorService.");
    }

    protected void onDestroy() {
        this.unbindService(this.nodeMainExecutorServiceConnection);
        this.nodeMainExecutorService.removeListener(this.nodeMainExecutorServiceConnection.getServiceListener());
        super.onDestroy();
    }

    protected void init() {
        (new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                RosAppCompat.this.init(RosAppCompat.this.nodeMainExecutorService);
                return null;
            }
        }).execute(new Void[0]);
    }

    protected abstract void init(NodeMainExecutor var1);

    public void startMasterChooser() {
        Preconditions.checkState(this.getMasterUri() == null);
        super.startActivityForResult(new Intent(this, this.masterChooserActivity), this.masterChooserRequestCode);
    }

    public URI getMasterUri() {
        Preconditions.checkNotNull(this.nodeMainExecutorService);
        return this.nodeMainExecutorService.getMasterUri();
    }

    public String getRosHostname() {
        Preconditions.checkNotNull(this.nodeMainExecutorService);
        return this.nodeMainExecutorService.getRosHostname();
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        Preconditions.checkArgument(requestCode != this.masterChooserRequestCode);
        super.startActivityForResult(intent, requestCode);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.onActivityResultCallback != null) {
            this.onActivityResultCallback.execute(requestCode, resultCode, data);
        }

    }

    private String getDefaultHostAddress() {
        return InetAddressFactory.newNonLoopback().getHostAddress();
    }

    public void setOnActivityResultCallback(RosAppCompat.OnActivityResultCallback callback) {
        this.onActivityResultCallback = callback;
    }

    public interface OnActivityResultCallback {
        void execute(int var1, int var2, Intent var3);
    }

    private final class NodeMainExecutorServiceConnection implements ServiceConnection {
        private NodeMainExecutorServiceListener serviceListener;
        private URI customMasterUri;

        public NodeMainExecutorServiceConnection(URI customUri) {
            this.customMasterUri = customUri;
        }

        public void onServiceConnected(ComponentName name, IBinder binder) {
            RosAppCompat.this.nodeMainExecutorService = ((NodeMainExecutorService.LocalBinder)binder).getService();
            if (this.customMasterUri != null) {
                RosAppCompat.this.nodeMainExecutorService.setMasterUri(this.customMasterUri);
                RosAppCompat.this.nodeMainExecutorService.setRosHostname(RosAppCompat.this.getDefaultHostAddress());
            }

            this.serviceListener = new NodeMainExecutorServiceListener() {
                public void onShutdown(NodeMainExecutorService nodeMainExecutorService) {
                    if (!RosAppCompat.this.isFinishing()) {
                        RosAppCompat.this.finish();
                    }

                }
            };
            RosAppCompat.this.nodeMainExecutorService.addListener(this.serviceListener);
            if (RosAppCompat.this.getMasterUri() == null) {
                RosAppCompat.this.startMasterChooser();
            } else {
                RosAppCompat.this.init();
            }

        }

        public void onServiceDisconnected(ComponentName name) {
            RosAppCompat.this.nodeMainExecutorService.removeListener(this.serviceListener);
            this.serviceListener = null;
        }

        public NodeMainExecutorServiceListener getServiceListener() {
            return this.serviceListener;
        }
    }
}
