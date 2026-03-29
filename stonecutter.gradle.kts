plugins {
    id("dev.kikugie.stonecutter")
    id("dev.kikugie.fletching-table") version "0.1.0-alpha.13" apply false
}

stonecutter parameters {
    replacements.string(eval(current.version, ">=1.20"), "mc_imports") {
        replace("import net.minecraft.item.ItemStack;", "import net.minecraft.world.item.ItemStack;")
        replace("import net.minecraft.item.Item;", "import net.minecraft.world.item.Item;")
        replace("import net.minecraft.nbt.NBTTagCompound;", "import net.minecraft.nbt.CompoundTag;")
        replace("import net.minecraft.nbt.NBTTagList;", "import net.minecraft.nbt.ListTag;")
        replace("import net.minecraft.nbt.NBTTagLong;", "import net.minecraft.nbt.LongTag;")
        replace("import net.minecraft.tileentity.TileEntity;", "import net.minecraft.world.level.block.entity.BlockEntity;")
        replace("import net.minecraft.util.math.BlockPos;", "import net.minecraft.core.BlockPos;")
        replace("import net.minecraft.util.math.ChunkPos;", "import net.minecraft.world.level.ChunkPos;")
        replace("import net.minecraft.world.World;", "import net.minecraft.world.level.Level;")
    }
}

stonecutter.active("1.12.2")