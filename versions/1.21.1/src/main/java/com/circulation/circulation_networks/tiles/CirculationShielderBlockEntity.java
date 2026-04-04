package com.circulation.circulation_networks.tiles;

import com.circulation.circulation_networks.api.ICirculationShielderBlockEntity;
import com.circulation.circulation_networks.container.ContainerCirculationShielder;
import com.circulation.circulation_networks.manager.BlockEntityLifecycleAware;
import com.circulation.circulation_networks.manager.CirculationShielderManager;
import com.circulation.circulation_networks.registry.CFNBlockEntityTypes;
import com.circulation.circulation_networks.registry.CFNMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CirculationShielderBlockEntity extends BaseCFNBlockEntity implements ICirculationShielderBlockEntity, MenuProvider, BlockEntityLifecycleAware {

    private transient final BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos();
    private transient final BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos();
    private int scope;
    private boolean redstoneMode = false;
    private boolean showingRange = false;

    public CirculationShielderBlockEntity(BlockPos pos, BlockState state) {
        super(CFNBlockEntityTypes.CIRCULATION_SHIELDER, pos, state);
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.min.set(this.getBlockPos().getX() - scope, this.getBlockPos().getY() - scope, this.getBlockPos().getZ() - scope);
        this.max.set(this.getBlockPos().getX() + scope, this.getBlockPos().getY() + scope, this.getBlockPos().getZ() + scope);
        this.scope = scope;
    }

    public boolean isShowingRange() {
        return showingRange;
    }

    public void setShowingRange(boolean showingRange) {
        this.showingRange = showingRange;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.putInt("scope", this.scope);
        compound.putBoolean("RedstoneMode", this.redstoneMode);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);
        setScope(compound.getInt("scope"));
        this.redstoneMode = compound.getBoolean("RedstoneMode");
    }

    @Override
    public boolean checkScope(BlockPos pos) {
        return min.getX() <= pos.getX() && min.getY() <= pos.getY() && min.getZ() <= pos.getZ()
            && max.getX() >= pos.getX() && max.getY() >= pos.getY() && max.getZ() >= pos.getZ();
    }

    @Override
    public boolean isActive() {
        if (level == null) return false;
        int redstoneState = level.hasNeighborSignal(worldPosition) ? 1 : 0;
        if (redstoneMode) {
            return redstoneState == 1;
        } else {
            return redstoneState == 0;
        }
    }

    public void toggleRedstoneMode() {
        this.redstoneMode = !this.redstoneMode;
        setChanged();
    }

    public boolean getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(boolean mode) {
        this.redstoneMode = mode;
        setChanged();
    }

    @Override
    public BlockPos getPos() {
        return getBlockPos();
    }

    @Override
    public void onValidate() {
        if (level != null && !level.isClientSide()) {
            CirculationShielderManager.INSTANCE.register(this, level.dimension().location().hashCode());
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        onValidate();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        onValidate();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide()) {
            CirculationShielderManager.INSTANCE.unregister(this, level.dimension().location().hashCode());
        }
        super.setRemoved();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.circulation_networks.circulation_shielder");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new ContainerCirculationShielder(CFNMenuTypes.CIRCULATION_SHIELDER_MENU, containerId, player, this);
    }
}
