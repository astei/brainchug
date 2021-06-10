package me.steinborn.brainchug.compiler.tree;

import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.List;

public class ProgramBrainfuckBlock implements BrainfuckBlock {
    private final List<BrainfuckBlock> blocks;

    public ProgramBrainfuckBlock(List<BrainfuckBlock> blocks) {
        this.blocks = List.copyOf(blocks);
    }

    public List<BrainfuckBlock> getBlocks() {
        return blocks;
    }

    @Override
    public void emit(GeneratorAdapter visitor, int ptrVar) {
        for (BrainfuckBlock block : this.blocks) {
            block.emit(visitor, ptrVar);
        }
    }

    @Override
    public String toString() {
        return blocks.toString();
    }
}
