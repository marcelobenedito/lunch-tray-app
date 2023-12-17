/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.EntreeMenuScreen
import com.example.lunchtray.ui.OrderViewModel
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen

// Screen enum
enum class LunchTrayScreenEnum(@StringRes val title: Int) {
    Start(title = R.string.start_order),
    Entree(title = R.string.choose_entree),
    SideDish(title = R.string.choose_side_dish),
    Accompaniment(title = R.string.choose_accompaniment),
    Checkout(title = R.string.order_checkout)
}

// AppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LunchTrayAppBar(
    currentScreen: LunchTrayScreenEnum,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_button)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayApp() {
    // Create Controller and initialization
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = LunchTrayScreenEnum.valueOf(
        backStackEntry?.destination?.route ?: LunchTrayScreenEnum.Start.name
    )

    // Create ViewModel
    val viewModel: OrderViewModel = viewModel()

    Scaffold(
        topBar = {
            // AppBar
            LunchTrayAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        // Navigation host
        NavHost(
            navController = navController,
            startDestination = LunchTrayScreenEnum.Start.name
        ) {
            composable(route = LunchTrayScreenEnum.Start.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = {
                        navController.navigate(LunchTrayScreenEnum.Entree.name)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            composable(route = LunchTrayScreenEnum.Entree.name) {
                EntreeMenuScreen(
                    options = DataSource.entreeMenuItems,
                    onCancelButtonClicked = { navController.navigateUp() },
                    onNextButtonClicked = { navController.navigate(LunchTrayScreenEnum.SideDish.name) },
                    onSelectionChanged = { viewModel.updateEntree(it) },
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                )
            }
            composable(route = LunchTrayScreenEnum.SideDish.name) {
                SideDishMenuScreen(
                    options = DataSource.sideDishMenuItems,
                    onCancelButtonClicked = { navController.navigateUp() },
                    onNextButtonClicked = { navController.navigate(LunchTrayScreenEnum.Accompaniment.name) },
                    onSelectionChanged = { viewModel.updateSideDish(it) },
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                )
            }
            composable(route = LunchTrayScreenEnum.Accompaniment.name) {
                AccompanimentMenuScreen(
                    options = DataSource.accompanimentMenuItems,
                    onCancelButtonClicked = { navController.navigateUp() },
                    onNextButtonClicked = { navController.navigate(LunchTrayScreenEnum.Checkout.name) },
                    onSelectionChanged = { viewModel.updateAccompaniment(it) },
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                )
            }
            composable(route = LunchTrayScreenEnum.Checkout.name) {
                CheckoutScreen(
                    orderUiState = uiState,
                    onNextButtonClicked = { navController.popBackStack(LunchTrayScreenEnum.Start.name, inclusive = false) },
                    onCancelButtonClicked = { navController.navigateUp() },
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding(),
                            start = dimensionResource(id = R.dimen.padding_medium),
                            end = dimensionResource(id = R.dimen.padding_medium)
                        )
                )
            }
        }
    }
}
