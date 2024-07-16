![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)

# FamiForth

FamiForth STC Forth Compiler targeting the NES, based on MVPForth for the Apple II.
The compiler was created using bnfc.  Requires Ant and Java to build the compiler.
cc65 is required to assembled the compiler's output.

## Clone the FamiForth repository

```
git clone https://github.com/superseeker13/FamiForth.git
```

# Kernel Configuration
W   = Working register one
W2   = Working register two
PSP = Parameter Stack Pointer
RSP = Return Stack Pointer
UP  = User Pointer
TOP = Top of stack
BOS = Bottom of stack


## Kernel Memory locations
W    ->  Zpage         -> $00 - $01
W2   ->  Zpage         -> $02 - $03
IP   ->  Zpage         -> $04 - $05
UP   ->  Zpage         -> $06 - $07
PSP  ->  X Register    -> $08 to $FF
RSP  ->  Stack Pointer -> $0100 to $01FF


# Comment style guide

https://www.forth.org/forth_style.html
