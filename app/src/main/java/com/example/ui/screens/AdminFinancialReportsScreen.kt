package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExpenseEntity
import com.example.data.local.IncomeEntity
import com.example.ui.components.*
import com.example.viewmodel.MainViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFinancialReportsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val expenses by viewModel.allExpenses.collectAsState()
    val incomes by viewModel.allIncomes.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    
    val students by viewModel.allStudents.collectAsState()
    val coaches by viewModel.allCoaches.collectAsState()
    val packageConfigs by viewModel.allPackageConfigs.collectAsState()

    val totalExpenseValue = expenses.sumOf { it.amount }
    val netBalance = (totalIncome ?: 0.0) - totalExpenseValue

    // Calculate Estimated Monthly Income (Tahmini Aylık Gelir)
    val estimatedMonthlyIncome = remember(students, coaches, packageConfigs) {
        val allUsers = students + coaches
        val pkgMap = packageConfigs.associateBy { it.id }
        allUsers.sumOf { user ->
            val pkg = pkgMap[user.packageId]
            val priceStr = pkg?.price ?: "0"
            // Simple parsing: find the number part. E.g. "99,99 TL/ay" -> 99.99
            priceStr.filter { it.isDigit() || it == ',' }.replace(',', '.').toDoubleOrNull() ?: 0.0
        }
    }

    var showAddExpense by remember { mutableStateOf(false) }
    var showAddIncome by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finansal Raporlar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            PremiumAnimatedBackground()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Balance Card
                StaggeredVerticalEntrance(index = 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth().premiumClickableScale(),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF233B5D))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Net Kasa Durumu", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelLarge)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("₺", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                                        AnimatedCounter(
                                            targetValue = netBalance.toInt(),
                                            style = MaterialTheme.typography.displaySmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                // Estimated Income Mini Info
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Tahmini Aylık", color = Color(0xFFD97706), style = MaterialTheme.typography.labelSmall)
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("₺", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                                        AnimatedCounter(
                                            targetValue = estimatedMonthlyIncome.toInt(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                FinanceStatMini(
                                    label = "Gerçekleşen Gelir",
                                    amount = totalIncome ?: 0.0,
                                    icon = Icons.Default.TrendingUp,
                                    color = Color(0xFF4ADE80),
                                    modifier = Modifier.weight(1f)
                                )
                                FinanceStatMini(
                                    label = "Toplam Gider",
                                    amount = totalExpenseValue,
                                    icon = Icons.Default.TrendingDown,
                                    color = Color(0xFFF87171),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                StaggeredVerticalEntrance(index = 1) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { showAddIncome = true },
                            modifier = Modifier.weight(1f).height(56.dp).premiumClickableScale(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gelir Ekle")
                        }
                        Button(
                            onClick = { showAddExpense = true },
                            modifier = Modifier.weight(1f).height(56.dp).premiumClickableScale(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gider Ekle")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                StaggeredVerticalEntrance(index = 2) {
                    Text(
                        text = "Son Hareketler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF233B5D)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                val allTransactions = remember(incomes, expenses) {
                    (incomes.map { true to it } + expenses.map { false to it })
                        .sortedByDescending { if (it.first) (it.second as IncomeEntity).date else (it.second as ExpenseEntity).date }
                }

                if (allTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Henüz işlem kaydı yok.", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(allTransactions) { (isIncome, item) ->
                            AnimatedEntry {
                                if (isIncome) {
                                    val income = item as IncomeEntity
                                    TransactionCard(
                                        title = income.description,
                                        subtitle = "Tahsilat",
                                        amount = income.amount,
                                        date = income.date,
                                        isIncome = true,
                                        onDelete = { viewModel.deleteIncome(income) }
                                    )
                                } else {
                                    val expense = item as ExpenseEntity
                                    TransactionCard(
                                        title = expense.title,
                                        subtitle = expense.description,
                                        amount = expense.amount,
                                        date = expense.date,
                                        isIncome = false,
                                        onDelete = { viewModel.deleteExpense(expense.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExpense) {
        AddTransactionDialog(isIncome = false, onDismiss = { showAddExpense = false }) { title, amount, desc ->
            viewModel.addExpense(ExpenseEntity(UUID.randomUUID().toString(), title, "Genel", amount, "TRY", "2026-10-29", desc, "admin1"))
            showAddExpense = false
        }
    }

    if (showAddIncome) {
        AddTransactionDialog(isIncome = true, onDismiss = { showAddIncome = false }) { title, amount, desc ->
            viewModel.addIncome(IncomeEntity(UUID.randomUUID().toString(), "user1", amount, "2026-10-29", desc, "std_pro", "admin1"))
            showAddIncome = false
        }
    }
}

@Composable
fun FinanceStatMini(label: String, amount: Double, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            Text("₺${String.format("%,.0f", amount)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TransactionCard(title: String, subtitle: String, amount: Double, date: String, isIncome: Boolean, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isIncome) Color(0xFF166534) else Color(0xFF991B1B),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF233B5D), style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"} ₺${String.format("%.2f", amount)}",
                    color = if (isIncome) Color(0xFF166534) else Color(0xFF991B1B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AddTransactionDialog(isIncome: Boolean, onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isIncome) "Yeni Gelir Kaydı" else "Yeni Gider Kaydı", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Başlık / Kaynak") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Tutar (₺)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Açıklama / Not") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (amt > 0 && title.isNotBlank()) onConfirm(title, amt, desc)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) Color(0xFF166534) else Color(0xFF991B1B))
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
