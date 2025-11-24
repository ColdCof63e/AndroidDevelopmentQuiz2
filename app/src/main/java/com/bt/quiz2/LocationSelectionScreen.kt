package com.bt.quiz2

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bt.quiz2.models.FavouriteLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun LocationSelectionScreen(
    viewModel: LocationViewModel
) {
    val context = LocalContext.current
    val favouriteLocations = viewModel.favouriteLocations.value
    val userLocation by viewModel.userLocation // Get the live user location

    var locationToEdit by remember { mutableStateOf<FavouriteLocation?>(null) }
    var newLocationLatLng by remember { mutableStateOf<LatLng?>(null) }
    val showDialog = locationToEdit != null || newLocationLatLng != null

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(43.65, -79.38), 10f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            MyLocationUtils(context).requestLocationUpdates(viewModel)
        }
    }

    LaunchedEffect(Unit) {
        val utils = MyLocationUtils(context)
        if (utils.hasLocationPermission(context)) {
            utils.requestLocationUpdates(viewModel)
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    // Center camera when user location is found
//    LaunchedEffect(userLocation) {
//        userLocation?.let {
//            cameraPositionState.animate(
//                CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
//            )
//        }
//    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                locationToEdit = null
                newLocationLatLng = latLng
            }
        ) {
            favouriteLocations.forEach { fav ->
                Marker(
                    state = MarkerState(position = LatLng(fav.latitude, fav.longitude)),
                    title = fav.title,
                    snippet = "${fav.rating} Stars: ${fav.description}",
                    onInfoWindowClick = {
                        newLocationLatLng = null
                        locationToEdit = fav
                    }
                )
            }

            newLocationLatLng?.let {
                Marker(state = MarkerState(it), alpha = 0.5f, title = "New Location")
            }
        }
    }

    if (showDialog) {
        val latLng = locationToEdit?.let { LatLng(it.latitude, it.longitude) } ?: newLocationLatLng!!

        AddLocationDialog(
            latLng = latLng,
            existingLocation = locationToEdit,
            onDismiss = {
                locationToEdit = null
                newLocationLatLng = null
            },
            onSave = { title, desc, rating ->
                viewModel.addOrUpdateFavourite(
                    id = locationToEdit?.id,
                    title = title,
                    desc = desc,
                    rating = rating,
                    location = LocationData(latLng.latitude, latLng.longitude)
                )
                locationToEdit = null
                newLocationLatLng = null
            },
            onDelete = {
                locationToEdit?.let { viewModel.deleteFavourite(it) }
                locationToEdit = null
                newLocationLatLng = null
            }
        )
    }
}

@Composable
fun AddLocationDialog(
    latLng: LatLng,
    existingLocation: FavouriteLocation?,
    onDismiss: () -> Unit,
    onSave: (String, String, Float) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(existingLocation?.title ?: "") }
    var description by remember { mutableStateOf(existingLocation?.description ?: "") }
    var rating by remember { mutableStateOf(existingLocation?.rating?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (existingLocation == null) "Add Favourite" else "Edit Favourite",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = rating, onValueChange = { rating = it }, label = { Text("Rating (1-5)") })

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    if (existingLocation != null) {
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Delete")
                        }
                    }

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