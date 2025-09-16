package com.example.kalkulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kalkulator.screen.CalculatorAction
import com.example.kalkulator.screen.CalculatorOperation
import com.example.kalkulator.screen.CalculatorViewModel
import com.example.kalkulator.ui.theme.KalkulatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KalkulatorTheme { // Use your actual theme
                CalculatorScreen()
            }
        }
    }
}

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom // Align buttons to bottom
        ) {
            // Expression Display
            Text(
                text = uiState.expression,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                fontWeight = FontWeight.Light,
                fontSize = 40.sp, // Smaller font for expression
                color = Color.White.copy(alpha = 0.7f), // Slightly faded for context
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Result Display
            Text(
                text = if (uiState.result.isNotEmpty()) "= ${uiState.result}" else "",
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
                    .padding(bottom = 16.dp),
                fontWeight = FontWeight.Bold, // Bolder for the result
                fontSize = 58.sp, // Larger font for result
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp)) // Space between display and buttons

            // Buttons Grid
            val buttonLayout = listOf(
                listOf("C", "%", "DEL", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            buttonLayout.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { symbol ->
                        CalculatorButton(
                            symbol = symbol,
                            modifier = Modifier
                                .weight(if (symbol == "0") 2f else 1f) // Make '0' wider
                                .aspectRatio(if (symbol == "0") 2f else 1f),
                            color = when (symbol) {
                                "C", "DEL" -> Color(0xFFD32F2F) // Red for Clear/Delete
                                "%", "÷", "×", "-", "+" -> MaterialTheme.colorScheme.secondary // Orange for operations
                                "=" -> MaterialTheme.colorScheme.primary // Blue for equals
                                else -> Color(0xFF616161) // Dark grey for numbers/decimal
                            },
                            onClick = {
                                when (symbol) {
                                    "C" -> viewModel.onAction(CalculatorAction.Clear)
                                    "DEL" -> viewModel.onAction(CalculatorAction.Backspace)
                                    "=" -> viewModel.onAction(CalculatorAction.Calculate)
                                    "." -> viewModel.onAction(CalculatorAction.Decimal)
                                    "%" -> viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.PERCENT))
                                    "÷" -> viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.DIVIDE))
                                    "×" -> viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.MULTIPLY))
                                    "-" -> viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.SUBTRACT))
                                    "+" -> viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.ADD))
                                    else -> symbol.toIntOrNull()?.let {
                                        viewModel.onAction(CalculatorAction.Number(it))
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// Reusing your CalculatorButton Composable
@Composable
fun CalculatorButton(
    symbol: String,
    modifier: Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = MaterialTheme.shapes.medium, // Using MaterialTheme's medium shape for rounded corners
        contentPadding = PaddingValues(0.dp) // Remove default padding for better control
    ) {
        Text(text = symbol, fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Normal)
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
    KalkulatorTheme { // Use your actual theme
        CalculatorScreen()
    }
}