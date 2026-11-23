# `+unlint` of non-existing defect

Use the `+unlint` meta to suppress a defect the linter would otherwise report.

Incorrect (the file contains no duplicate metas):

```eo
+unlint duplicate-metas

[] > foo
  42 > @
```

Correct:

```eo
+unlint duplicate-metas
+architect jeff
+architect foo

[] > foo
  42 > @
```
