package me.steinborn.brainchug.compiler.tree.optimizer;

import me.steinborn.brainchug.BrainfuckKeyword;
import me.steinborn.brainchug.compiler.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A greedy peephole optimizer. This optimize finds "runs" of nearby increment/decrement operations and combines them.
 * This is the classical Brainfuck optimization.
 */
public class GreedyPeepholeOptimizer implements Optimizer {
    @Override
    public ProgramBrainfuckBlock optimize(ProgramBrainfuckBlock in) {
        return new ProgramBrainfuckBlock(process(in.getBlocks()));
    }

    private List<BrainfuckBlock> process(List<BrainfuckBlock> blocks) {
        List<BrainfuckBlock> optimized = new ArrayList<>();

        Target currentTarget = null;
        int currentAmount = 0;
        for (BrainfuckBlock block : blocks) {
            if (block instanceof SuperwordBrainfuckBlock) {
                Target localTarget = ((SuperwordBrainfuckBlock) block).isPtr() ? Target.POINTER : Target.VALUE;
                int localAmount = ((SuperwordBrainfuckBlock) block).getCount();

                if (currentTarget == null) {
                    currentTarget = localTarget;
                    currentAmount = localAmount;
                } else {
                    if (localTarget == currentTarget) {
                        currentAmount += localAmount;
                    } else {
                        // we must stop emitting here
                        if (currentAmount != 0) {
                            optimized.add(SuperwordBrainfuckBlock.valueOf(currentTarget == Target.POINTER
                                    ? BrainfuckKeyword.INCREMENT_PTR : BrainfuckKeyword.INCREMENT_VAL, 0, currentAmount));
                        }

                        // this is the new target now
                        currentTarget = localTarget;
                        currentAmount = localAmount;
                    }
                }
            } else {
                // we must stop emitting here
                if (currentTarget != null && currentAmount != 0) {
                    optimized.add(SuperwordBrainfuckBlock.valueOf(currentTarget == Target.POINTER
                            ? BrainfuckKeyword.INCREMENT_PTR : BrainfuckKeyword.INCREMENT_VAL, 0, currentAmount));
                    currentAmount = 0;
                    currentTarget = null;
                }

                if (block instanceof LoopBrainfuckBlock) {
                    optimized.add(new LoopBrainfuckBlock(process(((LoopBrainfuckBlock) block).getBlocks())));
                } else {
                    optimized.add(block);
                }
            }
        }
        // still have the target?
        if (currentTarget != null && currentAmount != 0) {
            optimized.add(SuperwordBrainfuckBlock.valueOf(currentTarget == Target.POINTER
                    ? BrainfuckKeyword.INCREMENT_PTR : BrainfuckKeyword.INCREMENT_VAL, 0, currentAmount));
        }
        return optimized;
    }

    private enum Target {
        POINTER,
        VALUE
    }
}
