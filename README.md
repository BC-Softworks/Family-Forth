[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Family Forth Cross Compiler

Family Forth STC Forth Cross Compiler targeting the NES, based on MVPForth for the Apple II.
Requires Maven and Java11+ to run the compiler.
cc65 is required to assemble the compiler's output.

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
| PSP  |  X Register    |           | $10 to $FF     |
| RSP  |  Stack Pointer |           | $01A0 to $01FF |


# Comment style guide

https://www.forth.org/forth_style.html
