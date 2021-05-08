# Kilkakon's notes:

## 1.0.16:

- "Move Between Screens" toggle to prevent shimeji changing screens unexpectedly for multiscreen users
- "Transformation" behaviour toggle to stop Shimeji transforming
- hqx scaling filter for scaling factors above 1, using [Edu Garcia's version](https://github.com/Arcnor/hqx-java)
- Generic sound folder to allow different Shimeji to share the same sound files
- Count variable exposed to xml conditions (number of shimeji of the current type as opposed to all types)
- Muting action (stop a specific or all sounds)
- Windows Lockscreen freezing shimeji bug fix
- Plugging in another screen freezing shimeji bug fix

See [https://www.youtube.com/watch?v=fHzbW8glPMs](https://www.youtube.com/watch?v=fHzbW8glPMs) for more details

## 1.0.15:

- Japanese Shimeji-e backwards compatibility

See [https://www.youtube.com/watch?v=65p39FjbQPI](https://www.youtube.com/watch?v=65p39FjbQPI) for more details

## 1.0.14:

- Affordances

See [https://www.youtube.com/watch?v=24aPIWOOzfA](https://www.youtube.com/watch?v=24aPIWOOzfA) for more details

## 1.0.13:

- Draggable property
- `SelfDestruct` action
- Scaling bug fix
- Clarified missing imageRight error message

See [https://www.youtube.com/watch?v=Lcx2wVPiKUk](https://www.youtube.com/watch?v=Lcx2wVPiKUk) for more details

## 1.0.12:

- Transform action
- Shimeji scaling
- Menu DPI awareness
- Improved memory usage
- 3 more languages

See [https://www.youtube.com/watch?v=nZ_IW5quK8g](https://www.youtube.com/watch?v=nZ_IW5quK8g) for more details

## 1.0.11:

- New menu system
- Included [nimrod theming](http://nilogonzalez.es/nimrodlf/index-en.html)
- 5 more languages

See [https://www.youtube.com/watch?v=GUr-NiSyTM4](https://www.youtube.com/watch?v=GUr-NiSyTM4) for more details

## 1.0.10:

- Internationalisation
- Tweaked the build xml
- Shimeji that fail to load will not abort the load sequence if there's other shimeji in the queue

See [https://www.youtube.com/watch?v=mZ0Za94i76A](https://www.youtube.com/watch?v=mZ0Za94i76A) for more details

## 1.0.9:

- Sound and Volume attribute on Pose tags to allow playing sound files
- Fix for Turn action
- Improved error message dialog

See [https://www.youtube.com/watch?v=5CMy4xqdkrg](https://www.youtube.com/watch?v=5CMy4xqdkrg) for more details

## 1.0.8:

- Asymmetry support with new ImageRight attribute
- New MoveWithTurn and Turn actions
- Add Interactive Windows dialog no longer options twice

See [https://www.youtube.com/watch?v=RmkzZ3Shf24](https://www.youtube.com/watch?v=RmkzZ3Shf24) for more details

## 1.0.7:

- 64-bit support
- BornMascot attribute for the Breed action
- Allowed Behaviour menu

## 1.0.6:

- Added premultiplying to the images so translucent images work now, yay!
- Also did some regexing to the behaviour list so it looks nicer

## 1.0.5:

I did some basic changes to the program to make this new version. I did make an effort to contact
the original group but they didn't reply--so I'm releasing this as is, with full credit to them. :)

So cheers shimeji-ee group!

I may have accidentally pressed Alt+Shift+F in Netbeans in a few of the files because I can't stand 
`if (...) {` style brackets. Sorry if that offends anybody. :O

The main changes I made in this version:

- settings.properties instead of windows.txt and ActiveShimeji
- New form and renamed captions and reworked menus
- Not showing form on load