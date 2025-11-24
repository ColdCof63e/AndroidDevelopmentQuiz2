package com.bt.quiz2

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun LocationSelectionScreen(
    viewModel: LocationViewModel
) {
    val favouriteLocations = viewModel.favouriteLocations.value
    var showDialog by remember { mutableStateOf(false) }
    var clickedLocation by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(43.65, -79.38), 10f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                clickedLocation = latLng
                showDialog = true
            }
        ) {
            favouriteLocations.forEach { fav ->
                Marker(
                    state = MarkerState(position = LatLng(fav.latitude, fav.longitude)),
                    title = fav.title,
                    snippet = "${fav.rating} Stars: ${fav.description}",
                    onClick = {
                        // Optional: Click marker to delete?
                        it.showInfoWindow()
                        true
                    }
                )
            }

            clickedLocation?.let {
                Marker(state = MarkerState(it), alpha = 0.5f)
            }
        }
    }

    // 3. The Form Dialog
    if (showDialog && clickedLocation != null) {
        AddLocationDialog(
            latLng = clickedLocation!!,
            onDismiss = { showDialog = false },
            onSave = { title, desc, rating ->
                viewModel.addFavourite(
                    title,
                    desc,
                    rating,
                    LocationData(clickedLocation!!.latitude, clickedLocation!!.longitude)
                )
                showDialog = false
                clickedLocation = null // Clear temp selection
            }
        )
    }
}

@Composable
fun AddLocationDialog(
    latLng: LatLng,
    onDismiss: () -> Unit,
    onSave: (String, String, Float) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") } // Input as string for simplicity

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add Favourite Location", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = rating, onValueChange = { rating = it }, label = { Text("Rating (1-5)") })

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        val ratingFloat = rating.toFloatOrNull() ?: 0f
                        onSave(title, description, ratingFloat)
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}