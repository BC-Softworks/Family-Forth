[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Maven Build](https://github.com/BC-Softworks/Family-Forth/actions/workflows/maven.yml/badge.svg)

# Family Forth Cross Compiler

Forth cross compiler targeting the Ricoh 2A03.

Requires Maven and Java 11+ to build the compiler.

ca65 and ld65 are required to assemble the compiler's output.

## Installation guide

Clone the repository
```
git clone https://github.com/BC-Softworks/Family-Forth.git
```

Build the jar
```
mvn package
```

## Testing 

Test the compiler
```
mvn check
```

Build the jar
```
(cd src/main/asm; make)
```