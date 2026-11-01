package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import com.example.model.UserRole
import com.example.navigation.*
import com.example.ui.screens.*
import com.example.ui.theme.AppTheme
import com.example.viewmodel.MainViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.components.PremiumButton
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(user) {
        if (user == null) {
            if (currentRoute != null && !currentRoute.contains("Splash") && !currentRoute.contains("Register") && !currentRoute.contains("PackageSelection") && !currentRoute.contains("SelectLevel") && !currentRoute.contains("Payment")) {
                navController.navigate(Login) { popUpTo(0) }
            }
        } else {
            if (currentRoute?.contains("Login") == true) {
                when (user?.role) {
                    UserRole.STUDENT -> {
                        if (user?.educationLevel == null) navController.navigate(SelectLevel) { popUpTo(0) }
                        else navController.navigate(StudentDashboard(user?.educationLevel!!.name)) { popUpTo(0) }
                    }
                    UserRole.COACH -> navController.navigate(CoachPanel) { popUpTo(0) }
                    UserRole.ADMIN -> navController.navigate(AdminPanel) { popUpTo(0) }
                    null -> {}
                }
            }
        }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        com.example.ui.components.LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            }
        )
    }

    if (user?.role == UserRole.COACH && shouldShowDrawer(currentRoute)) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CoachDrawerContent(
                    user = user,
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        scope.launch { drawerState.close() }
                        navController.navigate(route)
                    },
                    onLogoutClick = {
                        scope.launch { drawerState.close() }
                        showLogoutDialog = true
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
                    TopAppBar(
                        title = { Text("Mahfaza.co - Koç Komuta") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            val infiniteTransition = rememberInfiniteTransition(label = "bell_pulse")
                            val bellScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = if (unreadCount > 0) 1.2f else 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "bell_scale"
                            )
                            IconButton(onClick = { navController.navigate(Notifications) }) {
                                if (unreadCount > 0) {
                                    BadgedBox(
                                        badge = { Badge { Text(unreadCount.toString()) } },
                                        modifier = Modifier.graphicsLayer(scaleX = bellScale, scaleY = bellScale)
                                    ) {
                                        Icon(Icons.Default.Notifications, contentDescription = "Bildirimler ($unreadCount okunmamış)")
                                    }
                                } else {
                                    Icon(Icons.Default.NotificationsNone, contentDescription = "Bildirimler")
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .clickable { navController.navigate(Profile) }
                            ) {
                                com.example.util.StudentAvatar(
                                    name = user?.name ?: "Koç",
                                    avatarUri = user?.avatarUri,
                                    size = 36.dp
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    CoachBottomBar(navController, currentRoute)
                }
            ) { innerPadding ->
                NavHostContainer(navController, viewModel, innerPadding)
            }
        }
    } else if (user?.role == UserRole.ADMIN && shouldShowDrawer(currentRoute)) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AdminDrawerContent(
                    user = user,
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        scope.launch { drawerState.close() }
                        navController.navigate(route)
                    },
                    onLogoutClick = {
                        scope.launch { drawerState.close() }
                        showLogoutDialog = true
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
                    TopAppBar(
                        title = { Text("Mahfaza.co - Süper Admin") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(Notifications) }) {
                                Icon(Icons.Default.NotificationsNone, contentDescription = "Bildirimler")
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .clickable { navController.navigate(Profile) }
                            ) {
                                com.example.util.StudentAvatar(
                                    name = user?.name ?: "Admin",
                                    avatarUri = user?.avatarUri,
                                    size = 36.dp
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    AdminBottomBar(navController, currentRoute)
                }
            ) { innerPadding ->
                NavHostContainer(navController, viewModel, innerPadding)
            }
        }
    } else if (user?.role == UserRole.STUDENT && shouldShowDrawer(currentRoute)) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                StudentDrawerContent(
                    user = user,
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        scope.launch { drawerState.close() }
                        navController.navigate(route)
                    },
                    onLogoutClick = {
                        scope.launch { drawerState.close() }
                        showLogoutDialog = true
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
                    TopAppBar(
                        title = { Text("Mahfaza.co Akademik Portal") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            val infiniteTransition = rememberInfiniteTransition(label = "bell_pulse_student")
                            val bellScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = if (unreadCount > 0) 1.2f else 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "bell_scale_student"
                            )
                            IconButton(onClick = { navController.navigate(Notifications) }) {
                                if (unreadCount > 0) {
                                    BadgedBox(
                                        badge = { Badge { Text(unreadCount.toString()) } },
                                        modifier = Modifier.graphicsLayer(scaleX = bellScale, scaleY = bellScale)
                                    ) {
                                        Icon(Icons.Default.Notifications, contentDescription = "Bildirimler ($unreadCount okunmamış)")
                                    }
                                } else {
                                    Icon(Icons.Default.NotificationsNone, contentDescription = "Bildirimler")
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .clickable { navController.navigate(Profile) }
                            ) {
                                com.example.util.StudentAvatar(
                                    name = user?.name ?: "Öğrenci",
                                    avatarUri = user?.avatarUri,
                                    size = 36.dp
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                NavHostContainer(navController, viewModel, innerPadding)
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar(currentRoute)) {
                    AppBottomBar(navController, currentRoute, user?.role, user?.educationLevel?.name)
                }
            }
        ) { innerPadding ->
            NavHostContainer(navController, viewModel, innerPadding)
        }
    }
}

@Composable
fun NavHostContainer(navController: androidx.navigation.NavHostController, viewModel: MainViewModel, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Splash,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable<Splash> {
            SplashScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate(Login) {
                        popUpTo(Splash) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    val user = viewModel.currentUser.value
                    if (user != null) {
                        when (user.role) {
                            UserRole.STUDENT -> {
                                if (user.educationLevel == null) navController.navigate(SelectLevel) { popUpTo(Splash) { inclusive = true } }
                                else navController.navigate(StudentDashboard(user.educationLevel.name)) { popUpTo(Splash) { inclusive = true } }
                            }
                            UserRole.COACH -> navController.navigate(CoachPanel) { popUpTo(Splash) { inclusive = true } }
                            UserRole.ADMIN -> navController.navigate(AdminPanel) { popUpTo(Splash) { inclusive = true } }
                        }
                    }
                }
            )
        }

        composable<Login> { 
            LoginScreen(
                viewModel = viewModel,
                onRegisterClick = { navController.navigate(SelectLevel) },
                onLoginSuccess = { /* Handled by LaunchedEffect in MainActivity */ }
            )
        }
        
        composable<SelectLevel> {
            com.example.ui.screens.SelectLevelScreen(
                onSelectRole = { role ->
                    navController.navigate(Register(role = role.name))
                }
            )
        }
        
        composable<Register> { backStackEntry ->
            val route: Register = backStackEntry.toRoute()
            val role = try { UserRole.valueOf(route.role) } catch (e: Exception) { UserRole.STUDENT }
            RegisterScreen(
                role = role,
                packageId = route.packageId,
                viewModel = viewModel,
                onSuccess = {
                    navController.navigate(PackageSelection(role = role.name))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<PackageSelection> { backStackEntry ->
            val route: PackageSelection = backStackEntry.toRoute()
            val role = try { UserRole.valueOf(route.role) } catch (e: Exception) { UserRole.STUDENT }
            PackageSelectionScreen(
                role = role,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPackageSelected = { packageId ->
                    navController.navigate(Payment(packageId = packageId, role = role.name))
                }
            )
        }

        composable<Payment> { backStackEntry ->
            val route: Payment = backStackEntry.toRoute()
            val role = try { UserRole.valueOf(route.role) } catch (e: Exception) { UserRole.STUDENT }
            PaymentScreen(
                packageId = route.packageId,
                role = role,
                viewModel = viewModel,
                onPaymentSuccess = {
                    val user = viewModel.currentUser.value
                    when (role) {
                        UserRole.STUDENT -> {
                            val level = user?.educationLevel?.name ?: "YKS_TYT_AYT"
                            navController.navigate(StudentDashboard(level)) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        UserRole.COACH -> {
                            navController.navigate(CoachPanel) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        UserRole.ADMIN -> {
                            navController.navigate(AdminPanel) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<StudentDashboard> { backStackEntry ->
            val route: StudentDashboard = backStackEntry.toRoute()
            StudentDashboardScreen(
                viewModel = viewModel,
                onFeatureLocked = { feature, pkg ->
                    navController.navigate(FeatureLocked(feature, pkg))
                },
                onNavigateToWeeklyProgram = { navController.navigate(WeeklyProgram) }
            )
        }
        
        composable<WeeklyProgram> { WeeklyProgramScreen(viewModel) }
        composable<ExamResults> { ExamResultsScreen(viewModel) }
        composable<Notifications> {
            NotificationsScreen(viewModel, onNavigateToRoute = { route -> navController.navigate(route) })
        }
        composable<Profile> { 
            val user by viewModel.currentUser.collectAsStateWithLifecycle()
            if (user?.role == UserRole.COACH) CoachProfileScreen(viewModel)
            else ProfileScreen(viewModel)
        }
        composable<CoachPanel> { 
            CoachDashboardScreen(
                viewModel = viewModel,
                onNavigateToPlanner = { navController.navigate(AiPlannerDraft) },
                onStudentClick = { studentId -> navController.navigate(StudentDetail(studentId)) },
                onNavigateToPortfolio = { navController.navigate(StudentPortfolio) },
                onNavigateToApprovals = { navController.navigate(ApprovalCenter) },
                onNavigateToMeetings = { navController.navigate(ParentMeetings) },
                onNavigateToAnalytics = { navController.navigate(AnalyticsReport) }
            )
        }
        composable<StudentPortfolio> {
            StudentPortfolioScreen(
                viewModel = viewModel,
                onStudentClick = { studentId -> navController.navigate(StudentDetail(studentId)) },
                onNavigateToChat = { studentId -> navController.navigate(Chat(studentId)) }
            )
        }
        composable<AdminPanel> { 
            AdminDashboardScreen(
                viewModel = viewModel,
                onNavigateToUsers = { navController.navigate(AdminUserManagement) },
                onNavigateToPackages = { navController.navigate(AdminPackageManagement) },
                onNavigateToCoupons = { navController.navigate(AdminCouponManagement) },
                onNavigateToFinancials = { navController.navigate(AdminFinancialReports) },
                onNavigateToBackup = { navController.navigate(AdminBackup) }
            )
        }
        composable<AdminUserManagement> { AdminUserManagementScreen(viewModel, { navController.popBackStack() }) }
        composable<AdminPackageManagement> { AdminPackageManagementScreen(viewModel, { navController.popBackStack() }) }
        composable<AdminCouponManagement> { AdminCouponManagementScreen(viewModel, { navController.popBackStack() }) }
        composable<AdminFinancialReports> { AdminFinancialReportsScreen(viewModel, { navController.popBackStack() }) }
        composable<AdminBackup> { AdminBackupScreen(viewModel, { navController.popBackStack() }) }

        composable<AiPlannerDraft> { 
            AiPlannerDraftScreen(onPublish = { tasks ->
                navController.popBackStack()
            })
        }
        composable<FeatureLocked> { backStackEntry ->
            val route: FeatureLocked = backStackEntry.toRoute()
            FeatureLockedScreen(
                featureName = route.featureName,
                requiredPackageName = route.requiredPackageName,
                onUpgradeClick = { navController.navigate(Profile) }
            )
        }
        composable<StudentDetail> { backStackEntry ->
            val route: StudentDetail = backStackEntry.toRoute()
            Student360Screen(
                viewModel = viewModel,
                studentId = route.studentId,
                onNavigateToChat = { studentId -> navController.navigate(Chat(studentId)) }
            )
        }
        
        // Fully Implemented Real Modules
        composable<ApprovalCenter> { ApprovalCenterScreen(viewModel) }
        composable<CoachingTasks> { CoachingTasksScreen(viewModel) }
        composable<RewardManagement> { RewardManagementScreen(viewModel) }
        composable<ParentMeetings> { ParentMeetingsScreen(viewModel) }
        composable<AnalyticsReport> { AnalyticsReportScreen(viewModel) }
        composable<RewardStore> { RewardStoreScreen(viewModel) }
        composable<PomodoroFocus> { PomodoroScreen(viewModel) }
        composable<Achievements> { AchievementsScreen(viewModel) }
        composable<Messages> { 
            MessagesScreen(viewModel, onNavigateToChat = { studentId ->
                navController.navigate(Chat(studentId))
            })
        }
        
        composable<Chat> { backStackEntry ->
            val route: Chat = backStackEntry.toRoute()
            ChatScreen(viewModel, route.studentId) { navController.popBackStack() }
        }
        
        composable<ReminderSettings> {
            ReminderSettingsScreen(viewModel, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun StudentDrawerContent(
    user: com.example.model.User?,
    currentRoute: String?,
    onItemClick: (Any) -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                com.example.util.StudentAvatar(
                    name = user?.name ?: "Öğrenci",
                    avatarUri = user?.avatarUri,
                    size = 52.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(user?.name ?: "Öğrenci", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val targetText = user?.targetUniversity?.takeIf { it.isNotBlank() }
                        ?: user?.educationLevel?.displayName
                        ?: "LGS & YKS"
                    Text(targetText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = com.example.ui.theme.SoftGoldSurface, shape = RoundedCornerShape(8.dp)) {
                    Text("${user?.currentXp ?: 0} XP", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = com.example.ui.theme.Gold)
                }
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                    Text("Seri: ${user?.streak ?: 0} Gün 🔥", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            val levelName = user?.educationLevel?.name ?: ""
            DrawerItem("Ana Sayfa", Icons.Default.Home, currentRoute?.contains("StudentDashboard") == true) { onItemClick(StudentDashboard(levelName)) }
            DrawerItem("Çalışmalarım ve Fotoğraflı Kanıtlar", Icons.Default.Assignment, currentRoute?.contains("CoachingTasks") == true) { onItemClick(CoachingTasks) }
            DrawerItem("Haftalık Programım", Icons.Default.DateRange, currentRoute?.contains("WeeklyProgram") == true) { onItemClick(WeeklyProgram) }
            DrawerItem("Deneme Analizlerim", Icons.Default.Assessment, currentRoute?.contains("ExamResults") == true) { onItemClick(ExamResults) }
            DrawerItem("Pomodoro Odaklanma", Icons.Default.Timer, currentRoute?.contains("PomodoroFocus") == true) { onItemClick(PomodoroFocus) }
            DrawerItem("Başarılar ve Rozetler", Icons.Default.EmojiEvents, currentRoute?.contains("Achievements") == true) { onItemClick(Achievements) }
            DrawerItem("Ödül Mağazası", Icons.Default.CardGiftcard, currentRoute?.contains("RewardStore") == true) { onItemClick(RewardStore) }
            DrawerItem("Bildirimler", Icons.Default.Notifications, currentRoute?.contains("Notifications") == true) { onItemClick(Notifications) }
            DrawerItem("Hatırlatma Ayarları", Icons.Default.Alarm, currentRoute?.contains("ReminderSettings") == true) { onItemClick(ReminderSettings) }
            DrawerItem("Mesajlar", Icons.Default.Message, currentRoute?.contains("Messages") == true) { onItemClick(Messages) }
            DrawerItem("Profilim", Icons.Default.Person, currentRoute?.contains("Profile") == true) { onItemClick(Profile) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DrawerItem("Çıkış Yap", Icons.Default.ExitToApp, false) { onLogoutClick() }
        }
    }
}

@Composable
fun CoachDrawerContent(
    user: com.example.model.User?,
    currentRoute: String?,
    onItemClick: (Any) -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                com.example.util.StudentAvatar(
                    name = user?.name ?: "Koç",
                    avatarUri = user?.avatarUri,
                    size = 52.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(user?.name ?: "Koç", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(user?.title ?: "Akademik Koç & Danışman", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (!user?.coachCode.isNullOrBlank()) {
                Surface(
                    color = com.example.ui.theme.SoftGoldSurface,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.Gold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Davet Kodu: ${user?.coachCode}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.Gold
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        }
        
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            DrawerItem("Komuta Merkezi", Icons.Default.Dashboard, currentRoute?.contains("CoachPanel") == true) { onItemClick(CoachPanel) }
            DrawerItem("Öğrenci Portföyüm", Icons.Default.People, currentRoute?.contains("StudentPortfolio") == true) { onItemClick(StudentPortfolio) }
            DrawerItem("Haftalık Program", Icons.Default.DateRange, currentRoute?.contains("WeeklyProgram") == true) { onItemClick(WeeklyProgram) }
            DrawerItem("Onay Merkezi", Icons.Default.FactCheck, currentRoute?.contains("ApprovalCenter") == true) { onItemClick(ApprovalCenter) }
            DrawerItem("Koçluk Görevleri", Icons.Default.ListAlt, currentRoute?.contains("CoachingTasks") == true) { onItemClick(CoachingTasks) }
            DrawerItem("Ödül Yönetimi", Icons.Default.EmojiEvents, currentRoute?.contains("RewardManagement") == true) { onItemClick(RewardManagement) }
            DrawerItem("Veli Görüşmeleri", Icons.Default.ContactPhone, currentRoute?.contains("ParentMeetings") == true) { onItemClick(ParentMeetings) }
            DrawerItem("AI Analiz & Raporlama", Icons.Default.AutoGraph, currentRoute?.contains("AnalyticsReport") == true) { onItemClick(AnalyticsReport) }
            DrawerItem("Hatırlatma Ayarları", Icons.Default.Alarm, currentRoute?.contains("ReminderSettings") == true) { onItemClick(ReminderSettings) }
            DrawerItem("Mesajlar", Icons.Default.Message, currentRoute?.contains("Messages") == true) { onItemClick(Messages) }
            DrawerItem("Koç Profili", Icons.Default.Person, currentRoute?.contains("Profile") == true) { onItemClick(Profile) }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            DrawerItem("Çıkış Yap", Icons.Default.ExitToApp, false) { onLogoutClick() }
        }

        Box(modifier = Modifier.padding(16.dp)) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                Text(
                    "AKTİF ROL: Coach",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun AppBottomBar(navController: NavController, currentRoute: String?, role: UserRole?, level: String?) {
    NavigationBar {
        if (role == UserRole.STUDENT) {
            NavigationBarItem(
                selected = currentRoute?.contains("StudentDashboard") == true,
                onClick = { navController.navigate(StudentDashboard(level ?: "")) },
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text("Ana Sayfa") }
            )
            NavigationBarItem(
                selected = currentRoute?.contains("WeeklyProgram") == true,
                onClick = { navController.navigate(WeeklyProgram) },
                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                label = { Text("Program") }
            )
            NavigationBarItem(
                selected = currentRoute?.contains("ExamResults") == true,
                onClick = { navController.navigate(ExamResults) },
                icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                label = { Text("Denemeler") }
            )
            NavigationBarItem(
                selected = currentRoute?.contains("RewardStore") == true,
                onClick = { navController.navigate(RewardStore) },
                icon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) },
                label = { Text("Mağaza") }
            )
        }
        NavigationBarItem(
            selected = currentRoute?.contains("Profile") == true,
            onClick = { navController.navigate(Profile) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profil") }
        )
    }
}

@Composable
fun CoachBottomBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute?.contains("CoachPanel") == true,
            onClick = { navController.navigate(CoachPanel) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text("Komuta") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("StudentPortfolio") == true,
            onClick = { navController.navigate(StudentPortfolio) },
            icon = { Icon(Icons.Default.People, contentDescription = null) },
            label = { Text("Portföy") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("ApprovalCenter") == true,
            onClick = { navController.navigate(ApprovalCenter) },
            icon = { Icon(Icons.Default.FactCheck, contentDescription = null) },
            label = { Text("Onaylar") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("Profile") == true,
            onClick = { navController.navigate(Profile) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profil") }
        )
    }
}

fun shouldShowBottomBar(route: String?): Boolean {
    if (route == null) return false
    val nonBottomScreens = listOf("Splash", "Login", "Register", "PackageSelection", "Payment", "SelectLevel", "FeatureLocked")
    return nonBottomScreens.none { route.contains(it) }
}

fun shouldShowDrawer(route: String?): Boolean {
    if (route == null) return false
    val noDrawerScreens = listOf("Splash", "Login", "Register", "PackageSelection", "Payment", "SelectLevel", "FeatureLocked")
    return noDrawerScreens.none { route.contains(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun AdminDrawerContent(
    user: com.example.model.User?,
    currentRoute: String?,
    onItemClick: (Any) -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                com.example.util.StudentAvatar(
                    name = user?.name ?: "Süper Admin",
                    avatarUri = user?.avatarUri,
                    size = 52.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(user?.name ?: "Süper Admin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Sistem Yöneticisi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            DrawerItem("Komuta Merkezi", Icons.Default.Dashboard, currentRoute?.contains("AdminPanel") == true) { onItemClick(AdminPanel) }
            DrawerItem("Kullanıcı Yönetimi", Icons.Default.People, currentRoute?.contains("AdminUserManagement") == true) { onItemClick(AdminUserManagement) }
            DrawerItem("Paket ve Fiyatlar", Icons.Default.WorkspacePremium, currentRoute?.contains("AdminPackageManagement") == true) { onItemClick(AdminPackageManagement) }
            DrawerItem("İndirim Kodları", Icons.Default.ConfirmationNumber, currentRoute?.contains("AdminCouponManagement") == true) { onItemClick(AdminCouponManagement) }
            DrawerItem("Finansal Raporlar", Icons.Default.Assessment, currentRoute?.contains("AdminFinancialReports") == true) { onItemClick(AdminFinancialReports) }
            DrawerItem("Veritabanı Yedekleme", Icons.Default.Backup, currentRoute?.contains("AdminBackup") == true) { onItemClick(AdminBackup) }
            DrawerItem("Hatırlatma Ayarları", Icons.Default.Alarm, currentRoute?.contains("ReminderSettings") == true) { onItemClick(ReminderSettings) }
            DrawerItem("Profil", Icons.Default.Person, currentRoute?.contains("Profile") == true) { onItemClick(Profile) }


            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DrawerItem("Çıkış Yap", Icons.Default.ExitToApp, false) { onLogoutClick() }
        }
    }
}

@Composable
fun AdminBottomBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute?.contains("AdminPanel") == true,
            onClick = { navController.navigate(AdminPanel) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text("Komuta") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("AdminUserManagement") == true,
            onClick = { navController.navigate(AdminUserManagement) },
            icon = { Icon(Icons.Default.People, contentDescription = null) },
            label = { Text("Üyeler") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("AdminPackageManagement") == true,
            onClick = { navController.navigate(AdminPackageManagement) },
            icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null) },
            label = { Text("Paketler") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("AdminFinancialReports") == true,
            onClick = { navController.navigate(AdminFinancialReports) },
            icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
            label = { Text("Finans") }
        )
    }
}
