package me.steinborn.brainchug.compiler.tree;

import me.steinborn.brainchug.BrainfuckKeyword;
import org.objectweb.asm.MethodVisitor;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.CASTORE;

public class SuperwordBrainfuckBlock implements BrainfuckBlock {

    private static final Map<BrainfuckKeyword, SuperwordBrainfuckBlock> FOR_ONES = new EnumMap<>(BrainfuckKeyword.class);

    public static final Set<BrainfuckKeyword> RELEVANT = Set.of(BrainfuckKeyword.INCREMENT_PTR,
            BrainfuckKeyword.INCREMENT_VAL, BrainfuckKeyword.DECREMENT_PTR, BrainfuckKeyword.DECREMENT_VAL);

    static {
        for (BrainfuckKeyword keyword : RELEVANT) {
            FOR_ONES.put(keyword, new SuperwordBrainfuckBlock(keyword, 1));
        }
    }

    private final BrainfuckKeyword keyword;
    private final int count;

    private SuperwordBrainfuckBlock(BrainfuckKeyword keyword, int count) {
        this.keyword = keyword;
        this.count = count;
    }

    public static SuperwordBrainfuckBlock valueOf(BrainfuckKeyword keyword, int count) {
        if (!RELEVANT.contains(keyword)) {
            throw new IllegalArgumentException(keyword + " isn't superword-capable");
        }

        if (count == 1) {
            return FOR_ONES.get(keyword);
        } else {
            return new SuperwordBrainfuckBlock(keyword, count);
        }
    }

    public BrainfuckKeyword getKeyword() {
        return keyword;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void emit(MethodVisitor mv) {
        if (keyword == BrainfuckKeyword.INCREMENT_PTR) {
            // Load count to the top of the stack and add it to index.
            mv.visitLdcInsn(this.count);
            mv.visitInsn(IADD);
        } else if (keyword == BrainfuckKeyword.DECREMENT_PTR) {
            // Load count to the top of the stack and subtract it from index.
            mv.visitLdcInsn(this.count);
            mv.visitInsn(ISUB);
        } else if (keyword == BrainfuckKeyword.INCREMENT_VAL) {
            // Emit DUP2 twice to prepare the stack
            mv.visitInsn(DUP2);
            mv.visitInsn(DUP2);

            // CALOAD undoes the second DUP2 and loads just the value.
            mv.visitInsn(CALOAD);

            // Load the count to the top of the stack and add it to value.
            mv.visitLdcInsn(this.count);
            mv.visitInsn(IADD);

            // Now use CASTORE to store the value back into the stack.
            mv.visitInsn(CASTORE);
        } else if (keyword == BrainfuckKeyword.DECREMENT_VAL) {
            // Emit DUP2 twice to prepare the stack
            mv.visitInsn(DUP2);
            mv.visitInsn(DUP2);

            // CALOAD undoes the second DUP2 and loads just the value.
            mv.visitInsn(CALOAD);

            // Load count to the top of the stack and subtract it from the value.
            mv.visitLdcInsn(this.count);
            mv.visitInsn(ISUB);

            // Now use CASTORE to store the value back into the stack.
            mv.visitInsn(CASTORE);
        }
    }

    @Override
    public String toString() {
        return keyword + " x " + count;
    }
}
