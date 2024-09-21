\ NES graphics library
\ author: Edward Conn

\ Defines the following words
\ NMI_READY SPRITE

require "core_ext.f"

PPU_MASK    CONSTANT $2001
PPU_STATUS  CONSTANT $2002
OAM_ADDR    CONSTANT $2003
PPU_SCROLL  CONSTANT $2005
PPU_ADDR    CONSTANT $2006
PPU_DATA    CONSTANT $2007
 
VARIABLE NMI_FLAG

segment "CODE"

: NMI_READY ( -- true | false) NMI_FLAG ;

: SPRITE ;

