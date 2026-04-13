package com.s1g1.tomatoro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination

@Composable
fun NavigationBottomToolbar(
    navController: NavHostController,
    currentColor: Color
) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        modifier = Modifier.padding(bottom = 2.dp, start = 16.dp, end=16.dp)
    ){
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationItem.items.forEach { item ->
                val isSelected = currentDestination?.hasRoute(item.route::class) ?: false

                Surface(
                    onClick = {
                        if(!isSelected){
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                              },
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ){
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Icon(
                            imageVector = if(isSelected) item.iconFilled else item.iconOutlined,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if(isSelected) currentColor.copy(alpha = 0.6f) else LocalContentColor.current
                        )
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.labelLarge,
                            color = if(isSelected) currentColor.copy(alpha = 0.75f) else Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}