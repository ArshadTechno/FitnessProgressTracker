package com.awscubetech.fitnesstracker.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awscubetech.fitnesstracker.data.local.SavedGymEntity
import com.awscubetech.fitnesstracker.ui.FitnessViewModel
import com.awscubetech.fitnesstracker.ui.theme.EmeraldPrimary
import com.awscubetech.fitnesstracker.ui.theme.GoldAccent

@Composable
fun GymsNearMeScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedGyms by viewModel.savedGyms.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "⭐ Favorites", "Powerlifting", "CrossFit", "24/7 Gym", "Calisthenics", "Olympic & Boxing")

    val filteredGyms = savedGyms.filter { gym ->
        val matchesQuery = searchQuery.isBlank() ||
                gym.name.contains(searchQuery, ignoreCase = true) ||
                gym.address.contains(searchQuery, ignoreCase = true) ||
                gym.facilitiesCsv.contains(searchQuery, ignoreCase = true) ||
                gym.category.contains(searchQuery, ignoreCase = true)

        val matchesCategory = when (selectedCategory) {
            "All" -> true
            "⭐ Favorites" -> gym.isFavorite
            "Powerlifting" -> gym.category.contains("Powerlifting", ignoreCase = true) || gym.category.contains("Olympic", ignoreCase = true)
            "CrossFit" -> gym.category.contains("CrossFit", ignoreCase = true) || gym.category.contains("Functional", ignoreCase = true)
            "24/7 Gym" -> gym.category.contains("24/7", ignoreCase = true) || gym.openingHours.contains("24", ignoreCase = true)
            "Calisthenics" -> gym.category.contains("Calisthenics", ignoreCase = true) || gym.category.contains("Bodyweight", ignoreCase = true)
            "Olympic & Boxing" -> gym.category.contains("Olympic", ignoreCase = true) || gym.category.contains("Boxing", ignoreCase = true)
            else -> true
        }

        matchesQuery && matchesCategory
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_custom_gym_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Gym")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Banner with Live Search in Maps
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Fitness Centers & Gyms",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${savedGyms.size} facilities saved • Real GPS navigation & dialer",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=gyms and fitness centers near me"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/gyms+near+me"))
                                    context.startActivity(webIntent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Live GPS Radar in Google Maps", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // 2. Interactive Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gym_search_field"),
                    placeholder = { Text("Search by name, address, facility (e.g. Sauna, Eleiko)...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = EmeraldPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 3. Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // 4. Gym Cards or Empty State
            if (filteredGyms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No fitness centers match your search", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tap the '+' button below to add your local gym or reset filters.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(filteredGyms, key = { it.id }) { gym ->
                    DynamicGymCard(
                        gym = gym,
                        onToggleFavorite = { viewModel.toggleGymFavorite(gym) },
                        onDelete = { viewModel.deleteGym(gym) },
                        onOpenMaps = { openGymInMaps(context, gym) },
                        onCallGym = { callGym(context, gym.phoneNumber) },
                        onShareGym = { shareGym(context, gym) }
                    )
                }
            }
        }
    }

    // Add Custom Gym Dialog
    if (showAddDialog) {
        AddCustomGymDialog(
            onDismiss = { showAddDialog = false },
            onAddGym = { name, category, address, phone, facilities, hours ->
                viewModel.addCustomGym(
                    name = name,
                    category = category,
                    address = address,
                    phoneNumber = phone,
                    facilitiesCsv = facilities,
                    openingHours = hours
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DynamicGymCard(
    gym: SavedGymEntity,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onOpenMaps: () -> Unit,
    onCallGym: () -> Unit,
    onShareGym: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gym_card_${gym.id}"),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (gym.isFavorite) EmeraldPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = gym.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (gym.isCustomUserGym) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EmeraldPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("My Gym", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                    }
                    Text(
                        text = "${gym.category} • ${gym.distanceKm} km away",
                        fontSize = 12.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (gym.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (gym.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (gym.isCustomUserGym) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${gym.rating} (${gym.reviewCount})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFFB45309)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⏰ ${gym.openingHours}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📍 ${gym.address}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Facilities tags
            val facilityList = gym.facilitiesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                facilityList.forEach { fac ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = fac, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenMaps,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Directions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCallGym,
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 12.sp)
                }

                IconButton(
                    onClick = onShareGym,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share Gym", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AddCustomGymDialog(
    onDismiss: () -> Unit,
    onAddGym: (name: String, category: String, address: String, phone: String, facilities: String, hours: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Powerlifting & Gym") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+1 555-") }
    var facilities by remember { mutableStateOf("Free Weights, Squat Racks, Cardio") }
    var hours by remember { mutableStateOf("6:00 AM - 10:00 PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Fitness Center", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Gym / Club Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. CrossFit, 24/7, Olympic)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Street Address / City") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = facilities,
                    onValueChange = { facilities = it },
                    label = { Text("Key Facilities (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Opening Hours (e.g. 24/7, 5am-11pm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAddGym(
                            name.trim(),
                            category.trim(),
                            if (address.isBlank()) "Local City Fitness Area" else address.trim(),
                            phone.trim(),
                            facilities.trim(),
                            hours.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Save Gym")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun openGymInMaps(context: Context, gym: SavedGymEntity) {
    val uri = Uri.parse("geo:0,0?q=${Uri.encode(gym.name + ", " + gym.address)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(gym.name + " " + gym.address)}"))
        context.startActivity(webIntent)
    }
}

private fun callGym(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.replace(" ", "")}"))
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // ignore if dialer unavailable
    }
}

private fun shareGym(context: Context, gym: SavedGymEntity) {
    val shareText = "Check out this fitness facility on Fitness Progress: ${gym.name}\n📍 ${gym.address}\n⏰ ${gym.openingHours}\nRating: ${gym.rating}⭐\nFacilities: ${gym.facilitiesCsv}"
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Gym Details"))
}
