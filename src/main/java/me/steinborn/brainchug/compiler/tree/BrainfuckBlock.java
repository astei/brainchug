package me.steinborn.brainchug.compiler.tree;

import org.objectweb.asm.MethodVisitor;

public interface BrainfuckBlock {
    void emit(MethodVisitor visitor);
}
