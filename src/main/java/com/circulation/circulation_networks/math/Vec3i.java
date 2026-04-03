package com.circulation.circulation_networks.math;

//? if <1.20 {
import com.github.bsideup.jabel.Desugar;

import javax.annotation.Nonnull;

@Desugar
//?}
public record Vec3i(int x, int y, int z) {

    public double distanceSquared(double x, double y, double z) {
        double dx = this.x - x;
        double dy = this.y - y;
        double dz = this.z - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distanceSquared(Vec3i other) {
        return distanceSquared(other.x, other.y, other.z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vec3i other)) {
            return false;
        }
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    @Nonnull
    public String toString() {
        return "Vec3i{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}