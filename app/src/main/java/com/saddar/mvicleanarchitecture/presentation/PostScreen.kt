package com.saddar.mvicleanarchitecture.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saddar.mvicleanarchitecture.domain.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(viewModel: PostViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Posts Feed", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "jsonplaceholder.typicode.com",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.handleIntent(UiIntent.Refresh) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> ErrorView(uiState.error!!) {
                    viewModel.handleIntent(UiIntent.Refresh)
                }
                else -> {
                    val filtered = uiState.posts.filter { post ->
                        (selectedFilter == "All" || post.id.toString() == selectedFilter
                            .removePrefix("User ")) &&
                                (searchQuery.isEmpty() || post.title.contains(searchQuery, true))
                    }

                    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {

                        // Search bar
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search posts...") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Stats row
                        item {
                            StatsRow(
                                total = uiState.posts.size,
                                authors = uiState.posts.map { it.id }.distinct().size
                            )
                        }

                        // Filter chips
                        item {
                            val filters = listOf("All") +
                                    uiState.posts.map { "User ${it.id}" }.distinct().take(4)
                            FilterChipRow(
                                filters = filters,
                                selected = selectedFilter,
                                onSelect = { selectedFilter = it }
                            )
                        }

                        // Section label
                        item {
                            Text(
                                "RECENT POSTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Post cards
                        items(filtered, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                onClick = { viewModel.handleIntent(UiIntent.SelectPost(post)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorView(x0: String, content: @Composable () -> Unit) {
    TODO("Not yet implemented")
}

@Composable
fun StatsRow(total: Int, authors: Int) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard("Total posts", "$total", "+12 today", Modifier.weight(1f))
        StatCard("Authors", "$authors", "users", Modifier.weight(1f))
        StatCard("Cached", "100%", "in memory", Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(sub, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun FilterChipRow(filters: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(filter, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Composable
fun PostCard(post: Post, onClick: () -> Unit) {
    // Avatar initials color from userId
    val avatarColor = when (post.id % 3) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Header: avatar + user + post id badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text("U${post.id}", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("User #${post.id}", style = MaterialTheme.typography.labelMedium)
                    Text("Just now", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Badge { Text("#${post.id}") }
            }

            Spacer(Modifier.height(8.dp))
            Text(post.title, style = MaterialTheme.typography.titleSmall, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(post.body, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
                overflow = TextOverflow.Ellipsis)

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = {
                    Text("General", style = MaterialTheme.typography.labelSmall)
                })
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {}) { Text("♡  ${post.id % 50}", style = MaterialTheme.typography.labelSmall) }
                TextButton(onClick = {}) { Text("↗  Share", style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}