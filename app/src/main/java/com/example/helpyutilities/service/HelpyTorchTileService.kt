package com.example.helpyutilities.service

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class HelpyTorchTileService : TileService() {

    private var isTorchOn = false
    private var torchCameraId: String? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchCameraId) {
                isTorchOn = enabled
                updateTileState()
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            torchCameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            cameraManager.registerTorchCallback(torchCallback, null)
        } catch (_: Exception) {
            torchCameraId = null
        }
        updateTileState()
    }

    override fun onStopListening() {
        super.onStopListening()
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraManager.unregisterTorchCallback(torchCallback)
        } catch (_: Exception) {
        }
    }

    override fun onClick() {
        super.onClick()
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val id = torchCameraId ?: return

        try {
            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(id, isTorchOn)
            updateTileState()
        } catch (_: Exception) {
            qsTile?.state = Tile.STATE_UNAVAILABLE
            qsTile?.updateTile()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        if (torchCameraId == null) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = "Torch Unavailable"
        } else {
            tile.state = if (isTorchOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = if (isTorchOn) "Helpy Torch On" else "Helpy Torch"
        }
        tile.updateTile()
    }
}
