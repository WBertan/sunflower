/*
 * Copyright 2018 Google LLC
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

package com.google.samples.apps.sunflower.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.sunflower.data.GardenPlanting
import com.google.samples.apps.sunflower.data.GardenPlantingRepository
import com.google.samples.apps.sunflower.data.PlantAndGardenPlantings
import kotlinx.coroutines.launch

class GardenPlantingListViewModel internal constructor(
    private val gardenPlantingRepository: GardenPlantingRepository
) : ViewModel() {

    val gardenPlantings = gardenPlantingRepository.getGardenPlantings()

    val plantAndGardenPlantings: LiveData<List<PlantAndGardenPlantings>> =
        Transformations.map(gardenPlantingRepository.getPlantAndGardenPlantings()) { plantings ->
            plantings.filter { it.gardenPlantings.isNotEmpty() }
        }

    fun removeGardenPlantings(plantIdsToRemove: List<Long>) {
        val currentPlantings = plantAndGardenPlantings.value ?: return
        val plantingsToRemove = plantIdsToRemove.map { index ->
            currentPlantings[index.toInt()].gardenPlantings
        }
        viewModelScope.launch {
            plantingsToRemove.forEach { plantings ->
                plantings.forEach { planting ->
                    gardenPlantingRepository.removeGardenPlanting(planting)
                }
            }
        }
    }
}