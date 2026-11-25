package com.supavault.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.supavault.app.data.model.VaultItem
import com.supavault.app.data.supabase.SupabaseManager
import com.supavault.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var allItems by remember { mutableStateOf<List<VaultItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isTestingStorage by remember { mutableStateOf(false) }
    var storageTestResult by remember { mutableStateOf<String?>(null) }

    fun loadAllData() {
        isLoading = true
        errorMessage = null
        scope.launch {
            val res = SupabaseManager.fetchAllItems()
            isLoading = false
            res.onSuccess {
                allItems = it
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "Failed to fetch admin data from Supabase"
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAllData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Admin Console",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = StatusYellow.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                color = StatusYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { loadAllData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Metrics and diagnostics card
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Global Supabase Status",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Metric 1: Total Posts
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Total Records", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = allItems.size.toString(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SupabaseGreen
                                )
                            }
                        }

                        // Metric 2: Contributors
                        val uniqueUsers = allItems.map { it.user_id }.distinct().size
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Active Users", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = uniqueUsers.toString(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Diagnostic button
                    Button(
                        onClick = {
                            isTestingStorage = true
                            storageTestResult = null
                            scope.launch {
                                val res = SupabaseManager.testStorageUpload()
                                isTestingStorage = false
                                res.onSuccess { url ->
                                    storageTestResult = "Storage Upload SUCCESS!\nPublic URL:\n$url"
                                }.onFailure { err ->
                                    storageTestResult = "Storage Upload Failed:\n${err.message}"
                                }
                            }
                        },
                        enabled = !isTestingStorage,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isTestingStorage) {
                            CircularProgressIndicator(color = SupabaseGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing Storage...", color = TextPrimary)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = SupabaseGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Supabase Storage Bucket ('vault_media')", color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Storage Result Banner if available
            if (storageTestResult != null) {
                Surface(
                    color = if (storageTestResult!!.contains("SUCCESS")) SupabaseGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (storageTestResult!!.contains("SUCCESS")) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (storageTestResult!!.contains("SUCCESS")) SupabaseGreen else StatusRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = storageTestResult!!,
                            color = if (storageTestResult!!.contains("SUCCESS")) SupabaseGreen else StatusRed,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { storageTestResult = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted)
                        }
                    }
                }
            }

            // Global Items List
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = SupabaseGreen,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (errorMessage != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: $errorMessage", color = StatusRed, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { loadAllData() }) {
                            Text("Retry")
                        }
                    }
                } else if (allItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No records found in database.", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allItems, key = { it.id ?: it.title }) { item ->
                            AdminItemCard(
                                item = item,
                                onDelete = {
                                    scope.launch {
                                        item.id?.let {
                                            SupabaseManager.deleteItem(it)
                                            loadAllData()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminItemCard(
    item: VaultItem,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = StatusBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.user_email,
                        fontSize = 11.sp,
                        color = StatusBlue,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Admin Delete", color = StatusRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!item.image_url.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.image_url)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ID: ${item.id?.take(18)}... | ${item.created_at?.take(16)?.replace("T", " ") ?: ""}",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}
