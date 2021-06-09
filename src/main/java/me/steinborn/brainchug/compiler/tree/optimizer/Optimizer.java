package me.steinborn.brainchug.compiler.tree.optimizer;

import me.steinborn.brainchug.compiler.tree.ProgramBrainfuckBlock;

public interface Optimizer {
    ProgramBrainfuckBlock optimize(ProgramBrainfuckBlock in);
}
