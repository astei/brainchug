package me.steinborn.brainchug;

/**
 * Represents every keyword in the Brainfuck language.
 */
public enum BrainfuckKeyword {
    /**
     * Shifts the pointer to the right.
     */
    INCREMENT_PTR,
    /**
     * Shifts the pointer to the left.
     */
    DECREMENT_PTR,
    /**
     * Increments the value pointed to by the pointer.
     */
    INCREMENT_VAL,
    /**
     * Decrements the value pointed to by the pointer.
     */
    DECREMENT_VAL,
    /**
     * Prints the value pointed to by the pointer (assuming it can be cast to a Java {@code char}.
     */
    PRINT_VAL,
    /**
     * Accepts input to be placed into the value pointed to by the pointer.
     */
    INPUT_VAL,
    /**
     * Begins a loop. If the value pointed to by the pointer is zero, skip the entire loop.
     */
    LOOP_BEGIN,
    /**
     * Ends a loop. If the value pointed to by the pointer is non-zero, skip back to the beginning of the loop.
     */
    LOOP_END,
    /**
     * Debug keyword. Prints the value in the current cell to the console.
     */
    DEBUG
}
