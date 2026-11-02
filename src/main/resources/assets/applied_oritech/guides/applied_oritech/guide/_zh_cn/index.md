---
navigation:
  title: 应用奥瑞科技
  position: 0
item_ids:
  - applied_oritech:me_dock
  - applied_oritech:me_pattern_provider_addon
  - applied_oritech:me_interface_addon
  - applied_oritech:me_extended_pattern_provider_addon
  - applied_oritech:me_extended_interface_addon
---

# 应用奥瑞科技

Applied Oritech 将 Oritech 机器接入 Applied Energistics 2 的 ME 网络。ME 扩展坞负责网络连接，ME 插件负责自动合成与库存补充。

## 安装与连接

<ItemImage id="applied_oritech:me_dock" scale="4" />

1. 组装一台能够正常工作的 Oritech 机器，并为机器供能。
2. 将 ME 扩展坞安装在机器附加装置位置，让机器识别扩展坞。
3. 使用 ME 线缆连接扩展坞，确保网络有电且有可用频道。
4. 将 ME 插件接入机器的附加装置结构，或紧贴这台机器已连接的扩展坞放置。必要时让机器重新识别附加装置。
5. 右键插件打开配置界面。插件必须同时找到机器与扩展坞才能使用。

每个扩展坞及其所属插件共用一个频道。插件通过扩展坞接入网络，不直接接受线缆连接。可以安装多个扩展坞；插件会选择与自身机器相匹配的扩展坞。

## ME 样板供应器插件

<ItemImage id="applied_oritech:me_pattern_provider_addon" scale="4" />

普通版提供九个样板槽。将与机器配方相匹配的处理样板编码后放入插件，ME 网络即可调用机器进行自动合成。物品原料送入机器输入槽，而非产物槽；支持流体的机器还可以通过其流体存储接收相应原料。

插件使用 AE2 的样板供应器逻辑，保留阻挡模式、优先级与样板访问等功能。当机器没有其他产物回传方式时，应开启自动回收。回收的产物先进入样板供应器的物品返回栏，再由 AE2 处理。

## ME 接口插件

<ItemImage id="applied_oritech:me_interface_addon" scale="4" />

在界面中配置要维持的标记物及数量。接口从 ME 网络取得对应资源，再将自身缓存中的资源补充到机器。机器暂时无法接受的资源会留在缓存中。支持相应存储的机器可以接收物品和流体。

安装合成卡后，接口可为网络中不足的配置资源请求合成。模糊卡可启用 AE2 支持的模糊匹配。自动回收与补货相互独立，会将机器产物送回 ME 存储。

## ExtendedAE 扩展版本

<ItemImage id="applied_oritech:me_extended_pattern_provider_addon" scale="4" />

<ItemImage id="applied_oritech:me_extended_interface_addon" scale="4" />

安装 ExtendedAE 后，ME 扩展样板供应器插件提供 36 个样板槽，ME 扩展接口插件通过 ExtendedAE 的分页界面提供 36 个配置槽。它们仍是 Applied Oritech 的插件，遵循相同的机器连接、扩展坞和自动回收规则。ExtendedAE 是可选依赖。

## 自动回收产物

使用插件界面中优先级按钮左侧的按钮切换。图标原色表示开启，变暗表示关闭；按钮背景不随状态变色。悬停提示会显示当前状态。

开关按插件单独保存，初始状态由通用配置决定。潜行右键不再用于切换。该按钮仅出现在 Applied Oritech 的插件界面，不添加到 AE2 或 ExtendedAE 的原生设备界面。

自动回收提取机器产物，而不是任意输入内容。传输需要 ME 网络在线、网络有可用存储空间，并满足配置所要求的机器能量消耗。如果希望由其他系统处理产物，请关闭自动回收。

## 常见问题

- **尚未连接机器：**检查机器附加装置位置与识别结果，或将插件放在该机器已连接的扩展坞旁。
- **没有 ME 扩展坞：**安装并识别属于同一台机器的扩展坞。
- **扩展坞离线：**检查线缆连接、ME 网络供能与频道。
- **自动合成无法完成：**检查处理样板、机器原料、机器供能、产物空间与自动回收开关。
- **配置资源无法传入：**确认机器能接受该资源、支持相应物品或流体存储，并检查网络和机器能量。
- **产物留在机器中：**检查回收开关、ME 存储容量，以及样板供应器的物品返回栏。

拆除插件会掉落其存储内容、编码样板和已安装的升级卡。调整运行中的结构前，建议先完成或取消相关合成任务。
