package me.steinborn.brainchug.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class LoopBrainfuckBlock implements BrainfuckBlock {
    private final List<BrainfuckBlock> blocks;

    public LoopBrainfuckBlock(List<BrainfuckBlock> blocks) {
        this.blocks = List.copyOf(blocks);
    }

    @Override
    public void emit(MethodVisitor visitor) {
        Label body = new Label();
        Label tail = new Label();

        visitor.visitLabel(body);

        // Emit DUP2 twice to prepare the stack
        visitor.visitInsn(DUP2);

        // CALOAD undoes the second DUP2 and loads just the value.
        visitor.visitInsn(CALOAD);

        // Loop done?
        visitor.visitJumpInsn(IFEQ, tail);

        // Emit code in the blocks
        for (BrainfuckBlock block : blocks) {
            block.emit(visitor);
        }

        visitor.visitJumpInsn(GOTO, body);
        visitor.visitLabel(tail);
    }
}
