package za.co.grab.yaldp

import android.app.PendingIntent
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.jayway.rplidarapi.RPLidarA2Api
import com.jayway.rplidarapi.ScanData

class MainActivity : AppCompatActivity() {
    private lateinit var usbManager: UsbManager
    private var TAG: String = "YALDP"
    private var usbSerialDevice: UsbSerialDevice? = null
    private lateinit var lidarView: LidarView

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.w(TAG, "Starting YALDP")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lidarView = findViewById<LidarView>(R.id.lidarView)

        // Remove the RadarView from its previous parent if it has one
        val parent = lidarView.parent as? ViewGroup
        parent?.removeView(lidarView)


        // Add the RadarView to the layout
        val layout = findViewById<LinearLayout>(R.id.layout)
        layout.addView(lidarView)

        usbManager = getSystemService(USB_SERVICE) as UsbManager
        val vendorId = 4292
        val productId = 60000

        val usbDevice: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        usbDevice?.let {
            val vendorId = usbDevice.vendorId
            val productId = usbDevice.productId

            // Now you can use vendorId and productId as needed
            Log.w(TAG, "Device Vendor ID: $vendorId")
            Log.w(TAG, "Device Product ID: $productId")
        }

        if (usbDevice != null) {
            if (usbDevice.vendorId == vendorId && usbDevice.productId == productId) {
                if (usbManager.hasPermission(usbDevice)) {
                    openSerialPort(usbDevice)
                    Log.e(TAG, "Opening USB serial port")
                } else {
                    Log.e(TAG, "No permissions")
                    val permissionIntent = PendingIntent.getBroadcast(
                        this,
                        0,
                        Intent(ACTION_USB_PERMISSION),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        } else {
                            PendingIntent.FLAG_UPDATE_CURRENT
                        }
                    )
                    usbManager.requestPermission(usbDevice, permissionIntent)
                }
            } else {
                Log.e(TAG, "USB device not found")
            }
        } else {
            Log.e(TAG, "No USB device found")
        }
    }

    private fun openSerialPort(usbDevice: UsbDevice) {
        usbSerialDevice =
            UsbSerialDevice.createUsbSerialDevice(usbDevice, usbManager.openDevice(usbDevice))
        usbSerialDevice?.let { usbSerial ->
            if (usbSerial.open()) {
                Log.e(TAG, "Setting USB serial port values")
                usbSerial.setBaudRate(115200)
                usbSerial.setDataBits(UsbSerialInterface.DATA_BITS_8)
                usbSerial.setStopBits(UsbSerialInterface.STOP_BITS_1)
                usbSerial.setParity(UsbSerialInterface.PARITY_NONE)
                usbSerial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                scanNow()
            } else {
                Log.e(TAG, "Error opening USB serial port.")
            }
        }
    }

    fun scanNow() {
        Log.d(TAG, "Start scanning")
        val rpLidar = RPLidarA2Api(usbManager as UsbManager?)

        fun handleResponse(scanDataList: List<ScanData>) {
            scanDataList.forEach {
                //Log.d(TAG, "SCAN: ${it.distance}, ${it.angle}")
                lidarView.setData(it.distance.toString() + "," + it.angle.toString())
            }
        }
        rpLidar.startScan({ scanDataList: List<ScanData> ->
            handleResponse(scanDataList)
        } as ((Any) -> Unit)?, 200)
    }

    override fun onDestroy() {
        super.onDestroy()
        usbSerialDevice?.close()
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "za.co.grab.yaldp.USB_PERMISSION"
    }
}


