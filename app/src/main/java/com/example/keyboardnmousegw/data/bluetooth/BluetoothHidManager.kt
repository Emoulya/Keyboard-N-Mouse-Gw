package com.example.keyboardnmousegw.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.example.keyboardnmousegw.domain.models.HidConsts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager untuk menangani koneksi Bluetooth HID.
 * Security Note: Anotasi SuppressLint digunakan di sini karena pengecekan izin
 * (BLUETOOTH_CONNECT) merupakan tanggung jawab Presentation Layer (UI) sebelum memanggil class ini.
 */
@SuppressLint("MissingPermission")
class BluetoothHidManager(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    // Komponen utama HID dari Android SDK
    private var hidDevice: BluetoothHidDevice? = null
    private var hostDevice: BluetoothDevice? = null

    // State Management untuk UI (Scalable & Reactive)
    private val _connectionState = MutableStateFlow(BluetoothProfile.STATE_DISCONNECTED)
    val connectionState: StateFlow<Int> = _connectionState.asStateFlow()

    // State untuk menyimpan perangkat yang sukses terkoneksi
    private val _connectedDevice = MutableStateFlow<BluetoothDevice?>(null)
    val connectedDevice: StateFlow<BluetoothDevice?> = _connectedDevice.asStateFlow()

    // 1. Callback untuk memantau status perangkat host (PC/Laptop)
    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            super.onConnectionStateChanged(device, state)
            Log.d("BluetoothHidManager", "Connection state changed: $state")
            if (state == BluetoothProfile.STATE_CONNECTED) {
                hostDevice = device
                _connectedDevice.value = device
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                hostDevice = null
                _connectedDevice.value = null
            }
            _connectionState.value = state
        }

        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            super.onAppStatusChanged(pluggedDevice, registered)
            Log.d("BluetoothHidManager", "App registered status: $registered")
        }
    }

    // 2. Listener untuk mendapatkan proxy BluetoothHidDevice dari sistem Android
    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as BluetoothHidDevice
                registerApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
            }
        }
    }

    init {
        // Mulai meminta akses ke Profile HID Device ke OS Android
        bluetoothAdapter?.getProfileProxy(context, profileListener, BluetoothProfile.HID_DEVICE)
    }

    // 3. Mendaftarkan aplikasi dengan Report Descriptor kita
    private fun registerApp() {
        if (hidDevice != null) {
            val sdpSettings = BluetoothHidDeviceAppSdpSettings(
                "KeyboardNMouseGW",
                "Mobile Input Device",
                "Android",
                BluetoothHidDevice.SUBCLASS1_COMBO.toByte(),
                HidConsts.HID_REPORT_DESCRIPTOR
            )

            hidDevice?.registerApp(
                sdpSettings,
                null,
                null,
                context.mainExecutor,
                hidCallback
            )
        }
    }

    // --- STATE UNTUK SCANNING ---
    private val _scannedDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    val scannedDevices: StateFlow<Set<BluetoothDevice>> = _scannedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // BroadcastReceiver untuk menangkap perangkat yang ditemukan
    private val scanReceiver = object : android.content.BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: android.content.Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && !device.name.isNullOrEmpty()) {
                        _scannedDevices.value = _scannedDevices.value + device
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        _scannedDevices.value = emptySet()

        // Daftarkan receiver secara dinamis
        val filter = android.content.IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(scanReceiver, filter)

        bluetoothAdapter?.startDiscovery()
        _isScanning.value = true
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        try {
            context.unregisterReceiver(scanReceiver)
        } catch (e: IllegalArgumentException) {
        }
        _isScanning.value = false
    }

    @SuppressLint("MissingPermission")
    fun connectDevice(device: BluetoothDevice) {
        stopScanning()

        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            // Jika belum pairing, munculkan pop-up PIN
            device.createBond()
        } else {
            // Jika SUDAH pairing, langsung hubungkan sebagai Keyboard/Mouse (HID)
            hidDevice?.connect(device)
        }
    }

    fun sendMouseReport(reportBytes: ByteArray) {
        hostDevice?.let { host ->
            hidDevice?.sendReport(host, HidConsts.ID_MOUSE, reportBytes)
        }
    }

    fun sendKeyboardReport(reportBytes: ByteArray) {
        hostDevice?.let { host ->
            hidDevice?.sendReport(host, HidConsts.ID_KEYBOARD, reportBytes)
        }
    }

    // Membersihkan resource saat aplikasi ditutup
    fun onDestroy() {
        hidDevice?.unregisterApp()
        bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
    }
}