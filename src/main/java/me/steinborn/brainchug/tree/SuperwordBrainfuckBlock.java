package me.steinborn.brainchug.tree;

import me.steinborn.brainchug.BrainfuckKeyword;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.CASTORE;

public class SuperwordBrainfuckBlock implements BrainfuckBlock {
    private final BrainfuckKeyword keyword;
    private final int count;

    public SuperwordBrainfuckBlock(BrainfuckKeyword keyword, int count) {
        this.keyword = keyword;
        this.count = count;
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
