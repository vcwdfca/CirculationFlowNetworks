package com.circulation.circulation_networks.blocks;

import com.circulation.circulation_networks.items.BaseItemTooltipModel;
import com.circulation.circulation_networks.tooltip.LocalizedComponent;
import com.circulation.circulation_networks.utils.CI18n;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public abstract class BaseBlock extends Block implements EntityBlock {

    private String[] cachedAutoTooltipKeys;

    protected BaseBlock(Properties properties) {
        super(properties);
    }

    protected List<LocalizedComponent> buildTooltips(ItemStack stack) {
        if (cachedAutoTooltipKeys == null) {
            cachedAutoTooltipKeys = BaseItemTooltipModel.resolveTooltipKeys(getDescriptionId(), CI18n::hasKey);
        }
        if (cachedAutoTooltipKeys.length == 0) {
            return Collections.emptyList();
        }
        List<LocalizedComponent> result = new ObjectArrayList<>(cachedAutoTooltipKeys.length);
        for (var key : cachedAutoTooltipKeys) {
            result.add(LocalizedComponent.of(key));
        }
        return result;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        for (var lc : buildTooltips(stack)) {
            tooltip.add(Component.literal(lc.get()));
        }
    }

    public abstract boolean hasGui();

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide() && hasGui()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MenuProvider menuProvider && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(menuProvider, pos);
                return InteractionResult.CONSUME;
            }
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return null;
    }
}
