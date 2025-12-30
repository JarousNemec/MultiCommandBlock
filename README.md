### Basic example scripts
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
### Basic debug scripts example
```
# ================================
# DEBUG SEKVENCE
# ================================

%repeat 2
    %sleep 5
    /say a

%repeat 2
    %sleep 20
    /say b
    %repeat 3
        /say c
/say d
/say d

# Test zprávy
/tellraw @a {"text":"[DEBUG] Start sekvence","color":"gray"}

#%sleep 10
/tellraw @a {"text":"[DEBUG] Sleep funguje","color":"gray"}

# Test efektů
/effect give @p minecraft:glowing 3 1 true

%sleep 20
/tellraw @a {"text":"[DEBUG] Efekt aplikován","color":"gray"}

# Test teleportu
#/tp @p ~ ~5 ~

# Konec
#/tellraw @a {"text":"[DEBUG] Konec sekvence","color":"gray"}

```
### Wild scripts example
```
# ======================================
# MULTICOMMAND BLOCK – UKÁZKOVÁ SEKQUENCE
# ======================================

# Oznámení startu
/tellraw @a {"text":"[SYSTEM] Spouštím sekvenci...","color":"gold"}

%sleep 20

# --------------------------------------
# Hlavní opakující se smyčka
# --------------------------------------
%repeat 3
    /tellraw @a {"text":"[LOOP] Nová iterace","color":"yellow"}
    %sleep 10

    # Vnitřní sekvence
    %repeat 2
        /say Vnitrni repeat
        %sleep 5

    # Efekt + zpráva
    /effect give @p minecraft:speed 3 1 true
    /tellraw @p {"text":"[EFFECT] Speed aktivní","color":"aqua"}
    %sleep 20

# --------------------------------------
# Lineární část po repeatu
# --------------------------------------
/say Repeat dokončen
/tellraw @a {"text":"[SYSTEM] Přechod do další fáze","color":"green"}

%sleep 40

# --------------------------------------
# Simulace countdownu
# --------------------------------------
%repeat 5
    /title @a actionbar {"text":"Start za chvíli...","color":"red"}
    %sleep 10

/title @a actionbar {"text":"START!","color":"dark_red"}

# --------------------------------------
# Finální akce
# --------------------------------------
/effect give @a minecraft:glowing 5 1 true
/say ✨ Sekvence dokončena ✨

# --------------------------------------
# Debug / testovací část
# --------------------------------------
# Tento blok je zakomentovaný a neměl by se provést
#%repeat 10
#    /say Toto se nespusti
#    %sleep 5

# Konec programu
/tellraw @a {"text":"[SYSTEM] Konec multicommand programu","color":"gray"}

```
```
# =====================================================
# MULTICOMMAND BLOCK – ABSOLUTNÍ TORTURE TEST
# =====================================================

/tellraw @a {"text":"[TORTURE] Inicializace...","color":"dark_red"}
%sleep 20

# -----------------------------------------------------
# LEVEL 1 REPEAT
# -----------------------------------------------------
%repeat 2
    /tellraw @a {"text":"[L1] Start iterace","color":"red"}
    %sleep 10

    # ---------------------------------------------
    # LEVEL 2 REPEAT
    # ---------------------------------------------
    %repeat 3
        /tellraw @a {"text":"[L2] Vnitrni smycka","color":"gold"}
        %sleep 5

        # -------------------------------------
        # LEVEL 3 REPEAT
        # -------------------------------------
        %repeat 2
            /tellraw @a {"text":"[L3] Hluboka smycka","color":"yellow"}
            %sleep 2

            # -----------------------------
            # LEVEL 4 REPEAT (MINI LOOP)
            # -----------------------------
            %repeat 4
                /say L4 ping
                %sleep 1

            /say L3 konec iterace
            %sleep 5

        /say L2 krok dokoncen
        %sleep 10

    /tellraw @a {"text":"[L1] Blok L2 dokoncen","color":"dark_green"}
    %sleep 20

    # ---------------------------------------------
    # CHAOS BLOK – mix linear + repeat
    # ---------------------------------------------
    /say Chaos start
    %repeat 3
        /say CHAOS
        %sleep 3
    /say Chaos end

    %sleep 10

# -----------------------------------------------------
# PO VŠECH REPEATECH
# -----------------------------------------------------
/tellraw @a {"text":"[TORTURE] Smycky dokonceny","color":"green"}
%sleep 40

# -----------------------------------------------------
# STRESS TEST: RYCHLÉ KRÁTKÉ LOOPY
# -----------------------------------------------------
%repeat 10
    /title @a actionbar {"text":"Tick loop","color":"aqua"}
    %sleep 1

/title @a actionbar {"text":"HOTOVO","color":"dark_aqua"}

# -----------------------------------------------------
# MIX KOMENTÁŘŮ A NEPLATNÝCH BLOKŮ
# -----------------------------------------------------
#%repeat 100
#    /say TOHLE SE NESMI SPUSTIT
#    %sleep 1

# -----------------------------------------------------
# FINÁLNÍ VÝSTUP
# -----------------------------------------------------
/effect give @a minecraft:glowing 5 1 true
/say 🔥 TORTURE TEST DOKONČEN 🔥
/tellraw @a {"text":"Pokud tohle probehlo spravne, repeat funguje 😉","color":"light_purple"}

```

### Compiler crash test
```
# =====================================================
# COMPILER EDGE-CASE TORTURE TEST
# =====================================================

#
# prázdný komentář nahoře
#

    

# -------------------------------------
# 1️⃣ PRÁZDNÝ A NEPLATNÝ VSTUP
# -------------------------------------

%
/
% 
/ 

# -------------------------------------
# 2️⃣ SLEEP EDGE CASES
# -------------------------------------

%sleep
%sleep -1
%sleep 0
%sleep 1
%sleep 999999999

    %sleep 10   # sleep s indentem bez parentu

# -------------------------------------
# 3️⃣ REPEAT EDGE CASES – HLAVIČKY
# -------------------------------------

%repeat
%repeat -1
%repeat 0
%repeat 1
%repeat 999999999

    %repeat 2   # repeat s indentem bez parentu

# -------------------------------------
# 4️⃣ REPEAT BEZ TĚLA
# -------------------------------------

%repeat 3
%repeat 1
/say repeat bez tela

# -------------------------------------
# 5️⃣ REPEAT S PRÁZDNÝM TĚLEM
# -------------------------------------

%repeat 2

%repeat 2
    
%repeat 2
    # jen komentar
    # dalsi komentar

# -------------------------------------
# 6️⃣ SKOKY V INDENTACI
# -------------------------------------

        /say preskoceny indent (2 urovne bez parenta)

    /say indent 1 bez repeat

%repeat 2
        /say indent 2 misto 1

# -------------------------------------
# 7️⃣ MIX VALID / INVALID V BLOKU
# -------------------------------------

%repeat 2
    /say valid
    %sleep -5
    %unknowncommand
    /say valid 2

# -------------------------------------
# 8️⃣ VNORENE REPEATY S ROZBITOU STRUKTUROU
# -------------------------------------

%repeat 2
    %repeat 2
        /say ok
    %repeat 2
    /say tohle je mimo repeat 2?

# -------------------------------------
# 9️⃣ KOMENTÁŘE UPROSTŘED BLOKŮ
# -------------------------------------

%repeat 2
    # komentar 1
    # komentar 2
    /say stale v repeat
        # komentar s indentem navic
    /say konec repeat

# -------------------------------------
# 🔟 HRANIČNÍ DÉLKY ŘÁDKŮ
# -------------------------------------

/
%
# 
#  
#   

# -------------------------------------
# 1️⃣1️⃣ KOMBINACE VŠEHO
# -------------------------------------

%repeat 1
    %sleep 0
    %repeat 0
        /say TOHLE SE NESMI SPUSTIT
    %repeat 1
        %repeat 1
            %repeat 1
                /say hluboke vnoreni
        /say zpet nahoru
    /say konec

# -------------------------------------
# 1️⃣2️⃣ VALID SEKCE NA KONTROLU
# -------------------------------------

/say Pokud tohle probehlo bez padu, compiler prezil 💀

```
### Runnable edge-case test
```
# =====================================================
# SAFE COMPILER EDGE-CASE TEST (RUNTIME FRIENDLY)
# =====================================================

# -------------------------------------
# 1️⃣ PRÁZDNÉ / KRÁTKÉ ŘÁDKY
# -------------------------------------

#
# 
    

%
/
% 
/ 

# -------------------------------------
# 2️⃣ SLEEP EDGE CASES (SAFE)
# -------------------------------------

%sleep
%sleep -1
%sleep 0
%sleep 1
%sleep 2

    %sleep 1   # indent bez parenta

# -------------------------------------
# 3️⃣ REPEAT EDGE CASES – HLAVIČKY (SAFE)
# -------------------------------------

%repeat
%repeat -1
%repeat 0
%repeat 1
%repeat 2

    %repeat 1   # indent bez parenta

# -------------------------------------
# 4️⃣ REPEAT BEZ / S PRÁZDNÝM TĚLEM
# -------------------------------------

%repeat 1
%repeat 2
/say repeat bez tela

%repeat 2

%repeat 2
    # jen komentar
    # dalsi komentar

# -------------------------------------
# 5️⃣ SKOKY V INDENTACI
# -------------------------------------

        /say preskoceny indent (2 urovne)

    /say indent bez parenta

%repeat 2
        /say spatny indent uvnitr repeatu

# -------------------------------------
# 6️⃣ MIX VALID / INVALID V BLOKU
# -------------------------------------

%repeat 2
    /say valid
    %sleep -1
    %unknowncommand
    /say valid 2

# -------------------------------------
# 7️⃣ VNORENÉ REPEATY – HRANICE
# -------------------------------------

%repeat 2
    %repeat 1
        /say OK
    %repeat 1
    /say zpet v L1

# -------------------------------------
# 8️⃣ KOMENTÁŘE V BLOKU
# -------------------------------------

%repeat 1
    # komentar
    /say stale v repeat
        # komentar s indentem navic
    /say konec repeat

# -------------------------------------
# 9️⃣ HRANIČNÍ SYNTAXE
# -------------------------------------

/
%
#  
#   

# -------------------------------------
# 🔟 KOMBINACE VŠEHO (SAFE)
# -------------------------------------

%repeat 1
    %sleep 0
    %repeat 0
        /say TOHLE SE NESMI SPUSTIT
    %repeat 1
        %repeat 1
            /say hluboke vnoreni
        /say zpet
    /say konec

# -------------------------------------
# 1️⃣1️⃣ KONTROLNÍ VÝSTUP
# -------------------------------------

/say SAFE TEST DOKONCEN

```