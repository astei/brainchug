package me.steinborn.brainchug.compiler.tree.optimizer;

import me.steinborn.brainchug.BrainfuckKeyword;
import me.steinborn.brainchug.compiler.tree.BrainfuckBlock;
import me.steinborn.brainchug.compiler.tree.LoopBrainfuckBlock;
import me.steinborn.brainchug.compiler.tree.ProgramBrainfuckBlock;
import me.steinborn.brainchug.compiler.tree.SuperwordBrainfuckBlock;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.CASTORE;

/**
 * Tries to optimize common Brainfuck idioms for moving items to another cell and multiplying numbers by a constant
 * (i.e. [->>>+<<<] or [->+<]. This is currently broken.
 */
public class MultiplicationIdiomOptimizer implements Optimizer {
    @Override
    public ProgramBrainfuckBlock optimize(ProgramBrainfuckBlock in) {
        return new ProgramBrainfuckBlock(process(in.getBlocks()));
    }

    private List<BrainfuckBlock> process(List<BrainfuckBlock> blocks) {
        List<BrainfuckBlock> optimized = new ArrayList<>();

        for (BrainfuckBlock block : blocks) {
            if (block instanceof LoopBrainfuckBlock) {
                List<BrainfuckBlock> loopBlocks = ((LoopBrainfuckBlock) block).getBlocks();
                MultiplicationIdiomBlock multiplication = checkIfMultiplyIdiom(loopBlocks);
                if (multiplication != null) {
                    optimized.add(multiplication);
                } else {
                    optimized.add(new LoopBrainfuckBlock(process(((LoopBrainfuckBlock) block).getBlocks())));
                }
            } else {
                optimized.add(block);
            }
        }

        return optimized;
    }

    private MultiplicationIdiomBlock checkIfMultiplyIdiom(List<BrainfuckBlock> loopBlocks) {
        // This idiom handles two common Brainfuck patterns, specifically moving a value to another cell, and
        // multiplying a value by a constant, like so:
        //
        // [->>>+<<<]
        // ...or in a different direction:
        // [-<<<+>>>]
        // This idiom handles the "single copy" case.
        if (loopBlocks.size() != 4) {
            return null;
        }

        if (!(loopBlocks.get(0) instanceof SuperwordBrainfuckBlock)
                || !mightDecrement((SuperwordBrainfuckBlock) loopBlocks.get(0))) {
            return null;
        }

        // Count the number of >/< pairs. They must agree. We must also count how many times additions/subtractions
        // show up.
        int ptrRights = 0;
        int ptrLefts = 0;
        boolean haveAddition = false;
        boolean leftFirst = false;
        int additions = 0;
        for (BrainfuckBlock loopBlock : loopBlocks.subList(1, loopBlocks.size())) {
            if (!(loopBlock instanceof SuperwordBrainfuckBlock)) {
                return null;
            }

            SuperwordBrainfuckBlock word = (SuperwordBrainfuckBlock) loopBlock;
            if (word.getKeyword() == BrainfuckKeyword.INCREMENT_VAL) {
                if (!haveAddition) {
                    additions = word.getCount();
                    haveAddition = true;
                } else {
                    return null;
                }
            } else if (word.getKeyword() == BrainfuckKeyword.INCREMENT_PTR) {
                if (word.getCount() < 0) {
                    if (!leftFirst && ptrLefts == 0 && ptrRights == 0) {
                        leftFirst = true;
                    }
                    ptrLefts += Math.abs(word.getCount());
                } else {
                    ptrRights += Math.abs(word.getCount());
                }
            }
        }

        if (ptrRights == ptrLefts && additions != 0) {
            return new MultiplicationIdiomBlock(leftFirst ? -ptrLefts : ptrLefts, additions);
        } else {
            return null;
        }
    }

    private static boolean mightDecrement(SuperwordBrainfuckBlock block) {
        return block.getKeyword() == BrainfuckKeyword.DECREMENT_VAL
                || (block.getKeyword() == BrainfuckKeyword.INCREMENT_VAL && Math.signum(block.getCount()) == -1);
    }

    private static class MultiplicationIdiomBlock implements BrainfuckBlock {

        private final int cellsAndDirection;
        private final int by;

        private MultiplicationIdiomBlock(int cellsAndDirection, int by) {
            this.cellsAndDirection = cellsAndDirection;
            this.by = by;
        }

        @Override
        public void emit(GeneratorAdapter mv, int ptrVar) {
            // load the current value at this pointer
            mv.visitInsn(DUP2);
            mv.visitInsn(CALOAD);

            // cells
            // ptr
            // cells
            // ptr
            // value

            // put ptr on top, shift by the number of cells and their direction, swap again for multtiplication/CASTORE
            mv.visitInsn(SWAP);
            mv.push(cellsAndDirection);
            mv.visitInsn(IADD);
            mv.visitInsn(SWAP);

            if (this.by != 1) {
                mv.push(by);
                mv.visitInsn(IMUL);
            }

            // cells
            // ptr
            // cells
            // ptr-modified
            // value-modified

            // Store the value.
            mv.visitInsn(CASTORE);

            mv.visitInsn(DUP2);
            mv.push(0);
            mv.visitInsn(CASTORE);
        }

        @Override
        public String toString() {
            return "MULT => " + this.cellsAndDirection + " x " + this.by;
        }
    }
}
