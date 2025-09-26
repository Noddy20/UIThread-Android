# Updating Android UI from Worker Threads

## Can We Update Android Views on a Worker Thread?

**Yes, we can, but it is highly conditional and generally discouraged.**

The common belief that Android UI (Views) can **only** be updated on the Main Thread (UI Thread) is largely true, but the actual mechanism that enforces this rule is more nuanced than a simple check on every view method call.

---

## The Core Constraint: The `ViewRootImpl` Check

The restriction primarily exists within the **`ViewRootImpl`** class, which acts as the top-level interface between the View hierarchy and the Android Window Manager.

The `ViewRootImpl` checks the thread whenever a **layout update** is triggered and propagated up the view hierarchy. This check enforces the following rule:

| Condition | Outcome |
| :--- | :--- |
| **Update Thread $\neq$ Initialization Thread** | **Throws `CalledFromWrongThreadException`** |
| **Update Thread $=$ Initialization Thread** | **No Exception Thrown** |

### The "Catch"

1.  **Standard Case:** When a View is created on the **Main Thread** (which is the standard practice), its `ViewRootImpl` is also initialized on the Main Thread. If you try to update it from a **Worker Thread**, the thread IDs mismatch, and the `CalledFromWrongThreadException` is thrown.
2.  **The Rare Exception:** If a View (and its corresponding `ViewRootImpl`) is somehow initialized entirely on a **Worker Thread**, then updates from that *same* Worker Thread will technically pass the check and not throw the exception. However, this is an extremely rare and non-standard use case. *(For more on this edge case, see: [Is the UI operation of Android sub-thread really impossible?](https://segmentfault.com/a/1190000041870945/en))*

---

## The Loophole: Avoiding Layout Changes

The `CalledFromWrongThreadException` is **only** thrown if the UI operation triggers a **layout update** that reaches the `ViewRootImpl`.

If a view property is changed in a way that *does not* invalidate the layout or request a new measure/layout pass on the parent, the exception is **not** thrown, and the UI can technically be updated.

### Example:

If you call `textView.setText("New Text")` on a **Worker Thread**:

* **If the `TextView` uses `wrap_content` or `match_parent`:** Setting the text can change the required width/height, triggering a layout update. This propagation will reach the `ViewRootImpl`, and the exception will be thrown.
* **If the `TextView` has a fixed size (e.g., `100dp`):** Setting the text will not change its bounds, so a layout update is often *not* triggered. The update succeeds without the `CalledFromWrongThreadException`.

---

## Conclusion

While these technical loopholes exist, **always ensure your UI updates are performed on the Main Thread** using mechanisms like `Handler`, `Activity.runOnUiThread()`, or Kotlin coroutines (`withContext(Dispatchers.Main)`). Relying on the absence of a layout pass is fragile, unpredictable, and leads to code that is difficult to maintain and debug.

### Recording

https://user-images.githubusercontent.com/36729469/234297100-468a183a-cec8-4124-a881-615bc7009921.mp4
