package com.example.alertify.ui.screens.hospitals


import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.net.URL

data class Hospital(
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyHospitalsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var hospitals by remember { mutableStateOf<List<Hospital>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedHospital by remember { mutableStateOf<Hospital?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // Initialize OSMDroid config
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName

        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            errorMessage = "Location permission not granted"
            isLoading = false
            return@LaunchedEffect
        }

        // Get user location
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location == null) {
                errorMessage = "Unable to get current location"
                isLoading = false
                return@addOnSuccessListener
            }

            userLocation = GeoPoint(location.latitude, location.longitude)

            // Fetch hospitals from Overpass API (free, no key needed)
            scope.launch {
                try {
                    val hospitals_result = fetchNearbyHospitals(
                        location.latitude,
                        location.longitude
                    )
                    hospitals = hospitals_result

                    // Add markers to map
                    mapView?.let { map ->
                        addMarkersToMap(map, hospitals_result, userLocation!!)
                    }
                } catch (e: Exception) {
                    errorMessage = "Failed to load hospitals: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }.addOnFailureListener {
            errorMessage = "Location error: ${it.message}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Hospitals", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A2A3A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1A2A3A))
        ) {

            // OSMDroid Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)

                            // Center on user location or default
                            val center = userLocation ?: GeoPoint(20.5937, 78.9629)
                            controller.setCenter(center)

                            mapView = this
                        }
                    },
                    update = { map ->
                        userLocation?.let {
                            map.controller.setCenter(it)
                            mapView = map
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Red
                    )
                }

                errorMessage?.let {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A3A4A)
                        )
                    ) {
                        Text(
                            text = it,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Hospital list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (hospitals.isEmpty() && !isLoading) {
                    item {
                        Text(
                            text = "No hospitals found nearby",
                            color = Color.Gray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                items(hospitals) { hospital ->
                    HospitalCard(
                        hospital = hospital,
                        isSelected = selectedHospital == hospital,
                        onClick = {
                            selectedHospital = hospital
                            // Zoom map to selected hospital
                            mapView?.controller?.animateTo(
                                GeoPoint(hospital.lat, hospital.lon)
                            )
                            mapView?.controller?.setZoom(17.0)
                        }
                    )
                }
            }
        }
    }
}

// Fetch hospitals using Overpass API — completely free, no key
suspend fun fetchNearbyHospitals(lat: Double, lon: Double): List<Hospital> {
    return withContext(Dispatchers.IO) {
        try {
            // Use a more reliable Overpass endpoint
            val query = "[out:json][timeout:25];" +
                    "(" +
                    "node[\"amenity\"=\"hospital\"](around:5000,$lat,$lon);" +
                    "way[\"amenity\"=\"hospital\"](around:5000,$lat,$lon);" +
                    ");" +
                    "out center;"

            val connection = java.net.URL("https://overpass-api.de/api/interpreter")
                .openConnection() as java.net.HttpURLConnection

            connection.apply {
                requestMethod = "POST"           // POST is more reliable than GET
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }

            // Write query as POST body
            connection.outputStream.use { os ->
                os.write("data=${java.net.URLEncoder.encode(query, "UTF-8")}".toByteArray())
            }

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                throw Exception("Server error: $responseCode")
            }

            val response = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            val json = JSONObject(response)
            val elements = json.getJSONArray("elements")

            val hospitalList = mutableListOf<Hospital>()
            for (i in 0 until elements.length()) {
                val element = elements.getJSONObject(i)
                val tags = element.optJSONObject("tags") ?: continue
                val name = tags.optString("name").ifEmpty { "Unknown Hospital" }

                val hospitalLat: Double
                val hospitalLon: Double

                when (element.getString("type")) {
                    "node" -> {
                        hospitalLat = element.getDouble("lat")
                        hospitalLon = element.getDouble("lon")
                    }
                    else -> {
                        val center = element.optJSONObject("center") ?: continue
                        hospitalLat = center.getDouble("lat")
                        hospitalLon = center.getDouble("lon")
                    }
                }

                val address = buildString {
                    tags.optString("addr:street").takeIf { it.isNotEmpty() }?.let { append(it) }
                    tags.optString("addr:city").takeIf { it.isNotEmpty() }?.let {
                        if (isNotEmpty()) append(", ")
                        append(it)
                    }
                }.ifEmpty { "Address unavailable" }

                hospitalList.add(Hospital(name, address, hospitalLat, hospitalLon))
            }

            hospitalList

        } catch (e: java.net.UnknownHostException) {
            throw Exception("No internet connection")
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Request timed out — try again")
        } catch (e: Exception) {
            throw Exception(e.message ?: "Unknown error")
        }
    }
}
fun addMarkersToMap(map: MapView, hospitals: List<Hospital>, userLocation: GeoPoint) {
    map.overlays.clear()

    // User location marker
    val userMarker = Marker(map).apply {
        position = userLocation
        title = "You are here"
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }
    map.overlays.add(userMarker)

    // Hospital markers
    hospitals.forEach { hospital ->
        val marker = Marker(map).apply {
            position = GeoPoint(hospital.lat, hospital.lon)
            title = hospital.name
            snippet = hospital.address
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        map.overlays.add(marker)
    }

    map.invalidate()
}

@Composable
fun HospitalCard(
    hospital: Hospital,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2A4A6A) else Color(0xFF243040)
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hospital.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    text = hospital.address,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}