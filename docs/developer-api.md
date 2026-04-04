# 开发者 API

[English](developer-api-en.md) | **中文**

环流网络（Circulation Flow Networks）模组提供了一套完整的 API 供其他模组集成自定义能源体系、扩展节点类型和查询网络状态。

本模组使用 [Stonecutter](https://stonecutter.kikugie.dev/) 进行多版本预处理。在以下文档中，1.12.2 版本的类名（如
`TileEntity`、`World`、`NBTTagCompound`）在 1.20+ 版本中分别对应 `BlockEntity`、`Level`、`CompoundTag`。

**包路径**：`com.circulation.circulation_networks.api`

---

## 目录

- [快速开始](#快速开始)
- [API 类方法总览](#api-类方法总览)
    - [节点查询](#节点查询)
    - [能量供应节点](#能量供应节点)
    - [中枢频道](#中枢频道)
    - [能量类型判断](#能量类型判断)
    - [注册](#注册)
- [节点接口](#节点接口)
    - [INode](#inode)
    - [IMachineNode](#imachinenode)
    - [IEnergySupplyNode](#ienergysupplynode)
    - [IHubNode](#ihubnode)
    - [IChargingNode](#ichargingnode)
    - [NodeType\<N\>](#nodetypen)
    - [NodeContext](#nodecontext)
    - [NodeDeserializer](#nodedeserializer)
- [方块实体接口](#方块实体接口)
    - [INodeBlockEntity](#inodeblockentity)
    - [IMachineNodeBlockEntity](#imachinenodeblockentity)
    - [IHubNodeBlockEntity](#ihubnodeblockentity)
    - [ICirculationShielderBlockEntity](#icirculationshielderblockentity)
- [能量系统](#能量系统)
    - [IEnergyHandler](#ienergyhandler)
    - [IEnergyHandlerManager](#ienergyhandlermanager)
    - [EnergyAmount](#energyamount)
    - [EnergyAmounts](#energyamounts)
    - [ConstantEnergyAmount](#constantenergyamount)
- [中枢系统](#中枢系统)
    - [IHubPlugin](#ihubplugin)
    - [IHubChannel](#ihubchannel)
    - [HubPermissionLevel](#hubpermissionlevel)
    - [PermissionMode](#permissionmode)
    - [ChargingDefinition](#chargingdefinition)
    - [ChargingPreference](#chargingpreference)
    - [快照类](#快照类)
- [Tick 接口](#tick-接口)
    - [ClientTickMachine](#clienttickmachine)
    - [ServerTickMachine](#servertickmachine)
- [网格接口](#网格接口)
    - [IGrid](#igrid)

---

## 快速开始

```
import com.circulation.circulation_networks.api.API;
import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IEnergyHandlerManager;
import com.circulation.circulation_networks.api.node.INode;

// 注册自定义能源处理器（必须在 postInit 阶段前调用）
API.registerEnergyHandler(myEnergyHandlerManager);

// 查询方块实体的能量处理器管理器
IEnergyHandlerManager mgr = API.getEnergyManager(tileEntity);

// 查询物品堆的能量处理器管理器
IEnergyHandlerManager itemMgr = API.getEnergyManager(itemStack);

// 判断方块实体是否为已注册的能源容器
boolean isEnergy = API.isEnergyTileEntity(tileEntity);

// 获取指定坐标处的节点
INode node = API.getNodeAt(world, pos);

// 获取所有活跃节点
ReferenceSet<INode> allNodes = API.getAllNodes();
```

---

## API 类方法总览

`com.circulation.circulation_networks.api.API` 是唯一的公开 API 入口类，所有方法均为 `static`。该类的方法名保证不会被重命名。

### 节点查询

| 方法签名                                                                                               | 说明                        |
|----------------------------------------------------------------------------------------------------|---------------------------|
| `@Nullable INode getNodeAt(@Nonnull World world, @Nonnull BlockPos pos)`                           | 根据坐标获取节点。可以获取到未被加载区块内的节点。 |
| `@Nonnull ReferenceSet<INode> getAllNodes()`                                                       | 返回当前所有处于活跃状态的节点。          |
| `@Nonnull Collection<IGrid> getAllGrids()`                                                         | 返回当前所有处于可用状态的网格。          |
| `@Nonnull ReferenceSet<INode> getNodesCoveringPos(@Nonnull World world, @Nonnull BlockPos pos)`    | 获取链接范围覆盖指定位置所在区块的所有节点。    |
| `@Nonnull ReferenceSet<INode> getNodesCoveringChunk(@Nonnull World world, int chunkX, int chunkZ)` | 获取链接范围覆盖指定区块的所有节点。        |
| `@Nonnull ReferenceSet<INode> getNodesInChunk(@Nonnull World world, int chunkX, int chunkZ)`       | 返回位于指定区块内的所有活跃节点。         |

### 能量供应节点

| 方法签名                                                                                                               | 说明                                                   |
|--------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| `@Nonnull ReferenceSet<IEnergySupplyNode> getEnergyNodes(@Nonnull World world, @Nonnull BlockPos pos)`             | 返回供能范围覆盖指定位置所在区块的所有能量供应节点。                           |
| `@Nonnull ReferenceSet<IEnergySupplyNode> getEnergyNodes(@Nonnull World world, int chunkX, int chunkZ)`            | 返回供能范围覆盖指定区块的所有能量供应节点。                               |
| `@Deprecated @Nonnull ReferenceSet<IEnergySupplyNode> getEnergyNodes(@Nonnull World world, @Nonnull ChunkPos pos)` | **已弃用**。使用 chunk 坐标或 `BlockPos` 重载代替。                |
| `@Nonnull Set<TileEntity> getMachinesSuppliedBy(@Nonnull IEnergySupplyNode node)`                                  | 返回指定能量供应节点当前所链接的所有设备。返回集可能包含同时拥有 `IMachineNode` 的实体。 |

### 中枢频道

| 方法签名                                                                    | 说明                                            |
|-------------------------------------------------------------------------|-----------------------------------------------|
| `@Nonnull ReferenceSet<IGrid> getChannelGrids(@Nonnull UUID channelId)` | 返回指定频道 UUID 所关联的所有网格。中枢节点通过相同频道 UUID 跨网格共享能量。 |

### 能量类型判断

| 方法签名                                                                                | 说明                                                      |
|-------------------------------------------------------------------------------------|---------------------------------------------------------|
| `boolean isEnergyBlacklisted(@Nonnull TileEntity blockEntity)`                      | 判断方块实体是否位于能源全局黑名单中。黑名单实体不会被任何节点识别为能源容器。                 |
| `boolean isSupplyBlacklisted(@Nonnull TileEntity blockEntity)`                      | 判断方块实体是否位于供应节点黑名单中。黑名单实体只能由覆写了 `isBlacklisted` 的专用节点连接。 |
| `boolean isEnergyItem(@Nonnull ItemStack stack)`                                    | 判断物品堆是否为受能量处理器管理的能源物品。                                  |
| `boolean isEnergyTileEntity(@Nonnull TileEntity blockEntity)`                       | 判断方块实体是否为受能量处理器管理的能源容器。                                 |
| `@Nullable IEnergyHandlerManager getEnergyManager(@Nonnull TileEntity blockEntity)` | 获取适用于指定方块实体的能量处理器管理器，无匹配时返回 `null`。                     |
| `@Nullable IEnergyHandlerManager getEnergyManager(@Nonnull ItemStack stack)`        | 获取适用于指定物品堆的能量处理器管理器，无匹配时返回 `null`。                      |

### 注册

| 方法签名                                                                                                     | 说明                                  |
|----------------------------------------------------------------------------------------------------------|-------------------------------------|
| `void registerEnergyHandler(@Nonnull IEnergyHandlerManager manager)`                                     | 注册自定义的能量管理器。**必须在 postInit 阶段前调用**。 |
| `void registerNodeType(@Nonnull NodeType<? extends INode> nodeType, @Nonnull NodeDeserializer function)` | 注册自定义节点类型及其 NBT 反序列化函数。             |

---

## 节点接口

### INode

`com.circulation.circulation_networks.api.node.INode`

所有节点的基础接口，定义了节点的位置、世界、链接范围、邻居管理及网格归属。

| 方法                                | 返回类型                   | 说明              |
|-----------------------------------|------------------------|-----------------|
| `getPos()`                        | `@Nonnull BlockPos`    | 节点的方块坐标         |
| `getVec3d()`                      | `@Nonnull Vec3d`       | 节点的精确向量坐标       |
| `getWorld()`                      | `@Nonnull World`       | 节点所在的世界         |
| `getNodeType()`                   | `@Nonnull NodeType<?>` | 节点类型标识          |
| `getVisualId()`                   | `@Nonnull String`      | 视觉标识（通常为注册表 ID） |
| `serialize()`                     | `NBTTagCompound`       | 将节点序列化为 NBT     |
| `isActive()`                      | `boolean`              | 节点是否处于活跃状态      |
| `setActive(boolean)`              | `void`                 | 设置节点活跃状态        |
| `getLinkScope()`                  | `double`               | 链接范围（方块距离）      |
| `getLinkScopeSq()`                | `double`               | 链接范围的平方（用于距离检测） |
| `getNeighbors()`                  | `ReferenceSet<INode>`  | 当前已链接的邻居节点      |
| `addNeighbor(INode)`              | `void`                 | 添加邻居链接          |
| `removeNeighbor(INode)`           | `void`                 | 移除邻居链接          |
| `clearNeighbors()`                | `void`                 | 清空所有邻居链接        |
| `getGrid()`                       | `IGrid`                | 节点所属的网格         |
| `setGrid(IGrid)`                  | `void`                 | 设置节点的网格归属       |
| `getCustomName()`                 | `@Nullable String`     | 节点的自定义名称        |
| `setCustomName(@Nullable String)` | `void`                 | 设置自定义名称         |
| `distanceSq(INode)`               | `double`               | 与另一节点的距离平方      |
| `distanceSq(BlockPos)`            | `double`               | 与坐标的距离平方        |
| `distanceSq(Vec3d)`               | `double`               | 与向量的距离平方        |
| `linkScopeCheck(INode)`           | `LinkType`             | 检查两个节点的链接范围关系   |

**内部枚举 `LinkType`**：

| 值            | 说明          |
|--------------|-------------|
| `DOUBLY`     | 双向可链接       |
| `A_TO_B`     | 仅 A 的范围可达 B |
| `B_TO_A`     | 仅 B 的范围可达 A |
| `DISCONNECT` | 双方均不可达      |

---

### IMachineNode

`com.circulation.circulation_networks.api.node.IMachineNode extends IEnergySupplyNode`

机器节点接口，表示网络中直接与能源设备交互的节点。

| 方法          | 返回类型                        | 说明                                |
|-------------|-----------------------------|-----------------------------------|
| `getType()` | `IEnergyHandler.EnergyType` | 节点的能量类型（SEND / RECEIVE / STORAGE） |

继承自 `IEnergySupplyNode` 和 `INode` 的所有方法。

---

### IEnergySupplyNode

`com.circulation.circulation_networks.api.node.IEnergySupplyNode extends INode`

能量供应节点接口，标识该节点可与周围设备进行能量交互。

| 方法                           | 返回类型      | 说明                   |
|------------------------------|-----------|----------------------|
| `getEnergyScope()`           | `double`  | 供能范围（方块距离）           |
| `getEnergyScopeSq()`         | `double`  | 供能范围的平方              |
| `supplyScopeCheck(BlockPos)` | `boolean` | 默认实现：判断坐标是否在供能范围内    |
| `isBlacklisted(TileEntity)`  | `boolean` | 默认实现：检查方块实体是否在供应黑名单中 |

---

### IHubNode

`com.circulation.circulation_networks.api.node.IHubNode extends IEnergySupplyNode, IChargingNode`

中枢节点接口。一个网络中只能存在一个中枢节点。中枢节点同时具有能量供应和玩家充能能力。

> **唯一内部实现**：此接口由 `HubNode` 单例实现，外部模组不应实现此接口，仅供查询和交互使用。

| 方法                                                    | 返回类型                            | 说明                            |
|-------------------------------------------------------|---------------------------------|-------------------------------|
| `getPermissionMode()`                                 | `PermissionMode`                | 权限模式（PUBLIC / TEAM / PRIVATE） |
| `setPermissionMode(PermissionMode)`                   | `void`                          | 设置权限模式                        |
| `getPlugins()`                                        | `IItemHandler`                  | 获取插件物品栏                       |
| `getHubData()`                                        | `HubMetadata`                   | 获取中枢元数据                       |
| `hasPluginCapability(HubPluginCapability<?>)`         | `boolean`                       | 默认实现：检查是否拥有指定插件能力             |
| `getPluginCapabilityData(HubPluginCapability<T>)`     | `T`                             | 默认实现：获取插件能力数据                 |
| `getChannelId()`                                      | `@Nonnull UUID`                 | 频道 UUID                       |
| `setChannelId(@Nonnull UUID)`                         | `void`                          | 设置频道 UUID                     |
| `getChannelName()`                                    | `@Nonnull String`               | 频道名称                          |
| `setChannelName(@Nonnull String)`                     | `void`                          | 设置频道名称                        |
| `getChargingPreference(UUID)`                         | `@Nonnull ChargingPreference`   | 获取指定玩家的充能偏好                   |
| `setChargingPreference(UUID, ChargingPreference)`     | `void`                          | 设置玩家充能偏好                      |
| `getChargingState(UUID, ChargingDefinition)`          | `boolean`                       | 获取指定玩家某充能槽位的开关状态              |
| `setChargingState(UUID, ChargingDefinition, boolean)` | `void`                          | 设置充能槽位状态                      |
| `getOwner()`                                          | `@Nullable UUID`                | 中枢所有者                         |
| `setOwner(@Nullable UUID)`                            | `void`                          | 设置所有者                         |
| `getExplicitPermission(UUID)`                         | `@Nullable HubPermissionLevel`  | 获取指定玩家的显式权限等级                 |
| `getExplicitPermissions()`                            | `Map<UUID, HubPermissionLevel>` | 获取所有玩家的显式权限映射                 |
| `setExplicitPermission(UUID, HubPermissionLevel)`     | `void`                          | 设置玩家的显式权限                     |
| `removeExplicitPermission(UUID)`                      | `void`                          | 移除玩家的显式权限                     |
| `getPermissionLevel(UUID)`                            | `HubPermissionLevel`            | 获取玩家的最终权限等级（综合显式权限和权限模式）      |
| `canEditPermissions(UUID)`                            | `boolean`                       | 玩家是否可编辑权限                     |

---

### IChargingNode

`com.circulation.circulation_networks.api.node.IChargingNode extends INode`

充能节点接口，标识该节点可向范围内的玩家物品充能。

| 方法                             | 返回类型      | 说明                    |
|--------------------------------|-----------|-----------------------|
| `getChargingScope()`           | `double`  | 充能范围（方块距离）            |
| `getChargingScopeSq()`         | `double`  | 默认实现：充能范围的平方，实现类应缓存此值 |
| `chargingScopeCheck(BlockPos)` | `boolean` | 默认实现：判断坐标是否在充能范围内     |

---

### NodeType\<N\>

`com.circulation.circulation_networks.api.node.NodeType<N extends INode>`

节点类型标识接口，用于注册和区分不同类型的节点。

| 方法                      | 返回类型                | 说明                            |
|-------------------------|---------------------|-------------------------------|
| `id()`                  | `@NotNull String`   | 节点类型的唯一标识符                    |
| `nodeClass()`           | `@NotNull Class<N>` | 节点类的 Class 对象                 |
| `allowsPocketNode()`    | `boolean`           | 是否允许口袋节点形态                    |
| `fallbackVisualId()`    | `@NotNull String`   | 回退视觉标识                        |
| `getId()`               | `@NotNull String`   | 默认实现：等同于 `id()`               |
| `getNodeClass()`        | `@NotNull Class<N>` | 默认实现：等同于 `nodeClass()`        |
| `getFallbackVisualId()` | `@NotNull String`   | 默认实现：等同于 `fallbackVisualId()` |
| `matches(INode)`        | `boolean`           | 默认实现：检查节点是否为此类型               |
| `cast(INode)`           | `@NotNull N`        | 默认实现：将节点强转为此类型                |

**注册自定义节点类型示例**：

```
// 定义节点类型
NodeType<MyCustomNode> MY_NODE_TYPE = new NodeType<>() {
        @Override
        public @NotNull String id() {
            return "mymod:my_node";
        }

        @Override
        public @NotNull Class<MyCustomNode> nodeClass() {
            return MyCustomNode.class;
        }

        @Override
        public boolean allowsPocketNode() {
            return true;
        }

        @Override
        public @NotNull String fallbackVisualId() {
            return "mymod:my_node_block";
        }
    };

// 注册节点类型和反序列化器
API.registerNodeType(MY_NODE_TYPE, nbt ->MyCustomNode.deserialize(nbt));
```

---

### NodeContext

`com.circulation.circulation_networks.api.node.NodeContext`

节点创建上下文，封装了创建节点所需的世界、坐标、默认名称和视觉标识。

| 方法                                    | 返回类型                 | 说明                  |
|---------------------------------------|----------------------|---------------------|
| `fromWorld(World, BlockPos)`          | `static NodeContext` | 从世界和坐标自动解析默认名称及视觉标识 |
| `of(World, BlockPos, String, String)` | `static NodeContext` | 手动指定所有参数            |
| `getWorld()`                          | `@NotNull World`     | 获取世界                |
| `getPos()`                            | `@NotNull BlockPos`  | 获取坐标                |
| `getDefaultName()`                    | `@NotNull String`    | 获取默认名称（通常为方块本地化名称）  |
| `getVisualId()`                       | `@NotNull String`    | 获取视觉标识（通常为方块注册表 ID） |

---

### NodeDeserializer

`com.circulation.circulation_networks.api.NodeDeserializer`

函数式接口，继承自 `Function<NBTTagCompound, INode>`，用于从 NBT 数据反序列化节点。

通过 `API.registerNodeType()` 注册。

---

## 方块实体接口

### INodeBlockEntity

`com.circulation.circulation_networks.api.INodeBlockEntity`

节点方块实体基础接口，方块实体实现此接口以表明其关联了一个网络节点。

| 方法               | 返回类型       | 说明        |
|------------------|------------|-----------|
| `getNode()`      | `INode`    | 获取关联的节点   |
| `getNodePos()`   | `BlockPos` | 获取节点坐标    |
| `getNodeWorld()` | `World`    | 获取节点所在的世界 |

---

### IMachineNodeBlockEntity

`com.circulation.circulation_networks.api.IMachineNodeBlockEntity extends INodeBlockEntity`

机器节点方块实体接口。

| 方法                   | 返回类型             | 说明                |
|----------------------|------------------|-------------------|
| `getNode()`          | `IMachineNode`   | 获取关联的机器节点（覆写返回类型） |
| `getEnergyHandler()` | `IEnergyHandler` | 获取节点的能量处理器        |

---

### IHubNodeBlockEntity

`com.circulation.circulation_networks.api.IHubNodeBlockEntity extends INodeBlockEntity`

中枢节点方块实体接口。

| 方法             | 返回类型           | 说明                |
|----------------|----------------|-------------------|
| `getNode()`    | `IHubNode`     | 获取关联的中枢节点（覆写返回类型） |
| `getPlugins()` | `IItemHandler` | 获取中枢插件物品栏         |

---

### ICirculationShielderBlockEntity

`com.circulation.circulation_networks.api.ICirculationShielderBlockEntity`

环流屏蔽仪方块实体接口。环流屏蔽仪阻止其范围内的节点自动链接。

| 方法                     | 返回类型       | 说明           |
|------------------------|------------|--------------|
| `checkScope(BlockPos)` | `boolean`  | 判断坐标是否在屏蔽范围内 |
| `isActive()`           | `boolean`  | 屏蔽仪是否处于工作状态  |
| `getScope()`           | `int`      | 获取屏蔽范围（方块距离） |
| `isShowingRange()`     | `boolean`  | 是否正在显示范围可视化  |
| `getPos()`             | `BlockPos` | 屏蔽仪的坐标       |

---

## 能量系统

### IEnergyHandler

`com.circulation.circulation_networks.api.IEnergyHandler`

能量处理器接口，封装了对具体能源系统（如 FE、EU 等）的能量提取与注入操作。实例通过对象池管理以减少 GC 压力。

**生命周期方法**：

| 方法                                 | 返回类型          | 说明                 |
|------------------------------------|---------------|--------------------|
| `release(TileEntity, HubMetadata)` | `static @Nullable IEnergyHandler` | 从对象池获取方块实体用处理器；若池为空则新建 |
| `release(ItemStack, HubMetadata)`  | `static @Nullable IEnergyHandler` | 从对象池获取物品用处理器；若池为空则新建   |
| `init(TileEntity, HubMetadata)`    | `IEnergyHandler` | 以方块实体初始化处理器状态，并返回 `this` |
| `init(ItemStack, HubMetadata)`     | `IEnergyHandler` | 以物品堆初始化处理器状态，并返回 `this`  |
| `clear()`                          | `void`        | 仅清空处理器内部状态；该生命周期重置不再依赖 `HubMetadata` |
| `recycle()`                        | `void`        | 调用 `clear()` 后将处理器实例回收至对象池 |

**能量操作方法**：

| 方法                                         | 返回类型         | 说明                                   |
|--------------------------------------------|--------------|--------------------------------------|
| `receiveEnergy(EnergyAmount, HubMetadata)` | `EnergyAmount` | 向容器注入能量，并返回实际成功注入的数量                |
| `extractEnergy(EnergyAmount, HubMetadata)` | `EnergyAmount` | 从容器提取能量，并返回实际成功提取的数量                |
| `canExtractValue(HubMetadata)`             | `EnergyAmount` | 返回当前可提取的能量数量                         |
| `canReceiveValue(HubMetadata)`             | `EnergyAmount` | 返回当前可接收的能量数量                         |
| `canExtract(IEnergyHandler, HubMetadata)`  | `boolean`    | 是否可从此处理器提取（用于兼容性检查）                  |
| `canReceive(IEnergyHandler, HubMetadata)`  | `boolean`    | 是否可向此处理器注入（用于兼容性检查）                  |
| `getType(HubMetadata)`                     | `EnergyType` | 处理器的能量类型                             |

**内部枚举 `EnergyType`**：

| 值         | 说明        |
|-----------|-----------|
| `SEND`    | 仅输出能量     |
| `RECEIVE` | 仅接收能量     |
| `STORAGE` | 可双向传输（储能） |
| `INVALID` | 无效状态      |

---

### IEnergyHandlerManager

`com.circulation.circulation_networks.api.IEnergyHandlerManager implements Comparable<IEnergyHandlerManager>`

能量处理器管理器接口。每种能源体系（FE、EU 等）需实现此接口并通过 `API.registerEnergyHandler()` 注册。管理器通过优先级排序来决定应用顺序。

| 方法                        | 返回类型                              | 说明                  |
|---------------------------|-----------------------------------|---------------------|
| `isAvailable(TileEntity)` | `boolean`                         | 判断方块实体是否由此管理器处理     |
| `isAvailable(ItemStack)`  | `boolean`                         | 判断物品堆是否由此管理器处理      |
| `getEnergyHandlerClass()` | `Class<? extends IEnergyHandler>` | 返回关联的处理器实现类         |
| `getPriority()`           | `int`                             | 管理器优先级，数值越小优先级越高    |
| `newBlockEntityInstance()` | `IEnergyHandler`                | 为方块实体场景创建新的处理器实例    |
| `newItemInstance()`        | `IEnergyHandler`                | 为物品场景创建新的处理器实例      |
| `getUnit()`               | `String`                          | 默认返回 `"FE"`。能量单位名称。 |
| `getMultiplying()`        | `double`                          | 默认返回 `1`。能量值的乘数系数。  |

**自定义能量系统集成示例**：

```
public class MyEnergyManager implements IEnergyHandlerManager {

    @Override
    public boolean isAvailable(TileEntity tile) {
        return tile.hasCapability(MY_ENERGY_CAP, null);
    }

    @Override
    public boolean isAvailable(ItemStack stack) {
        return stack.hasCapability(MY_ENERGY_CAP, null);
    }

    @Override
    public Class<? extends IEnergyHandler> getEnergyHandlerClass() {
        return MyEnergyHandler.class;
    }

    @Override
    public int getPriority() {
        return 100; // 数值越小优先级越高
    }

    @Override
    public IEnergyHandler newBlockEntityInstance() {
        return new MyEnergyHandler();
    }

    @Override
    public IEnergyHandler newItemInstance() {
        return new MyEnergyHandler();
    }

    @Override
    public String getUnit() {
        return "MJ";
    }
}

// 注册
API.registerEnergyHandler(new MyEnergyManager());
```

---

### EnergyAmount

`com.circulation.circulation_networks.api.EnergyAmount`

可变的能量数值对象。内部以 `long` 存储，当溢出时自动升级为 `BigInteger`。通过对象池管理以减少 GC 压力，使用完毕后应调用
`recycle()` 归还。

**获取实例**：

| 方法                     | 返回类型                  | 说明                        |
|------------------------|-----------------------|---------------------------|
| `obtain(long)`         | `static EnergyAmount` | 从对象池获取值为 `long` 的实例       |
| `obtain(BigInteger)`   | `static EnergyAmount` | 从对象池获取值为 `BigInteger` 的实例 |
| `obtain(String)`       | `static EnergyAmount` | 从字符串数值解析                  |
| `obtain(EnergyAmount)` | `static EnergyAmount` | 复制另一个实例的值                 |

**算术运算**（就地修改并返回 `this`）：

| 方法              | 重载                                 | 说明 |
|-----------------|------------------------------------|----|
| `add(...)`      | `long` / `EnergyAmount` / `double` | 加法 |
| `subtract(...)` | `long` / `EnergyAmount` / `double` | 减法 |
| `multiply(...)` | `long` / `EnergyAmount` / `double` | 乘法 |
| `divide(...)`   | `long` / `EnergyAmount` / `double` | 除法 |

**比较方法**：

| 方法                                | 说明                     |
|-----------------------------------|------------------------|
| `compareTo(long)`                 | 与 `long` 值比较           |
| `compareTo(EnergyAmount)`         | 与另一个 `EnergyAmount` 比较 |
| `min(EnergyAmount, EnergyAmount)` | 返回较小值（静态方法）            |
| `max(EnergyAmount, EnergyAmount)` | 返回较大值（静态方法）            |

**状态查询**：

| 方法                | 返回类型      | 说明                   |
|-------------------|-----------|----------------------|
| `isZero()`        | `boolean` | 值是否为零                |
| `isPositive()`    | `boolean` | 值是否为正                |
| `isNegative()`    | `boolean` | 值是否为负                |
| `fitsLong()`      | `boolean` | 值是否可以用 `long` 表示     |
| `isInitialized()` | `boolean` | 是否已初始化               |
| `isBig()`         | `boolean` | 是否处于 `BigInteger` 模式 |

**类型转换**：

| 方法                | 返回类型         | 说明                                                 |
|-------------------|--------------|----------------------------------------------------|
| `intValue()`      | `int`        | 转为 int（可能截断）                                       |
| `longValue()`     | `long`       | 转为 long（可能截断）                                      |
| `floatValue()`    | `float`      | 转为 float                                           |
| `doubleValue()`   | `double`     | 转为 double                                          |
| `asBigInteger()`  | `BigInteger` | 转为 BigInteger                                      |
| `asLongExact()`   | `long`       | 精确转为 long，溢出时抛异常                                   |
| `asLongClamped()` | `long`       | 转为 long，溢出时截断到 `Long.MAX_VALUE` / `Long.MIN_VALUE` |

**生命周期**：

| 方法          | 说明        |
|-------------|-----------|
| `recycle()` | 将实例回收至对象池 |
| `clear()`   | 清空内部状态    |
| `setZero()` | 将值重置为零    |

> ⚠️ **注意**：`EnergyAmount` 是可变对象，`recycle()` 后不应再使用。对于需要共享的不可变值，使用 `EnergyAmounts` 常量或
`ConstantEnergyAmount`。

---

### EnergyAmounts

`com.circulation.circulation_networks.api.EnergyAmounts`

提供常用的不可变 `EnergyAmount` 常量，均为 `ConstantEnergyAmount` 实例。

| 常量         | 值                   | 说明       |
|------------|---------------------|----------|
| `ZERO`     | `0`                 | 零        |
| `ONE`      | `1`                 | 一        |
| `INT_MIN`  | `Integer.MIN_VALUE` | int 最小值  |
| `INT_MAX`  | `Integer.MAX_VALUE` | int 最大值  |
| `LONG_MAX` | `Long.MAX_VALUE`    | long 最大值 |
| `LONG_MIN` | `Long.MIN_VALUE`    | long 最小值 |

---

### ConstantEnergyAmount

`com.circulation.circulation_networks.api.ConstantEnergyAmount extends EnergyAmount`

不可变的 `EnergyAmount` 子类。所有修改操作（`add`、`subtract`、`multiply`、`divide`、`clear`、`setZero` 等）均抛出
`UnsupportedOperationException`。用于安全共享能量值常量。

---

## 中枢系统

### IHubPlugin

`com.circulation.circulation_networks.api.hub.IHubPlugin`

中枢插件接口，物品实现此接口以成为可插入中枢插件槽位的插件。

| 方法                | 返回类型                     | 说明           |
|-------------------|--------------------------|--------------|
| `getCapability()` | `HubPluginCapability<?>` | 返回此插件提供的能力标识 |

`HubPluginCapability<T>` 是一个抽象类（位于 `com.circulation.circulation_networks.network.hub` 包），定义了插件数据的创建和保存方式：

| 方法                                                 | 说明            |
|----------------------------------------------------|---------------|
| `abstract T newPluginData(ItemStack plugin)`       | 为插件物品创建新的数据实例 |
| `abstract void saveData(T data, ItemStack plugin)` | 将数据保存回插件物品    |

---

### IHubChannel

`com.circulation.circulation_networks.api.hub.IHubChannel`

中枢频道接口，管理跨网格的频道连接和权限。

> **唯一内部实现**：此接口由 `HubChannel` 单例实现，外部模组不应实现此接口，仅供查询和交互使用。

| 方法                                                | 返回类型                            | 说明          |
|---------------------------------------------------|---------------------------------|-------------|
| `getChannelId()`                                  | `UUID`                          | 频道唯一标识      |
| `getGrids()`                                      | `ReferenceSet<IGrid>`           | 频道包含的所有网格   |
| `getName()`                                       | `String`                        | 频道名称        |
| `setName(String)`                                 | `void`                          | 设置频道名称      |
| `getPermissionMode()`                             | `PermissionMode`                | 权限模式        |
| `setPermissionMode(PermissionMode)`               | `void`                          | 设置权限模式      |
| `getOwner()`                                      | `@Nullable UUID`                | 频道所有者       |
| `setOwner(@Nullable UUID)`                        | `void`                          | 设置所有者       |
| `getExplicitPermission(UUID)`                     | `@Nullable HubPermissionLevel`  | 获取指定玩家的显式权限 |
| `getExplicitPermissions()`                        | `Map<UUID, HubPermissionLevel>` | 获取所有显式权限    |
| `setExplicitPermission(UUID, HubPermissionLevel)` | `void`                          | 设置显式权限      |
| `removeExplicitPermission(UUID)`                  | `void`                          | 移除显式权限      |
| `getPermissionLevel(UUID)`                        | `HubPermissionLevel`            | 获取最终权限等级    |
| `canEditPermissions(UUID)`                        | `boolean`                       | 是否可编辑权限     |

---

### HubPermissionLevel

`com.circulation.circulation_networks.api.hub.HubPermissionLevel`（枚举）

中枢权限等级。

| 值        | 说明  |
|----------|-----|
| `NONE`   | 无权限 |
| `MEMBER` | 成员  |
| `ADMIN`  | 管理员 |
| `OWNER`  | 所有者 |

| 方法                     | 返回类型                        | 说明                |
|------------------------|-----------------------------|-------------------|
| `fromId(int)`          | `static HubPermissionLevel` | 从序号获取枚举值          |
| `getId()`              | `int`                       | 返回序号              |
| `canEditPermissions()` | `boolean`                   | 是否为 OWNER 或 ADMIN |

---

### PermissionMode

`com.circulation.circulation_networks.api.hub.PermissionMode`（枚举）

中枢权限模式，决定默认的访问策略。

| 值         | 说明          |
|-----------|-------------|
| `PUBLIC`  | 公开，任何玩家均可访问 |
| `TEAM`    | 团队，需要显式权限   |
| `PRIVATE` | 私密，仅所有者可访问  |

| 方法            | 返回类型                    | 说明       |
|---------------|-------------------------|----------|
| `fromId(int)` | `static PermissionMode` | 从序号获取枚举值 |
| `getId()`     | `int`                   | 返回序号     |

---

### ChargingDefinition

`com.circulation.circulation_networks.api.hub.ChargingDefinition`（枚举）

定义中枢充能可作用的玩家物品槽位类别。

| 值           | 说明                      |
|-------------|-------------------------|
| `INVENTORY` | 背包                      |
| `HOTBAR`    | 快捷栏                     |
| `MAIN_HAND` | 主手                      |
| `OFF_HAND`  | 副手                      |
| `ARMOR`     | 盔甲槽                     |
| `ACCESSORY` | 饰品栏（如 Baubles / Curios） |

---

### ChargingPreference

`com.circulation.circulation_networks.api.hub.ChargingPreference`

玩家充能偏好，使用位掩码存储各槽位的开关状态。

| 方法                                                                         | 返回类型                        | 说明                                                            |
|----------------------------------------------------------------------------|-----------------------------|---------------------------------------------------------------|
| `ChargingPreference(boolean, boolean, boolean, boolean, boolean, boolean)` | 构造器                         | 按 INVENTORY, HOTBAR, ACCESSORY, MAIN_HAND, OFF_HAND, ARMOR 顺序 |
| `ChargingPreference(byte)`                                                 | 构造器                         | 从原始位掩码构造                                                      |
| `defaultAll()`                                                             | `static ChargingPreference` | 所有槽位均启用                                                       |
| `deserialize(NBTTagCompound)`                                              | `static ChargingPreference` | 从 NBT 反序列化                                                    |
| `getPreference(ChargingDefinition)`                                        | `boolean`                   | 查询指定槽位是否启用                                                    |
| `setPreference(ChargingDefinition, boolean)`                               | `void`                      | 设置槽位开关                                                        |
| `setPrefs(byte)`                                                           | `void`                      | 直接设置原始位掩码                                                     |
| `toByte()`                                                                 | `byte`                      | 导出为原始位掩码                                                      |
| `serialize()`                                                              | `NBTTagCompound`            | 序列化为 NBT                                                      |

---

### 快照类

以下类用于 GUI 同步和网络传输，提供网格/频道/权限的快照数据。均支持 JSON 和二进制序列化。

**`ChannelSnapshotEntry`** — 频道快照条目（record）：

| 字段               | 类型                   | 说明        |
|------------------|----------------------|-----------|
| `id`             | `UUID`               | 频道 UUID   |
| `name`           | `String`             | 频道名称      |
| `permissionMode` | `PermissionMode`     | 权限模式      |
| `permission`     | `HubPermissionLevel` | 当前玩家的权限等级 |
| `connected`      | `boolean`            | 是否已连接     |

**`ChannelSnapshotList`** — 频道快照列表：

- `getEntries()` → `List<ChannelSnapshotEntry>`
- `toJson()` / `fromJson(String)` — JSON 序列化
- `toBytes()` / `fromBytes(byte[])` — 二进制序列化

**`NodeSnapshotEntry`** — 节点快照条目（record）：

| 字段            | 类型       | 说明         |
|---------------|----------|------------|
| `itemId`      | `String` | 节点物品注册表 ID |
| `x`, `y`, `z` | `int`    | 节点坐标       |
| `customName`  | `String` | 自定义名称      |

**`NodeSnapshotList`** — 节点快照列表：

- `getEntries()` → `List<NodeSnapshotEntry>`
- `fromGrid(IGrid)` — 从网格生成快照
- `toJson()` / `fromJson(String)` — JSON 序列化
- `toBytes()` / `fromBytes(byte[])` — 二进制序列化（使用 delta 编码压缩坐标）

**`PermissionSnapshotEntry`** — 权限快照条目（record）：

| 字段           | 类型                   | 说明      |
|--------------|----------------------|---------|
| `id`         | `UUID`               | 玩家 UUID |
| `name`       | `String`             | 玩家名称    |
| `permission` | `HubPermissionLevel` | 权限等级    |

**`PermissionSnapshotList`** — 权限快照列表：

- `getEntries()` → `List<PermissionSnapshotEntry>`
- `toJson()` / `fromJson(String)` — JSON 序列化
- `toBytes()` / `fromBytes(byte[])` — 二进制序列化

---

## Tick 接口

### ClientTickMachine

`com.circulation.circulation_networks.api.ClientTickMachine`

方块实体实现此接口以在客户端 tick 中接收更新。

```java
public interface ClientTickMachine {
    void clientUpdate();
}
```

### ServerTickMachine

`com.circulation.circulation_networks.api.ServerTickMachine`

方块实体实现此接口以在服务端 tick 中接收更新。

```java
public interface ServerTickMachine {
    void serverUpdate();
}
```

---

## 网格接口

### IGrid

`com.circulation.circulation_networks.api.IGrid`

网格（Grid）代表一组互相链接的节点所组成的连通子图。

> **唯一内部实现**：此接口由 `Grid` 单例实现，外部模组不应实现此接口，仅供查询使用。

| 方法                     | 返回类型                  | 说明                  |
|------------------------|-----------------------|---------------------|
| `getId()`              | `int`                 | 网格唯一 ID             |
| `getNodes()`           | `ReferenceSet<INode>` | 网格中的所有节点            |
| `serialize()`          | `NBTTagCompound`      | 将网格序列化为 NBT         |
| `getHubNode()`         | `IHubNode`            | 网格的中枢节点（可能为 `null`） |
| `setHubNode(IHubNode)` | `void`                | 设置中枢节点              |
| `getSnapshotVersion()` | `int`                 | 快照版本号，用于增量同步        |
| `markSnapshotDirty()`  | `void`                | 标记快照需要更新            |
