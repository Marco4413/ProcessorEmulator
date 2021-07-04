package io.github.hds.pemu.memory.flags;

public interface IMemoryFlag extends IFlag {
    int getAddress();
    int getBit();
}
