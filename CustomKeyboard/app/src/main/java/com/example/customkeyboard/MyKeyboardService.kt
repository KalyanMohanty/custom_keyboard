package com.example.customkeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button
import android.widget.LinearLayout


class MyKeyboardService : InputMethodService() {

    private var keyboardView: View? = null
    private var isNumericMode = false
    private var isCapsLock = false // Track the Caps Lock state
    private var isSearchMode: Boolean = false

    val keyIds = listOf(
        R.id.key_a, R.id.key_b, R.id.key_c, R.id.key_d, R.id.key_e, R.id.key_f,
        R.id.key_g, R.id.key_h, R.id.key_i, R.id.key_j, R.id.key_k, R.id.key_l,
        R.id.key_m, R.id.key_n, R.id.key_o, R.id.key_p, R.id.key_q, R.id.key_r,
        R.id.key_s, R.id.key_t, R.id.key_u, R.id.key_v, R.id.key_w, R.id.key_x,
        R.id.key_y, R.id.key_z
    )

    val keyTexts = listOf(
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
        "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    )

    var isShifted = false
    private lateinit var alphabetRows: Array<LinearLayout> // Using lateinit to defer initialization
    private lateinit var numberRow: LinearLayout // Using lateinit to defer initialization
//    private lateinit var specialCharsRow: Array<LinearLayout> // Using lateinit to defer initialization
    private lateinit var specialCharsRow: LinearLayout // Using lateinit to defer initialization
    private lateinit var specialCharsRow2: LinearLayout // Using lateinit to defer initialization
    private lateinit var specialCharsRow3: LinearLayout // Using lateinit to defer initialization
    private var isDeleting = false

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard, null)
        keyboardView?.setBackgroundResource(R.drawable.keyboard_background)
        // Initialize rows safely using the non-nullable values
        alphabetRows = arrayOf(
            keyboardView!!.findViewById(R.id.alphabet_row_1),
            keyboardView!!.findViewById(R.id.alphabet_row_2),
            keyboardView!!.findViewById(R.id.alphabet_row_3),
            keyboardView!!.findViewById(R.id.alphabet_row_4)
        )
        numberRow = keyboardView!!.findViewById(R.id.number_row)
        specialCharsRow = keyboardView!!.findViewById(R.id.special_chars_row)
        specialCharsRow2 = keyboardView!!.findViewById(R.id.special_chars_row_2)
        specialCharsRow3 = keyboardView!!.findViewById(R.id.special_chars_row_3)

//        specialCharsRow = arrayOf(
//            keyboardView!!.findViewById(R.id.special_chars_row),
//            keyboardView!!.findViewById(R.id.special_chars_row_2)
//        )
        // Handle individual key presses
        for (i in keyIds.indices) {
            val button = keyboardView?.findViewById<Button>(keyIds[i])
            button?.setOnClickListener {
                val text = if (isShifted) keyTexts[i].uppercase() else keyTexts[i].lowercase()
                currentInputConnection.commitText(text, 1)
            }
        }
        keyboardView?.findViewById<Button>(R.id.key_comma)?.setOnClickListener {
            currentInputConnection.commitText(",", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_period)?.setOnClickListener {
            currentInputConnection.commitText(".", 1)
        }
        // Handle space key
        keyboardView?.findViewById<Button>(R.id.key_space)?.setOnClickListener {
            currentInputConnection.commitText(" ", 1)
        }

        // Handle enter key
        keyboardView?.findViewById<Button>(R.id.key_enter)?.setOnClickListener {
            currentInputConnection.commitText("\n", 1)
        }



        // Handle shift key
        keyboardView?.findViewById<Button>(R.id.key_shft)?.setOnClickListener {
            isShifted = !isShifted
            updateKeyLabels()
            updateShiftButtonLabel() // Update the label of the shift button to reflect the shift state
        }

// Handle individual key presses
        for (i in keyIds.indices) {
            val button = keyboardView?.findViewById<Button>(keyIds[i])
            button?.setOnClickListener {
                val text = when {
                    isCapsLock -> keyTexts[i].uppercase() // Caps lock is active, always uppercase
                    isShifted -> keyTexts[i].uppercase() // Shift is active, switch to uppercase
                    else -> keyTexts[i].lowercase() // Default, lowercase
                }
                currentInputConnection.commitText(text, 1)
            }
        }

        // Handle backspace key long press
        keyboardView?.findViewById<Button>(R.id.key_backspace)?.setOnLongClickListener {
            // Start a continuous deletion operation
            val handler = android.os.Handler()
            isDeleting = true
            val runnable = object : Runnable {
                override fun run() {
                    if (isDeleting) { // Check if deletion should continue
                        val currentTextLength = currentInputConnection.getTextBeforeCursor(1, 0)?.length ?: 0
                        if (currentTextLength > 0) {
                            currentInputConnection.deleteSurroundingText(1, 0) // Delete 1 character before the cursor
                            handler.postDelayed(this, 100) // Repeat after 100ms (can adjust speed)
                        } else {
                            isDeleting = false // Stop if no text left
                        }
                    }
                }
            }
            handler.post(runnable) // Start the deletion
            true // Return true to indicate that the long click event is handled
        }

        // Stop deletion when any key is pressed
        val allKeys = listOf(    R.id.key_a, R.id.key_b, R.id.key_c, R.id.key_d, R.id.key_e, R.id.key_f,
            R.id.key_g, R.id.key_h, R.id.key_i, R.id.key_j, R.id.key_k, R.id.key_l,
            R.id.key_m, R.id.key_n, R.id.key_o, R.id.key_p, R.id.key_q, R.id.key_r,
            R.id.key_s, R.id.key_t, R.id.key_u, R.id.key_v, R.id.key_w, R.id.key_x,
            R.id.key_y, R.id.key_z, R.id.key_space, R.id.key_backspace)
        for (keyId in allKeys) {
            keyboardView?.findViewById<Button>(keyId)?.setOnClickListener {
                isDeleting = false // Stop deletion
                // Normal key handling
                val keyText = (it as Button).text.toString()
                currentInputConnection.commitText(keyText, 1)
            }
        }

        keyboardView?.findViewById<Button>(R.id.key_backspace)?.setOnClickListener {
            val currentTextLength = currentInputConnection.getTextBeforeCursor(1, 0)?.length ?: 0
            if (currentTextLength > 0) {
                currentInputConnection.deleteSurroundingText(1, 0) // Delete 1 character before the cursor
            }
        }

        // Handle the "123" button press to toggle modes
        keyboardView?.findViewById<Button>(R.id.key_123)?.setOnClickListener {
            toggleNumericAndSpecialModes()
        }

        setupNumericAndSpecialKeys()
        setupSpecialKeys()
        return keyboardView!!
    }

    // Update key labels based on shift state
    private fun updateKeyLabels() {
        for (i in keyIds.indices) {
            val button = keyboardView?.findViewById<Button>(keyIds[i])
            val newText = if (isShifted) keyTexts[i].uppercase() else keyTexts[i].lowercase()
            button?.text = newText
        }
    }

//    // Update the label of the shift button (optional)
    private fun updateShiftButtonLabel() {
        val shiftButton = keyboardView?.findViewById<Button>(R.id.key_shft)
        shiftButton?.text = if (isShifted) "⬆" else "⇧"
    }

    // Toggle between numeric, special characters, and alphabetic modes
    private fun toggleNumericAndSpecialModes() {
        isNumericMode = !isNumericMode
        if (isNumericMode) {
            // Hide alphabet rows and show number row and special characters row
            alphabetRows.forEachIndexed { index, row ->
                row.visibility = if (index == alphabetRows.size - 1) View.VISIBLE else View.GONE
            }
            numberRow.visibility = View.VISIBLE
            specialCharsRow.visibility = View.VISIBLE
            specialCharsRow2.visibility = View.VISIBLE
            specialCharsRow3.visibility = View.VISIBLE
            // Update the toggle button label to show "ABC" for switching to alphabetic mode
            keyboardView?.findViewById<Button>(R.id.key_123)?.text = "ABC"

        } else {
            // Show alphabet rows and hide number row and special characters row
            alphabetRows.forEachIndexed { index, row ->
                row.visibility = View.VISIBLE
            }
            numberRow.visibility = View.GONE
            specialCharsRow.visibility = View.GONE
            specialCharsRow2.visibility = View.GONE
            specialCharsRow3.visibility = View.GONE
            // Update the toggle button label to show "123" for switching to numeric mode
            keyboardView?.findViewById<Button>(R.id.key_123)?.text = "123"
        }
    }
    // Function to handle enter key press





    private fun setupNumericAndSpecialKeys() {
        // Handle numeric/special keys, if not already visible
        keyboardView?.findViewById<Button>(R.id.key_1)?.setOnClickListener {
            currentInputConnection.commitText("1", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_2)?.setOnClickListener {
            currentInputConnection.commitText("2", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_3)?.setOnClickListener {
            currentInputConnection.commitText("3", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_4)?.setOnClickListener {
            currentInputConnection.commitText("4", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_5)?.setOnClickListener {
            currentInputConnection.commitText("5", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_6)?.setOnClickListener {
            currentInputConnection.commitText("6", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_7)?.setOnClickListener {
            currentInputConnection.commitText("7", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_8)?.setOnClickListener {
            currentInputConnection.commitText("8", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_9)?.setOnClickListener {
            currentInputConnection.commitText("9", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_0)?.setOnClickListener {
            currentInputConnection.commitText("0", 1)
        }

        // Repeat for other numeric and special character keys like `@`, `#`, etc...
    }

    private fun setupSpecialKeys() {
        // Existing keys
        keyboardView?.findViewById<Button>(R.id.key_at)?.setOnClickListener {
            currentInputConnection.commitText("@", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_hash)?.setOnClickListener {
            currentInputConnection.commitText("#", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_dollar)?.setOnClickListener {
            currentInputConnection.commitText("$", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_percent)?.setOnClickListener {
            currentInputConnection.commitText("%", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_slash)?.setOnClickListener {
            currentInputConnection.commitText("/", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_dash)?.setOnClickListener {
            currentInputConnection.commitText("-", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_colon)?.setOnClickListener {
            currentInputConnection.commitText(":", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_open_paren)?.setOnClickListener {
            currentInputConnection.commitText("(", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_close_paren)?.setOnClickListener {
            currentInputConnection.commitText(")", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_question)?.setOnClickListener {
            currentInputConnection.commitText("?", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_exclamation)?.setOnClickListener {
            currentInputConnection.commitText("!", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_apostrophe)?.setOnClickListener {
            currentInputConnection.commitText("'", 1)
        }

        // New keys
        keyboardView?.findViewById<Button>(R.id.key_tilde)?.setOnClickListener {
            currentInputConnection.commitText("~", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_backtick)?.setOnClickListener {
            currentInputConnection.commitText("`", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_bslash)?.setOnClickListener {
            currentInputConnection.commitText("\\", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_semi_colon)?.setOnClickListener {
            currentInputConnection.commitText(";", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_curly_left)?.setOnClickListener {
            currentInputConnection.commitText("{", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_curly_right)?.setOnClickListener {
            currentInputConnection.commitText("}", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_star)?.setOnClickListener {
            currentInputConnection.commitText("*", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_underscore)?.setOnClickListener {
            currentInputConnection.commitText("_", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_square_left)?.setOnClickListener {
            currentInputConnection.commitText("[", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_square_right)?.setOnClickListener {
            currentInputConnection.commitText("]", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_rupee)?.setOnClickListener {
            currentInputConnection.commitText("₹", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_copyright)?.setOnClickListener {
            currentInputConnection.commitText("©", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_pipe)?.setOnClickListener {
            currentInputConnection.commitText("|", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_plus)?.setOnClickListener {
            currentInputConnection.commitText("+", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_mul)?.setOnClickListener {
            currentInputConnection.commitText("x", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_div)?.setOnClickListener {
            currentInputConnection.commitText("÷", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_check)?.setOnClickListener {
            currentInputConnection.commitText("✓", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_pi)?.setOnClickListener {
            currentInputConnection.commitText("π", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_square_root)?.setOnClickListener {
            currentInputConnection.commitText("√", 1)
        }
        keyboardView?.findViewById<Button>(R.id.key_double_quote)?.setOnClickListener {
            currentInputConnection.commitText("\"", 1)
        }

    }


}
