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

package net.daporkchop.pepsimod.module.impl.movement;

import net.daporkchop.pepsimod.module.ModuleCategory;
import net.daporkchop.pepsimod.module.api.Module;
import net.daporkchop.pepsimod.module.api.ModuleOption;
import net.daporkchop.pepsimod.module.api.OptionCompletions;
import net.daporkchop.pepsimod.module.api.option.ExtensionSlider;
import net.daporkchop.pepsimod.module.api.option.ExtensionType;
import net.daporkchop.pepsimod.util.config.impl.StepTranslator;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class StepMod extends Module {
    public static StepMod INSTANCE;

    {
        INSTANCE = this;
    }

    public StepMod() {
        super("Step");
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        if (pepsimod.hasInitializedModules) {
            mc.player.stepHeight = 0.5F;
        }
    }

    @Override
    public void tick() {
        if (StepTranslator.INSTANCE.legit) {
            EntityPlayerSP player = mc.player;

            player.stepHeight = 0.5f;

            if (!player.collidedHorizontally) {
                return;
            }

            if (!player.onGround || player.isOnLadder() || player.isInWater() || player.isInLava()) {
                return;
            }

            if (player.movementInput.moveForward == 0 && player.movementInput.moveStrafe == 0) {
                return;
            }

            if (player.movementInput.jump) {
                return;
            }

            AxisAlignedBB bb = player.getEntityBoundingBox().expand(0.0625, 0, 0.0625).expand(-0.0625, 0, -0.0625);
            boolean found = false;
            for (double d = 1.0d; d > 0.0d; d -= 1.0d / 16.0d)  {
                if (mc.world.getCollisionBoxes(player, bb.offset(0, d, 0)).isEmpty())   {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return;
            }

            double stepHeight = -1;
            List<AxisAlignedBB> bbs = mc.world.getCollisionBoxes(player, bb);
            for (AxisAlignedBB box : bbs) {
                if (box.maxY > stepHeight) {
                    stepHeight = box.maxY;
                }
            }

            stepHeight -= player.posY;

            if (stepHeight < 0 || stepHeight > 1) {
                return;
            }

            mc.player.connection.sendPacket(new CPacketPlayer.Position(player.posX, player.posY + 0.42 * stepHeight, player.posZ, player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(player.posX, player.posY + 0.753 * stepHeight, player.posZ, player.onGround));
            player.setPosition(player.posX, player.posY + 1 * stepHeight, player.posZ);
        } else {
            mc.player.stepHeight = StepTranslator.INSTANCE.height;
        }
    }

    @Override
    public void init() {
        INSTANCE = this;
    }

    @Override
    public ModuleOption[] getDefaultOptions() {
        return new ModuleOption[]{
                new ModuleOption<>(StepTranslator.INSTANCE.height, "height", OptionCompletions.INTEGER,
                        (value) -> {
                            StepTranslator.INSTANCE.height = Math.max(0, value);
                            return true;
                        },
                        () -> {
                            return StepTranslator.INSTANCE.height;
                        }, "Height", new ExtensionSlider(ExtensionType.VALUE_INT, 1, 64, 1)),
                new ModuleOption<>(StepTranslator.INSTANCE.legit, "legit", OptionCompletions.BOOLEAN,
                        (value) -> {
                            StepTranslator.INSTANCE.legit = value;
                            return true;
                        },
                        () -> {
                            return StepTranslator.INSTANCE.legit;
                        }, "Legit")
        };
    }

    public ModuleCategory getCategory() {
        return ModuleCategory.MOVEMENT;
    }

    @Override
    public boolean hasModeInName() {
        return true;
    }

    @Override
    public String getModeForName() {
        if (StepTranslator.INSTANCE.legit) {
            return "Legit";
        } else {
            return String.valueOf(StepTranslator.INSTANCE.height);
        }
    }
}
