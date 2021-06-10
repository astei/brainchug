package me.steinborn.brainchug.compiler.tree.optimizer;

import me.steinborn.brainchug.BrainfuckKeyword;
import me.steinborn.brainchug.compiler.tree.BrainfuckBlock;
import me.steinborn.brainchug.compiler.tree.LoopBrainfuckBlock;
import me.steinborn.brainchug.compiler.tree.ProgramBrainfuckBlock;
import me.steinborn.brainchug.compiler.tree.SuperwordBrainfuckBlock;

import java.util.ArrayList;
import java.util.List;

public class OffsetBakeOptimizer implements Optimizer {
    @Override
    public ProgramBrainfuckBlock optimize(ProgramBrainfuckBlock in) {
        return new ProgramBrainfuckBlock(optimize(in.getBlocks()));
    }

    private List<BrainfuckBlock> optimize(List<BrainfuckBlock> blockList) {
        List<BrainfuckBlock> optimized = new ArrayList<>();
        int offsetFromCurrentPointer = 0;
        for (BrainfuckBlock block : blockList) {
            if (block instanceof SuperwordBrainfuckBlock) {
                if (((SuperwordBrainfuckBlock) block).getKeyword() == BrainfuckKeyword.INCREMENT_PTR) {
                    offsetFromCurrentPointer += ((SuperwordBrainfuckBlock) block).getCount();
                } else if (((SuperwordBrainfuckBlock) block).getKeyword() == BrainfuckKeyword.INCREMENT_VAL) {
                    optimized.add(SuperwordBrainfuckBlock.valueOf(BrainfuckKeyword.INCREMENT_VAL,
                            offsetFromCurrentPointer, ((SuperwordBrainfuckBlock) block).getCount()));
                } else {
                    // Flush out the current pointer write
                    if (offsetFromCurrentPointer != 0) {
                        optimized.add(SuperwordBrainfuckBlock.valueOf(BrainfuckKeyword.INCREMENT_PTR, 0, offsetFromCurrentPointer));
                        offsetFromCurrentPointer = 0;
                    }
                    optimized.add(block);
                }
            } else if (block instanceof ZeroIdiomOptimizer.ZeroIdiomInstruction) {
                optimized.add(new ZeroIdiomOptimizer.ZeroIdiomInstruction(offsetFromCurrentPointer));
            } else {
                // Flush out the current pointer write
                if (offsetFromCurrentPointer != 0) {
                    optimized.add(SuperwordBrainfuckBlock.valueOf(BrainfuckKeyword.INCREMENT_PTR, 0, offsetFromCurrentPointer));
                    offsetFromCurrentPointer = 0;
                }
                if (block instanceof LoopBrainfuckBlock) {
                    optimized.add(new LoopBrainfuckBlock(optimize(((LoopBrainfuckBlock) block).getBlocks())));
                } else {
                    optimized.add(block);
                }
            }
        }

        if (offsetFromCurrentPointer != 0) {
            optimized.add(SuperwordBrainfuckBlock.valueOf(BrainfuckKeyword.INCREMENT_PTR, 0, offsetFromCurrentPointer));
        }
        return optimized;
    }
}
