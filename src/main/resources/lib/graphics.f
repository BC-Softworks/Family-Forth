\ NES graphics library
\ author: Edward Conn

\ Defines the following words
\ NMI_READY SPRITE

require "core_ext.f"

PPUMASK    CONSTANT $2001
PPUSTATUS  CONSTANT $2002
OAMADDR    CONSTANT $2003
PPUSCROLL  CONSTANT $2005
PPUADDR    CONSTANT $2006
PPUDATA    CONSTANT $2007
 
VARIABLE NMI_FLAG

segment "CODE"

: NMI_READY ( -- true | false) NMI_FLAG ;

: SPRITE ;

