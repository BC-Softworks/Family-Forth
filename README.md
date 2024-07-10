![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)](http://unlicense.org/)

# FamiForth

FamiForth STC Forth Compiler targeting the NES, based on MVPForth for the Apple II.
The compiler is built on top of cc65 and is build using make.
cc65 and make are the only dependencies.

## Clone the FamiForth repository

```
git clone https://github.com/superseeker13/FamiForth.git

```

# Kernel Configuration
W   = Working register one
X   = Working register two
PSP = Parameter Stack Pointer
RSP = Return Stack Pointer
UP  = User Pointer
TOP = Top of stack
BOS = Bottom of stack


## Kernel Memory locations
W    -> Zpage $00-$01
IP   -> Zpage $02-$03
UP   -> Zpage $04-$05
TOP  -> Zpage $06
BOS  -> Zpage $07
PSP  -> X Register    -> $08 to $FF
RSP  -> Stack Pointer -> $0100 to $01FF



# Comment style guide

https://www.forth.org/forth_style.html
