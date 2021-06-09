package me.steinborn.brainchug.tree;

import me.steinborn.brainchug.BrainfuckKeyword;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class BasicBrainfuckBlock implements BrainfuckBlock {
    private final BrainfuckKeyword keyword;

    public BasicBrainfuckBlock(BrainfuckKeyword keyword) {
        this.keyword = keyword;
    }

    public BrainfuckKeyword getKeyword() {
        return keyword;
    }

    @Override
    public void emit(MethodVisitor mv) {
        if (keyword == BrainfuckKeyword.INCREMENT_PTR) {
            // Load 1 to the top of the stack and add it to index.
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IADD);
        } else if (keyword == BrainfuckKeyword.DECREMENT_PTR) {
            // Load 1 to the top of the stack and subtract it from index.
            mv.visitInsn(ICONST_1);
            mv.visitInsn(ISUB);
        } else if (keyword == BrainfuckKeyword.INCREMENT_VAL) {
            // Emit DUP2 twice to prepare the stack
            mv.visitInsn(DUP2);
            mv.visitInsn(DUP2);

            // CALOAD undoes the second DUP2 and loads just the value.
            mv.visitInsn(CALOAD);

            // Load 1 to the top of the stack and add it to value.
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IADD);

            // Now use CASTORE to store the value back into the stack.
            mv.visitInsn(CASTORE);
        } else if (keyword == BrainfuckKeyword.DECREMENT_VAL) {
            // Emit DUP2 twice to prepare the stack
            mv.visitInsn(DUP2);
            mv.visitInsn(DUP2);

            // CALOAD undoes the second DUP2 and loads just the value.
            mv.visitInsn(CALOAD);

            // Load 1 to the top of the stack and subtract it from the value.
            mv.visitInsn(ICONST_1);
            mv.visitInsn(ISUB);

            // Now use CASTORE to store the value back into the stack.
            mv.visitInsn(CASTORE);
        } else if (keyword == BrainfuckKeyword.PRINT_VAL) {
            // Duplicate the top entries of the stack.
            mv.visitInsn(DUP2);

            // CALOAD undoes the DUP2 and loads the value from the array we need.
            mv.visitInsn(CALOAD);

            // We now need to call System.out.print(C)V. First, fetch System.out...
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
                    Type.getObjectType("java/io/PrintStream").getDescriptor());

            // Swap the top two items of the stack since they're in the wrong order.
            mv.visitInsn(SWAP);

            // Call the function.
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print",
                    "(C)V", false);

            // Fetch it again since it was popped off the stack again. We need to flush.
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
                    Type.getObjectType("java/io/PrintStream").getDescriptor());

            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "flush",
                    "()V", false);
        } else if (keyword == BrainfuckKeyword.INPUT_VAL) {
            // Clone the pointers...
            mv.visitInsn(DUP2);

            // We need to call System.in.read()I...
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in",
                    Type.getObjectType("java/io/InputStream").getDescriptor());

            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/InputStream", "read",
                    "()I", false);

            // Our implementation will store -1, just like Java
            mv.visitInsn(CASTORE);
        }
    }

    @Override
    public String toString() {
        return keyword.toString();
    }
}
