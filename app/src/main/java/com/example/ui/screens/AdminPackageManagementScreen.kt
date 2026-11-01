package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PackageConfigEntity
import com.example.ui.components.PremiumCard
import com.example.ui.components.premiumClickableScale
import com.example.util.PriceFormatter
import com.example.viewmodel.MainViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPackageManagementScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val packages by viewModel.allPackageConfigs.collectAsState()
    var editingPackage by remember { mutableStateOf<PackageConfigEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paket ve Fiyat Yönetimi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF233B5D),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Paket Ekle")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
        ) {
            if (packages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Henüz paket tanımlanmamış.", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(packages) { pkg ->
                        PackageCard(pkg = pkg, onEdit = { editingPackage = pkg })
                    }
                }
            }
        }
    }

    if (editingPackage != null) {
        PackageDialog(
            pkg = editingPackage,
            onDismiss = { editingPackage = null },
            onConfirm = { updatedPkg ->
                viewModel.updatePackageConfig(updatedPkg)
                editingPackage = null
            }
        )
    }

    if (showAddDialog) {
        PackageDialog(
            pkg = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newPkg ->
                viewModel.addPackageConfig(newPkg)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PackageCard(pkg: PackageConfigEntity, onEdit: () -> Unit) {
    PremiumCard(
        modifier = Modifier
            .fillMaxWidth()
            .premiumClickableScale(),
        onClick = onEdit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pkg.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF233B5D)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "👥 ${pkg.studentCapacity} Öğrenci Kapasitesi",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = Color(0xFF233B5D))
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Aylık Tutar", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                    Text(
                        text = PriceFormatter.formatMonthly(pkg.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Yıllık Tutar", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                    Text(
                        text = PriceFormatter.formatYearly(pkg.yearlyPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                }
            }
        }
    }
}

@Composable
fun PackageDialog(
    pkg: PackageConfigEntity?,
    onDismiss: () -> Unit,
    onConfirm: (PackageConfigEntity) -> Unit
) {
    var name by remember { mutableStateOf(pkg?.name ?: "") }
    var price by remember { mutableStateOf(pkg?.price ?: "") }
    var yearlyPrice by remember { mutableStateOf(pkg?.yearlyPrice ?: "") }
    var capacity by remember { mutableStateOf(pkg?.studentCapacity?.toString() ?: "10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (pkg == null) "Yeni Paket Ekle" else "Paket Düzenle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Paket Adı") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Aylık Fiyat (Örn: 999)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = yearlyPrice,
                    onValueChange = { yearlyPrice = it },
                    label = { Text("Yıllık Fiyat (Örn: 9990)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Öğrenci Kapasitesi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            PackageConfigEntity(
                                id = pkg?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                price = price,
                                yearlyPrice = yearlyPrice,
                                studentCapacity = capacity.toIntOrNull() ?: 10
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF233B5D))
            ) {
                Text(if (pkg == null) "Ekle" else "Güncelle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal", color = Color(0xFF64748B)) }
        }
    )
}
