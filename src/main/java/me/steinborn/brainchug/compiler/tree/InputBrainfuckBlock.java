package me.steinborn.brainchug.compiler.tree;

import me.steinborn.brainchug.BrainfuckKeyword;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import static org.objectweb.asm.Opcodes.*;

public class InputBrainfuckBlock implements BrainfuckBlock {
    private final BrainfuckKeyword keyword;

    public InputBrainfuckBlock(BrainfuckKeyword keyword) {
        this.keyword = keyword;
    }

    public BrainfuckKeyword getKeyword() {
        return keyword;
    }

    @Override
    public void emit(GeneratorAdapter mv, int ptrVar) {
        if (keyword == BrainfuckKeyword.PRINT_VAL) {
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
        } else if (keyword == BrainfuckKeyword.DEBUG) {
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
                    "(I)V", false);
        }
    }

    @Override
    public String toString() {
        return keyword.toString();
    }
}
