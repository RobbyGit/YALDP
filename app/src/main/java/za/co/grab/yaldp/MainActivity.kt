package za.co.grab.yaldp

import android.app.PendingIntent
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.jayway.rplidarapi.ScanData
import java.nio.charset.Charset


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
        //val parent = lidarView.parent as? ViewGroup
        //parent?.removeView(lidarView)


        // Add the RadarView to the layout
        //val layout = findViewById<LinearLayout>(R.id.layout)
        //layout.addView(lidarView)

        usbManager = getSystemService(USB_SERVICE) as UsbManager
        //val vendorId = 4292
        //val productId = 60000

        val vendorId = 12346
        val productId = 4097


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
                    Log.e(TAG, "ok permissions")
                } else {
                    Log.e(TAG, "No permissions")
                    val permissionIntent = PendingIntent.getBroadcast(
                        this,
                        0,
                        Intent(ACTION_USB_PERMISSION),
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
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
        usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(usbDevice, usbManager.openDevice(usbDevice))
        usbSerialDevice?.let { usbSerial ->
            if (usbSerial.open()) {
                Log.e(TAG, "Setting USB serial port values")
                usbSerial.setBaudRate(115200)
                usbSerial.setDataBits(UsbSerialInterface.DATA_BITS_8)
                usbSerial.setStopBits(UsbSerialInterface.STOP_BITS_1)
                usbSerial.setParity(UsbSerialInterface.PARITY_NONE)
                usbSerial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                usbSerial.setDTR(false)
                Log.e(TAG, "USB set and ready")
                usbSerial.read(object : UsbSerialInterface.UsbReadCallback {
                    private var buffer: String = ""
                    val distanceArray = Array<Int?>(360) { 0 }

                    override fun onReceivedData(data: ByteArray?) {
                        data?.let {
                            val receivedData = String(it, Charset.defaultCharset())
                            buffer += receivedData // Append received data to the buffer
                            val lines = buffer.split("|")

                            // Iterate over each line in the buffer
                            for (i in 0 until lines.size - 1) {
                                val line = lines[i].trim() // Trim the line to remove leading/trailing whitespace
                                //Log.d(TAG, "Received data line: $line")
                                //Distance|Angle
                                try {
                                    val (distance, angle) = line.split(",")
                                    val distanceInt = distance.toInt()
                                    val angleInt = angle.toInt()
                                    //Log.w(TAG, "Distance: $distanceInt, Angle: $angleInt")
                                    distanceArray[angleInt % 360] = distanceInt
                                } catch (e: NumberFormatException) {
                                    // This is because of the Arduino startup
                                }
                            }
                            buffer = lines.last()
                        }
                        val distanceArrayString = distanceArray.joinToString(", ") { it?.toString() ?: "null" }
                        //Log.w(TAG,"Distance Array: [$distanceArrayString]")
                        lidarView.setData(distanceArrayString)
                    }
                })
            } else {
                Log.e(TAG, "Error opening USB serial port.")
            }
        }
    }

    override fun onDestroy() {
        Log.w(TAG, "DESTROYED")
        usbSerialDevice?.close()
        super.onDestroy()
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "za.co.grab.yaldp.USB_PERMISSION"
    }
}


