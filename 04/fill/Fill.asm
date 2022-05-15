// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

(KEYBOARD)
@fill
M=0

@KBD
D=M
@DRAWBLACK
D;JEQ

@fill
M=-1

(DRAWBLACK)
@SCREEN
D=A
@address
M=D

(LOOP)
@address
D=M
@24576
D=D-A
@KEYBOARD
D;JEQ

@fill
D=M
@address
A=M
M=D

@address
M=M+1

@LOOP
0;JMP