<p align="center">
  <img src="https://img.shields.io/badge/JaomcStorage-2.0-00C853?style=for-the-badge&logo=minecraft&logoColor=white" alt="JaomcStorage">
</p>

<h1 align="center">💎 JaomcStorage 2.0</h1>
<p align="center"><i>Plugin kho khoáng sản nâng cao cho server Skyblock — phát triển bởi <b>Tài Nguyễn</b></i></p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20%2B-2D7D46?style=flat-square&logo=minecraft" />
  <img src="https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Database-SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white" />
  <img src="https://img.shields.io/badge/Economy-Vault-FFD700?style=flat-square" />
  <img src="https://img.shields.io/badge/API-PlaceholderAPI-9C27B0?style=flat-square" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Type-Skyblock%20Storage-FF6F00?style=flat-square" />
  <img src="https://img.shields.io/badge/Author-Tài%20Nguyễn-lightgrey?style=flat-square" />
  <img src="https://img.shields.io/badge/License-Private-red?style=flat-square" />
</p>

---

## 🧰 Mô Tả

**JaomcStorage** là plugin quản lý kho khoáng sản nâng cao dành cho **máy chủ Skyblock** hoặc các server PvE.  
Khi người chơi đào quặng, vật phẩm sẽ được **tự động lưu vào kho ảo** thay vì rơi vào túi đồ — giúp tối ưu trải nghiệm thu thập, hỗ trợ **chia sẻ kho (Coop)**, **bán lấy tiền qua Vault**, và **nâng cấp dung lượng** linh hoạt.

**Version 2.0** sử dụng **SQLite database** thay cho YAML flat-file, đảm bảo hiệu suất cao với nhiều người chơi đồng thời.

## 🧰 Description

**JaomcStorage** is an advanced ore storage plugin for **Skyblock** or PvE servers.  
When players mine ores, items are **automatically stored** in a virtual storage instead of the inventory — with support for **Coop sharing**, **Vault economy selling**, and **flexible capacity upgrades**.

**Version 2.0** uses **SQLite database** for high performance with concurrent players.

---

## 🔑 Tính Năng Chính

### ⛏ Khai Thác & Lưu Trữ
- ✅ **AutoStore** — Tự động lưu quặng vào kho khi đào block
- ✅ **Fortune Support** — Hỗ trợ enchantment Fortune tăng drop
- ✅ **XP Bonus** — Vẫn cho kinh nghiệm khi đào
- ✅ **Item Pickup** — Tự động lưu khi nhặt item rơi trên đất
- ✅ **Partial Store** — Kho gần đầy? Lưu được bao nhiêu thì lưu, dư rơi ra đất

### ⛔ Giới Hạn & Nâng Cấp
- ✅ **MaxSpace** — Giới hạn dung lượng kho (mặc định 100,000)
- ✅ **BlockedMining** — Chặn đào block nếu kho đầy
- ✅ **Infinity Mode** — Kho vô hạn cho VIP (`/jaostorage infinity`)
- ✅ **Slot Upgrade** — Hệ thống nâng cấp slot linh hoạt

### 💰 Kinh Tế
- ✅ **Vault Economy** — Bán khoáng sản thực sự nhận tiền
- ✅ **Sell All** — Bán toàn bộ kho với GUI xác nhận
- ✅ **Bảng giá** — Cấu hình giá bán từng loại trong `config.yml`

### 👥 Coop System
- ✅ **Chia sẻ kho** — Thêm/xóa thành viên coop
- ✅ **Access Control** — Coop member có thể xem & cất vào kho chủ
- ✅ **Persistent** — Dữ liệu coop lưu trong database, không mất khi restart

### 🎨 Giao Diện
- ✅ **Premium GUI** — Gradient border, rarity tags, capacity bar
- ✅ **ActionBar** — Thông báo nhặt item đẹp mắt
- ✅ **Sound Effects** — Âm thanh cho mọi tương tác
- ✅ **FormatName & FormatLore** — Tùy chỉnh tên/mô tả tiếng Việt

### 🔧 Kỹ Thuật
- ✅ **SQLite Database** — Hiệu suất cao, async I/O
- ✅ **In-memory Cache** — Đọc nhanh từ cache, ghi async vào DB
- ✅ **Auto-save** — Tự động lưu định kỳ (cấu hình được)
- ✅ **Action Logging** — Ghi log mọi hành động (bán, rút, cất, admin)
- ✅ **PlaceholderAPI** — 8+ placeholders cho scoreboard/hologram
- ✅ **Tab Complete** — Gợi ý lệnh thông minh
- ✅ **YAML Migration** — Tự động migrate `storage.yml` cũ sang SQLite

---

## 🔑 Main Features

| Category | Features |
|----------|----------|
| **Mining** | AutoStore, Fortune, XP, Item Pickup, Partial Store |
| **Limits** | MaxSpace, BlockedMining, Infinity Mode, Slot Upgrade |
| **Economy** | Vault selling, Sell All with confirmation, Configurable prices |
| **Coop** | Share storage, Access control, Persistent in database |
| **GUI** | Gradient borders, Rarity tags, Capacity bar, Sound effects |
| **Tech** | SQLite, In-memory cache, Async I/O, Action logging, PlaceholderAPI |

---

## 📥 Cài Đặt

```
1. Tải file JaomcStorage.jar và đưa vào thư mục plugins/
2. Đảm bảo đã cài Vault + Economy plugin (Essentials, CMI, etc.)
3. Khởi động server để plugin tự tạo file cấu hình + database
4. Tùy chỉnh config.yml, guiore.yml theo nhu cầu
5. Reload plugin: /jaostorage reload
```

### Yêu cầu
| Yêu cầu | Phiên bản |
|----------|-----------|
| Spigot / Paper | 1.20+ |
| Java | 17+ |
| Vault | Bắt buộc |
| Economy Plugin | Essentials / CMI / etc. |
| PlaceholderAPI | Tùy chọn |

### Nâng cấp từ v1.0
> Plugin sẽ **tự động phát hiện** file `storage.yml` cũ và migrate toàn bộ dữ liệu sang SQLite. File cũ được đổi tên thành `storage.yml.migrated`.

---

## 📁 Cấu Hình

### `config.yml` — Cấu hình chính

```yaml
# ⛏ Auto Store
PickupToStorage: true       # Tự động lưu khi đào
BlockedMining: true         # Chặn đào nếu kho đầy
OnlyStoreWhenInvFull: false # Chỉ lưu khi túi đầy
PickupSound: ENTITY_ITEM_PICKUP
UseActionBar: true

# 🌍 Blacklist thế giới
BlacklistWorlds:
  - world_nether
  - world_the_end

# 🚫 Blacklist / ✅ Whitelist vật phẩm
Blacklist: [DIAMOND_PICKAXE, TNT]
Whitelist: [COAL, RAW_IRON, RAW_COPPER, RAW_GOLD, ...]

# 📦 Dung lượng
MaxSpace: 100000

# 💰 Bảng giá
Prices:
  COAL: 2.5
  DIAMOND: 15.0
  EMERALD: 12.0
  # ... xem đầy đủ trong file

# 💾 Auto save (giây)
AutoSaveInterval: 60

# 📜 Log
Log:
  Sales: true
  Transfer: true
  Withdraw: true
  RetentionDays: 30
```

### `guiore.yml` — Danh sách quặng

```yaml
ores:
  - COAL_ORE
  - IRON_ORE
  - DIAMOND_ORE
  - EMERALD_ORE
  # ... xem đầy đủ trong file
```

---

## 🎮 Lệnh

### `/kho` — Lệnh người chơi

| Lệnh | Quyền | Mô tả |
|-------|-------|-------|
| `/kho` | `jaostorage.kho` | Mở GUI kho cá nhân |
| `/kho store <item> <số\|all>` | `jaostorage.kho` | Cất vật phẩm từ túi vào kho |
| `/kho take <item> <số\|all>` | `jaostorage.kho` | Rút vật phẩm từ kho ra túi |
| `/kho info` | `jaostorage.kho` | Xem thông tin kho |
| `/kho coop add <tên>` | `jaostorage.kho` | Thêm thành viên coop |
| `/kho coop remove <tên>` | `jaostorage.kho` | Xóa thành viên coop |
| `/kho coop list` | `jaostorage.kho` | Xem danh sách coop |
| `/kho open <player>` | `jaostorage.admin` | Mở kho người chơi khác |
| `/kho add <player> <item> <số>` | `jaostorage.admin` | Thêm vật phẩm (admin) |
| `/kho remove <player> <item> <số>` | `jaostorage.admin` | Xóa vật phẩm (admin) |

### `/autostore` — Toggle nhanh

| Lệnh | Quyền | Mô tả |
|-------|-------|-------|
| `/autostore` | `jaostorage.autostore` | Bật/tắt tự động lưu kho |

### `/jaostorage` — Lệnh admin

| Lệnh | Quyền | Mô tả |
|-------|-------|-------|
| `/jaostorage reload` | `jaostorage.admin` | Reload config.yml + guiore.yml |
| `/jaostorage open <player>` | `jaostorage.admin` | Mở kho người chơi |
| `/jaostorage add <player> <item> <số>` | `jaostorage.admin` | Thêm vật phẩm vào kho |
| `/jaostorage remove <player> <item> <số>` | `jaostorage.admin` | Xóa vật phẩm khỏi kho |
| `/jaostorage infinity <player>` | `jaostorage.admin` | Toggle kho vô hạn |
| `/jaostorage info <player>` | `jaostorage.admin` | Xem chi tiết kho + top items |
| `/jaostorage cleanup [days]` | `jaostorage.admin` | Dọn log cũ |

---

## 🔑 Quyền (Permissions)

| Permission | Mặc định | Mô tả |
|------------|----------|-------|
| `jaostorage.kho` | `true` | Truy cập kho cá nhân + coop |
| `jaostorage.autostore` | `true` | Bật/tắt auto-store |
| `jaostorage.admin` | `op` | Toàn quyền quản trị plugin |

---

## 📊 PlaceholderAPI

Cài PlaceholderAPI để sử dụng trên scoreboard, hologram, tab list:

| Placeholder | Output | Ví dụ |
|-------------|--------|-------|
| `%jaostorage_total%` | Tổng items trong kho | `1,250` |
| `%jaostorage_maxspace%` | Dung lượng tối đa | `100,000` |
| `%jaostorage_remaining%` | Dung lượng còn lại | `98,750` |
| `%jaostorage_slot%` | Số slot nâng cấp | `5` |
| `%jaostorage_infinity%` | Chế độ vô hạn | `true` / `false` |
| `%jaostorage_autostore%` | Trạng thái auto-store | `true` / `false` |
| `%jaostorage_coop_count%` | Số coop members | `3` |
| `%jaostorage_amount_DIAMOND%` | Số lượng Diamond | `150` |
| `%jaostorage_amount_COAL%` | Số lượng Coal | `500` |

---

## 🗄️ Database

Plugin sử dụng **SQLite** (file `storage.db` trong thư mục plugin). Không cần cài đặt database bên ngoài.

### Bảng dữ liệu

| Bảng | Mô tả |
|------|-------|
| `player_storage` | Vật phẩm trong kho (uuid, material, amount) |
| `player_settings` | Cài đặt người chơi (auto_store, slots, infinity) |
| `coop_members` | Quan hệ chia sẻ kho (owner, member) |
| `storage_logs` | Nhật ký hành động (action, material, amount, timestamp) |

### Tối ưu hiệu suất
- **WAL mode** — Cho phép đọc/ghi đồng thời
- **In-memory cache** — Tất cả đọc từ RAM, không query DB
- **Async batch write** — Ghi DB trên thread riêng, không lag main thread
- **Dirty tracking** — Chỉ lưu data đã thay đổi

---

## 🔨 Build

```bash
# Yêu cầu: Maven 3.8+ và JDK 17+
mvn clean package

# Output: target/jaostorage-2.0.jar
```

---

## 📝 Changelog

### v2.0 (Major Rewrite)
- 🆕 SQLite database thay YAML flat-file
- 🆕 Vault Economy tích hợp hoàn chỉnh (bán thực sự trả tiền)
- 🆕 Coop System với database persistence
- 🆕 BlockedMining — chặn đào khi kho đầy
- 🆕 MaxSpace enforcement — kiểm tra giới hạn thực sự
- 🆕 Action logging — ghi log mọi hành động
- 🆕 Tab completion cho tất cả lệnh
- 🆕 Sell confirm GUI — chống bán nhầm
- 🆕 Premium GUI — gradient border, rarity tags, capacity bar
- 🆕 Auto-migrate từ v1.0 YAML → SQLite
- 🆕 8+ PlaceholderAPI placeholders
- 🐛 Fix I/O blocking gây lag server
- 🐛 Fix admin xem kho người khác thấy kho mình
- 🐛 Fix reload chỉ reload guiore.yml
- 🐛 Fix PlaceholderAPI không được đăng ký

### v1.0 (Initial Release)
- AutoStore cơ bản
- GUI kho 54 slots
- Lệnh /kho, /autostore, /jaostorage
- YAML storage

---

<p align="center">
  <b>💎 JaomcStorage 2.0</b> — Made with ❤️ by <b>Tài Nguyễn</b>
  <br>
  <i>Plugin kho khoáng sản tốt nhất cho Skyblock Việt Nam</i>
</p>
