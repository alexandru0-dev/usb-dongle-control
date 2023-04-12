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

package io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.business

import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.FiioKa5
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.DacMode
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.Filter
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.Gain
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.HidMode
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.VolumeMode
import io.github.tommy_geenexus.usbdonglecontrol.main.business.MainViewModel
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce

fun MainViewModel.setFilter(
    fiioKa5: FiioKa5,
    filter: Filter
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setFilter(connection, filter)
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(filter = if (success) filter else fiioKa5.filter),
            isLoading = false
        )
    }
}

fun MainViewModel.setGain(
    fiioKa5: FiioKa5,
    gain: Gain
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setGain(connection, gain)
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(gain = if (success) gain else fiioKa5.gain),
            isLoading = false
        )
    }
}

fun MainViewModel.setVolumeLevel(
    fiioKa5: FiioKa5,
    volumeLevel: Int
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setVolumeLevel(
                connection = connection,
                volumeMode = fiioKa5.volumeMode,
                volumeLevel = volumeLevel
            )
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(
                volumeLevel = if (success) volumeLevel else fiioKa5.volumeLevel
            ),
            isLoading = false
        )
    }
}

fun MainViewModel.setChannelBalance(
    fiioKa5: FiioKa5,
    channelBalance: Int
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setChannelBalance(
                connection = connection,
                channelBalance = channelBalance
            )
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(
                channelBalance = if (success) channelBalance else fiioKa5.channelBalance
            ),
            isLoading = false
        )
    }
}

fun MainViewModel.setDacMode(
    fiioKa5: FiioKa5,
    dacMode: DacMode
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setDacMode(
                connection = connection,
                dacMode = dacMode
            )
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(
                dacMode = if (success) dacMode else fiioKa5.dacMode
            ),
            isLoading = false
        )
    }
}

fun MainViewModel.setHardwareMute(
    fiioKa5: FiioKa5,
    hardwareMuteEnabled: Boolean
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setHardwareMuteEnabled(
                connection = connection,
                hardwareMuteEnabled = hardwareMuteEnabled
            )
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(
                hardwareMuteEnabled = if (success) {
                    hardwareMuteEnabled
                } else {
                    fiioKa5.hardwareMuteEnabled
                }
            ),
            isLoading = false
        )
    }
}

fun MainViewModel.setSpdifOut(
    fiioKa5: FiioKa5,
    spdifOutEnabled: Boolean
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setSpdifOutEnabled(
                connection = connection,
                spdifOutEnabled = spdifOutEnabled
            )
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(
                spdifOutEnabled = if (success) spdifOutEnabled else fiioKa5.spdifOutEnabled
            ),
            isLoading = false
        )
    }
}

fun MainViewModel.setDisplayTimeout(
    fiioKa5: FiioKa5,
    displayTimeout: Int
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setDisplayTimeout(
                connection = connection,
                displayTimeout = displayTimeout
            )
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(
                displayTimeout = if (success) displayTimeout else fiioKa5.displayTimeout
            ),
            isLoading = false
        )
    }
}

fun MainViewModel.setHidMode(
    fiioKa5: FiioKa5,
    hidMode: HidMode
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setHidMode(
                connection = connection,
                hidMode = hidMode
            )
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(
                hidMode = if (success) hidMode else fiioKa5.hidMode
            ),
            isLoading = false
        )
    }
}

fun MainViewModel.setDisplayBrightness(
    fiioKa5: FiioKa5,
    displayBrightness: Int
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setDisplayBrightness(
                connection = connection,
                displayBrightness = displayBrightness
            )
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(
                displayBrightness = if (success) {
                    displayBrightness
                } else {
                    fiioKa5.displayBrightness
                }
            ),
            isLoading = false
        )
    }
}

fun MainViewModel.setDisplayInvert(
    fiioKa5: FiioKa5,
    displayInvertEnabled: Boolean
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setDisplayInvertEnabled(
                connection = connection,
                displayInvertEnabled = displayInvertEnabled
            )
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(
                displayInvertEnabled = if (success) {
                    displayInvertEnabled
                } else {
                    fiioKa5.displayInvertEnabled
                }
            ),
            isLoading = false
        )
    }
}

fun MainViewModel.setVolumeMode(
    fiioKa5: FiioKa5,
    volumeMode: VolumeMode
) = intent {
    reduce {
        state.copy(isLoading = true)
    }
    val device = usbRepository.getAttachedDeviceOrNull()
    val success = if (device != null) {
        val connection = usbRepository.openDeviceOrNull(device)
        if (connection != null) {
            fiioKa5UsbCommunicationRepository.setVolumeMode(
                connection = connection,
                volumeMode = volumeMode
            )
        } else {
            false
        }
    } else {
        false
    }
    reduce {
        state.copy(
            usbDongle = fiioKa5.copy(
                volumeMode = if (success) volumeMode else fiioKa5.volumeMode
            ),
            isLoading = false
        )
    }
}