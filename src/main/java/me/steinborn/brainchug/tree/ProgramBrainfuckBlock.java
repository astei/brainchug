package me.steinborn.brainchug.tree;

import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class ProgramBrainfuckBlock implements BrainfuckBlock {
    private final List<BrainfuckBlock> blocks;

    public ProgramBrainfuckBlock(List<BrainfuckBlock> blocks) {
        this.blocks = List.copyOf(blocks);
    }

    @Override
    public void emit(MethodVisitor visitor) {
        for (BrainfuckBlock block : this.blocks) {
            block.emit(visitor);
        }
    }
}
