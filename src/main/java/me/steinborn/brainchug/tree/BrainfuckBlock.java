package me.steinborn.brainchug.tree;

import org.objectweb.asm.MethodVisitor;

public interface BrainfuckBlock {
    void emit(MethodVisitor visitor);
}
