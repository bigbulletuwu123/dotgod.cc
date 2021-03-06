/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2016-2020 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.pepsimod.module.impl.player;

import net.daporkchop.pepsimod.module.ModuleCategory;
import net.daporkchop.pepsimod.module.api.Module;
import net.daporkchop.pepsimod.module.api.ModuleOption;
import net.daporkchop.pepsimod.the.wurst.pkg.name.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ScaffoldMod extends Module {
    public static ScaffoldMod INSTANCE;

    {
        INSTANCE = this;
    }

    public ScaffoldMod() {
        super("Scaffold");
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void tick() {
        BlockPos belowPlayer = new BlockPos(mc.player).down();

        // check if block is already placed
        IBlockState state = mc.world.getBlockState(belowPlayer);
        if (!state.getBlock().isReplaceable(mc.world, belowPlayer)) {
            return;
        }

        // search blocks in hotbar
        int newSlot = -1;
        for (int i = 0; i < 9; i++) {
            // filter out non-block items
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            // filter out non-solid blocks
            Block block = Block.getBlockFromItem(stack.getItem());
            if (!block.getDefaultState().isFullBlock() && !(block instanceof BlockPistonBase)) {
                continue;
            }

            newSlot = i;
            break;
        }

        // check if any blocks were found
        if (newSlot == -1) {
            return;
        }

        // set slot
        int oldSlot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = newSlot;

        // place block
        BlockUtils.placeBlockScaffold(belowPlayer);

        // reset slot
        mc.player.inventory.currentItem = oldSlot;
    }

    @Override
    public void init() {
        INSTANCE = this;
    }

    @Override
    public ModuleOption[] getDefaultOptions() {
        return new ModuleOption[0];
    }

    public ModuleCategory getCategory() {
        return ModuleCategory.PLAYER;
    }
}
