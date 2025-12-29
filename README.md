### Ukázkové programy
```
# ================================
# MULTICOMMAND PROGRAM
# Magický teleport s efekty
# ================================

# Zpráva hráčům
/tellraw @a {"text":"Magická energie se začíná shromažďovat...","color":"dark_purple","bold":true}

# Zpomalení hráčů v okolí
/effect give @a[distance=..10] minecraft:slowness 5 3 true
/effect give @a[distance=..10] minecraft:blindness 3 1 true

# Malá pauza
%sleep 40

# Částice okolo bloku
/particle minecraft:portal ~ ~1 ~ 1 1 1 0.1 200

# Zvuk nabíjení
/playsound minecraft:block.beacon.power_select master @a[distance=..15] ~ ~ ~

# Další pauza
%sleep 20

# TELEPORT!
/tellraw @a {"text":"Teleportace spuštěna!","color":"light_purple","bold":true}
/tp @a[distance=..10] 0 100 0

# Exploze (jen vizuální)
/particle minecraft:explosion ~ ~1 ~ 0.5 0.5 0.5 0 50

# Krátká pauza
%sleep 10

# Očista efektů
/effect clear @a

# Závěrečný zvuk
/playsound minecraft:entity.enderman.teleport master @a

# Hotovo
/tellraw @a {"text":"Teleportace dokončena.","color":"green","bold":true}

```
```
# ================================
# BEZPEČNOSTNÍ ALARM
# ================================

# Detekce hráčů v okolí
/execute if entity @a[distance=..8]

# Zvuk alarmu
/playsound minecraft:block.note_block.pling master @a ~ ~ ~

# Červené částice
/particle minecraft:dust 1 0 0 1 ~ ~1 ~ 1 1 1 0 100

# Výstražná zpráva
/tellraw @a {"text":"VAROVÁNÍ! Neoprávněná osoba v oblasti!","color":"red","bold":true}

# Krátká pauza
%sleep 40

# Opakovaný alarm
/playsound minecraft:block.note_block.pling master @a ~ ~ ~

```
```
# ================================
# MINI BOSS EVENT
# ================================

# Oznámení
/tellraw @a {"text":"Probouzí se Strážce...","color":"dark_red","bold":true}

# Přípravné efekty
/effect give @a[distance=..15] minecraft:weakness 5 1 true

# Nabíjení
/particle minecraft:smoke ~ ~1 ~ 1 1 1 0.05 200

%sleep 40

# Vyvolání bosse
/summon minecraft:iron_golem ~ ~1 ~ {CustomName:'{"text":"Strážce","color":"dark_red","bold":true}',Health:200f}

# Zvuk
/playsound minecraft:entity.wither.spawn master @a ~ ~ ~

# Boj může začít
/tellraw @a {"text":"Připravte se k boji!","color":"red","bold":true}

```
```
# ================================
# MAGICKÁ BOUŘE
# ================================

# Upozornění
/tellraw @a {"text":"Nebe se začíná zatahovat...","color":"blue","bold":true}

%sleep 20

# Bouře
/weather thunder 600

# Hromy okolo
/execute at @a run summon minecraft:lightning_bolt ~ ~ ~

%sleep 60

# Déšť skončil
/weather clear

# Konec
/tellraw @a {"text":"Bouře odezněla.","color":"green","bold":true}

```
```
# ================================
# DEBUG SEKVENCE
# ================================

# Test zprávy
/tellraw @a {"text":"[DEBUG] Start sekvence","color":"gray"}

%sleep 10
/tellraw @a {"text":"[DEBUG] Sleep funguje","color":"gray"}

# Test efektů
/effect give @p minecraft:glowing 3 1 true

%sleep 20
/tellraw @a {"text":"[DEBUG] Efekt aplikován","color":"gray"}

# Test teleportu
/tp @p ~ ~5 ~

# Konec
/tellraw @a {"text":"[DEBUG] Konec sekvence","color":"gray"}

```