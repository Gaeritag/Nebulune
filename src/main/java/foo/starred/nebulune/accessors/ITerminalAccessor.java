package foo.starred.nebulune.accessors;

import foo.starred.athen.modules.impl.dungeon.terminals.solver.base.Click;

import java.util.concurrent.CopyOnWriteArrayList;

public interface ITerminalAccessor {
    CopyOnWriteArrayList<Click> nebulune$getList();
    float nebulune$float();
    int nebulune$int0();
    int nebulune$int1();
}