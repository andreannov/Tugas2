package com.example.kalkulator.screen

data class CalculatorState(
    val expression: String = "0",
    val result: String = ""
)


sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    data class Operation(val operation: CalculatorOperation) : CalculatorAction()
    object Clear : CalculatorAction()
    object Calculate : CalculatorAction()
    object Decimal : CalculatorAction()
    object Backspace : CalculatorAction()
}

enum class CalculatorOperation(val symbol: String) {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("×"),
    DIVIDE("÷"),
    PERCENT("%")
}