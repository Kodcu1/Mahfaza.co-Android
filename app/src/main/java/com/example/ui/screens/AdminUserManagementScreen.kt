package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.model.UserRole
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val allStudents by viewModel.allStudents.collectAsState()
    val allCoaches by viewModel.allCoaches.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tümü", "Öğrenciler", "Koçlar")

    val filteredUsers = remember(allStudents, allCoaches, searchQuery, selectedTab) {
        val baseList = when (selectedTab) {
            1 -> allStudents
            2 -> allCoaches
            else -> allStudents + allCoaches
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.email.contains(searchQuery, ignoreCase = true) 
            }
        }
    }

    var userToDelete by remember { mutableStateOf<User?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kullanıcı Yönetimi", fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
        ) {
            // Search and Filter Section
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("İsim veya e-posta ile ara...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title, 
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                ) 
                            }
                        )
                    }
                }
            }

            // User List
            if (filteredUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Kullanıcı bulunamadı.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers) { user ->
                        AdminUserCard(
                            user = user, 
                            onDelete = { userToDelete = user },
                            isEnabled = !isDeleting
                        )
                    }
                }
            }
        }
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Kullanıcıyı Sil", fontWeight = FontWeight.Bold) },
            text = { Text("${userToDelete?.name} isimli kullanıcıyı ve tüm verilerini (görevler, sınavlar, XP vb.) kalıcı olarak silmek istediğinize emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        userToDelete?.id?.let { id ->
                            isDeleting = true
                            viewModel.deleteUser(id)
                            // Note: deletion is async, but we can reset state here or wait for flow update
                            isDeleting = false
                            userToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Evet, Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
fun AdminUserCard(user: User, onDelete: () -> Unit, isEnabled: Boolean = true) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (user.role == UserRole.COACH) MaterialTheme.colorScheme.secondaryContainer 
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person, 
                    contentDescription = null,
                    tint = if (user.role == UserRole.COACH) MaterialTheme.colorScheme.secondary 
                           else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF233B5D)
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    color = if (user.role == UserRole.COACH) Color(0xFFF0FDF4) else Color(0xFFEFF6FF),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = if (user.role == UserRole.COACH) "KOÇ" else "ÖĞRENCİ",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (user.role == UserRole.COACH) Color(0xFF166534) else Color(0xFF1E40AF),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(
                onClick = onDelete,
                enabled = isEnabled,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFFEF2F2))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFDC2626))
            }
        }
    }
}
