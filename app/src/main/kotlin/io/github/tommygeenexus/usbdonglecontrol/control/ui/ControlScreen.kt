/*
 * Copyright (c) 2022-2024, Tom Geiselmann (tomgapplicationsdevelopment@gmail.com)
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

package io.github.tommygeenexus.usbdonglecontrol.control.ui

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.core.content.ContextCompat
import io.github.tommygeenexus.usbdonglecontrol.R
import io.github.tommygeenexus.usbdonglecontrol.UsbReceiver
import io.github.tommygeenexus.usbdonglecontrol.consumeProfileShortcut
import io.github.tommygeenexus.usbdonglecontrol.control.business.ControlSideEffect
import io.github.tommygeenexus.usbdonglecontrol.control.business.ControlViewModel
import io.github.tommygeenexus.usbdonglecontrol.control.data.Profile
import io.github.tommygeenexus.usbdonglecontrol.control.data.ProfilesList
import io.github.tommygeenexus.usbdonglecontrol.dongle.UnsupportedUsbDongle
import io.github.tommygeenexus.usbdonglecontrol.dongle.UsbDongle
import io.github.tommygeenexus.usbdonglecontrol.dongle.fiio.ka.ka5.FiioKa5
import io.github.tommygeenexus.usbdonglecontrol.dongle.fiio.ka.ka5.ui.FiioKa5Items
import io.github.tommygeenexus.usbdonglecontrol.dongle.moondrop.dawn.dawn3544Pro.MoondropDawn
import io.github.tommygeenexus.usbdonglecontrol.dongle.moondrop.dawn.dawn3544Pro.ui.MoondropDawnItems
import io.github.tommygeenexus.usbdonglecontrol.theme.getHorizontalPadding
import io.github.tommygeenexus.usbdonglecontrol.volume.UsbService
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ControlScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: ControlViewModel,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val profileApplyFail = stringResource(id = R.string.profile_apply_fail)
    val profileApplySuccess = stringResource(id = R.string.profile_apply_success)
    val profileDeleteFail = stringResource(id = R.string.profile_delete_fail)
    val profileDeleteSuccess = stringResource(id = R.string.profile_delete_success)
    val profileExportFail = stringResource(id = R.string.profile_export_fail)
    val profileExportSuccess = stringResource(id = R.string.profile_export_success)
    val shortcutAddFail = stringResource(id = R.string.shortcut_add_fail)
    val shortcutAddSuccess = stringResource(id = R.string.shortcut_add_success)
    val shortcutDeleteFail = stringResource(id = R.string.shortcut_delete_fail)
    val shortcutDeleteSuccess = stringResource(id = R.string.shortcut_delete_success)
    val usbCommunicationFailure = stringResource(id = R.string.usb_comm_failure)
    val usbCommunicationSuccess = stringResource(id = R.string.usb_comm_success)
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            ControlSideEffect.Profile.Apply.Failure -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = profileApplyFail,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            ControlSideEffect.Profile.Apply.Success -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = profileApplySuccess,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            ControlSideEffect.Profile.Delete.Failure -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = profileDeleteFail,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            ControlSideEffect.Profile.Delete.Success -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = profileDeleteSuccess,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            ControlSideEffect.Profile.Export.Failure -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = profileExportFail,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            ControlSideEffect.Profile.Export.Success -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = profileExportSuccess,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            is ControlSideEffect.Profile.Get.All -> {
                viewModel.getProfiles(usbDongle = sideEffect.usbDongle)
            }
            ControlSideEffect.Profile.Get.Failure -> {
            }
            ControlSideEffect.Profile.Get.Success -> {
            }
            ControlSideEffect.Service.Start -> {
                context.startService(Intent(context, UsbService::class.java))
            }
            ControlSideEffect.Service.Stop -> {
                context.stopService(Intent(context, UsbService::class.java))
            }
            ControlSideEffect.Shortcut.Add.Failure -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = shortcutAddFail,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            ControlSideEffect.Shortcut.Add.Success -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = shortcutAddSuccess,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            ControlSideEffect.Shortcut.Delete.Failure -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = shortcutDeleteFail,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            ControlSideEffect.Shortcut.Delete.Success -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = shortcutDeleteSuccess,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            ControlSideEffect.UsbCommunication.Get.Failure -> {
                onNavigateUp()
            }
            is ControlSideEffect.UsbCommunication.Get.Success -> {
                viewModel.getCurrentStateForUsbDongle(usbDongle = sideEffect.usbDongle)
            }
            ControlSideEffect.UsbCommunication.Rw.Failure -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = usbCommunicationFailure,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            ControlSideEffect.UsbCommunication.Rw.Success -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        message = usbCommunicationSuccess,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    DisposableEffect(LocalLifecycleOwner.current) {
        val usbReceiver = UsbReceiver(
            onAttachedDevicesChanged = { isAttached ->
                if (!isAttached) {
                    scrollBehavior.state.heightOffset = 0f
                    scrollBehavior.state.contentOffset = 0f
                    onNavigateUp()
                }
            }
        )
        ContextCompat.registerReceiver(
            context,
            usbReceiver,
            IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(usbReceiver)
        }
    }
    val activity = LocalContext.current as Activity
    val bottomAppBarColor = MaterialTheme
        .colorScheme
        .surfaceColorAtElevation(BottomAppBarDefaults.ContainerElevation)
        .toArgb()
    SideEffect {
        activity.window?.navigationBarColor = bottomAppBarColor
    }
    val state by viewModel.collectAsState()
    ControlScreen(
        windowSizeClass = windowSizeClass,
        scrollBehavior = scrollBehavior,
        snackBarHostState = snackBarHostState,
        usbDongle = state.usbDongle,
        profiles = state.profiles,
        isLoading = state.loadingTasks > 0.toUInt(),
        onRefresh = { viewModel.getCurrentStateForUsbDongle(state.usbDongle) },
        onReset = { usbDongle ->
            viewModel.setProfile(usbDongle, usbDongle.defaultStateAsProfile())
        },
        onProfileShortcutAdd = { profile ->
            viewModel.addProfileShortcut(profile)
        },
        onProfileShortcutRemove = { profile ->
            viewModel.removeProfileShortcut(profile)
        },
        onProfileDelete = { usbDongle, profile ->
            viewModel.deleteProfile(usbDongle, profile)
        },
        onProfileApply = { usbDongle, profile ->
            viewModel.setProfile(usbDongle, profile)
        },
        onProfileExport = { usbDongle, profileName ->
            viewModel.exportProfile(usbDongle, usbDongle.currentStateAsProfile(profileName))
        },
        onDisplayBrightnessSelected = { usbDongle, displayBrightness ->
            viewModel.setDisplayBrightness(usbDongle, displayBrightness)
        },
        onChannelBalanceSelected = { usbDongle, channelBalance ->
            viewModel.setChannelBalance(usbDongle, channelBalance)
        },
        onDacModeSelected = { usbDongle, id ->
            viewModel.setDacMode(usbDongle, id)
        },
        onDisplayTimeoutSelected = { usbDongle, displayTimeout ->
            viewModel.setDisplayTimeout(usbDongle, displayTimeout)
        },
        onDisplayInvertChange = { usbDongle, isDisplayInvertEnabled ->
            viewModel.setDisplayInvertEnabled(usbDongle, isDisplayInvertEnabled)
        },
        onFilterSelected = { usbDongle, id ->
            viewModel.setDacFilter(usbDongle, id)
        },
        onGainSelected = { usbDongle, id ->
            viewModel.setGain(usbDongle, id)
        },
        onHardwareMuteEnabledSelected = { usbDongle, isHardwareMuteEnabled ->
            viewModel.setHardwareMuteEnabled(usbDongle, isHardwareMuteEnabled)
        },
        onHidModeSelected = { usbDongle, id ->
            viewModel.setHidMode(usbDongle, id)
        },
        onIndicatorStateSelected = { usbDongle, id ->
            viewModel.setIndicatorState(usbDongle, id)
        },
        onSpdifOutEnabledSelected = { usbDongle, isSpdifOutEnabled ->
            viewModel.setSpdifOutEnabled(usbDongle, isSpdifOutEnabled)
        },
        onVolumeLevelSelected = { usbDongle, volumeLevel ->
            viewModel.setVolumeLevel(usbDongle, volumeLevel)
        },
        onVolumeModeSelected = { usbDongle, id ->
            viewModel.setVolumeMode(usbDongle, id)
        }
    )
}

@Composable
fun ControlScreen(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    profileListState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    usbDongle: UsbDongle = UnsupportedUsbDongle,
    profiles: ProfilesList = ProfilesList(),
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    onReset: (UsbDongle) -> Unit = {},
    onProfileShortcutAdd: (Profile) -> Unit = {},
    onProfileShortcutRemove: (Profile) -> Unit = {},
    onProfileDelete: (UsbDongle, Profile) -> Unit = { _, _ -> },
    onProfileApply: (UsbDongle, Profile) -> Unit = { _, _ -> },
    onProfileExport: (UsbDongle, String) -> Unit = { _, _ -> },
    onChannelBalanceSelected: (UsbDongle, Int) -> Unit = { _, _ -> },
    onDacModeSelected: (UsbDongle, Byte) -> Unit = { _, _ -> },
    onDisplayBrightnessSelected: (UsbDongle, Int) -> Unit = { _, _ -> },
    onDisplayTimeoutSelected: (UsbDongle, Int) -> Unit = { _, _ -> },
    onDisplayInvertChange: (UsbDongle, Boolean) -> Unit = { _, _ -> },
    onFilterSelected: (UsbDongle, Byte) -> Unit = { _, _ -> },
    onGainSelected: (UsbDongle, Byte) -> Unit = { _, _ -> },
    onHardwareMuteEnabledSelected: (UsbDongle, Boolean) -> Unit = { _, _ -> },
    onHidModeSelected: (UsbDongle, Byte) -> Unit = { _, _ -> },
    onIndicatorStateSelected: (UsbDongle, Byte) -> Unit = { _, _ -> },
    onSpdifOutEnabledSelected: (UsbDongle, Boolean) -> Unit = { _, _ -> },
    onVolumeLevelSelected: (UsbDongle, Int) -> Unit = { _, _ -> },
    onVolumeModeSelected: (UsbDongle, Byte) -> Unit = { _, _ -> }
) {
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ControlTopAppBar(
                windowSizeClass = windowSizeClass,
                scrollBehavior = scrollBehavior,
                shouldShowActions = {
                    windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
                },
                usbDongle = usbDongle,
                onRefresh = onRefresh,
                onReset = onReset,
                onProfileExport = onProfileExport
            )
        },
        bottomBar = {
            if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Expanded) {
                ControlBottomAppBar(
                    windowSizeClass = windowSizeClass,
                    usbDongle = usbDongle,
                    onRefresh = onRefresh,
                    onReset = onReset,
                    onProfileExport = onProfileExport
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            AnimatedVisibility(visible = isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            val surfaceColor = MaterialTheme.colorScheme.surface
            val bottomAppBarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                BottomAppBarDefaults.ContainerElevation
            )
            val overlappedFraction = if (scrollBehavior.state.overlappedFraction > 0.01f) 1f else 0f
            val animatedColor by animateColorAsState(
                targetValue = lerp(
                    surfaceColor,
                    bottomAppBarColor,
                    FastOutLinearInEasing.transform(overlappedFraction)
                ),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "StatusBarColorAnimation"
            )
            val activity = LocalContext.current as? Activity
            if (activity != null) {
                LaunchedEffect(animatedColor) {
                    activity.window?.statusBarColor = animatedColor.toArgb()
                }
            }
            var selectedTabIndex by rememberSaveable { mutableIntStateOf(ControlTabs.State.index) }
            ControlTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = animatedColor,
                onTabSelected = { index ->
                    val prev = selectedTabIndex
                    selectedTabIndex = index
                    if (prev == ControlTabs.Profiles.index &&
                        selectedTabIndex == ControlTabs.State.index
                    ) {
                        onRefresh()
                    }
                }
            )
            if (selectedTabIndex == ControlTabs.State.index) {
                when (usbDongle) {
                    is FiioKa5 -> {
                        FiioKa5Items(
                            modifier = Modifier.padding(
                                horizontal = windowSizeClass.getHorizontalPadding()
                            ),
                            fiioKa5 = usbDongle,
                            onChannelBalanceSelected = { channelBalance ->
                                onChannelBalanceSelected(usbDongle, channelBalance)
                            },
                            onVolumeLevelSelected = { volumeLevel ->
                                onVolumeLevelSelected(usbDongle, volumeLevel)
                            },
                            onVolumeModeSelected = { volumeMode ->
                                onVolumeModeSelected(usbDongle, volumeMode)
                            },
                            onDisplayBrightnessSelected = { displayBrightness ->
                                onDisplayBrightnessSelected(usbDongle, displayBrightness)
                            },
                            onDisplayTimeoutSelected = { displayTimeout ->
                                onDisplayTimeoutSelected(usbDongle, displayTimeout)
                            },
                            onDisplayInvertSelected = { isDisplayInvertEnabled ->
                                onDisplayInvertChange(usbDongle, isDisplayInvertEnabled)
                            },
                            onGainSelected = { gain ->
                                onGainSelected(usbDongle, gain)
                            },
                            onFilterSelected = { filter ->
                                onFilterSelected(usbDongle, filter)
                            },
                            onSpdifOutSelected = { isSpdifOutEnabled ->
                                onSpdifOutEnabledSelected(usbDongle, isSpdifOutEnabled)
                            },
                            onHardwareMuteSelected = { isHardwareMuteEnabled ->
                                onHardwareMuteEnabledSelected(usbDongle, isHardwareMuteEnabled)
                            },
                            onDacModeSelected = { dacMode ->
                                onDacModeSelected(usbDongle, dacMode)
                            },
                            onHidModeSelected = { hidMode ->
                                onHidModeSelected(usbDongle, hidMode)
                            }
                        )
                    }
                    is MoondropDawn -> {
                        MoondropDawnItems(
                            modifier = Modifier.padding(
                                horizontal = windowSizeClass.getHorizontalPadding()
                            ),
                            moondropDawn = usbDongle,
                            onFilterSelected = { filter ->
                                onFilterSelected(usbDongle, filter)
                            },
                            onGainSelected = { gain ->
                                onGainSelected(usbDongle, gain)
                            },
                            onIndicatorStateSelected = { indicatorState ->
                                onIndicatorStateSelected(usbDongle, indicatorState)
                            },
                            onVolumeLevelSelected = { volumeLevel ->
                                onVolumeLevelSelected(usbDongle, volumeLevel)
                            }
                        )
                    }
                }
            } else {
                ProfileItems(
                    modifier = Modifier.padding(
                        horizontal = windowSizeClass.getHorizontalPadding()
                    ),
                    state = profileListState,
                    profiles = profiles,
                    onProfileShortcutAdd = onProfileShortcutAdd,
                    onProfileShortcutRemove = onProfileShortcutRemove,
                    onProfileDelete = { profile ->
                        onProfileDelete(usbDongle, profile)
                    },
                    onProfileApply = { profile ->
                        onProfileApply(usbDongle, profile)
                    }
                )
            }
            val context = (LocalContext.current as? Activity)
            if (context != null) {
                LaunchedEffect(Unit) {
                    val profileShortcut = context.intent.consumeProfileShortcut()
                    if (profileShortcut != null) {
                        selectedTabIndex = ControlTabs.Profiles.index
                        val index = profiles.items.indexOf(profileShortcut)
                        profileListState.animateScrollToItem(if (index >= 0) index else 0)
                    }
                }
            }
        }
    }
}
