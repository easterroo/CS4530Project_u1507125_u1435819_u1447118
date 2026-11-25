package com.example.drawingapp.library

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drawingapp.DrawingViewModelProvider
import com.example.drawingapp.auth.AuthDialog
import com.example.drawingapp.auth.AuthViewModel

/**
 * Main library screen that allows you to add, edit, and delete drawings
 *
 * @param myVM ViewModel used to retrieve the drawings to display
 * @param authViewModel ViewModel used for user authentication
 * @param onOpenDrawing Calls the correct navigation path when clicked
 * @param onNewDrawing Calls the correct navigation to new drawing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    myVM: LibraryViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(factory = DrawingViewModelProvider.Factory),
    onOpenDrawing: (Int) -> Unit = {},
    onNewDrawing: () -> Unit = {}
) {
    val drawings by myVM.drawingsSnapshots.collectAsStateWithLifecycle()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAuthDialog by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) myVM.importImage(uri, context) }

    if (showAuthDialog) {
        AuthDialog(
            authViewModel = authViewModel,
            onDismiss = { showAuthDialog = false },
            onAuthSuccess = {
                myVM.syncCloudDrawings(context)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color.LightGray
                ),
                title = {
                    Column {
                        Text(
                            if (authState.isLoggedIn && !authState.userEmail.isNullOrBlank()) {
                                "My Drawings (${authState.userEmail})"
                            } else {
                                "My Drawings"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = onNewDrawing) { Text("New Drawing") }
                            TextButton(onClick = {
                                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }) { Text("Import Image") }

                            if (authState.isLoggedIn) {
                                TextButton(onClick = { myVM.syncCloudDrawings(context) }) {
                                    Text("Cloud Sync")
                                }
                                TextButton(onClick = { authViewModel.signOut() }) {
                                    Text("Sign Out")
                                }
                            } else {
                                TextButton(onClick = { showAuthDialog = true }) {
                                    Text("Sign In")
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(drawings.entries.toList(), key = { entry -> entry.key }) { drawing ->
                DrawingCard(
                    thumbnail = drawing.value.copy(),
                    title = myVM.getDrawing(drawing.key).title,
                    onOpen = { onOpenDrawing(drawing.key) },
                    onDelete = { myVM.deleteDrawing(drawing.key, context) },
                    onShare = { myVM.shareDrawing(drawing.key, context) }
                )
            }
        }
    }
}

/**
 * Helper method that creates a drawing card that is duplicated
 * on the library screen
 *
 * @param thumbnail Image to display on card
 * @param title The title of the drawing
 * @param onOpen When card is clicked, opens it up on the canvas
 * @param onDelete When "Delete" is selected from the menu, it is deleted
 */
@Composable
private fun DrawingCard(
    thumbnail: ImageBitmap,
    title: String,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Column {
        // Card with the selectable thumbnail and menu option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .testTag("drawingCard")
                .clickable(onClick = onOpen)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = thumbnail,
                    contentDescription = title,
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.fillMaxSize()
                )
                Box(Modifier.align(Alignment.TopEnd)) {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("dropdownMenu")
                    ) {
                        Text("⋮")
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export") },
                            onClick = {
                                menuOpen = false
                                onShare()
                            }
                        )
                    }
                }
            }
        }
        // Text showing the drawing's title
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

/**
 * Creates a mutable copy of an ImageBitmap so modifications do not alter the cached original.
 */
fun ImageBitmap.copy(): ImageBitmap {
    val androidBitmap = this.asAndroidBitmap()
    val config = androidBitmap.config ?: Bitmap.Config.ARGB_8888
    return androidBitmap.copy(config, true).asImageBitmap()
}
