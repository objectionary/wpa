# Duplicate object name

Use a unique name for each top-level object in a package.

Incorrect:

`foo.eo`:

```eo
+package foo

# Foo.
[] > foo
```

`bar.eo`:

```eo
+package foo

# Bar.
[] > foo
```

Correct:

`foo.eo`:

```eo
+package foo

# Foo.
[] > foo
```

`bar.eo`:

```eo
+package foo

# Bar.
[] > bar
```
