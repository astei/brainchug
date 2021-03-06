package me.steinborn.brainchug.compiler.tree;

import me.steinborn.brainchug.BrainfuckKeyword;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.CASTORE;

public class SuperwordBrainfuckBlock implements BrainfuckBlock {

    private static final SuperwordBrainfuckBlock PTR_DEC = new SuperwordBrainfuckBlock(true, 0, -1);
    private static final SuperwordBrainfuckBlock PTR_INC = new SuperwordBrainfuckBlock(true, 0, 1);
    private static final SuperwordBrainfuckBlock VAL_DEC = new SuperwordBrainfuckBlock(false, 0, -1);
    private static final SuperwordBrainfuckBlock VAL_INC = new SuperwordBrainfuckBlock(false, 0, 1);

    public static final Set<BrainfuckKeyword> RELEVANT = Set.of(BrainfuckKeyword.INCREMENT_PTR,
            BrainfuckKeyword.INCREMENT_VAL, BrainfuckKeyword.DECREMENT_PTR, BrainfuckKeyword.DECREMENT_VAL);

    private final boolean isPtr;
    private final int offset;
    private final int count;

    private SuperwordBrainfuckBlock(boolean isPtr, int offset, int count) {
        this.isPtr = isPtr;
        this.offset = offset;
        this.count = count;
    }

    public static SuperwordBrainfuckBlock valueOf(BrainfuckKeyword keyword, int offset, int count) {
        if (!RELEVANT.contains(keyword)) {
            throw new IllegalArgumentException(keyword + " isn't superword-capable");
        }

        switch (keyword) {
            case INCREMENT_PTR:
                if (count == 1 && offset == 0) {
                    return PTR_INC;
                }
                return new SuperwordBrainfuckBlock(true, offset, count);
            case DECREMENT_PTR:
                if (count == 1 && offset == 0) {
                    return PTR_DEC;
                }
                return new SuperwordBrainfuckBlock(true, offset, -count);
            case INCREMENT_VAL:
                if (count == 1 && offset == 0) {
                    return VAL_INC;
                }
                return new SuperwordBrainfuckBlock(false, offset, count);
            case DECREMENT_VAL:
                if (count == 1 && offset == 0) {
                    return VAL_DEC;
                }
                return new SuperwordBrainfuckBlock(false, offset, -count);
            default:
                throw new IllegalStateException(keyword + " isn't a superword");
        }
    }

    public boolean isPtr() {
        return isPtr;
    }

    public int getOffset() {
        return offset;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void emit(GeneratorAdapter mv, int ptrVar) {
        if (isPtr) {
            // Load count to the top of the stack and add it to index.
            mv.push(this.count);
            mv.visitInsn(IADD);
        } else {
            // Emit DUP2 twice to prepare the stack
            mv.visitInsn(DUP2);
            if (this.offset != 0) {
                mv.push(this.offset);
                mv.visitInsn(IADD);
            }
            mv.visitInsn(DUP2);

            // CALOAD undoes the second DUP2 and loads just the value.
            mv.visitInsn(CALOAD);

            // Load the count to the top of the stack and add it to value.
            mv.push(this.count);
            mv.visitInsn(IADD);

            // Now use CASTORE to store the value back into the stack.
            mv.visitInsn(CASTORE);
        }
    }

    @Override
    public String toString() {
        String keyword = isPtr ? "PTR" : "VAL";
        if (offset == 0) {
            return keyword + " x " + count;
        } else {
            return keyword + " => " + offset + " x " + count;
        }
    }
}
