package me.steinborn.brainchug.compiler;

import me.steinborn.brainchug.compiler.tree.ProgramBrainfuckBlock;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class BrainfuckClassCompiler {

    private static final String SUPER_NAME = "java/lang/Object";
    private static final String[] IMPLEMENTED = new String[]{Type.getInternalName(Runnable.class)};

    public static byte[] compile(ProgramBrainfuckBlock block) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        // Emit a public Java 8 class extending Object and implementing the Runnable interface
        writer.visit(V1_8, ACC_PUBLIC | ACC_FINAL, "Produced", null, SUPER_NAME, IMPLEMENTED);

        // Create the constructor. In our case, we only want to invoke the super constructor.
        {
            MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, SUPER_NAME, "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // Implement the run()V method. This is the "meat and potatoes" of the whole venture.
        {
            MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
            mv.visitCode();

            // Create the cell array
            mv.visitLdcInsn(16384);
            mv.visitIntInsn(NEWARRAY, T_CHAR);

            // Store the pointer here.
            mv.visitInsn(ICONST_0);

            // At this point, we now have the following stack layout:
            // - cells
            // - index
            // This makes it possible for us to use the DUP2 instruction for saload/sastore, along with swapping
            // operands.

            // With our array created, we can now execute program logic.
            block.emit(mv);

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        return writer.toByteArray();
    }
}
