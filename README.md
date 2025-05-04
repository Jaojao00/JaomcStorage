<p align="center">
  <img src="https://img.shields.io/badge/JaomcStorage-1.0-green?style=for-the-badge&logo=storage" alt="JaomcStorage">
</p>

<h1 align="center">💎 JaomcStorage</h1>
<p align="center"><i>Plugin kho nâng cấp cho server Skyblock - phát triển bởi <b>Tài Nguyễn</b></i></p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20+-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/Plugin--Type-Skyblock--Storage-orange?style=flat-square" />
  <img src="https://img.shields.io/badge/Author-Tài%20Nguyễn-lightgrey?style=flat-square" />
</p>

---

## 🧰 MÔ TẢ

**JaomcStorage** là plugin quản lý kho lưu trữ nâng cao dành cho **máy chủ Skyblock** hoặc các server PvE.  
Plugin giúp người chơi tự động thu thập tài nguyên vào kho, chia sẻ kho với người chơi khác, nâng cấp sức chứa, bán nhanh và tối ưu trải nghiệm thu thập vật phẩm.

---
## 🔑 TÍNH NĂNG MỚI

| Tính năng                 | Thực hiện qua        | Trạng thái |
| ------------------------- | -------------------- | ---------- |
| Admin thêm item           | Lệnh + GUI           | ✅ Đã xong  |
| Admin xoá item            | Lệnh + GUI           | ✅ Đã xong  |
| Mở kho người khác         | Lệnh `/kho open`     | ✅ Đã xong  |
| Cập nhật dữ liệu lưu      | `StorageManager`     | ✅ Đã xong  |
| Giao diện và thao tác GUI | `StorageGUIListener` | ✅ Đã xong  |
| Phân quyền sử dụng        | Permission check     | ✅ Đã xong  |

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

---

## 📥 CÀI ĐẶT

1. Tải file `JaomcStorage.jar` và đưa vào thư mục `plugins/`.
2. Khởi động server để plugin tự tạo file cấu hình.
3. Tuỳ chỉnh file `config.yml`, `guiore.yml` theo nhu cầu.
4. Reload lại plugin bằng lệnh: `/jaostorage reload`

---

## 📁 CẤU HÌNH CHÍNH (`config.yml`)

```yaml
# ✅ Cấu hình chính cho plugin JaomcStorage

# ✔️ Khi bật, vật phẩm khai thác sẽ chuyển thẳng vào kho
PickupToStorage: true

# ✔️ Nếu kho đầy, không cho khai thác block nữa
BlockedMining: true

# ✔️ Chỉ tự lưu khi túi đồ đầy
OnlyStoreWhenInvFull: false

# ✔️ Hiệu ứng âm thanh khi vật phẩm được đưa vào kho
PickupSound: ENTITY_ITEM_PICKUP

# ✔️ Hiển thị thông báo dưới dạng ActionBar
UseActionBar: true

# ✔️ Thế giới không áp dụng tự lưu
BlacklistWorlds:
  - world_nether
  - world_the_end

# ✔️ Vật phẩm không được lưu vào kho
Blacklist:
  - DIAMOND_PICKAXE
  - TNT

# ✔️ Vật phẩm được phép lưu (nếu bật whitelist)
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

# ✔️ Tên hiển thị tuỳ chỉnh
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

# ✔️ Thiết lập hệ thống tiền tệ
Economy:
  Provider: VAULT
  Currency: "$"

# ✔️ Dung lượng kho mặc định
MaxSpace: 100000

# ✔️ Thời gian cập nhật kho tự động (giây)
AutoUpdateTime: 30

# ✔️ Restart plugin nếu config thay đổi?
RestartOnChange: false

# ✔️ Log các hành động
Log:
  Sales: true
  Transfer: true
  Withdraw: true

# ✔️ Giá bán vật phẩm
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
```

---

## 🧾 CẤU HÌNH QUẶNG (`guiore.yml`)

```yaml
ores:
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
```

---

## 📜 CÁC LỆNH

| Lệnh                 | Mô tả                           | Quyền              |
| -------------------- | ------------------------------- | ------------------ |
| `/kho`               | Mở GUI kho của bạn              | `jaostorage.kho`   |
| `/autostore`         | Bật / tắt chế độ tự lưu khi đào | *Không cần quyền*  |
| `/jaostorage reload` | Tải lại file cấu hình plugin    | `jaostorage.admin` |

---

## 🔐 PHÂN QUYỀN

```yaml
jaostorage.kho:
  description: Quyền mở kho và nâng cấp
  default: true

jaostorage.admin:
  description: Quyền quản trị plugin
  default: op
```

---

## 🧭 SƠ ĐỒ HOẠT ĐỘNG PLUGIN

```plaintext
                 ┌────────────┐
                 │   Main     │
                 └────┬───────┘
                      │
   ┌──────────────────┼────────────────────────────┐
   ▼                  ▼                            ▼
OreConfig       StorageManager              SettingManager
   │                  │                            ▲
   ▼                  ▼                            │
BlockBreakListener    └─────┐                      │
AutoStoreListener           ▼                      │
                            CoopManager            │
                            │                      │
                            ▼                      │
                       StorageGUI ──┐              │
                            │       ▼              │
                            └──► StorageGUIListener│
                                                 ▲
                                                 │
                                 ┌───────────────┘
                                 │
          ┌──────────────────────┼───────────────────────────┐
          ▼                      ▼                           ▼
   AdminCommand       StorageCommand                AutoStoreCommand
```

---

## 🖼️ HÌNH ẢNH MINH HỌA

<p align="center">
  <img src="https://i.imgur.com/68KB0Ab.png" alt="Giao diện kho" width="600"/>
</p>


---

## ❤️ BẢN QUYỀN

Plugin JaomcStorage được phát triển bởi Tài Nguyễn – hoàn toàn miễn phí, dành cho cộng đồng Minecraft Việt Nam.  
Mọi hành vi thương mại hóa không có sự cho phép đều bị nghiêm cấm.  
Nếu bạn thấy plugin hữu ích, hãy để lại ⭐ trên GitHub để ủng hộ tinh thần phát triển nhé!

---

## 📬 LIÊN HỆ & HỖ TRỢ

- Facebook: https://fb.com/tainguyen.dev
- Email: tainguyen.dev@gmail.com
- Discord: https://discord.gg/jccNvur28z

---

**Nếu bạn cần hỗ trợ thêm hoặc muốn tùy chỉnh plugin theo nhu cầu riêng, đừng ngần ngại liên hệ!**

---
