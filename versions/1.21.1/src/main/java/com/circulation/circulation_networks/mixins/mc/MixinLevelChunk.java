package com.circulation.circulation_networks.mixins.mc;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = LevelChunk.class, remap = false)
public abstract class MixinLevelChunk {

    @Shadow
    @Nullable
    public abstract BlockEntity getBlockEntity(BlockPos p_62912_);

    @Shadow
    @Final
    Level level;

    @Inject(method = "addAndRegisterBlockEntity", at = @At("TAIL"))
    private void addAndRegisterBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity != null) {
            CirculationFlowNetworks.onBlockEntityValidate(this.level, blockEntity.getBlockPos(), blockEntity);
        }
    }

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;setRemoved()V"))
    private void removeBlockEntity(BlockEntity blockentity, Operation<Void> original) {
        if (blockentity != null) {
            CirculationFlowNetworks.onBlockEntityInvalidate(this.level, blockentity.getBlockPos(), blockentity);
            blockentity.setRemoved();
        }
    }
}