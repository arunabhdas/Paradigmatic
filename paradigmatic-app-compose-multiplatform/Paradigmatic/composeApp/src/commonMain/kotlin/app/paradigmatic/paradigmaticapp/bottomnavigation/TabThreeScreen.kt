package app.paradigmatic.paradigmaticapp.bottomnavigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.paradigmatic.paradigmaticapp.domain.CurrencyApiService
import app.paradigmatic.paradigmaticapp.domain.model.CurrencyType
import app.paradigmatic.paradigmaticapp.presentation.component.CurrencyPickerDialog
import app.paradigmatic.paradigmaticapp.presentation.component.HomeBody
import app.paradigmatic.paradigmaticapp.presentation.component.HomeHeader
import app.paradigmatic.paradigmaticapp.presentation.viewmodel.HomeUiEvent
import app.paradigmatic.paradigmaticapp.presentation.viewmodel.HomeViewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class TabThreeScreen: Screen, KoinComponent {
    private val currencyApiService: CurrencyApiService by inject()
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<HomeViewModel>()
        val rateStatus by viewModel.rateStatus
        val allCurrencies = viewModel.allCurrencies
        val sourceCurrency by viewModel.sourceCurrency
        val targetCurrency by viewModel.targetCurrency
        val sourceCurrencyDisplay by viewModel.sourceCurrencyDisplay
        val targetCurrencyDisplay by viewModel.targetCurrencyDisplay
        var amount by rememberSaveable { mutableStateOf(0.0) }

        var selectedCurrencyType: CurrencyType by remember {
            mutableStateOf(CurrencyType.None)
        }

        var dialogOpened by remember { mutableStateOf(false) }

        if (dialogOpened && selectedCurrencyType != CurrencyType.None) {
            CurrencyPickerDialog(
                currencies = allCurrencies,
                currencyType = selectedCurrencyType,
                onConfirmClick = { currencyCode ->
                    if (selectedCurrencyType is CurrencyType.Source) {
                        println("---------source currencyCode ${currencyCode}")
                        viewModel.sendEvent(
                            HomeUiEvent.SaveSourceCurrencyCode(
                                code = currencyCode.name
                            )
                        )
                    } else if (selectedCurrencyType is CurrencyType.Target) {
                        println("---------target currencyCode ${currencyCode}")
                        viewModel.sendEvent(
                            HomeUiEvent.SaveTargetCurrencyCode(
                                code = currencyCode.name
                            )
                        )
                    }

                    selectedCurrencyType = CurrencyType.None
                    dialogOpened = false
                },
                onDismiss = {
                    selectedCurrencyType = CurrencyType.None
                    dialogOpened = false
                }
            )
        }

        LaunchedEffect(Unit) {
            println("TabThreeScreen")
            currencyApiService.getLatestExchangeRates()
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HomeHeader(
                    status = rateStatus,
                    source = sourceCurrency,
                    target = targetCurrency,
                    amount = amount,
                    onAmountChange = { amount = it },
                    onRatesRefresh = {
                        viewModel.sendEvent(
                            HomeUiEvent.RefreshRates
                        )
                    },
                    onSwitchClick = {
                        println("onSwitchClick triggered on TabThreeScreen")
                        viewModel.sendEvent(
                            HomeUiEvent.SwitchCurrencies
                        )
                    },
                    onCurrencyTypeSelect = { currencyType ->
                        selectedCurrencyType = currencyType
                        dialogOpened = true
                    }
                )
                HomeBody(
                    source = sourceCurrency,
                    target = targetCurrency,
                    amount = amount,
                    sourceDisplay = sourceCurrencyDisplay,
                    targetDisplay = targetCurrencyDisplay
                )
            }
        }
    }
}

