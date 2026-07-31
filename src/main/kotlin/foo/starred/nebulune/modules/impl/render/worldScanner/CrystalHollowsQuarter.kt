package foo.starred.nebulune.modules.impl.render.worldScanner

import net.minecraft.core.BlockPos
import java.util.function.Predicate

enum class CrystalHollowsQuarter(
    private val predicate: Predicate<in BlockPos>
) {
    NUCLEUS(Predicate { blockPos ->
        blockPos.x in 449..576 &&
                blockPos.z in 449..576
    }),
    // expand by 4 chunks to make sure we don't miss any structures
    JUNGLE(Predicate { blockPos ->
        blockPos.x <= 576 && blockPos.z <= 576
    }),

    PRECURSOR_REMNANTS(Predicate { blockPos ->
        blockPos.x > 448 && blockPos.z > 448
    }),

    GOBLIN_HOLDOUT(Predicate { blockPos ->
        blockPos.x <= 576 && blockPos.z > 448
    }),

    MITHRIL_DEPOSITS(Predicate { blockPos ->
        blockPos.x > 448 && blockPos.z <= 576
    }),

    MAGMA_FIELDS(Predicate { blockPos ->
        blockPos.y < 80
    }),

    OUT_OF_BOUND(Predicate { blockPos ->
        blockPos.x > 824 || blockPos.z > 824 ||
                blockPos.x < 201 || blockPos.z < 201
    }),

    ANY(Predicate { true });

    fun testPredicate(blockPos: BlockPos): Boolean {
        return predicate.test(blockPos)
    }
}