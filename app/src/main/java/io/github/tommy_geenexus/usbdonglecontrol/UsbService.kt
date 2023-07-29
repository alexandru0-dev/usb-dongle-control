/*
 * Copyright (c) 2022-2023, Tom Geiselmann (tomgapplicationsdevelopment@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY,WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.tommy_geenexus.usbdonglecontrol

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.core.content.IntentCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.tommy_geenexus.usbdonglecontrol.di.DispatcherIo
import io.github.tommy_geenexus.usbdonglecontrol.di.DispatcherMainImmediate
import io.github.tommy_geenexus.usbdonglecontrol.dongle.UsbDongle
import io.github.tommy_geenexus.usbdonglecontrol.dongle.UsbRepository
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.FiioKa5
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.FiioKa5Defaults
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.FiioKa5UsbCommunicationRepository
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.VolumeMode
import io.github.tommy_geenexus.usbdonglecontrol.dongle.productName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class UsbService : Service() {

    private companion object {

        const val REQUEST_CODE = 0
        const val ID_NOTIFICATION = 1
        const val ID_NOTIFICATION_CHANNEL = TOP_LEVEL_PACKAGE_NAME + "NOTIFICATION_CHANNEL"
        const val INTENT_ACTION_VOLUME_UP = TOP_LEVEL_PACKAGE_NAME + "VOLUME_UP"
        const val INTENT_ACTION_VOLUME_DOWN = TOP_LEVEL_PACKAGE_NAME + "VOLUME_DOWN"
        const val INTENT_ACTION_DISPLAY_INVERT = TOP_LEVEL_PACKAGE_NAME + "DISPLAY_INVERT"
        const val INTENT_EXTRA_USB_DONGLE = TOP_LEVEL_PACKAGE_NAME + "USB_DONGLE"
    }

    @Inject
    lateinit var usbRepository: UsbRepository

    @Inject
    lateinit var fiioKa5UsbCommunicationRepository: FiioKa5UsbCommunicationRepository

    @DispatcherIo
    @Inject
    lateinit var dispatcherIo: CoroutineDispatcher

    @DispatcherMainImmediate
    @Inject
    lateinit var dispatcherMainImmediate: CoroutineDispatcher

    private lateinit var coroutineScope: CoroutineScope

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        coroutineScope = CoroutineScope(dispatcherIo)
        coroutineScope.launch {
            val device = usbRepository.getAttachedDeviceOrNull()
            if (device != null) {
                val connection = usbRepository.openDeviceOrNull(device)
                if (connection != null) {
                    val usbDongle = fiioKa5UsbCommunicationRepository.getCurrentState(connection)
                    if (usbDongle is FiioKa5) {
                        withContext(dispatcherMainImmediate) {
                            coroutineContext.suspendRunCatching {
                                val nm = getSystemService(Context.NOTIFICATION_SERVICE)
                                    as NotificationManager
                                nm.createNotificationChannel(
                                    NotificationChannel(
                                        ID_NOTIFICATION_CHANNEL,
                                        getString(R.string.app_name),
                                        NotificationManager.IMPORTANCE_HIGH
                                    ).apply {
                                        setShowBadge(false)
                                    }
                                )
                                startForeground(
                                    ID_NOTIFICATION,
                                    buildNotification(
                                        usbDongle = usbDongle,
                                        volumePercent = usbDongle.currentVolumeLevelInPercent()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            INTENT_ACTION_VOLUME_UP -> {
                coroutineScope.launch {
                    val device = usbRepository.getAttachedDeviceOrNull()
                    if (device != null) {
                        val connection = usbRepository.openDeviceOrNull(device)
                        val usbDongle = IntentCompat.getParcelableExtra(
                            intent,
                            INTENT_EXTRA_USB_DONGLE,
                            UsbDongle::class.java
                        )
                        if (connection != null && usbDongle is FiioKa5) {
                            val volumeLevel = (usbDongle.volumeLevel + 1).coerceIn(
                                minimumValue = FiioKa5Defaults.VOLUME_LEVEL_MIN,
                                maximumValue = if (usbDongle.volumeMode == VolumeMode.S120) {
                                    FiioKa5Defaults.VOLUME_LEVEL_A_MAX
                                } else {
                                    FiioKa5Defaults.VOLUME_LEVEL_B_MAX
                                }
                            )
                            val success = fiioKa5UsbCommunicationRepository.setVolumeLevel(
                                connection = connection,
                                volumeMode = usbDongle.volumeMode,
                                volumeLevel = volumeLevel
                            )
                            if (success) {
                                val dongle = usbDongle.copy(volumeLevel = volumeLevel)
                                updateNotification(
                                    buildNotification(
                                        usbDongle = dongle,
                                        volumePercent = dongle.currentVolumeLevelInPercent()
                                    )
                                )
                            }
                        }
                    }
                }
            }
            INTENT_ACTION_VOLUME_DOWN -> {
                coroutineScope.launch {
                    val device = usbRepository.getAttachedDeviceOrNull()
                    if (device != null) {
                        val connection = usbRepository.openDeviceOrNull(device)
                        val usbDongle = IntentCompat.getParcelableExtra(
                            intent,
                            INTENT_EXTRA_USB_DONGLE,
                            UsbDongle::class.java
                        )
                        if (connection != null && usbDongle is FiioKa5) {
                            val volumeLevel = (usbDongle.volumeLevel - 1).coerceIn(
                                minimumValue = FiioKa5Defaults.VOLUME_LEVEL_MIN,
                                maximumValue = if (usbDongle.volumeMode == VolumeMode.S120) {
                                    FiioKa5Defaults.VOLUME_LEVEL_A_MAX
                                } else {
                                    FiioKa5Defaults.VOLUME_LEVEL_B_MAX
                                }
                            )
                            val success = fiioKa5UsbCommunicationRepository.setVolumeLevel(
                                connection = connection,
                                volumeMode = usbDongle.volumeMode,
                                volumeLevel = volumeLevel
                            )
                            if (success) {
                                val dongle = usbDongle.copy(volumeLevel = volumeLevel)
                                updateNotification(
                                    buildNotification(
                                        usbDongle = dongle,
                                        volumePercent = dongle.currentVolumeLevelInPercent()
                                    )
                                )
                            }
                        }
                    }
                }
            }
            INTENT_ACTION_DISPLAY_INVERT -> {
                coroutineScope.launch {
                    val device = usbRepository.getAttachedDeviceOrNull()
                    if (device != null) {
                        val connection = usbRepository.openDeviceOrNull(device)
                        val usbDongle = IntentCompat.getParcelableExtra(
                            intent,
                            INTENT_EXTRA_USB_DONGLE,
                            UsbDongle::class.java
                        )
                        if (connection != null && usbDongle is FiioKa5) {
                            val displayInvertEnabled = !usbDongle.displayInvertEnabled
                            val success = fiioKa5UsbCommunicationRepository.setDisplayInvertEnabled(
                                connection = connection,
                                displayInvertEnabled = displayInvertEnabled
                            )
                            if (success) {
                                updateNotification(
                                    buildNotification(
                                        usbDongle = usbDongle.copy(
                                            displayInvertEnabled = displayInvertEnabled
                                        ),
                                        volumePercent = usbDongle.currentVolumeLevelInPercent()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private fun buildNotification(
        usbDongle: UsbDongle,
        volumePercent: String
    ): Notification {
        return Notification
            .Builder(applicationContext, ID_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(usbDongle.productName())
            .setContentText(getString(R.string.volume_level, volumePercent))
            .setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    REQUEST_CODE,
                    packageManager.getLaunchIntentForPackage(packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                Notification.Action.Builder(
                    null,
                    getString(R.string.volume_up),
                    PendingIntent.getService(
                        applicationContext,
                        REQUEST_CODE,
                        Intent(applicationContext, UsbService::class.java).apply {
                            action = INTENT_ACTION_VOLUME_UP
                            putExtra(INTENT_EXTRA_USB_DONGLE, usbDongle)
                        },
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    null,
                    getString(R.string.volume_down),
                    PendingIntent.getService(
                        applicationContext,
                        REQUEST_CODE,
                        Intent(applicationContext, UsbService::class.java).apply {
                            action = INTENT_ACTION_VOLUME_DOWN
                            putExtra(INTENT_EXTRA_USB_DONGLE, usbDongle)
                        },
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    null,
                    getString(R.string.display_invert),
                    PendingIntent.getService(
                        applicationContext,
                        REQUEST_CODE,
                        Intent(applicationContext, UsbService::class.java).apply {
                            action = INTENT_ACTION_DISPLAY_INVERT
                            putExtra(INTENT_EXTRA_USB_DONGLE, usbDongle)
                        },
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                ).build()
            )
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build()
    }

    private suspend fun updateNotification(notification: Notification) {
        withContext(Dispatchers.Main.immediate) {
            coroutineContext.suspendRunCatching {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(ID_NOTIFICATION, notification)
            }
        }
    }
}
