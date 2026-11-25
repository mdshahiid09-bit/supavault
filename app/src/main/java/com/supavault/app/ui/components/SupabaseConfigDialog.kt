package com.supavault.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.supavault.app.data.supabase.SupabaseManager
import com.supavault.app.ui.theme.*

@Composable
fun SupabaseConfigDialog(
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var url by remember { mutableStateOf(SupabaseManager.getSupabaseUrl()) }
    var key by remember { mutableStateOf(SupabaseManager.getSupabaseKey()) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Supabase Connection Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "App ke andar Supabase Project credentials configure karein. Yeh instantly active ho jayenge.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Project URL (https://xyz.supabase.co)") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = SupabaseGreen) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SupabaseGreen,
                        unfocusedBorderColor = DarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Anon Public Key") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = SupabaseGreen) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SupabaseGreen,
                        unfocusedBorderColor = DarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (message != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isError) StatusRed.copy(alpha = 0.15f) else SupabaseGreen.copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isError) StatusRed else SupabaseGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message!!,
                            color = if (isError) StatusRed else SupabaseGreen,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!url.startsWith("http")) {
                                message = "Valid URL dalein (starting with https://)"
                                isError = true
                                return@Button
                            }
                            if (key.isBlank()) {
                                message = "Anon Key khali nahi ho sakti"
                                isError = true
                                return@Button
                            }
                            SupabaseManager.updateConfig(url, key)
                            message = "Settings saved successfully!"
                            isError = false
                            onSaved()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SupabaseGreen)
                    ) {
                        Text("Save & Connect", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
