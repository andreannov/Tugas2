package com.example.kalkulator.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorState(expression = "0", result = ""))
    val uiState = _uiState.asStateFlow()

    private var currentInput: String = "0"
    private var operand1: Double? = null
    private var operation: CalculatorOperation? = null
    private var hasCalculated: Boolean = false

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
        if (hasCalculated) {
            currentInput = "0"
            hasCalculated = false
            operand1 = null
            operation = null
        }
        if (currentInput == "0" && numberStr != ".") {
            currentInput = numberStr
        } else {
            currentInput += numberStr
        }
        updateExpressionDisplay()
    }

    private fun setOperation(op: CalculatorOperation) {
        if (currentInput.isNotBlank() && currentInput != "0") {
            if (operand1 != null && operation != null && !hasCalculated) {
                performCalculation()
            }

            operand1 = _uiState.value.result.toDoubleOrNull() ?: currentInput.toDoubleOrNull()
            if (operand1 == null) return

            operation = op
            currentInput = "0"
            hasCalculated = false
            updateExpressionDisplay()
        }
    }

    private fun enterDecimal() {
        if (hasCalculated) {
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
                CalculatorOperation.PERCENT -> op1Val * (op2Val / 100.0)
                null -> return
            }

            val resultString = formatResult(result)

            _uiState.update {
                it.copy(
                    expression = "${formatNumber(op1Val)} ${operation?.symbol ?: ""} ${formatNumber(op2Val)}",
                    result = resultString
                )
            }
            operand1 = result
            currentInput = resultString
            hasCalculated = true
            operation = null
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
            clear()
            return
        }

        if (currentInput.length > 1) {
            currentInput = currentInput.dropLast(1)
        } else if (currentInput.length == 1 && currentInput != "0") {
            currentInput = "0"
        } else {
            if (operation != null) {
                currentInput = formatNumber(operand1 ?: 0.0)
                operation = null
                operand1 = null
            }
        }
        updateExpressionDisplay()
    }

    private fun updateExpressionDisplay() {
        val currentExpression = buildString {
            append(formatNumber(operand1 ?: currentInput.toDoubleOrNull() ?: 0.0))
            operation?.let { append(" ${it.symbol} ") }
            if (operation != null) {
                append(currentInput)
            } else if (operand1 == null) {
                append(if (currentInput != formatNumber(operand1 ?: 0.0)) "" else currentInput)
            }
        }.trim()

        val finalExpression = if (currentExpression.isBlank() && operand1 == null && operation == null && currentInput == "0") "0"
        else currentExpression

        _uiState.update {
            it.copy(
                expression = if (finalExpression.isBlank()) "0" else finalExpression,
                result = if (hasCalculated) _uiState.value.result else ""
            )
        }
    }

    private fun formatResult(value: Double): String {
        return if (value.isNaN()) "Error"
        else if (value.isInfinite()) "Infinity"
        else if (value % 1.0 == 0.0) value.toLong().toString()
        else String.format("%.8f", value).trimEnd('0').trimEnd('.')
    }

    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) value.toLong().toString()
        else value.toString().trimEnd('0').trimEnd('.')
    }
}