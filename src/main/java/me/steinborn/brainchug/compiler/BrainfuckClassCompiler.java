package me.steinborn.brainchug.compiler;

import me.steinborn.brainchug.compiler.tree.ProgramBrainfuckBlock;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import static org.objectweb.asm.Opcodes.*;

public class BrainfuckClassCompiler {

    private static final String SUPER_NAME = "java/lang/Object";
    private static final String[] IMPLEMENTED = new String[]{Type.getInternalName(Runnable.class)};

    public static byte[] compile(ProgramBrainfuckBlock block, boolean asMain) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        // Emit a public Java 8 class extending Object and implementing the Runnable interface
        writer.visit(V1_8, ACC_PUBLIC | ACC_FINAL, "Produced", null, SUPER_NAME, asMain ? new String[0] : IMPLEMENTED);

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

        // Implement the run()V method (or main(). This is the "meat and potatoes" of the whole venture.
        {
            GeneratorAdapter mv;
            if (asMain) {
                mv = new GeneratorAdapter(ACC_PUBLIC | ACC_STATIC, Method.getMethod("void main (String[])"), null, null, writer);
            } else {
                mv = new GeneratorAdapter(ACC_PUBLIC, Method.getMethod("void run ()"), null, null, writer);
            }

            mv.push(16384);
            mv.visitIntInsn(NEWARRAY, T_CHAR);
            mv.push(0);

            // At this point, we now have the following stack layout:
            // - cells
            // - index
            // This makes it possible for us to use the DUP2 instruction for saload/sastore, along with swapping
            // operands.

            // With our array created, we can now execute program logic.
            block.emit(mv, 0);

            // Flush all output if we haven't done so already.
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
                    Type.getObjectType("java/io/PrintStream").getDescriptor());

            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "flush",
                    "()V", false);

            mv.visitInsn(RETURN);
            mv.endMethod();
        }

        writer.visitEnd();
        return writer.toByteArray();
    }
}
