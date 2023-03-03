package dev.sdex.configviewer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sdex.configviewer.model.Setting
import dev.sdex.configviewer.model.SettingsFile
import dev.sdex.configviewer.ui.theme.ConfigViewerTheme
import dev.sdex.configviewer.ui.theme.Purple40

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    ConfigViewerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.currentSettingsFile
                                .replaceFirstChar { it.uppercaseChar() }
                        )
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple40),
                    actions = {
                        IconButton(
                            onClick = { isMenuExpanded = true }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = null
                            )
                        }
                        DropdownMenu(
                            modifier = Modifier.defaultMinSize(minWidth = 180.dp),
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(text = {
                                Text("Config")
                            }, onClick = {
                                isMenuExpanded = false
                                viewModel.load(SettingsFile.CONFIG)
                            })
                            DropdownMenuItem(text = {
                                Text("Global")
                            }, onClick = {
                                isMenuExpanded = false
                                viewModel.load(SettingsFile.GLOBAL)
                            })
                            DropdownMenuItem(text = {
                                Text("Secure")
                            }, onClick = {
                                isMenuExpanded = false
                                viewModel.load(SettingsFile.SECURE)
                            })
                            DropdownMenuItem(text = {
                                Text("System")
                            }, onClick = {
                                isMenuExpanded = false
                                viewModel.load(SettingsFile.SYSTEM)
                            })
                        }
                    }
                )
            },
        ) { paddingValues ->
            val modifier = Modifier.padding(paddingValues)
            if (uiState.items.isNotEmpty()) {
                LazyColumn(
                    modifier = modifier,
                    state = rememberLazyListState(),
                ) {
                    uiState.items.forEach {
                        stickyHeader {
                            AppHeader(Modifier, it.packageName)
                        }
                        items(it.items) { setting ->
                            SettingItem(Modifier, setting) {
                                viewModel.showSettingDetail(setting)
                            }
                        }
                    }
                }
            }
            if (uiState.currentSetting != null) {
                ItemDetail(uiState.currentSetting!!) {
                    viewModel.showSettingDetail(null)
                }
            }
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
    Column(
        modifier = modifier.padding(horizontal = 8.dp)
            .clickable { onClick(setting) },
    ) {
        Text(
            text = setting.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier.height(8.dp))
        Text(

            text = setting.getValueSafe(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Divider()
    }
}

@Composable
fun ItemDetail(
    setting: Setting,
    onClose: () -> Unit,
) {
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
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = setting.name,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    modifier = Modifier.padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    text = setting.getValueSafe(),
                    style = MaterialTheme.typography.bodyMedium
                )
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
