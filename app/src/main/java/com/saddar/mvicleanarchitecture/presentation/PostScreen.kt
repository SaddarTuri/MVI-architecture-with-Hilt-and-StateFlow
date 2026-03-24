package com.saddar.mvicleanarchitecture.presentation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saddar.mvicleanarchitecture.domain.model.Post

// ─────────────────────────────────────────────────────────────────────────────
// Color palette (add these to your Theme.kt / Color.kt instead if preferred)
// ─────────────────────────────────────────────────────────────────────────────
private val Indigo600  = Color(0xFF4F46E5)
private val Indigo100  = Color(0xFFE0E7FF)
private val Indigo50   = Color(0xFFEEF2FF)
private val Emerald600 = Color(0xFF059669)
private val Emerald100 = Color(0xFFD1FAE5)
private val Amber600   = Color(0xFFD97706)
private val Amber100   = Color(0xFFFEF3C7)
private val Rose600    = Color(0xFFE11D48)
private val Rose100    = Color(0xFFFFE4E6)
private val Sky600     = Color(0xFF0284C7)
private val Sky100     = Color(0xFFE0F2FE)
private val Slate50    = Color(0xFFF8FAFC)
private val Slate100   = Color(0xFFF1F5F9)
private val Slate200   = Color(0xFFE2E8F0)
private val Slate400   = Color(0xFF94A3B8)
private val Slate600   = Color(0xFF475569)
private val Slate800   = Color(0xFF1E293B)
private val Slate900   = Color(0xFF0F172A)

// Per-user avatar colors  (cycles through 10 users from jsonplaceholder)
private val avatarPalette = listOf(
    Indigo600  to Indigo100,
    Emerald600 to Emerald100,
    Amber600   to Amber100,
    Rose600    to Rose100,
    Sky600     to Sky100,
    Color(0xFF7C3AED) to Color(0xFFEDE9FE),  // Violet
    Color(0xFFDB2777) to Color(0xFFFCE7F3),  // Pink
    Color(0xFF0891B2) to Color(0xFFCFFAFE),  // Cyan
    Color(0xFF65A30D) to Color(0xFFECFCCB),  // Lime
    Color(0xFFEA580C) to Color(0xFFFFEDD5),  // Orange
)

private fun avatarColors(userId: Int): Pair<Color, Color> =
    avatarPalette[(userId - 1).coerceAtLeast(0) % avatarPalette.size]

private fun userInitials(userId: Int): String = "U$userId"

private val tagData = listOf("General", "Tech", "News", "Design", "Science", "Culture")
private fun postTag(postId: Int) = tagData[postId % tagData.size]

private val tagColors = mapOf(
    "General" to (Indigo600 to Indigo50),
    "Tech"    to (Sky600    to Sky100),
    "News"    to (Rose600   to Rose100),
    "Design"  to (Color(0xFF7C3AED) to Color(0xFFEDE9FE)),
    "Science" to (Emerald600 to Emerald100),
    "Culture" to (Amber600  to Amber100),
)

// ─────────────────────────────────────────────────────────────────────────────
// PostScreen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(viewModel: PostViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Local UI state (purely presentational — does NOT go into UiState)
    var searchQuery    by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var minPostId      by remember { mutableFloatStateOf(1f) }    // slider: filter by ID range
    var maxPostId      by remember { mutableFloatStateOf(100f) }  // slider: filter by ID range
    var showFilters    by remember { mutableStateOf(false) }

    // One-shot effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    // Derived filtered list — recomputed only when inputs change
    val filteredPosts by remember {
        derivedStateOf {
            uiState.posts.filter { post ->
                val userMatch  = selectedFilter == "All" || post.id.toString() == selectedFilter.removePrefix("User ")
                val textMatch  = searchQuery.isEmpty() || post.title.contains(searchQuery, true) || post.body.contains(searchQuery, true)
                val rangeMatch = post.id in minPostId.toInt()..maxPostId.toInt()
                userMatch && textMatch && rangeMatch
            }
        }
    }

    Scaffold(
        containerColor = Slate50,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Slate900,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    Column {
                        Text(
                            "Posts Feed",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 17.sp,
                            color      = Color.White
                        )
                        Text(
                            "jsonplaceholder.typicode.com",
                            fontSize = 11.sp,
                            color    = Slate400
                        )
                    }
                },
                actions = {
                    // Filter toggle
                    TextButton(
                        onClick = { showFilters = !showFilters },
                        colors  = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text(
                            if (showFilters) "Hide filters" else "Filters",
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { viewModel.handleIntent(UiIntent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // ── Loading ────────────────────────────────────────────────
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Indigo600, strokeWidth = 3.dp)
                        Text("Loading posts...", color = Slate600, fontSize = 14.sp)
                    }
                }

                // ── Error ──────────────────────────────────────────────────
                uiState.error != null -> {
                    ErrorView(message = uiState.error!!) {
                        viewModel.handleIntent(UiIntent.Refresh)
                    }
                }

                // ── Content ────────────────────────────────────────────────
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {

                        // Search bar
                        item {
                            SearchBar(
                                query    = searchQuery,
                                onChange = { searchQuery = it }
                            )
                        }

                        // Expandable filter panel (slider + user chips)
                        item {
                            AnimatedVisibility(
                                visible = showFilters,
                                enter   = fadeIn() + slideInVertically(),
                                exit    = fadeOut()
                            ) {
                                FilterPanel(
                                    posts          = uiState.posts,
                                    selectedFilter = selectedFilter,
                                    onFilterSelect = { selectedFilter = it },
                                    minPostId      = minPostId,
                                    maxPostId      = maxPostId,
                                    onRangeChange  = { min, max -> minPostId = min; maxPostId = max }
                                )
                            }
                        }

                        // Stats row
                        item {
                            StatsRow(
                                total   = uiState.posts.size,
                                authors = uiState.posts.map { it.id }.distinct().size,
                                showing = filteredPosts.size
                            )
                        }

                        // Section header
                        item {
                            SectionHeader(
                                label = "POSTS",
                                count = filteredPosts.size
                            )
                        }

                        // Post cards
                        items(filteredPosts, key = { it.id }) { post ->
                            AnimatedVisibility(
                                visible = true,
                                enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
                            ) {
                                PostCard(
                                    post    = post,
                                    onClick = { viewModel.handleIntent(UiIntent.SelectPost(post)) }
                                )
                            }
                        }

                        // Empty state
                        item {
                            if (filteredPosts.isEmpty()) {
                                EmptyState(query = searchQuery)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search Bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SearchBar(query: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value           = query,
        onValueChange   = onChange,
        placeholder     = { Text("Search by title or body…", fontSize = 13.sp, color = Slate400) },
        leadingIcon     = { Icon(Icons.Default.Search, null, tint = Slate400, modifier = Modifier.size(18.dp)) },
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        singleLine      = true,
        shape           = RoundedCornerShape(14.dp),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Indigo600,
            unfocusedBorderColor = Slate200,
            focusedContainerColor   = Color.White,
            unfocusedContainerColor = Color.White,
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Slate800, fontSize = 14.sp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter Panel  (user chips + ID range slider)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FilterPanel(
    posts: List<Post>,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    minPostId: Float,
    maxPostId: Float,
    onRangeChange: (Float, Float) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape  = RoundedCornerShape(16.dp),
        color  = Color.White,
        shadowElevation = 1.dp,
        tonalElevation  = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── User filter chips ─────────────────────────────────────────
            Text("Filter by user", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate600)
            Spacer(Modifier.height(8.dp))
            val userFilters = listOf("All") + posts.map { "User ${it.id}" }.distinct().sorted()
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(userFilters) { filter ->
                    val (fg, bg) = if (filter == "All") {
                        Color.White to Indigo600
                    } else {
                        val uid = filter.removePrefix("User ").toIntOrNull() ?: 1
                        val (icon, _) = avatarColors(uid)
                        Color.White to icon
                    }
                    FilterChip(
                        selected = filter == selectedFilter,
                        onClick  = { onFilterSelect(filter) },
                        label    = {
                            Text(filter, fontSize = 12.sp,
                                fontWeight = if (filter == selectedFilter) FontWeight.SemiBold else FontWeight.Normal)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor   = if (filter == selectedFilter) Indigo600 else Color.Transparent,
                            selectedLabelColor       = Color.White,
                            containerColor           = Slate100,
                            labelColor               = Slate600
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled          = true,
                            selected         = filter == selectedFilter,
                            borderColor      = Slate200,
                            selectedBorderColor = Indigo600
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Slate100)
            Spacer(Modifier.height(14.dp))

            // ── ID range slider ───────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Post ID range", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate600)
                Spacer(Modifier.weight(1f))
                Text(
                    "#${minPostId.toInt()} – #${maxPostId.toInt()}",
                    fontSize   = 12.sp,
                    color      = Indigo600,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(4.dp))

            // Min slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Min", fontSize = 11.sp, color = Slate400, modifier = Modifier.width(28.dp))
                Slider(
                    value         = minPostId,
                    onValueChange = { new ->
                        if (new < maxPostId) onRangeChange(new, maxPostId)
                    },
                    valueRange    = 1f..100f,
                    steps         = 98,
                    modifier      = Modifier.weight(1f),
                    colors        = SliderDefaults.colors(
                        thumbColor              = Indigo600,
                        activeTrackColor        = Indigo600,
                        inactiveTrackColor      = Indigo100
                    )
                )
            }

            // Max slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Max", fontSize = 11.sp, color = Slate400, modifier = Modifier.width(28.dp))
                Slider(
                    value         = maxPostId,
                    onValueChange = { new ->
                        if (new > minPostId) onRangeChange(minPostId, new)
                    },
                    valueRange    = 1f..100f,
                    steps         = 98,
                    modifier      = Modifier.weight(1f),
                    colors        = SliderDefaults.colors(
                        thumbColor              = Emerald600,
                        activeTrackColor        = Emerald600,
                        inactiveTrackColor      = Emerald100
                    )
                )
            }

            // Visual range progress bar
            Spacer(Modifier.height(4.dp))
            val progress by animateFloatAsState(
                targetValue = (maxPostId - minPostId) / 99f,
                label       = "range"
            )
            LinearProgressIndicator(
                progress          = { progress },
                modifier          = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color             = Indigo600,
                trackColor        = Indigo100,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${(maxPostId - minPostId).toInt() + 1} posts in range",
                fontSize = 11.sp,
                color    = Slate400
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stats Row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StatsRow(total: Int, authors: Int, showing: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard("Total",   "$total",   "posts",   Indigo600,  Indigo50,  Modifier.weight(1f))
        StatCard("Authors", "$authors", "users",   Emerald600, Emerald100, Modifier.weight(1f))
        StatCard("Showing", "$showing", "filtered",Sky600,     Sky100,    Modifier.weight(1f))
    }
}

@Composable
fun StatCard(
    label:    String,
    value:    String,
    sub:      String,
    accent:   Color,
    bg:       Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Accent bar
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Slate400)
            Text(sub,   fontSize = 10.sp, color = accent)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Slate400,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Indigo50)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("$count", fontSize = 11.sp, color = Indigo600, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Post Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PostCard(post: Post, onClick: () -> Unit) {
    val (avatarFg, avatarBg) = avatarColors(post.id)
    val tag                  = postTag(post.id)
    val (tagFg, tagBg)       = tagColors[tag] ?: (Slate600 to Slate100)

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ── Header row ────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userInitials(post.id),
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = avatarFg
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "User #${post.id}",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Slate800
                    )
                    Text(
                        "Post #${post.id}  ·  just now",
                        fontSize = 11.sp,
                        color    = Slate400
                    )
                }

                // Tag badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tagBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(tag, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = tagFg)
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Title ─────────────────────────────────────────────────────
            Text(
                text     = post.title.replaceFirstChar { it.uppercase() },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color    = Slate900,
                maxLines = 2,
                lineHeight = 20.sp,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(5.dp))

            // ── Body ──────────────────────────────────────────────────────
            Text(
                text     = post.body,
                fontSize = 12.sp,
                color    = Slate600,
                maxLines = 2,
                lineHeight = 18.sp,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Slate100)
            Spacer(Modifier.height(10.dp))

            // ── Footer row ────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like count pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Rose100)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "♡  ${(post.id * 7) % 50 + 1}",
                        fontSize = 11.sp,
                        color    = Rose600,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Comment count pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Sky100)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "💬  ${(post.id * 3) % 20 + 1}",
                        fontSize = 11.sp,
                        color    = Sky600,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.weight(1f))

                // Share button
                TextButton(
                    onClick = {},
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "↗ Share",
                        fontSize = 11.sp,
                        color    = Slate400,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error View
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Rose100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint     = Rose600,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                "Something went wrong",
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Slate900
            )
            Text(
                message,
                fontSize = 13.sp,
                color    = Slate400,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = onRetry,
                colors  = ButtonDefaults.buttonColors(containerColor = Indigo600),
                shape   = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Try again", fontSize = 13.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun EmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("No results", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Slate600)
        if (query.isNotEmpty()) {
            Text(
                "No posts matched \"$query\"",
                fontSize = 13.sp,
                color = Slate400
            )
        } else {
            Text("Try adjusting the filters above", fontSize = 13.sp, color = Slate400)
        }
    }
}