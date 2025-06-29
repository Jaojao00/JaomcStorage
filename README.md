<p align="center">
  <img src="https://img.shields.io/badge/JaomcStorage-2.0-green?style=for-the-badge&logo=storage" alt="JaomcStorage">
</p>

<h1 align="center">💎 JaomcStorage-2.0</h1>
<p align="center"><i>Plugin kho nâng cấp cho server Skyblock - phát triển bởi <b>Tài Nguyễn</b></i></p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21+-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/Plugin--Type-Skyblock--Storage-orange?style=flat-square" />
  <img src="https://img.shields.io/badge/Author-Tài%20Nguyễn-lightgrey?style=flat-square" />
</p>

---

## 🧰 MÔ TẢ

**JaomcStorage** là plugin quản lý kho lưu trữ nâng cao dành cho **máy chủ Skyblock** hoặc các server PvE.  
Plugin giúp người chơi tự động thu thập tài nguyên vào kho, chia sẻ kho với người chơi khác, nâng cấp sức chứa, bán nhanh và tối ưu trải nghiệm thu thập vật phẩm.

---

## 🔑 TÍNH NĂNG CHÍNH

- ✅ Tự động lưu vật phẩm vào kho khi khai thác (AutoStore).
- ✅ Giới hạn dung lượng kho & hệ thống nâng cấp linh hoạt.
- ✅ Kho vô hạn cho người chơi VIP hoặc cấu hình.
- ✅ Bán nhanh tài nguyên từ kho (Vault economy hỗ trợ).
- ✅ Hệ thống **chia sẻ kho (Coop)** giữa nhiều người chơi.
- ✅ Chặn phá block nếu kho đầy (BlockedMining).
- ✅ Hiệu ứng âm thanh khi vật phẩm được thu vào kho.
- ✅ Tuỳ chỉnh sâu với **whitelist** và **blacklist** vật phẩm.
- ✅ Giao diện GUI thân thiện, dễ sử dụng.
- ✅ Hỗ trợ PlaceholderAPI (tuỳ chọn).
- ✅ Tương thích Minecraft **1.21** mới nhất.

---

## 🔑 MAIN FEATURES

- ✅ Automatically save items to the warehouse when mining (AutoStore).
- ✅ Warehouse capacity limit & flexible upgrade system.
- ✅ Unlimited warehouse for VIP players or configuration.
- ✅ Quickly sell resources from the warehouse (Vault economy supported).
- ✅ **warehouse sharing (Coop)** system between multiple players.
- ✅ Block block breaking if the warehouse is full (BlockedMining).
- ✅ Sound effects when items are collected into the warehouse.
- ✅ Deep customization with **whitelist** and **blacklist** items.
- ✅ Friendly GUI interface, easy to use.
- ✅ PlaceholderAPI support (optional).
- ✅ Compatible with the latest Minecraft **1.21**.

## 📥 CÀI ĐẶT

1. Tải file `JaomcStorage.jar` và đưa vào thư mục `plugins/`.
2. Khởi động server để plugin tự tạo file cấu hình.
3. Tuỳ chỉnh file `config.yml`, `guiore.yml` theo nhu cầu.
4. Reload lại plugin bằng lệnh: `/jaostorage reload`

---

## 📁 CẤU HÌNH CHÍNH (`config.yml`)

```yaml
# ✅ Cấu hình chính cho plugin JaomcStorage

PickupToStorage: true
BlockedMining: true
OnlyStoreWhenInvFull: false
PickupSound: ENTITY_ITEM_PICKUP
UseActionBar: true

BlacklistWorlds:
  - world_nether
  - world_the_end

Blacklist:
  - DIAMOND_PICKAXE
  - TNT

Whitelist:
  - COAL
  - RAW_IRON
  - RAW_COPPER
  - RAW_GOLD
  - REDSTONE
  - LAPIS_LAZULI
  - EMERALD
  - DIAMOND
  - QUARTZ
  - STONE
  - COBBLESTONE

FormatName:
  COAL: "&7Than"
  RAW_IRON: "&fQuặng Sắt"
  RAW_COPPER: "&6Đồng Thô"
  RAW_GOLD: "&eQuặng Vàng"
  REDSTONE: "&cĐá Đỏ"
  LAPIS_LAZULI: "&9Ngọc Lưu Ly"
  EMERALD: "&aNgọc Lục Bảo"
  DIAMOND: "&bKim Cương"
  QUARTZ: "&fThạch Anh"
  COBBLESTONE: "&8Đá Cuội"
  STONE: "&7Đá"

Economy:
  Provider: VAULT
  Currency: "$"

MaxSpace: 100000
AutoUpdateTime: 30
RestartOnChange: false

Log:
  Sales: true
  Transfer: true
  Withdraw: true

Prices:
  COAL: 5
  RAW_IRON: 10
  RAW_COPPER: 8
  RAW_GOLD: 12
  REDSTONE: 6
  LAPIS_LAZULI: 7
  EMERALD: 15
  DIAMOND: 20
  QUARTZ: 9
  STONE: 2
  COBBLESTONE: 1
