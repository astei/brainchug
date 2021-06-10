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
                BrainfuckKeyword localKeyword = ((SuperwordBrainfuckBlock) block).getKeyword();
                int localAmount = ((SuperwordBrainfuckBlock) block).getCount() * value(localKeyword);

                if (currentTarget == null) {
                    currentTarget = target(localKeyword);
                    currentAmount = localAmount;
                } else {
                    if (target(localKeyword) == currentTarget) {
                        currentAmount += localAmount;
                    } else {
                        // we must stop emitting here
                        if (currentAmount != 0) {
                            optimized.add(SuperwordBrainfuckBlock.valueOf(currentTarget == Target.POINTER
                                    ? BrainfuckKeyword.INCREMENT_PTR : BrainfuckKeyword.INCREMENT_VAL, 0, currentAmount));
                        }

                        // this is the new target now
                        currentTarget = target(localKeyword);
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

    private static int value(BrainfuckKeyword keyword) {
        switch (keyword) {
            case INCREMENT_PTR:
            case INCREMENT_VAL:
                return 1;
            case DECREMENT_PTR:
            case DECREMENT_VAL:
                return -1;
            default:
                return 0;
        }
    }

    private static Target target(BrainfuckKeyword keyword) {
        switch (keyword) {
            case INCREMENT_PTR:
            case DECREMENT_PTR:
                return Target.POINTER;
            case INCREMENT_VAL:
            case DECREMENT_VAL:
                return Target.VALUE;
            default:
                return null;
        }
    }

    private enum Target {
        POINTER,
        VALUE
    }
}
