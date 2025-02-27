# Family Forth Documentation

## RAM Map

| Addresses   | Size        | What can go there                                                    |
| ----------- | ----------- | -------------------------------------------------------------------- |
| $0000-$0009 |	8 bytes 	| Kernel Registers                                                     |
| $000A-$00FF |	240 bytes 	| Parameter Stack                                                      |
| $0100-$019F |	160 bytes   | Data to be copied to nametable during next vertical blank            |
| $01A0-$01FF |	96 bytes    | Return Stack                                                         |
| $0200-$02FF |	256 bytes   | Data to be copied to OAM during next vertical blank                  |
| $0300-$037F |	128 bytes 	| Input buffer                                                         |
| $0380-$03FF |	128 bytes   | Variables used by sound player                                       |
| $0400-$07FD |	1024 bytes  | User Data Space                                                      |

## Kernel Configuration
* W   = Working register one
* W2  = Working register two
* PSP = Parameter Stack Pointer
* RSP = Return Stack Pointer
* DSP = Data Space Pointer

## Kernel Memory locations

| Reg  |  Mem Type      | RAM Addr  | Vaild values   |
| ---- |--------------- | --------- | -------------- |
| W    |  Zpage         | $00 - $01 |                |
| W2   |  Zpage         | $02 - $03 |                |
| DSP  |  Zpage         | $04 - $05 | $0400 to $07FF |
| SRC  |  Zpage         | $08 - $09 | $300  to $037F |
| PSP  |  X Register    |           | $08   to $00   |
| RSP  |  Stack Pointer |           | $01A0 to $01FD |

