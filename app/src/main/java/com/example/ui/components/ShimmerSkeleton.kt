package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Returns an animated Brush for subtle shimmer skeleton loading.
 * Dynamically adjusts color gradient according to the current MaterialTheme color scheme.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val isDark = isSystemInDarkTheme() || MaterialTheme.colorScheme.surface.let {
        // Simple luminance heuristic to detect dark background
        (it.red * 0.299f + it.green * 0.587f + it.blue * 0.114f) < 0.5f
    }

    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF1E293B),
            Color(0xFF334155),
            Color(0xFF1E293B)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0)
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 400f, y = translateAnim - 400f),
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

/**
 * Modifier extension to apply shimmer skeleton background with optional rounded clipping.
 */
@Composable
fun Modifier.shimmerBackground(shape: Shape = RoundedCornerShape(8.dp)): Modifier {
    val brush = rememberShimmerBrush()
    return this
        .clip(shape)
        .background(brush)
}

/**
 * Skeleton placeholder for a single text or badge line.
 */
@Composable
fun ShimmerLine(
    widthFraction: Float = 1f,
    height: Dp = 14.dp,
    shape: Shape = RoundedCornerShape(6.dp),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .shimmerBackground(shape)
    )
}

/**
 * Subtle Skeleton Loading Card for a Business / Store Hero Banner.
 */
@Composable
fun BusinessCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header row with avatar, title, and status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Business Icon / Avatar Skeleton
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shimmerBackground(RoundedCornerShape(10.dp))
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShimmerLine(widthFraction = 0.55f, height = 18.dp, shape = RoundedCornerShape(6.dp))
                        ShimmerLine(widthFraction = 0.35f, height = 12.dp, shape = RoundedCornerShape(4.dp))
                    }
                }
                // Status pill skeleton
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(26.dp)
                        .shimmerBackground(RoundedCornerShape(14.dp))
                )
            }

            // Description skeleton (2 lines)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShimmerLine(widthFraction = 0.95f, height = 12.dp)
                ShimmerLine(widthFraction = 0.75f, height = 12.dp)
            }

            // Bottom info chips row (Rating, City, Hours)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(24.dp)
                        .shimmerBackground(RoundedCornerShape(8.dp))
                )
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(24.dp)
                        .shimmerBackground(RoundedCornerShape(8.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .shimmerBackground(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

/**
 * Subtle Skeleton Loading for a Business Directory list item.
 */
@Composable
fun BusinessListItemSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Store Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shimmerBackground(RoundedCornerShape(10.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerLine(widthFraction = 0.6f, height = 15.dp)
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(18.dp)
                            .shimmerBackground(RoundedCornerShape(6.dp))
                    )
                }
                ShimmerLine(widthFraction = 0.85f, height = 12.dp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(16.dp)
                            .shimmerBackground(RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(16.dp)
                            .shimmerBackground(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

/**
 * Subtle Skeleton Loading Card for a Catalog Product or Service item.
 */
@Composable
fun CatalogItemSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Item Icon / Thumbnail
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shimmerBackground(RoundedCornerShape(10.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerLine(widthFraction = 0.5f, height = 15.dp)
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(18.dp)
                            .shimmerBackground(RoundedCornerShape(6.dp))
                    )
                }

                ShimmerLine(widthFraction = 0.85f, height = 11.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerLine(widthFraction = 0.35f, height = 16.dp)
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(28.dp)
                            .shimmerBackground(RoundedCornerShape(14.dp))
                    )
                }
            }
        }
    }
}

/**
 * Full business list skeleton placeholder showing a header and multiple business item skeletons.
 */
@Composable
fun BusinessListSkeleton(
    count: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Subtle indicator header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .shimmerBackground(CircleShape)
            )
            ShimmerLine(widthFraction = 0.4f, height = 14.dp)
        }

        repeat(count) {
            BusinessListItemSkeleton()
        }
    }
}
