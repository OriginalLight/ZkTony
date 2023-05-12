package com.zktony.android.ui.navigation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
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
        Layout(modifier = Modifier.widthIn(max = 80.dp), content = {
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
        }, measurePolicy = { measurables, constraints ->
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
        })
    }
}

/**
 * Navigation drawer content for permanent drawer
 *
 * @param selectedDestination String
 * @param navigateToTopLevelDestination Function1<TopLevelDestination, Unit>
 * @param onDrawerClicked Function0<Unit>
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        Layout(content = {
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
                                modifier = Modifier.size(36.dp),
                                painter = painterResource(id = destination.iconId),
                                contentDescription = stringResource(id = destination.iconTextId),
                            )
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(
                            topStart = 0.dp, topEnd = 32.dp, bottomStart = 0.dp, bottomEnd = 0.dp
                        ),
                        onClick = { navigateToTopLevelDestination(destination) })
                }
            }
        }, measurePolicy = { measurables, constraints ->
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
        })
    }
}

enum class LayoutType {
    HEADER, CONTENT
}
