package com.example

/**
 * Bridge activity in `com.example` inheriting from `com.fsag.peek.MainActivity`
 * so existing unit/Robolectric tests referencing `com.example.MainActivity` continue to work seamlessly.
 */
class MainActivity : com.fsag.peek.MainActivity()
