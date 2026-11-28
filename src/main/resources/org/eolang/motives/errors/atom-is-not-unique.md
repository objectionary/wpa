# Duplicate atom name

Use a unique fully qualified name (FQN) for each atom across all `.eo` files.

Incorrect:

`foo.eo`:

```eo
+package xyz

# A.
[] > a /number
```

`bar.eo`:

```eo
+package xyz

# A.
[] > a /number
```

To fix this, rename the duplicated object.
