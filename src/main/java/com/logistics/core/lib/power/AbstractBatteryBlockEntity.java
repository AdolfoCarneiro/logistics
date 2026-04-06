package com.logistics.core.lib.power;

import com.logistics.core.lib.BaseBlockEntity;
import com.logistics.core.lib.block.capability.HasEnergyStorage;
import com.logistics.core.lib.energy.EnergyComponent;
import com.logistics.core.lib.network.ILogisticsNetwork;
import com.logistics.core.lib.pipe.IPipeAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for energy buffer blocks that can supply power to adjacent pipe networks.
 *
 * <p>Concrete subclasses provide capacity/rate constants via the constructor.
 *
 * <p>Behavior:
 * <ul>
 *   <li>Accepts energy on all sides (Team Reborn Energy API + low-tier from Redstone Engines)</li>
 *   <li>Each tick: pushes energy into adjacent non-pipe blocks that accept it (machines, etc.)</li>
 *   <li>Each tick: registers this battery's storage with any adjacent logistics pipe networks
 *       so that modules can {@link ILogisticsNetwork#consumeEnergy draw from it transactionally}</li>
 *   <li>On removal: unregisters from all tracked networks</li>
 * </ul>
 *
 * <p>Multiple batteries adjacent to the same network are all registered; the network
 * draws from them in registration order until the requested amount is satisfied.
 */
public abstract class AbstractBatteryBlockEntity extends BaseBlockEntity
        implements HasEnergyStorage, AcceptsLowTierEnergy {

    /** Max RF to push into a single adjacent machine per tick. */
    private static final long MAX_OUTPUT_PER_SIDE = 200L;

    /** How often (in ticks) to refresh pipe-network registrations. */
    private static final int NETWORK_SCAN_INTERVAL = 20;

    protected final EnergyComponent energy;

    /** Networks this battery is currently registered with. */
    private final Set<ILogisticsNetwork> registeredNetworks = new HashSet<>();

    private int networkScanTick = 0;

    protected AbstractBatteryBlockEntity(
            BlockEntityType<?> type, BlockPos pos, BlockState state,
            long capacity, long maxInsert, long maxExtract) {
        super(type, pos, state);
        this.energy = new EnergyComponent(capacity, maxInsert, maxExtract, this::setChanged);
    }

    // ==================== Server Tick ====================

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractBatteryBlockEntity entity) {
        if (level.isClientSide()) return;
        entity.pushEnergyToMachines(level, pos);
        entity.refreshNetworkRegistrations(level, pos);
    }

    /** Push energy into adjacent blocks that have energy storage (skipping pipe blocks). */
    private void pushEnergyToMachines(Level level, BlockPos pos) {
        if (energy.amount <= 0) return;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            // Skip pipe-access blocks — they use the network energy transaction path
            if (level.getBlockEntity(neighborPos) instanceof IPipeAccess) continue;
            EnergyStorage target = EnergyStorage.SIDED.find(level, neighborPos, dir.getOpposite());
            if (target != null && target.supportsInsertion()) {
                EnergyStorageUtil.move(energy, target, MAX_OUTPUT_PER_SIDE, null);
            }
        }
    }

    /** Scan adjacent pipes to keep network registrations up to date. */
    private void refreshNetworkRegistrations(Level level, BlockPos pos) {
        networkScanTick++;
        if (networkScanTick < NETWORK_SCAN_INTERVAL) return;
        networkScanTick = 0;

        Set<ILogisticsNetwork> currentNetworks = new HashSet<>();
        for (Direction dir : Direction.values()) {
            if (level.getBlockEntity(pos.relative(dir)) instanceof IPipeAccess pipe) {
                ILogisticsNetwork net = pipe.getNetwork();
                if (net != null) currentNetworks.add(net);
            }
        }

        // Register with newly found networks
        for (ILogisticsNetwork net : currentNetworks) {
            if (registeredNetworks.add(net)) {
                net.registerEnergySource(worldPosition, energy);
            }
        }

        // Unregister from networks no longer adjacent
        Set<ILogisticsNetwork> toRemove = new HashSet<>(registeredNetworks);
        toRemove.removeAll(currentNetworks);
        for (ILogisticsNetwork net : toRemove) {
            net.unregisterEnergySource(worldPosition);
            registeredNetworks.remove(net);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (ILogisticsNetwork net : registeredNetworks) {
            net.unregisterEnergySource(worldPosition);
        }
        registeredNetworks.clear();
    }

    // ==================== HasEnergyStorage ====================

    @Override
    public EnergyStorage energyStorage(@Nullable Direction side) {
        return energy;
    }

    // ==================== AcceptsLowTierEnergy ====================

    @Override
    public boolean acceptsLowTierEnergyFrom(Direction from) {
        return true;
    }

    // ==================== NBT ====================

    @Override
    protected void saveLogisticsData(CompoundTag nbt) {
        super.saveLogisticsData(nbt);
        energy.writeNbt(nbt, "Energy");
    }

    @Override
    protected void loadLogisticsData(CompoundTag nbt) {
        super.loadLogisticsData(nbt);
        energy.readNbt(nbt, "Energy");
    }

    // ==================== Info ====================

    public long getEnergyStored() {
        return energy.amount;
    }

    public long getEnergyCapacity() {
        return energy.getCapacity();
    }
}
