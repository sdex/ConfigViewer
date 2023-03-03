package dev.sdex.configviewer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sdex.configviewer.model.Setting
import dev.sdex.configviewer.model.Settings
import dev.sdex.configviewer.model.SettingsFile
import dev.sdex.configviewer.ui.theme.ConfigViewerTheme
import dev.sdex.configviewer.utils.copyText
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    ConfigViewerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.title.replaceFirstChar { it.uppercaseChar() }
                        )
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(),
                    actions = {
                        TopBarActions { settingsFile ->
                            viewModel.load(settingsFile)
                        }
                    }
                )
            },
        ) { paddingValues ->
            val modifier = Modifier.padding(paddingValues)
            if (uiState.items.isNotEmpty()) {
                SettingsList(modifier, uiState.items, uiState.currentSettingsFile) { setting ->
                    viewModel.showSettingDetail(setting)
                }
            }
            if (uiState.error != null) {
                ErrorMessage(uiState.error!!)
            }
            if (uiState.currentSetting != null) {
                ItemDetail(uiState.currentSetting!!) {
                    viewModel.showSettingDetail(null)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsList(
    modifier: Modifier,
    items: List<Settings>,
    settingsFile: SettingsFile?,
    showSettingDetail: (Setting) -> Unit
) {
    var lastSettingsFile: SettingsFile? by rememberSaveable {
        mutableStateOf(null)
    }
    val listState: LazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        modifier = modifier,
        state = listState,
    ) {
        items.forEach {
            stickyHeader {
                AppHeader(Modifier, it.packageName)
            }
            items(it.items) { setting ->
                SettingItem(Modifier, setting) {
                    showSettingDetail(setting)
                }
            }
        }
    }
    SideEffect {
        coroutineScope.launch {
            if (lastSettingsFile != settingsFile) {
                lastSettingsFile = settingsFile
                listState.scrollToItem(0)
            }
        }
    }
}

@Composable
fun TopBarActions(
    selectFile: (SettingsFile) -> Unit
) {
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    IconButton(
        onClick = { isMenuExpanded = true }
    ) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = null
        )
        DropdownMenu(
            modifier = Modifier.defaultMinSize(minWidth = 180.dp),
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            DropdownMenuItem(text = {
                Text("Config")
            }, onClick = {
                isMenuExpanded = false
                selectFile(SettingsFile.CONFIG)
            })
            DropdownMenuItem(text = {
                Text("Global")
            }, onClick = {
                isMenuExpanded = false
                selectFile(SettingsFile.GLOBAL)
            })
            DropdownMenuItem(text = {
                Text("Secure")
            }, onClick = {
                isMenuExpanded = false
                selectFile(SettingsFile.SECURE)
            })
            DropdownMenuItem(text = {
                Text("System")
            }, onClick = {
                isMenuExpanded = false
                selectFile(SettingsFile.SYSTEM)
            })
        }
    }
}

@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    packageName: String,
) {
    Box(
        modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            modifier = modifier.padding(horizontal = 8.dp),
            text = packageName,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    setting: Setting,
    onClick: (setting: Setting) -> Unit,
) {
    Box(modifier = modifier.clickable { onClick(setting) }) {
        Column(
            modifier = modifier.padding(horizontal = 8.dp)
        ) {
            Spacer(modifier.height(4.dp))
            Text(
                text = setting.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier.height(8.dp))
            Text(
                text = setting.getValueSafe(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier.height(4.dp))
            Divider()
        }
    }
}

@Composable
fun ItemDetail(
    setting: Setting,
    onClose: () -> Unit,
) {
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    Dialog(
        onDismissRequest = { onClose() },
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column {
                Box(
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ).fillMaxWidth()
                ) {
                    Row {
                        Text(
                            modifier = Modifier.padding(16.dp).weight(1f, fill = true),
                            text = setting.name,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(
                            modifier = Modifier.padding(top = 8.dp),
                            onClick = { isMenuExpanded = true },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = null,
                            )
                            DropdownMenu(
                                modifier = Modifier.defaultMinSize(minWidth = 180.dp),
                                expanded = isMenuExpanded,
                                onDismissRequest = { isMenuExpanded = false }
                            ) {
                                DropdownMenuItem(text = {
                                    Text("Copy name")
                                }, onClick = {
                                    copyText(context, setting.name)
                                    isMenuExpanded = false
                                })
                                DropdownMenuItem(text = {
                                    Text("Copy value")
                                }, onClick = {
                                    copyText(context, setting.getValueSafe())
                                    isMenuExpanded = false
                                })
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    Text(
                        text = setting.getValueSafe(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Box(modifier = Modifier.align(Alignment.End)) {
                    TextButton(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        onClick = { onClose() }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            text = message
        )
    }
}
