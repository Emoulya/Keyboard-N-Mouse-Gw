# 🔍 Code Audit Report — KeyboardnMouseGw
**Tanggal Audit:** 20 Mei 2026  
**Project:** Android HID Keyboard & Mouse via Bluetooth    
**Scope:** Seluruh source code (`app/src/main/`)

---

## Ringkasan Eksekutif

Project ini merupakan aplikasi Android yang berfungsi sebagai keyboard dan mouse Bluetooth (HID). Arsitektur sudah cukup baik dengan pemisahan Layer (Data, Domain, Presentation) mengikuti prinsip Clean Architecture. Namun ditemukan **1 bug kritis**, **5 bug tinggi**, dan beberapa isu minor yang perlu diperbaiki sebelum aplikasi dapat berfungsi secara penuh.

| Severity | Jumlah |
|---|---|
| 🔴 CRITICAL | 1 |
| 🟠 HIGH | 5 |
| 🟡 MEDIUM | 3 |
| 🔵 LOW | 3 |

---



### [C-01] Keyboard Software Tidak Mengirim Data HID
**File:** `MainScreen.kt`, `MainViewModel.kt`  
**Dampak:** Fitur keyboard **tidak berfungsi sama sekali** — tidak ada teks yang terkirim ke host.

Terdapat `TextField` tersembunyi untuk menangkap input keyboard, namun nilai yang diketik (`keyboardInput`) **tidak pernah diteruskan ke ViewModel** sebagai HID report:

```kotlin
// MainScreen.kt
var keyboardInput by remember { mutableStateOf("") }

TextField(
    value = keyboardInput,
    onValueChange = { keyboardInput = it },  // ← State berubah
    modifier = Modifier.size(1.dp).alpha(0f)...
)
```

Tidak ada `LaunchedEffect` atau observer yang memantau perubahan `keyboardInput` dan memanggil `mainViewModel.sendKeyboardReport(...)`. Tombol keyboard di toolbar bisa memunculkan keyboard, tapi semua ketikan hilang begitu saja.

---

## 🟠 HIGH

---

### [H-01] Setelah Bonding, Device Tidak Otomatis Connect
**File:** `BluetoothHidManager.kt`  
**Dampak:** User harus memilih device **dua kali** (sekali untuk pair, sekali lagi untuk connect).

```kotlin
fun connectDevice(device: BluetoothDevice) {
    stopScanning()
    if (device.bondState != BluetoothDevice.BOND_BONDED) {
        device.createBond()  // ← Memulai bonding
        // Tidak ada listener setelah bonding selesai!
    } else {
        hidDevice?.connect(device)
    }
}
```

`createBond()` bersifat asynchronous. Setelah bonding selesai, tidak ada `BroadcastReceiver` yang memantau `ACTION_BOND_STATE_CHANGED` untuk otomatis memanggil `hidDevice?.connect(device)`.

---

### [H-02] `pointerSpeed` dan `scrollSpeed` Tidak Digunakan
**File:** `MainViewModel.kt`, `SettingsViewModel.kt`  
**Dampak:** Slider kecepatan pointer dan scroll di Settings **tidak berpengaruh apapun** pada perilaku mouse.

Setting disimpan ke DataStore dengan benar, namun `MainViewModel` sama sekali tidak menggunakannya:

```kotlin
// MainViewModel.kt
fun moveMouse(dx: Float, dy: Float) {
    val byteDx = dx.toInt().coerceIn(-127, 127).toByte()  // ← Kecepatan hardcoded!
    val byteDy = dy.toInt().coerceIn(-127, 127).toByte()
    ...
}

fun scroll(dy: Float) {
    val scaledScroll = (dy / 5).toInt()  // ← Faktor scroll hardcoded = 5!
    ...
}
```

`MainViewModel` tidak memiliki akses ke `SettingsRepository` sama sekali. `pointerSpeed` dan `scrollSpeed` dibaca oleh `SettingsViewModel` tetapi tidak dishare ke `MainViewModel`.


---

### [H-03] Fitur Vibration Tidak Diimplementasikan
**File:** `SettingsDrawer.kt`, `SettingsViewModel.kt`  
**Dampak:** Toggle "Touch Vibration" di Settings **tidak melakukan apa-apa**.

State `isVibrationEnabled` dibaca dari DataStore dan ditampilkan di UI, namun tidak ada kode yang memanggil `Vibrator` atau `VibrationEffect` di manapun dalam codebase.

```kotlin
// SettingsDrawer.kt
Switch(
    checked = vibrationEnabled,
    onCheckedChange = { viewModel.updateVibration(it) }  // ← Tersimpan tapi tidak dipakai
)
```

---

### [H-04] `RequireBluetoothPermissions` Tidak Cek Izin yang Sudah Diberikan
**File:** `RequireBluetoothPermissions.kt`  
**Dampak:** Setiap kali app dibuka, user **selalu melihat layar "Berikan Izin"** meski izin sudah diberikan sebelumnya. Konten utama tidak akan tampil tanpa menekan tombol lagi.

```kotlin
var permissionsGranted by remember { mutableStateOf(false) }
// ↑ Selalu false saat pertama Composition, tidak dicek dari sistem
```

---

### [H-05] BroadcastReceiver Bisa Didaftarkan Ganda
**File:** `BluetoothHidManager.kt`  
**Dampak:** Memory leak dan penerimaan event ganda jika `startScanning()` dipanggil lebih dari sekali berturut-turut.

```kotlin
fun startScanning() {
    if (bluetoothAdapter?.isDiscovering == true) {
        bluetoothAdapter.cancelDiscovery()
    }
    // Tidak ada unregisterReceiver sebelum registerReceiver!
    context.registerReceiver(scanReceiver, filter)  // ← Bisa didaftarkan 2x+
    bluetoothAdapter?.startDiscovery()
}
```

Setiap panggilan `startScanning()` mendaftarkan `scanReceiver` lagi tanpa melepas yang lama, sehingga event `ACTION_FOUND` bisa diterima dua kali dan perangkat muncul duplikat.

---

## 🟡 MEDIUM

---

### [M-01] `onDestroy()` Tidak Melepas BroadcastReceiver
**File:** `BluetoothHidManager.kt`  
**Dampak:** Jika app ditutup saat scanning sedang berjalan, `scanReceiver` tidak pernah di-unregister → **memory leak**.

```kotlin
fun onDestroy() {
    hidDevice?.unregisterApp()
    bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
    // ← stopScanning() tidak dipanggil di sini!
}
```

---

### [M-02] `KeyboardReport` data class dengan `ByteArray` — Equality Broken
**File:** `KeyboardReport.kt`  
**Dampak:** `==` operator dan `hashCode()` pada `KeyboardReport` menggunakan **referensi**, bukan isi array — menyebabkan perbandingan yang tidak akurat.

```kotlin
data class KeyboardReport(
    var modifier: Byte = 0,
    val keys: ByteArray = ByteArray(6) { 0 }  // ← ByteArray: equals() pakai referensi!
)
```

Kotlin `data class` menghasilkan `equals()`/`hashCode()` yang memanggil `ByteArray.equals()` (referensi), bukan konten array. Ini dapat menyebabkan bug jika dua report dengan isi sama dianggap berbeda.

---

### [M-03] Klik Mouse Tidak Ada Jeda antara Press dan Release
**File:** `MainScreen.kt`, `MainViewModel.kt`  
**Dampak:** Beberapa host (PC/Laptop) mungkin tidak mendeteksi click karena kedua event terkirim dalam **satu frame** (< 1ms).

```kotlin
// MainScreen.kt
ActionButton(text = "Left Click", onClick = {
    mainViewModel.setLeftClick(true)   // ← Langsung kirim press
    mainViewModel.setLeftClick(false)  // ← Langsung kirim release
})
```

Standar HID mengharapkan ada jeda yang cukup (minimal beberapa milidetik) antara press dan release. Meski di banyak host ini bekerja, ada risiko click tidak terdeteksi.

---

## 🔵 LOW

---

### [L-01] Permission `BLUETOOTH_ADVERTISE` Dideklarasikan tapi Tidak Digunakan
**File:** `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
```

---

### [L-02] Kondisi Logika Redundan pada Multi-Touch Handler
**File:** `MainScreen.kt`

```kotlin
} else if (pressedPointers.size > 2 || pressedPointers.size < 2) {
    pressTime = 0L
}
```

Kondisi `size > 2 || size < 2`.

---

### [L-03] Versi Dependency Sudah Usang
**File:** `gradle/libs.versions.toml`

Beberapa dependency sudah jauh tertinggal dari versi stabil terbaru:

| Dependency | Versi Saat Ini | Versi Stabil Terbaru |
|---|---|---|
| `core-ktx` | 1.10.1 | 1.18.0 |
| `appcompat` | 1.6.1 | 1.7.1 |
| `junit (androidx)` | 1.1.5 | 1.3.0 |
| `espresso-core` | 3.5.1 | 3.6.1 |

Disarankan untuk update secara berkala menggunakan `./gradlew dependencyUpdates`.

---