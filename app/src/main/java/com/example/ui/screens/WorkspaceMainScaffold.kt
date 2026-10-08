package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationEntity
import com.example.i18n.t
import com.example.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.launch

enum class MainTab {
    CHAT, PROJECTS, SEARCH, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceMainScaffold(
    viewModel: WorkspaceViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(MainTab.CHAT) }

    val conversations by viewModel.conversations.collectAsState()
    val currentConvId by viewModel.currentConversationId.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()

    val currentConv = conversations.find { it.id == currentConvId }

    // System Back Handler
    BackHandler(enabled = currentTab != MainTab.CHAT || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            currentTab = MainTab.CHAT
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
            ) {
                DrawerContent(
                    viewModel = viewModel,
                    conversations = conversations,
                    currentConvId = currentConvId,
                    onSelectConversation = { convId ->
                        viewModel.selectConversation(convId)
                        currentTab = MainTab.CHAT
                        scope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        viewModel.startNewChat()
                        currentTab = MainTab.CHAT
                        scope.launch { drawerState.close() }
                    },
                    onNavigateTab = { tab ->
                        currentTab = tab
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (currentTab) {
                                    MainTab.CHAT -> currentConv?.title ?: t("app.name")
                                    MainTab.PROJECTS -> t("chat.projects")
                                    MainTab.SEARCH -> t("chat.search")
                                    MainTab.SETTINGS -> t("chat.settings")
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentTab == MainTab.CHAT && currentProject != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = currentProject?.name ?: "",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {
                        if (currentTab == MainTab.CHAT) {
                            IconButton(
                                onClick = { currentTab = MainTab.SEARCH },
                                modifier = Modifier.testTag("top_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = t("chat.search")
                                )
                            }
                            IconButton(
                                onClick = { viewModel.startNewChat() },
                                modifier = Modifier.testTag("top_new_chat_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = t("chat.newChat"),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.CHAT,
                        onClick = { currentTab = MainTab.CHAT },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.CHAT) Icons.Default.ChatBubble else Icons.Default.ChatBubbleOutline,
                                contentDescription = t("app.name")
                            )
                        },
                        label = { Text(t("app.name")) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("tab_chat")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.PROJECTS,
                        onClick = { currentTab = MainTab.PROJECTS },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.PROJECTS) Icons.Default.Folder else Icons.Default.FolderOpen,
                                contentDescription = t("chat.projects")
                            )
                        },
                        label = { Text(t("chat.projects")) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("tab_projects")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.SEARCH,
                        onClick = { currentTab = MainTab.SEARCH },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = t("chat.search")
                            )
                        },
                        label = { Text(t("chat.search")) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("tab_search")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.SETTINGS,
                        onClick = { currentTab = MainTab.SETTINGS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = t("chat.settings")
                            )
                        },
                        label = { Text(t("chat.settings")) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("tab_settings")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    MainTab.CHAT -> ChatScreen(viewModel = viewModel)
                    MainTab.PROJECTS -> ProjectsScreen(
                        viewModel = viewModel,
                        onOpenProjectChat = { currentTab = MainTab.CHAT }
                    )
                    MainTab.SEARCH -> SearchScreen(
                        viewModel = viewModel,
                        onSelectConversation = { currentTab = MainTab.CHAT }
                    )
                    MainTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    viewModel: WorkspaceViewModel,
    conversations: List<ConversationEntity>,
    currentConvId: String?,
    onSelectConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onNavigateTab: (MainTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // App Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Workspace",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Mobile-First AI Assistant",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // New Chat Button
        OutlinedButton(
            onClick = onNewChat,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("drawer_new_chat_btn")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = t("chat.newChat"), fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Spacer(modifier = Modifier.height(8.dp))

        // History Groups (วันนี้, เมื่อวาน, 7 วันที่ผ่านมา, เก่ากว่านั้น)
        val groupedHistory = remember(conversations) {
            groupConversations(conversations)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            groupedHistory.forEach { (categoryKey, convList) ->
                if (convList.isNotEmpty()) {
                    item {
                        Text(
                            text = t(categoryKey),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp, start = 4.dp)
                        )
                    }
                    items(convList, key = { it.id }) { conv ->
                        val isSelected = conv.id == currentConvId
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else Color.Transparent
                                )
                                .clickable { onSelectConversation(conv.id) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("drawer_conv_${conv.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = conv.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.deleteConversation(conv.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(8.dp))

        // Quick Bottom Links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(onClick = { onNavigateTab(MainTab.PROJECTS) }) {
                Icon(Icons.Default.Folder, contentDescription = t("chat.projects"))
            }
            IconButton(onClick = { onNavigateTab(MainTab.SEARCH) }) {
                Icon(Icons.Default.Search, contentDescription = t("chat.search"))
            }
            IconButton(onClick = { onNavigateTab(MainTab.SETTINGS) }) {
                Icon(Icons.Default.Settings, contentDescription = t("chat.settings"))
            }
        }
    }
}

private fun groupConversations(conversations: List<ConversationEntity>): Map<String, List<ConversationEntity>> {
    val now = System.currentTimeMillis()
    val oneDay = 86400_000L
    val sevenDays = oneDay * 7

    val today = mutableListOf<ConversationEntity>()
    val yesterday = mutableListOf<ConversationEntity>()
    val last7Days = mutableListOf<ConversationEntity>()
    val older = mutableListOf<ConversationEntity>()

    conversations.forEach { conv ->
        val diff = now - conv.updatedAt
        when {
            diff < oneDay -> today.add(conv)
            diff < oneDay * 2 -> yesterday.add(conv)
            diff < sevenDays -> last7Days.add(conv)
            else -> older.add(conv)
        }
    }

    return linkedMapOf(
        "history.today" to today,
        "history.yesterday" to yesterday,
        "history.last7Days" to last7Days,
        "history.older" to older
    )
}
