[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Family Forth Cross Compiler

Family Forth STC Forth Cross Compiler targeting the Ricoh 2A03.

Requires Maven and Java11+ to build the compiler.

ca65 is required to assemble the compiler's output.

## Installation guide

Clone the repository
```
git clone https://github.com/Family-Forth/Family-ForthCC.git
```

Build the app
```
    mvn clean
```

Build the jar
```
    mvn package
```

## RAM Map

| Addresses   | Size        | What can go there                                                                     |
| ----------- | ----------- | ------------------------------------------------------------------------------------- |
| $0000-$0007 |	8 bytes 	| Kernel Registers                                                                      |
| $0008-$00FD |	240 bytes 	| Parameter Stack                                                                       |
| $00FE-$00FF |	2 bytes 	| Parameter Stack underflow buffer                                                      |
| $0100-$019F |	160 bytes   | Data to be copied to nametable during next vertical blank (see The frame and NMIs)    |
| $01A0-$01FF |	96 bytes    | Return Stack                                                                          |
| $0200-$02FF |	256 bytes   | Data to be copied to OAM during next vertical blank                                   |
| $0300-$03FF |	256 bytes   | Variables used by sound player, and possibly other variables                          |
| $0400-$07FD |	1024 bytes  | User Data Space                                                                       |

# Kernel Configuration
* W   = Working register one
* W2  = Working register two
* PSP = Parameter Stack Pointer
* RSP = Return Stack Pointer
* DP  = Data Space Pointer

## Kernel Memory locations

| Reg  |  Mem Type      | RAM Addr  | Vaild values   |
| ---- |--------------- | --------- | -------------- |
| W    |  Zpage         | $00 - $01 |                |
| W2   |  Zpage         | $02 - $03 |                |
| DP   |  Zpage         | $04 - $05 | $0400 to $07FF |
| TBD  |  Zpage         | $06 - $07 |                |
| PSP  |  X Register    |           | $08   to $FD   |
| RSP  |  Stack Pointer |           | $01A0 to $01FD |

