import me.steinborn.brainchug.BrainfuckKeyword;
import me.steinborn.brainchug.compiler.BrainfuckClassCompiler;
import me.steinborn.brainchug.compiler.tree.BrainfuckBlock;
import me.steinborn.brainchug.compiler.tree.LoopBrainfuckBlock;
import me.steinborn.brainchug.compiler.tree.optimizer.*;
import me.steinborn.brainchug.reader.BrainfuckLexer;
import me.steinborn.brainchug.compiler.tree.BrainfuckTreeProducer;
import me.steinborn.brainchug.compiler.tree.ProgramBrainfuckBlock;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Test {
    public static void main(String... args) throws Exception {
        List<BrainfuckKeyword> lexed = BrainfuckLexer.lex(Files.newBufferedReader(Paths.get("mandelbrot.b")));
//        System.out.println(lexed);

        ProgramBrainfuckBlock tree = BrainfuckTreeProducer.treeify(lexed);

        List<Optimizer> optimizers = List.of(
                new GreedyPeepholeOptimizer(), new ZeroIdiomOptimizer(), new OffsetBakeOptimizer()//, new MultiplicationIdiomOptimizer()
        );

        for (Optimizer optimizer : optimizers) {
            tree = optimizer.optimize(tree);
        }
        dump(tree.getBlocks(), 0);

        byte[] klazz = BrainfuckClassCompiler.compile(tree, true);
        // CheckClassAdapter.verify(new ClassReader(klazz), true, new PrintWriter(System.err));

        try (OutputStream s = Files.newOutputStream(Paths.get("Produced.class"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            s.write(klazz);
        }

//        Class<?> x = MethodHandles.lookup().defineClass(klazz);
//        ((Runnable) x.newInstance()).run();
    }

    private static void dump(List<BrainfuckBlock> blocks, int level) {
        for (BrainfuckBlock subblock : blocks) {
            if (subblock instanceof LoopBrainfuckBlock) {
                System.out.println(" ".repeat(level) + "LOOP");
                dump(((LoopBrainfuckBlock) subblock).getBlocks(), level + 2);
            } else {
                System.out.println(" ".repeat(level) + subblock);
            }
        }
    }
}
