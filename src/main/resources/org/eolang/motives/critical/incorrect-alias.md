# Incorrect alias

The special meta `+alias` must point to an existing file in the directory specified by
the `+package` meta.

Incorrect:

```eo
+alias foo
+package ttt

# Bar.
[] > bar
  foo > @
```

The analyzer reports a `critical` defect because `ttt/foo.xmir` doesn't exist.
