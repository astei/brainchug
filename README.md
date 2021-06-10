# Brainchug

Brainchug is an implementation of the venerable [Brainfuck](https://en.wikipedia.org/wiki/Brainfuck) esolang on the JVM.
It compiles the Brainfuck source code to JVM bytecode, generating classes which implement the `Runnable` interface.

This is some work I'm planning to write up on my blog.

## Environment

Brainchug presents to the executed Brainfuck program an execution environment that has:

* 16,384 signed 16-bit cells that do not wrap
* EOF is -1

## Should I use this?

Only if you like incomplete software, esolangs running on platforms that don't usually get esoteric languages
implemented on them, and have a cruel sense of humor.

## Licensing?

MIT license, see `LICENSE` for details.

## Current status

Functionality-wise, it is a complete implementation of Brainfuck, but it is hilariously unoptimized. Only the very basic
of peephole optimizations has been applied.