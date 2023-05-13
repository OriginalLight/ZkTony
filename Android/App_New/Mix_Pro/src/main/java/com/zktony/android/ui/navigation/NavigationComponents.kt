package com.zktony.android.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.sp
import com.zktony.android.R

/**
 * NavigationRailItem
 *
 * @param selectedDestination String
 * @param navigateToTopLevelDestination Function1<TopLevelDestination, Unit>
 * @param onDrawerClicked Function0<Unit>
 */
@Composable
fun AppNavigationRail(
    selectedDestination: String,
    navigateToTopLevelDestination: (TopLevelDestination) -> Unit,
    onDrawerClicked: () -> Unit = {},
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.inverseOnSurface
    ) {
        // TODO remove custom nav rail positioning when NavRail component supports it. ticket : b/232495216
        Layout(
            modifier = Modifier.widthIn(max = 80.dp), content = {
                Column(
                    modifier = Modifier.layoutId(LayoutType.HEADER),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    NavigationRailItem(selected = false, onClick = onDrawerClicked, icon = {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Filled.Menu,
                            contentDescription = null
                        )
                    })

                    Spacer(Modifier.height(16.dp))
                }

                Column(
                    modifier = Modifier.layoutId(LayoutType.CONTENT),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TOP_LEVEL_DESTINATIONS.forEach { destination ->
                        NavigationRailItem(selected = selectedDestination == destination.route,
                            onClick = { navigateToTopLevelDestination(destination) },
                            icon = {
                                Image(
                                    modifier = Modifier.size(36.dp),
                                    painter = painterResource(id = destination.iconId),
                                    contentDescription = stringResource(id = destination.iconTextId),
                                )
                            })
                    }
                }
            },
            measurePolicy = navigationMeasurePolicy()
        )
    }
}

/**
 * Navigation drawer content for permanent drawer
 *
 * @param selectedDestination String
 * @param navigateToTopLevelDestination Function1<TopLevelDestination, Unit>
 * @param onDrawerClicked Function0<Unit>
 */
@Composable
fun PermanentNavigationDrawerContent(
    selectedDestination: String,
    navigateToTopLevelDestination: (TopLevelDestination) -> Unit,
    onDrawerClicked: () -> Unit = {},
) {
    PermanentDrawerSheet(
        modifier = Modifier.sizeIn(minWidth = 200.dp, maxWidth = 200.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        Layout(
            content = {
                Column(
                    modifier = Modifier.layoutId(LayoutType.HEADER),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.White, Color.LightGray.copy(alpha = 0.5f)
                                    )
                                )
                            )
                            .clickable { onDrawerClicked() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .layoutId(LayoutType.CONTENT)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    TOP_LEVEL_DESTINATIONS.forEach { destination ->
                        NavigationDrawerItem(selected = selectedDestination == destination.route,
                            label = {
                                Text(
                                    text = stringResource(id = destination.iconTextId),
                                    fontSize = 22.sp,
                                    lineHeight = 28.sp,
                                    maxLines = 1,
                                )
                            },
                            icon = {
                                Image(
                                    modifier = Modifier.size(48.dp),
                                    painter = painterResource(id = destination.iconId),
                                    contentDescription = stringResource(id = destination.iconTextId),
                                )
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                            ),
                            shape = CutCornerShape(topEnd = 36.dp, bottomStart = 16.dp),
                            onClick = { navigateToTopLevelDestination(destination) })
                    }
                }
            },
            measurePolicy = navigationMeasurePolicy()
        )
    }
}

fun navigationMeasurePolicy(): MeasurePolicy {
    return MeasurePolicy { measurables, constraints ->
        lateinit var headerMeasurable: Measurable
        lateinit var contentMeasurable: Measurable
        measurables.forEach {
            when (it.layoutId) {
                LayoutType.HEADER -> headerMeasurable = it
                LayoutType.CONTENT -> contentMeasurable = it
                else -> error("Unknown layoutId encountered!")
            }
        }

        val headerPlaceable = headerMeasurable.measure(constraints)
        val contentPlaceable = contentMeasurable.measure(
            constraints.offset(vertical = -headerPlaceable.height)
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Place the header, this goes at the top
            headerPlaceable.placeRelative(0, 0)

            contentPlaceable.placeRelative(0, headerPlaceable.height)
        }
    }
}

enum class LayoutType {
    HEADER, CONTENT
}
