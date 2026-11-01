package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Composable
fun AdminBackupScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isLoading = true
                try {
                    val json = viewModel.getBackupData()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->
                                writer.write(json)
                            }
                        }
                    }
                    Toast.makeText(context, "Yedekleme başarıyla tamamlandı.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Yedekleme hatası: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isLoading = true
                try {
                    val json = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                                reader.readText()
                            }
                        } ?: ""
                    }
                    if (json.isNotBlank()) {
                        val success = viewModel.restoreDatabaseFromJson(json)
                        if (success) {
                            Toast.makeText(context, "Veriler başarıyla geri yüklendi. Uygulamayı yeniden başlatmanız önerilir.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Geri yükleme hatası: Geçersiz dosya formatı veya sürüm uyuşmazlığı.", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Dosya okuma hatası: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Veritabanı Yedekleme ve Kurtarma", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Text(
            "Uygulama verilerini JSON formatında dışa aktarabilir veya daha önce aldığınız bir yedeği geri yükleyebilirsiniz.",
            style = MaterialTheme.typography.bodyMedium
        )

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Dışa Aktar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Tüm veritabanını (Kullanıcılar, Görevler, Finans vb.) güvenli bir dosyaya kaydeder.", style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = { createDocumentLauncher.launch("mahfaza_backup_${System.currentTimeMillis()}.json") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.SdStorage, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Şimdi Yedekle")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Geri Yükle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text("DİKKAT: Bu işlem mevcut tüm verileri silecek ve yedek dosyasındaki verileri yükleyecektir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                Button(
                    onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yedekten Geri Yükle")
                }
            }
        }
    }
}
