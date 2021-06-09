# Brainchug

Brainchug is an implementation of the venerable [Brainfuck](https://en.wikipedia.org/wiki/Brainfuck) esolang on the JVM.
It compiles the Brainfuck source code to JVM bytecode, generating classes which implement the `Runnable` interface.

This is some work I'm planning to write up on my blog.

## Should I use this?

Only if you like incomplete software, esolangs running on platforms that don't usually get esoteric languages
implemented on them, and have a cruel sense of humor.

## Licensing?

MIT license, see `LICENSE` for details.

## Current status

Functionality-wise, it is a complete implementation of Brainfuck, but it is hilariously unoptimized (not even basic
peephole optimizations have been applied).