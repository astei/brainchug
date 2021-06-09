package me.steinborn.brainchug.tree.optimizer;

import me.steinborn.brainchug.tree.ProgramBrainfuckBlock;

public interface Optimizer {
    ProgramBrainfuckBlock optimize(ProgramBrainfuckBlock in);
}
