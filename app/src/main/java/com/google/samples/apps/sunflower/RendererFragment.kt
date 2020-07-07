/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.sunflower

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import androidx.window.DeviceState
import androidx.window.DisplayFeature
import androidx.window.WindowManager
import java.util.concurrent.Executor

abstract class RendererFragment : Fragment() {

    internal data class ViewParams(
        val isPortrait: Boolean,
        val firstPanel: FrameLayout.LayoutParams,
        val hingePanel: FrameLayout.LayoutParams,
        val secondPanel: FrameLayout.LayoutParams
    )

    private val handler = Handler(Looper.getMainLooper())
    private val mainThreadExecutor = Executor { r: Runnable -> handler.post(r) }
    private val windowManager by lazy { WindowManager(requireContext(), null) }
    private val windowLayoutInfoConsumer = Consumer<DeviceState> {
        val displayFeature = windowManager.windowLayoutInfo.displayFeatures
        println("asas DeviceState Consumer: displayFeature=$displayFeature | deviceState=$it")

        // This way was chosen as changing the DeviceState isn't triggering a configuration changes,
        //   where I would expect it to trigger, as the device configuration changed!
        with(parentFragmentManager.findFragmentById(this.id) ?: this) {
            parentFragmentManager
                .beginTransaction()
                .detach(this)
                .attach(this)
                .commit()
        }
    }

    internal val deviceState: DeviceState
        get() = windowManager.deviceState

    internal val displayFeature: DisplayFeature?
        get() = windowManager.windowLayoutInfo.displayFeatures
            .firstOrNull {
                it.type == DisplayFeature.TYPE_HINGE || it.type == DisplayFeature.TYPE_FOLD
            }

    internal fun getViewParams(): ViewParams? = displayFeature?.bounds?.let { bounds ->
        val view = requireView()
        val isPortrait = bounds.isPortrait()
        if (isPortrait) {
            val statusBarHeight = requireActivity().getStatusBarHeight()
            bounds.apply {
                top -= statusBarHeight
                bottom -= statusBarHeight
            }
            ViewParams(
                isPortrait = isPortrait,
                firstPanel = FrameLayout.LayoutParams(view.width, bounds.top),
                hingePanel = FrameLayout.LayoutParams(bounds.width(), bounds.height())
                    .apply { setMargins(0, bounds.top, 0, 0) },
                secondPanel = FrameLayout.LayoutParams(view.width, view.height - bounds.bottom)
                    .apply { setMargins(0, bounds.bottom, 0, 0) }
            )
        } else {
            ViewParams(
                isPortrait = isPortrait,
                firstPanel = FrameLayout.LayoutParams(bounds.left, view.height),
                hingePanel = FrameLayout.LayoutParams(bounds.width(), bounds.height())
                    .apply { setMargins(bounds.left, 0, 0, 0) },
                secondPanel = FrameLayout.LayoutParams(view.width - bounds.right, view.height)
                    .apply { setMargins(bounds.right, 0, 0, 0) }
            )
        }
    }

    private fun Activity.getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
        else Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.top
    }

    /*
        To determine if Landscape or not is based on the bounds of the hinge:

        width >= height = Portrait
        ----------
        |        |
        |        |
        |--------| (width = 10 | height = 1)
        |        |
        |        |
        ----------

        width < height = Landscape
        -------------------
        |        |        |
        |        |        |
        |        |        | (width = 1 | height = 10)
        |        |        |
        |        |        |
        -------------------
     */
    private fun Rect.isPortrait(): Boolean = width() >= height()

    override fun onDetach() {
        // This should be removed as it could lead to leaks.
        //  My opinion? Do not use Consumer, but other class where do not satisfy SAM rules.
        // windowManager.unregisterDeviceStateChangeCallback {  }

        windowManager.unregisterDeviceStateChangeCallback(windowLayoutInfoConsumer)
        super.onDetach()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // It is really annoying have to still register/unregister callbacks, specially when we have
        //   the lifecycle awareness.
        // By the way, for me we are missing Fragment Lifecycle Awareness where we can listen for
        //   Fragments lifecycle events, such this 'onAttach' and 'onDetach'.
        windowManager.registerDeviceStateChangeCallback(
            mainThreadExecutor,
            windowLayoutInfoConsumer
        )
    }
}