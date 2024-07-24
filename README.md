# Family Forth Cross Compiler

Family Forth STC Forth Cross Compiler targeting the NES, based on MVPForth for the Apple II.
The compiler was created using bnfc.  Requires Maven and Java11+ to run the compiler.
cc65 is required to assembled the compiler's output.

## Clone the FamiForth repository

```
git clone https://github.com/Family-Forth/Family-ForthCC.git
```

# Kernel Configuration
W   = Working register one
W2   = Working register two
PSP = Parameter Stack Pointer
RSP = Return Stack Pointer
UP  = User Pointer

## Kernel Memory locations
W    ->  Zpage         -> $00 - $01
W2   ->  Zpage         -> $02 - $03
IP   ->  Zpage         -> $04 - $05
UP   ->  Zpage         -> $06 - $07
PSP  ->  X Register    -> $08 to $FF
RSP  ->  Stack Pointer -> $0100 to $01FF


# Comment style guide

https://www.forth.org/forth_style.html
