
![](./logo.png)

# Documentation

 - [Common Mistakes](#common-mistakes)
   * [Declaring variables between instructions](#declaring-variables-between-instructions)
   * [Dividing by ZERO](#dividing-by-zero)
 - [Compiler Basics](#compiler-basics)
   * [Constants](#constants)
   * [Labels](#labels)
   * [Compiler Instructions](#compiler-instructions)
 - [Instructions](#instructions)
   * [NULL](#null)
   * [BRK](#brk)
   * [DATA](#data)
   * [MOV](#mov)
   * [SWP](#swp)
   * [OUTI](#outi)
   * [OUTC](#outc)
   * [GETI](#geti)
   * [GETC](#getc)
   * [GETK](#getk)
   * [TS](#ts)
   * [TMS](#tms)
   * [INC](#inc)
   * [DEC](#dec)
   * [ADD](#add)
   * [SUB](#sub)
   * [MUL](#mul)
   * [DIV](#div)
   * [MOD](#mod)
   * [AND](#and)
   * [OR](#or)
   * [NOT](#not)
   * [XOR](#xor)
   * [CMP](#cmp)
   * [JMP](#jmp)
   * [JC](#jc)
   * [JNC](#jnc)
   * [JZ](#jz)
   * [JNZ](#jnz)
   * [CALL](#call)
   * [RET](#ret)
   * [PUSH](#push)
   * [POP](#pop)
   * [HLT](#hlt)

# Common Mistakes

## Declaring variables between instructions

The following example would show the variable to the console and execute the instruction with code 20 and stop
the processor:

```Assembly
OUTI var
var: #DW 20
HLT
```

We don't want to execute that because it's not intended to be an instruction, a fix for that would be adding a label
which indicates where the program starts and defining variables before that:

```Assembly
JMP start

var: #DW 20

start:
OUTI var
HLT
```

The code above won't execute the contents of the declared variable.

## Dividing by ZERO

The processor crashes when dividing a number by zero, no division by zero is executed inside the emulator's code,
so most of the time it's the user's fault.

## Setting the clock too high

The clock can go up to 1 instruction every nanosecond (1GHz), though it's overkill for most programs and will probably
lock the emulator if the program has an uncapped loop that writes to the console, so it's not recommended going above the
1MHz mark.

# Compiler Basics

## Constants

Constants can be declared as follows:

```Assembly
@const 10
```

Constants are values, they don't point to anything in memory, so they are rarely used as arguments for instructions (see [DATA](#data)).
Though they are useful in one case, since they don't change the memory where they are defined, you can use them to put
config options at the top of your program and add them later to memory:

```Assembly
; Since this doesn't change memory it doesn't execute any instruction
@time_till_stop 10

JMP start

start:
HLT

; We can add it to memory where we like
key: #DW @time_till_stop
```

Constants are parsed as they come up, so if you redefine one, values where said constant is used depend on its
last defined value.
There are some constants that are already defined, such as virtual keys (or VK), the emulator uses reflection to get
all VKs from the [KeyEvent](https://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyEvent.html) class that then get
used as a base template for constants. So if you want to define a variable that stores the value of a virtual key, you
can go to the above linked class's docs and search if there's the field (starting with `VK_`) that you want to use:

```Assembly
; If I want to put the enter key's key code inside my memory,
;  I can do it like this:
VK_ENTER: #DW @VK_ENTER
; Then I can use the declared variable to check if the key was pressed
```

## Labels

These are the most useful, since they point to somewhere in memory, where they point is based on where they are declared:

```Assembly
; Since labels can only be declared once, they are global to the program
; All instructions use pointers to handle arguments
; i.e. OUTC wants the pointer to the character to print
OUTC char

; This label will always point to the memory where
;  the character 'C' is stored
char: #DS 'C'
```

Labels are declared by putting `:` in front of a name and used by using only the name, because they aren't declared and
used using the same syntax, if the compiler sees `:` after a name will always try to turn it into a label:

```Assembly
; Moving the contents of str_ptr to arg
; This makes possible to change instruction arguments at runtime
MOV arg str_ptr
OUTC arg: 0
HLT

; Declaring a label that points to a string
str: #DS "Hello World!\0"
; Declaring a label that points to a label's address
str_ptr: #DW str
```

## Compiler Instructions

These instructions are useful to put values into memory where there's no processor instruction:

```Assembly
; This puts the value 10 at address 0
#DW 10
```

They are 2 and always have an `#` in front of them:
 - \#DW (Define Word): Can be followed by a [constant](#constants), a [label](#labels) or a numeric value.
 - \#DS (Define String): Can only be followed by a string (`"\"This is a string\""` or `'"This is a string"'`).

```Assembly
; This declares a label called "number" that points to
;  the address 0, which contains the value defined by #DW, which is 30
number: #DW 30

; #DS adds all characters in the string to memory, so the label "string"
;  is only pointing to the first character ("H")
string: #DS "Hello World!\0"
; #DS also supports common special characters: '\0', '\n', '\t'
```

# Instructions

## NULL

This instruction doesn't do anything and its code is 0, this is used to ignore empty memory.

## BRK

This instruction is a simple breakpoint, the processor can be resumed by going to Processor->Pause/Resume.
This instruction shouldn't be used in the final program, it's just a way of debugging your code by automatically pausing
the processor at a point in the program without having to press the pause button in the app.

## DATA

`DATA addr val`

Sets the value at `addr` to `val`.

**Example:**

```Assembly
; Sets the contents of val to the value of the
;  constant @VK_A
DATA val @VK_A

; Printing contents of val, it isn't equal to
;  10 because it was overridden by DATA
OUTI val
HLT

val: #DW 10
```

## MOV

`MOV dst src`

Moves the contents of `src` into `dst` (doesn't reset `src`).

**Example:**

```Assembly
; This is used to show on the console
;  what values the two variables hold
OUTI _value1 OUTC space OUTI _value2

; This copies the value at _value2
;  to _value1, so _value1 becomes 25
MOV _value1 _value2

OUTI _value1 OUTC space OUTI _value2
HLT

_value1: #DW 0
_value2: #DW 25
space: #DS ' '
```

## SWP

`SWP val1 val2`

Swaps the contents of `val1` and `val2`.

**Example:**

```Assembly
; This is used to show on the console
;  what values the two variables hold
OUTI _value1 OUTC space OUTI _value2

; This swaps the values in _value1 and _value2
;  this makes _value1 become 25 and _value2 0
SWP _value1 _value2

OUTI _value1 OUTC space OUTI _value2
HLT

_value1: #DW 0
_value2: #DW 25
space: #DS ' '
```

## OUTI

`OUTI val`

Outputs the contents of `val` to the console as a number.

**Example:**

```Assembly
; This prints on the console the
;  specified variable as a number
OUTI _number

HLT

_number: #DW 136
```

## OUTC

`OUTC char`

Outputs the contents of `char` to the console as a character.
The null termination character can be used to clear the console.

**Example:**

```Assembly
; This prints on the console the
;  specified variable as a character
; If the null character is displayed
;  the console is cleared
OUTC _char

HLT

_char: #DS 'C'
```

## GETI

`GETI dst`

Gets the currently selected key's numeric value and sets `dst` to it.
If the pressed key doesn't have a numeric character, it's set to **0**.

**Example:**

```Assembly
loop:

; Gets the currently pressed key
;  converted to a number and stores
;  it in the specified variable
GETI _number_pressed

; Clearing console and displaying
;  the pressed number
OUTC null
OUTI _number_pressed

; Checking if the number pressed is
;  the one used to exit the loop
CMP _number_pressed _quit_value
JNZ loop
HLT

_number_pressed: #DW 0
_quit_value: #DW 9
null: #DS '\0'
```

## GETC

`GETC dst`

Gets the currently selected valid character and sets `dst` to its [char code](https://www.rapidtables.com/code/text/ascii-table.html).
If no valid character is pressed, it's set to **0**.

**Example:**

```Assembly
loop:

; Gets the currently pressed
;  valid character
GETC _char_pressed

; Clearing console and displaying
;  the pressed character
OUTC null
OUTI _char_pressed

; Checking if the char pressed is
;  the one used to exit the loop
CMP _char_pressed _quit_char
JNZ loop
HLT

_char_pressed: #DW 0
_quit_char: #DS 'q'
null: #DW 0
```

## GETK

`GETK dst`

Gets the currently pressed key and sets `dst` to its [keycode](https://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyEvent.html)
(Constants can be used to add keys to memory. e.g. `@VK_ENTER`).
If no key is pressed, it's set to **0**.

**Example:**

```Assembly
loop:

; Keycodes can be found @https://stackoverflow.com/a/31637206
; Gets the currently pressed key
GETK _key_pressed

; Clearing console and displaying
;  the pressed key keycode
OUTC null
OUTI _key_pressed

; Checking if the key pressed is
;  the ENTER key
CMP _key_pressed VK_ENTER
JNZ loop
HLT

_key_pressed: #DW 0
VK_ENTER: #DW @VK_ENTER
null: #DW 0
```

## TS

`TS dst`

Gets the seconds elapsed since the processor started and puts them into `dst`.

**Example:**

```Assembly
loop:

; Getting time elapsed (in seconds) since the
;  processor started
TS _time_elapsed

; Showing the time on the console
OUTC null
OUTI _time_elapsed

; Stopping processor after 10 seconds
CMP _time_elapsed _max_time
JC loop
HLT

_time_elapsed: #DW 0
_max_time: #DW 10
null: #DW 0
```

## TMS

`TMS dst`

Gets the milliseconds elapsed since the processor started and puts them into `dst`.
**This instruction is basically useless if using a processor with words that are less than 16 bits long.**

**Example:**

```Assembly
; This example only works with processors that have
;  16 bits or greater words
loop:

; Getting time elapsed (in milliseconds) since the
;  processor started
TMS _time_elapsed

; Showing the time on the console
OUTC null
OUTI _time_elapsed

; Stopping processor after 10 seconds
CMP _time_elapsed _max_time
JC loop
HLT

_time_elapsed: #DW 0
_max_time: #DW 10000
null: #DS '0'
```

## INC

`INC target`

Increments the value of `target` by 1.
Sets Carry and Zero flags accordingly.

**Example:**

```Assembly
; Showing value on the console
OUTI _value

; Incrementing _value
INC _value

OUTC newline
OUTI _value
HLT

_value: #DW 10
newline: #DS '\n'
```

## DEC

`DEC target`

Decrements the value of `target` by 1.
Sets Carry and Zero flags accordingly.

**Example:**

```Assembly
; Showing value on the console
OUTI _value

; Decrementing _value
DEC _value

OUTC newline
OUTI _value
HLT

_value: #DW 10
newline: #DS '\n'
```

## ADD

`ADD a b`

Adds `b` to `a` and stores the result in `a`.
Sets Carry and Zero flags accordingly.

**Example:**

```Assembly
; Showing operation on the console
OUTI _a OUTC plus_sign OUTI _b OUTC equal_sign

; Adding _b to _a and storing value in _a
ADD _a _b

OUTI _a
HLT

_a: #DW 11
_b: #DW 10
plus_sign: #DS '+'
equal_sign: #DS '='
```

## SUB

`SUB a b`

Subtracts `b` from `a` and stores the result in `a`.
Sets Carry and Zero flags accordingly.

**Example:**

```Assembly
; Showing operation on the console
OUTI _a OUTC minus_sign OUTI _b OUTC equal_sign

; Subtracting _b to _a and storing value in _a
SUB _a _b

OUTI _a
HLT

_a: #DW 11
_b: #DW 10
minus_sign: #DS '-'
equal_sign: #DS '='
```

## MUL

`MUL a b`

Multiplies `a` by `b` and stores the result in `a`.
Sets Carry and Zero flags accordingly.

**Example:**

```Assembly
; Showing operation on the console
OUTI _a OUTC mult_sign OUTI _b OUTC equal_sign

; Multiplying _a by _b and storing value in _a
MUL _a _b

OUTI _a
HLT

_a: #DW 11
_b: #DW 10
mult_sign: #DS '*'
equal_sign: #DS '='
```

## DIV

`DIV a b`

Divides `a` by `b` and stores the result (floored) in `a`.
Sets Carry and Zero flags accordingly.

**NOTE:** May produce Division by Zero exception.

**Example:**

```Assembly
; Showing operation on the console
OUTI _a OUTC div_sign OUTI _b OUTC equal_sign

; Dividing _a by _b and storing value in _a
; NOTE: The result is floored, division by 0 will cause
;        the processor to crash
DIV _a _b

OUTI _a
HLT

_a: #DW 11
_b: #DW 10
div_sign: #DS '/'
equal_sign: #DS '='
```

## MOD

`MOD a b`

Stores `a` modulus `b` in `a`.
Sets Carry and Zero flags accordingly.

**NOTE:** May produce Division by Zero exception.

**Example:**

```Assembly
; Showing operation on the console
OUTI _a OUTC modulo_sign OUTI _b OUTC equal_sign

; Calculating _a mod _b and storing value in _a
MOD _a _b

OUTI _a
HLT

_a: #DW 11
_b: #DW 3
modulo_sign: #DS '%'
equal_sign: #DS '='
```

## AND

`AND a b`

Calculates the Bitwise And on `a` and `b` and stores the result in `a`.
Sets Zero flag accordingly (Carry flag doesn't change).

**Example:**

```Assembly
; Showing operation on the console
OUTI _a OUTC and_sign OUTI _b OUTC equal_sign

; Calculating _a & _b and storing value in _a
AND _a _b

OUTI _a
HLT

_a: #DW 0x0F
_b: #DW 0xFA
and_sign: #DS '&'
equal_sign: #DS '='
```

## OR

`OR a b`

Calculates the Bitwise Or on `a` and `b` and stores the result in `a`.
Sets Zero flag accordingly (Carry flag doesn't change).

**Example:**

```Assembly
; Showing operation on the console
OUTI _a OUTC or_sign OUTI _b OUTC equal_sign

; Calculating _a | _b and storing value in _a
OR _a _b

OUTI _a
HLT

_a: #DW 0x0C
_b: #DW 0xF2
or_sign: #DS '|'
equal_sign: #DS '='
```

## NOT

`NOT val`

Calculates the Bitwise Not on `val` and stores the result in `val`.
Sets Zero flag accordingly (Carry flag doesn't change).

**Example:**

```Assembly
; Showing operation on the console
OUTC not_sign OUTI _val OUTC equal_sign

; Calculating ~_val storing value in _val
NOT _val

OUTI _val
HLT

_val: #DW 0x0F
not_sign: #DS '~'
equal_sign: #DS '='
```

## XOR

`OR a b`

Calculates the Bitwise Xor on `a` and `b` and stores the result in `a`.
Sets Zero flag accordingly (Carry flag doesn't change).

**Example:**

```Assembly
; Showing operation on the console
OUTI _a OUTC xor_sign OUTI _b OUTC equal_sign

; Calculating _a ^ _b and storing value in _a
XOR _a _b

OUTI _a
HLT

_a: #DW 0x0C
_b: #DW 0xAD
xor_sign: #DS '^'
equal_sign: #DS '='
```

## CMP

`CMP a b`

Compares `a` and `b` and sets Carry and Zero flags as follows:
 - Carry: `a < b`
 - Zero: `a == b`

**Example:**

```Assembly
; Jump to the start of the program
JMP start

; Temp registry, used to store temp values
_temp: #DW 0

; Function's arguments
_are_eq_a: #DW 0
_are_eq_b: #DW 0
; Function that prints if two numbers are equal
f_are_eq:
    ; Getting arguments
    ; (When calling a function the first thing on the stack is the
    ;  return pointer, so we need to POP it and PUSH it again)
    POP _temp
    POP _are_eq_a
    POP _are_eq_b
    PUSH _temp

    ; Compare the two arguments and set
    ;  Zero and Carry flags accordingly
    ;  (Zero = 1 -> a == b; Carry = 1 -> a < b)
    CMP _are_eq_a _are_eq_b
    ; If Zero flag is off (not equal), jump to the
    ;  part of the function that prints that
    JNZ _are_eq_not

    ; If they are equal print that and return
    OUTI _are_eq_a OUTC equal_sign OUTC equal_sign OUTI _are_eq_b
    RET

    _are_eq_not:
    OUTI _are_eq_a OUTC not_sign OUTC equal_sign OUTI _are_eq_b
    RET

start:
    ; Pushing arguments to the stack
    ;  and calling the function
    PUSH _v1 PUSH _v2
    CALL f_are_eq

    OUTC newline

    PUSH _v1 PUSH _v3
    CALL f_are_eq
    HLT

_v1: #DW 11
_v2: #DW 10
_v3: #DW 11

equal_sign: #DS '='
not_sign: #DS '!'
newline: #DS '\n'
```

## JMP

`JMP addr`

Jumps to the specified address (`addr`).

**Example:**

```Assembly
; Jumping to the start label
JMP start

; This instruction doesn't get executed because it was jumped
OUTC _char

start:
; Jumping to the end label
JMP end

; This instruction doesn't get executed because it was jumped
OUTC _char

end:
HLT

_char: #DS 'H'
```

## JC

The same as [JMP](#jmp) but gets executed only if the Carry flag is set to true.

## JNC

The same as [JMP](#jmp) but gets executed only if the Carry flag is set to false.

## JZ

The same as [JMP](#jmp) but gets executed only if the Zero flag is set to true.

## JNZ

The same as [JMP](#jmp) but gets executed only if the Zero flag is set to false.

## CALL

`CALL addr`

Jumps to the specified address (`addr`) and pushes the pointer to the next instruction into the stack.
This is used with [RET](#ret).

**Example:**

```Assembly
JMP start

_temp: #DW 0

_inc_max_value_ptr: #DW 0
_inc_max_max_value: #DW 0
; Declaring function that will get called
f_inc_max:
    ; Popping arguments from the stack without forgetting that
    ;  there's also the return pointer because this is a function
    POP _temp
    POP _inc_max_max_value
    POP _inc_max_value_ptr
    PUSH _temp
    
    ; Moving the pointer to the value to CMP's first argument
    MOV _inc_max_cmp_ptr _inc_max_value_ptr
    CMP _inc_max_cmp_ptr: 0 _inc_max_max_value
    JZ _inc_max_end
    
    ; Moving the pointer to the value to INC's first argument
    MOV _inc_max_inc_ptr _inc_max_value_ptr
    INC _inc_max_inc_ptr: 0
    
    _inc_max_end:
    ; Use the return pointer in the stack to go back
    ;  to where this function was called
    RET

start:
; Print to the console the value
OUTI _value
OUTC newline

; Pushing arguments for function
;  and using CALL to call it
PUSH _value_ptr PUSH _max
CALL f_inc_max ; The value should be 11

PUSH _value_ptr PUSH _max
CALL f_inc_max ; The value should be 12

PUSH _value_ptr PUSH _max
CALL f_inc_max ; The value should still be 12

OUTI _value
HLT

_value: #DW 10
_value_ptr: #DW _value
_max: #DW 12
newline: #DS '\n'
```

## RET

`RET`

Pops the stack and jumps to the address that was popped.
This is used with [CALL](#call), see its example.

## PUSH

`PUSH val`

Pushes `val` to the stack.
This is used with [POP](#pop).

**Example:**

```Assembly
; Printing the value of _v1 to the console
OUTI _v1
OUTC newline

; Pushing the value of _v1 to the stack
PUSH _v1
; Popping the last value on the stack to
;  the variable _v2
POP _v2

; Printing the value of _v2 to the console
;  (Should be the same as _v1)
OUTI _v2
HLT

_v1: #DW 33
_v2: #DW 46
newline: #DS '\n'
```

## POP

`POP dst`

Pops the last value on the stack and stores it into `dst`.
This is used with [PUSH](#push), see its example.

## HLT

`HLT`

Tells the processor to stop the execution of the program.

**Example:**

```Assembly
start:
; Tells the processor to stop the execution
;  of this program
HLT

; This instruction will never get executed
JMP start
```
