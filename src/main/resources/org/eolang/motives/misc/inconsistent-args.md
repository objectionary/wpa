# Inconsistent args

Objects with the same `@base` must have the same number of arguments passed
to them.
A bare reference without arguments, like `bar > z`, never clashes
with other usages, since EO permits partial binding of voids.

Incorrect:

```eo
# Foo.
[] > foo
  bar 42 > x
  bar 1 2 3 > y
```

Correct:

```eo
# Foo.
[] > foo
  bar 42 > x
  bar 1 > y
```
