# Todo

## Immediate
- Start writing xml documentation
- Test actions and sounds
- Keyboard shortcuts

## Distant
- Possibly try JNI to replace some JNA code for performance
- ChromeOS/Android
- True Non-rectangular window on mac (pass-through clicks on transparent areas)
  - Will need some JNI
- Scaling
  - I've gotten scaling to working on mac, but it's not really worth implementing as is.
    - It breaks scaling on Windows, and I's rather not add more platform specific checks than necessary
    - It's a pretty bad experience without the pass-through clicks.
- Add testing