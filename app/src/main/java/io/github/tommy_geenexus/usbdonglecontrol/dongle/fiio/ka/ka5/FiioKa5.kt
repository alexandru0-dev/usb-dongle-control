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

package io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5

import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.FiioUsbDongle
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.DacMode
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.FiioKa5UsbCommand
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.Filter
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.Gain
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.HidMode
import io.github.tommy_geenexus.usbdonglecontrol.dongle.fiio.ka.ka5.data.VolumeMode
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class FiioKa5(
    val channelBalance: Int = FiioKa5Defaults.CHANNEL_BALANCE,
    val dacMode: DacMode = DacMode.ClassAB,
    val displayBrightness: Int = FiioKa5Defaults.DISPLAY_BRIGHTNESS,
    val displayInvertEnabled: Boolean = false,
    val displayTimeout: Int = FiioKa5Defaults.DISPLAY_TIMEOUT,
    val filter: Filter = Filter.default(),
    val firmwareVersion: String = FiioKa5Defaults.FW_VERSION,
    val gain: Gain = Gain.default(),
    val hardwareMuteEnabled: Boolean = false,
    val hidMode: HidMode = HidMode.default(),
    val sampleRate: String = FiioKa5Defaults.SAMPLE_RATE,
    val spdifOutEnabled: Boolean = false,
    val volumeLevel: Int = FiioKa5Defaults.VOLUME_LEVEL,
    val volumeMode: VolumeMode = VolumeMode.default()
) : FiioUsbDongle(modelName = "KA5"),
    FiioKa5UsbCommand {

    @IgnoredOnParcel
    override val setFilter = byteArrayOf(-57, -91, 1)

    @IgnoredOnParcel
    override val setGain = byteArrayOf(-57, -91, 2)

    @IgnoredOnParcel
    override val setVolumeLevel = byteArrayOf(-57, -91, 4)

    @IgnoredOnParcel
    override val setChannelBalance = byteArrayOf(-57, -91, 5)

    @IgnoredOnParcel
    override val setDacMode = byteArrayOf(-57, -91, 6)

    @IgnoredOnParcel
    override val setHardwareMute = byteArrayOf(-57, -91, 7)

    @IgnoredOnParcel
    override val setSpdifOut = byteArrayOf(-57, -91, 8)

    @IgnoredOnParcel
    override val setDisplayTimeout = byteArrayOf(-57, -91, 9)

    @IgnoredOnParcel
    override val setHidMode = byteArrayOf(-57, -91, 10)

    @IgnoredOnParcel
    override val setDisplayBrightness = byteArrayOf(-57, -91, 11)

    @IgnoredOnParcel
    override val setDisplayInvert = byteArrayOf(-57, -91, 12)

    @IgnoredOnParcel
    override val setVolumeMode = byteArrayOf(-57, -91, 13)

    @IgnoredOnParcel
    override val getVersion = byteArrayOf(-57, -91, -96)

    @IgnoredOnParcel
    override val getSampleRate = byteArrayOf(-57, -91, -95)

    @IgnoredOnParcel
    override val getVolumeLevel = byteArrayOf(-57, -91, -94)

    @IgnoredOnParcel
    override val getFilter = byteArrayOf(-57, -91, -93)

    @IgnoredOnParcel
    override val getOtherState = byteArrayOf(-57, -91, -92)

    fun currentVolumeLevelInPercent() = "${(volumeLevel * 100 / volumeMode.steps)}%"
}
