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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.window.DeviceState
import kotlinx.android.synthetic.main.fragment_plant_list_renderer_half_opened.*

class PlantListRendererFragment : RendererFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutRes = when (deviceState.posture) {
            DeviceState.POSTURE_FLIPPED -> R.layout.fragment_plant_list_renderer_flipped
            DeviceState.POSTURE_HALF_OPENED -> R.layout.fragment_plant_list_renderer_half_opened
            else -> R.layout.fragment_plant_list_renderer
        }
        return inflater.inflate(layoutRes, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.doOnLayout {
            println("asas onViewCreated: displayFeature=$displayFeature | deviceState=$deviceState")

            val viewParams = getViewParams() ?: return@doOnLayout

            with(viewParams) {
                if (isPortrait) {
                    detail_nav_container_wrapper?.layoutParams = firstPanel
                    hinge?.layoutParams = hingePanel
                    list_wrapper?.layoutParams = secondPanel
                } else {
                    // We invert in Landscape keeping the Landscape rule to looks like Master/Detail
                    list_wrapper?.layoutParams = firstPanel
                    hinge?.layoutParams = hingePanel
                    detail_nav_container_wrapper?.layoutParams = secondPanel
                }
            }
        }
    }
}