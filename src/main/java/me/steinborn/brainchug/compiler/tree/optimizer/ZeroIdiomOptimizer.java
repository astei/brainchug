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

/**
 * Optimizes the zero-cell idiom ([-]).
 */
public class ZeroIdiomOptimizer implements Optimizer {
    @Override
    public ProgramBrainfuckBlock optimize(ProgramBrainfuckBlock in) {
        return new ProgramBrainfuckBlock(process(in.getBlocks()));
    }

    private List<BrainfuckBlock> process(List<BrainfuckBlock> blocks) {
        List<BrainfuckBlock> optimized = new ArrayList<>();

        for (BrainfuckBlock block : blocks) {
            if (block instanceof LoopBrainfuckBlock) {
                List<BrainfuckBlock> loopBlocks = ((LoopBrainfuckBlock) block).getBlocks();
                if (loopBlocks.size() == 1 && loopBlocks.get(0) instanceof SuperwordBrainfuckBlock &&
                        mightDecrement((SuperwordBrainfuckBlock) loopBlocks.get(0))) {
                    // Common idiom: [-] will set the value in the given cell at the position to 0.
                    optimized.add(new ZeroIdiomInstruction(0));
                } else {
                    optimized.add(new LoopBrainfuckBlock(process(((LoopBrainfuckBlock) block).getBlocks())));
                }
            } else {
                optimized.add(block);
            }
        }

        return optimized;
    }

    private static boolean mightDecrement(SuperwordBrainfuckBlock block) {
        return !block.isPtr() & Math.signum(block.getCount()) == -1;
    }

    public static class ZeroIdiomInstruction implements BrainfuckBlock {

        private final int offset;

        public ZeroIdiomInstruction(int offset) {
            this.offset = offset;
        }

        @Override
        public void emit(GeneratorAdapter mv, int ptrVar) {
            mv.visitInsn(DUP2);
            if (this.offset != 0) {
                mv.push(this.offset);
                mv.visitInsn(IADD);
            }
            mv.visitInsn(ICONST_0);
            mv.visitInsn(CASTORE);
        }

        @Override
        public String toString() {
            if (this.offset != 0) {
                return "ZERO => " + this.offset;
            } else {
                return "ZERO";
            }
        }
    }
}
