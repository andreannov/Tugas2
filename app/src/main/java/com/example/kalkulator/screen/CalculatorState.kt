package com.example.kalkulator.screen

// Represents the data to be displayed on the screen
data class CalculatorState(
    val expression: String = "0", // Renamed 'display' to 'expression' for clarity
    val result: String = ""       // New field for the calculated result
)

// CalculatorAction and CalculatorOperation remain the same
sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    data class Operation(val operation: CalculatorOperation) : CalculatorAction()
    object Clear : CalculatorAction()
    object Calculate : CalculatorAction()
    object Decimal : CalculatorAction()
    // Add Backspace if desired
    object Backspace : CalculatorAction()
}

enum class CalculatorOperation(val symbol: String) {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("×"),
    DIVIDE("÷"),
    PERCENT("%") // Added percent operation
}