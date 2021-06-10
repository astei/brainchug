package me.steinborn.brainchug.compiler.tree;

import org.objectweb.asm.commons.GeneratorAdapter;

public interface BrainfuckBlock {
    void emit(GeneratorAdapter visitor, int ptrVar);
}
