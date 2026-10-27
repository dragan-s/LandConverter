package com.example.landconverter.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.landconverter.presentation.ConverterScreen
import com.example.landconverter.presentation.manage.ManageUnitsScreen

@Composable
fun LandConverterNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "converter") {
        composable("converter") {
            ConverterScreen(
                onNavigateToManageUnits = { countryCode ->
                    navController.navigate("manage_units/$countryCode")
                }
            )
        }
        composable(
            route = "manage_units/{countryCode}",
            arguments = listOf(navArgument("countryCode") { type = NavType.StringType })
        ) {
            ManageUnitsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
