package me.steinborn.brainchug.tree.optimizer;

import me.steinborn.brainchug.BrainfuckKeyword;
import me.steinborn.brainchug.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A greedy peephole optimizer.
 */
public class GreedyPeepholeOptimizer implements Optimizer {
    private static final Set<BrainfuckKeyword> RELEVANT = Set.of(BrainfuckKeyword.INCREMENT_PTR,
            BrainfuckKeyword.INCREMENT_VAL, BrainfuckKeyword.DECREMENT_PTR, BrainfuckKeyword.DECREMENT_VAL);

    @Override
    public ProgramBrainfuckBlock optimize(ProgramBrainfuckBlock in) {
        return new ProgramBrainfuckBlock(process(in.getBlocks()));
    }

    private List<BrainfuckBlock> process(List<BrainfuckBlock> blocks) {
        List<BrainfuckBlock> optimized = new ArrayList<>();

        Target currentTarget = null;
        int currentAmount = 0;
        for (BrainfuckBlock block : blocks) {
            BrainfuckKeyword localKeyword;
            int localAmount;

            if (block instanceof BasicBrainfuckBlock) {
                localKeyword = ((BasicBrainfuckBlock) block).getKeyword();
                localAmount = value(localKeyword);
            } else if (block instanceof SuperwordBrainfuckBlock) {
                localKeyword = ((SuperwordBrainfuckBlock) block).getKeyword();
                localAmount = ((SuperwordBrainfuckBlock) block).getCount() * value(localKeyword);
            } else if (block instanceof LoopBrainfuckBlock) {
                if (currentTarget != null && currentAmount != 0) {
                    optimized.add(new SuperwordBrainfuckBlock(currentTarget == Target.POINTER
                            ? BrainfuckKeyword.INCREMENT_PTR : BrainfuckKeyword.INCREMENT_VAL, currentAmount));
                    currentTarget = null;
                    currentAmount = 0;
                }
                optimized.add(new LoopBrainfuckBlock(process(((LoopBrainfuckBlock) block).getBlocks())));
                continue;
            } else {
                continue;
            }

            if (RELEVANT.contains(localKeyword)) {
                if (currentTarget == null) {
                    currentTarget = target(localKeyword);
                    currentAmount = localAmount;
                } else {
                    if (target(localKeyword) == currentTarget) {
                        currentAmount += localAmount;
                    } else {
                        // we must stop emitting here
                        if (currentAmount != 0) {
                            optimized.add(new SuperwordBrainfuckBlock(currentTarget == Target.POINTER
                                    ? BrainfuckKeyword.INCREMENT_PTR : BrainfuckKeyword.INCREMENT_VAL, currentAmount));
                        }

                        // this is the new target now
                        currentTarget = target(localKeyword);
                        currentAmount = localAmount;
                    }
                }
            } else {
                // we must stop emitting here
                if (currentTarget != null && currentAmount != 0) {
                    optimized.add(new SuperwordBrainfuckBlock(currentTarget == Target.POINTER
                            ? BrainfuckKeyword.INCREMENT_PTR : BrainfuckKeyword.INCREMENT_VAL, currentAmount));
                    currentAmount = 0;
                    currentTarget = null;
                }

                optimized.add(block);
            }
        }

        // still have the target?
        if (currentTarget != null && currentAmount != 0) {
            optimized.add(new SuperwordBrainfuckBlock(currentTarget == Target.POINTER
                    ? BrainfuckKeyword.INCREMENT_PTR : BrainfuckKeyword.INCREMENT_VAL, currentAmount));
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
