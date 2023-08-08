# Monkey-Interpreter

A java version `monkey` language interpreter based on [《Writing an interpreter in Go》](https://interpreterbook.com/) without any third party libraries.

## Usage

```
Usage: java -jar Interpreter.jar <command> [<arguments>]
Available commands:
  -h           Show help
  -c           Specifies the source file path
  -cs          Specifies the source file path,and display the description of the program
```

or

```
java -jar Interpreter.jar
>>> let x = 10;
>>> puts(x);
```

## Summary

the monkey language has the following characteristics:

- C-like syntax
- variable bindings
- doubles 、 integers and booleans
- a string data structure
- an array data structure
- a hash data structure
- implicit data conversion
- arithmetic expressions
- built-in functions
- functions closures
- dot function call
- loop and if and ternary expression
- call java function
- macro system

### Syntax overview

An example of Fibonacci function.

```
let fibonacci = function(x) {
  if (x <= 0) {
    x
  } else {
    fibonacci(x - 1) + fibonacci(x - 2);
  }
};

fibonacci(10);
```

#### If

It supports the general `if`. `else` exists, but` else if` does not exist.

```
let x = true;
if (x) {
  10;
} else {
  5;
}

or

x ? 10 : 5;
```

#### Operators

It supports the general operations.

```
1 + 2 + (3 * 4) - (10 / 5);
!true;
!false;
+10;
-5;
"Hello" + " " + "World";
[1,2] + [3,4]
{"key":"value,3:4} - {3:4}
```

#### Return

It returns the value immediately. No further processing will be executed.

```
if (true) {
  return;
}
```

```
let identity = function(x) {
  return x;
};

identity("Monkey");
```

### while loop

```
let x = 0;
let y = 0;
while(x < 10){
    y += x;
    x += 1;
}
y
```

### Variable bindings

Variable bindings, such as those supported by many programming languages, are implemented. Variables can be defined using the `let` keyword.

**Format:**

```
let <identifier> = <expression>;
```

**Example:**

```
let x = 0;
let y = 10;
let foobar = add(5, 5);
let alias = foobar;
let identity = function(x) { x };
```

### Literals

#### Integer

`Integer` represents an integer value. Floating point numbers can not be handled.

**Format:**

```
[-+]?[1-9][0-9]*;
```

**Example:**

```
10;
1234;
```

#### Boolean

`Boolean` represents a general boolean types.

**Format:**

```
true | false;
```

**Example:**

```
true;
false;

let truthy = !false;
let falsy = !true;
```

#### Double

`Double` represents a floating point number.;

**Format:**

```
[-+]?[1-9][0-9]*.[0-9]*;
```

**Example:**

```
3.14159;
-23.14159265359;
```

#### String

`String` represents a string. Only double quotes can be used.

**Format:**

```
"<value>";
```

**Example:**

```
"Monkey Programming Language";
"Hello" + " " + "World";
```

#### Array

`Array` represents an ordered contiguous element. Each element can contain different data types.

**Format:**

```
[<expression>, <expression>, ...];
```

**Example:**

```
[1, 2, 3 + 3, function(x) { x }, add(2, 2), true];
```

```
let arr = [1, true, function(x) { x }];

arr[0];
arr[1];
arr[2](10);
arr[1 + 1](10);
```

#### Hashes

`Hash` expresses data associating keys with values.

**Format:**

```
{ <expression>: <expression>, <expression>: <expression>, ... };
```

**Example:**

```
let hash = {
  "name": "Jimmy",
  "age": 72,
  true: "a boolean",
  99: "an integer"
};

hash["name"];
hash["a" + "ge"];
hash[true];
hash[99];
hash[100 - 1];
```

#### Function

`Function` supports functions like those supported by other programming languages.

**Format:**

```
function (<parameter one>, <parameter two>, ...) { <block statement> };
```

**Example:**

```
let add = function(x, y) {
  return x + y;
};

add(10, 20);
10.add(20);
```

```
let add = function(x, y) {
  x + y;
};

add(10, 20);
10.add(20);
```

If `return` does not exist, it returns the result of the last evaluated expression.

```
let addThree = function(x) { x + 3 };
let callTwoTimes = function(x, f) { f(f(x)) };

callTwoTimes(3, addThree);
3.callTwoTimes(addThree);
```

Passing around functions, higher-order functions and closures will also work.

### Built-in Functions

You can use 14 built-in functions :rocket:

#### `puts(<arg1>, <arg2>, ...): void`

it will print the arguments to the console.

```
puts("Hello");
puts("World!");
```

#### `size(<arg>): Intger`

For `String`, it returns the number of characters. If it's `Array`, it returns the number of elements.

```
size("Monkey");
"Monkey".size();
size([0, 1, 2]);
[0,1,2].size();
```

#### `push(<arg1>, <arg2>): Array`

Returns a new `Array` with the element specified at the end added.

```
push([0, 1], 2);
```

#### `pop(<arg>): Object`

remove the last element of the `Array` and returns it.

```
[1,2].pop();
```

#### `type(<arg>):String`

Returns the type of the argument.

```
1.type();
type(3.14);
type([]);
size.type();
```

#### `toString(<arg>):String`

Convert the argument to a `String`.

```
1.toString();
[].toString();
```

#### `toInteger(<arg>):Integer`

Convert the argument to an `Integer`.

```
true.toInteger();
3.14.toInteger();
```

#### `toDouble(<arg>):Double`

Convert the argument to a `Double`.

```
true.toDouble();
3.14.toDouble();
1.toDouble();
```

#### `toBoolean(<arg>):Boolean`

Convert the argument to a `Boolean`.

```
0.toBoolean();
"".toBoolean();
null.toBoolean();
```

#### `toJava(<arg>):Java`

Convert the monkey object to java object to Call java function

```
1.toJava();
```

#### `clone(<arg>):Object`

clone a monkey object

```
[1,3,4].clone();
4.clone();
true.clone();
clone({});
clone("s");
```

#### `listBuiltin()`

list all builtin monkey functions

```
listBuiltin()
```

#### `callJava` and `callJavaD`

call `java` function in `monkey`

```
callJava("com.example.JavaClass","javaMethod",instance,args...);
callJava("directory","com.example.JavaClass","javaMethod",instance,args...);
```

### macro system

```
let unless = macro(condition, consequence, alternative) {
    quote(if (!(unquote(condition))) {
        unquote(consequence);
    } else {
        unquote(alternative);
    });
};

unless(10 > 5,puts("not greater"), puts("greater"));
(10 > 12).unless(puts("not greater"), puts("greater"));
```