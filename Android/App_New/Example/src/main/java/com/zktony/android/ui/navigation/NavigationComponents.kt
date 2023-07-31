package com.zktony.android.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.ui.utils.NavigationContentPosition

/**
 * NavigationRailItem
 *
 * @param selectedDestination String
 * @param navigationContentPosition NavigationContentPosition
 * @param navigateToTopLevelDestination Function1<TopLevelDestination, Unit>
 * @param onDrawerClicked Function0<Unit>
 */
@Composable
fun AppNavigationRail(
    selectedDestination: String,
    navigationContentPosition: NavigationContentPosition,
    navigateToTopLevelDestination: (TopLevelDestination) -> Unit,
    onDrawerClicked: () -> Unit = {},
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = Color.Transparent,
    ) {
        Layout(
            modifier = Modifier.widthIn(max = 80.dp),
            content = {
                Column(
                    modifier = Modifier.layoutId(LayoutType.HEADER),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(16.dp))

                    NavigationRailItem(
                        selected = false, onClick = onDrawerClicked, icon = {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                imageVector = Icons.Filled.Menu,
                                contentDescription = null
                            )
                        })
                }

                Column(
                    modifier = Modifier.layoutId(LayoutType.CONTENT),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TOP_LEVEL_DESTINATIONS.forEach { destination ->
                        NavigationRailItem(
                            selected = selectedDestination == destination.route,
                            onClick = { navigateToTopLevelDestination(destination) },
                            icon = {
                                Image(
                                    modifier = Modifier.size(32.dp),
                                    painter = painterResource(id = destination.imageId),
                                    contentDescription = stringResource(id = destination.iconTextId),
                                )
                            }
                        )
                    }
                }
            },
            measurePolicy = navigationMeasurePolicy(navigationContentPosition)
        )
    }
}

/**
 * Navigation drawer content for permanent drawer
 *
 * @param selectedDestination String
 * @param navigationContentPosition NavigationContentPosition
 * @param navigateToTopLevelDestination Function1<TopLevelDestination, Unit>
 * @param onDrawerClicked Function0<Unit>
 */
@Composable
fun PermanentNavigationDrawerContent(
    selectedDestination: String,
    navigationContentPosition: NavigationContentPosition,
    navigateToTopLevelDestination: (TopLevelDestination) -> Unit,
    onDrawerClicked: () -> Unit = {},
) {
    PermanentDrawerSheet(
        modifier = Modifier.sizeIn(minWidth = 200.dp, maxWidth = 200.dp),
        drawerContainerColor = Color.Transparent
    ) {
        Layout(
            modifier = Modifier.padding(16.dp),
            content = {
                Column(
                    modifier = Modifier.layoutId(LayoutType.HEADER),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { onDrawerClicked() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Icon(
                            modifier = Modifier.size(32.dp),
                            imageVector = Icons.Default.MenuOpen,
                            contentDescription = null
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
                        NavigationDrawerItem(
                            selected = selectedDestination == destination.route,
                            label = {
                                Text(
                                    text = stringResource(id = destination.iconTextId),
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            },
                            icon = {
                                Image(
                                    modifier = Modifier.size(32.dp),
                                    painter = painterResource(id = destination.imageId),
                                    contentDescription = stringResource(id = destination.iconTextId),
                                )
                            },
                            onClick = { navigateToTopLevelDestination(destination) })
                    }
                }
            },
            measurePolicy = navigationMeasurePolicy(navigationContentPosition)
        )
    }
}

/**
 * Different position of navigation content inside Navigation Rail, Navigation Drawer depending on device size and state.
 */
fun navigationMeasurePolicy(
    navigationContentPosition: NavigationContentPosition,
): MeasurePolicy {
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

            // Determine how much space is not taken up by the content
            val nonContentVerticalSpace = constraints.maxHeight - contentPlaceable.height

            val contentPlaceableY = when (navigationContentPosition) {
                // Figure out the place we want to place the content, with respect to the
                // parent (ignoring the header for now)
                NavigationContentPosition.TOP -> 0
                NavigationContentPosition.CENTER -> nonContentVerticalSpace / 2
            }
                // And finally, make sure we don't overlap with the header.
                .coerceAtLeast(headerPlaceable.height)

            contentPlaceable.placeRelative(0, contentPlaceableY)
        }
    }
}

enum class LayoutType {
    HEADER, CONTENT
}
