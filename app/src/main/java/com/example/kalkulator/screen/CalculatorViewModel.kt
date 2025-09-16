package com.example.kalkulator.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel : ViewModel() {

    // Initial state: expression "0", result empty
    private val _uiState = MutableStateFlow(CalculatorState(expression = "0", result = ""))
    val uiState = _uiState.asStateFlow()

    // Internal state holders for the ongoing calculation
    private var currentInput: String = "0" // The number currently being typed
    private var operand1: Double? = null
    private var operation: CalculatorOperation? = null
    private var hasCalculated: Boolean = false // To reset input after a calculation

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number.toString())
            is CalculatorAction.Operation -> setOperation(action.operation)
            CalculatorAction.Calculate -> performCalculation()
            CalculatorAction.Clear -> clear()
            CalculatorAction.Decimal -> enterDecimal()
            CalculatorAction.Backspace -> deleteLast()
        }
    }

    private fun enterNumber(numberStr: String) {
        if (hasCalculated) { // If a calculation just happened, start new input
            currentInput = "0"
            hasCalculated = false
            operand1 = null
            operation = null
        }
        if (currentInput == "0" && numberStr != ".") { // Avoid leading zeros, unless it's "0."
            currentInput = numberStr
        } else {
            currentInput += numberStr
        }
        updateExpressionDisplay()
    }

    private fun setOperation(op: CalculatorOperation) {
        if (currentInput.isNotBlank() && currentInput != "0") {
            // If an operation is already set, perform pending calculation first
            if (operand1 != null && operation != null && !hasCalculated) {
                performCalculation() // Calculate previous part
                // The result of performCalculation becomes the new operand1
            }

            operand1 = _uiState.value.result.toDoubleOrNull() ?: currentInput.toDoubleOrNull()
            if (operand1 == null) return // Cannot set operation without a valid first operand

            operation = op
            currentInput = "0" // Reset current input for the second operand
            hasCalculated = false // Reset calculation flag
            updateExpressionDisplay()
        }
    }

    private fun enterDecimal() {
        if (hasCalculated) { // If a calculation just happened, start new input
            currentInput = "0"
            hasCalculated = false
            operand1 = null
            operation = null
        }
        if (!currentInput.contains(".")) {
            currentInput += "."
        }
        updateExpressionDisplay()
    }

    private fun performCalculation() {
        val op1Val = operand1
        val op2Val = currentInput.toDoubleOrNull()

        if (op1Val != null && op2Val != null && operation != null) {
            val result = when (operation) {
                CalculatorOperation.ADD -> op1Val + op2Val
                CalculatorOperation.SUBTRACT -> op1Val - op2Val
                CalculatorOperation.MULTIPLY -> op1Val * op2Val
                CalculatorOperation.DIVIDE -> if (op2Val != 0.0) op1Val / op2Val else Double.NaN
                CalculatorOperation.PERCENT -> op1Val * (op2Val / 100.0) // A simple percent calculation
                null -> return
            }

            val resultString = formatResult(result)

            // Update UI state with both expression and result
            _uiState.update {
                it.copy(
                    expression = "${formatNumber(op1Val)} ${operation?.symbol ?: ""} ${formatNumber(op2Val)}",
                    result = resultString
                )
            }
            // For subsequent operations (chaining), the result becomes the new first operand
            operand1 = result
            currentInput = resultString // Keep the result for potential further number input
            hasCalculated = true
            operation = null // Clear operation after calculation
        }
    }

    private fun clear() {
        currentInput = "0"
        operand1 = null
        operation = null
        hasCalculated = false
        _uiState.update { CalculatorState(expression = "0", result = "") }
    }

    private fun deleteLast() {
        if (hasCalculated) {
            clear() // If a result is displayed, clear everything
            return
        }

        if (currentInput.length > 1) {
            currentInput = currentInput.dropLast(1)
        } else if (currentInput.length == 1 && currentInput != "0") {
            currentInput = "0"
        } else {
            // If currentInput is "0" and no operation/operand1, try to clear operation or operand1
            if (operation != null) {
                currentInput = formatNumber(operand1 ?: 0.0) // Restore operand1 to currentInput
                operation = null
                operand1 = null
            }
        }
        updateExpressionDisplay()
    }

    // Helper to update the top expression display
    private fun updateExpressionDisplay() {
        val currentExpression = buildString {
            append(formatNumber(operand1 ?: currentInput.toDoubleOrNull() ?: 0.0))
            operation?.let { append(" ${it.symbol} ") }
            if (operation != null) { // Only append currentInput if an operation is set (second operand)
                append(currentInput)
            } else if (operand1 == null) { // If no operand1 and no operation, show just currentInput
                // If currentInput is already the operand1, don't repeat it
                // This handles the initial state "0" and typing numbers.
                append(if (currentInput != formatNumber(operand1 ?: 0.0)) "" else currentInput)
            }
        }.trim()

        // Handle initial "0" display when nothing is typed yet
        val finalExpression = if (currentExpression.isBlank() && operand1 == null && operation == null && currentInput == "0") "0"
        else currentExpression

        _uiState.update {
            it.copy(
                expression = if (finalExpression.isBlank()) "0" else finalExpression,
                result = if (hasCalculated) _uiState.value.result else "" // Clear result if typing
            )
        }
    }

    // Formats a Double for display, removing ".0" if it's an integer
    private fun formatResult(value: Double): String {
        return if (value.isNaN()) "Error"
        else if (value.isInfinite()) "Infinity"
        else if (value % 1.0 == 0.0) value.toLong().toString()
        else String.format("%.8f", value).trimEnd('0').trimEnd('.') // Limit decimal places and remove trailing zeros
    }

    // Formats a Double for display within the expression
    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) value.toLong().toString()
        else value.toString().trimEnd('0').trimEnd('.')
    }
}