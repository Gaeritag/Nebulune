package foo.starred.nebulune.accessors;

import foo.starred.athen.modules.impl.dungeon.terminals.solver.data.TerminalClick;

import java.util.concurrent.CopyOnWriteArrayList;

public interface ITerminalAccessor {
    CopyOnWriteArrayList<TerminalClick> nebulune$getList();
    float nebulune$float();
    int nebulune$int0();
    int nebulune$int1();
}