# Advanced Scripting

This page assumes that you have read/know the customization basics pages.
It covers the scripting part of the xml files.

## Useful Resources

Consider skimming this section before getting started.

### Nashorn Engine

[Nashorn](https://github.com/openjdk/nashorn>) is the javascript engine that
is used to for scripting in Shimeji. No that is not a typo; it *is* JavaScript and
not Java. JavaScript and Java still are completely different languages but
JavaScript can be run inside Java.

When calling a function from XML, trying to access a non-existent or non-public
function leads to an error. It is recommended to learn about Nashorn and its
behavior since it has a few interesting quirks that are important for debugging
(i.e print statements, accessors).

ShimejiEE has a class filter that blocks access to all classes.
Java objects cannot be directly created from the scripts.

* [Nashorn javascript engine](https://docs.oracle.com/javase/10/nashorn/introduction.htm#JSNUG136)
* [interesting behavior of Nashorn](https://www.graalvm.org/reference-manual/js/NashornMigrationGuide/)
* [More interesting behavior of Nashorn](https://wiki.openjdk.java.net/display/Nashorn/Nashorn+extensions)

### General JS & XML

When editing the XML, you cannot use certain
symbols directly (i.e <, >, &) and will have to escape them.

[Newlines aren't actually preserved in XML](https://stackoverflow.com/questions/2004386/),
so you will have to use semicolons (`;`) when writing multi-line scripts.

* [XML escape characters](https://stackoverflow.com/questions/1091945/)
* [Javascript MDN (General JS Info)](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide)

### Coordinates

0,0 is always the top left.

```
  0
0-│───────────→ x
  │
  │
  ↓
  y
```

## Variables

XML Attributes which aren't reserved for the XML structure are used as variables/scripts.

Variables and scripts are pretty much the same thing when it comes to shimeji scripting.
The only time a variable is not also a script is when it's a constant.

The returned value of a script variables is the value of the last statement that gets run.

### Kinds of Variables

The kind of variable is determined by how it's declared

```xml
<!-- numConstant = 500 -->
<ActionReference Name="Lorem" numConstant="500"/>
        
<!-- repeatScript = updated with the mascot's X coordinate each frame after it is first used -->
<ActionReference Name="Lorem" repeatScript="#{mascot.anchor.x}"/>

<!-- storedScript = the mascot's X coordinate when the variable is first used -->
<ActionReference Name="Lorem" storedScript="${mascot.anchor.x}"/>
```

#### Constant

These variables aren't evaluated as scripts and can only hold numbers, boolean, and strings.
They are defined without the symbol and curly braces.

#### Repeating Script (#)

`#{}`:
Gets initialized the first time it's used and then refreshes
the value each frame after that.

#### Stored Script ($)

`${}`:
Gets initialized the first time it's used and stores the value. Because it is not
reset, this value can be changed other scripts.

### Actions and Variables

By default, a variable named `mascot` is inserted into all script contexts. This represents the
`ScriptableMascot` object that the script is controlling.

Some actions also insert their own custom variables. For example, the dragging action inserts the
`FootX` variable, allowing users to write animations responding to it.

When defining your own variables that are independent of the built-in actions, use a lowercase
first letter to avoid conflicts with the built-in actions' variables.

### A Warning about Initialization

When relying on side effects of scripted variables to change the mascot, you need
to make sure that the script is actually called! Since scripts are lazily initialized, just
defining the variable is not enough.

If you are using a built-in variable (such as `Condition`), the program will *usually*
call the variable and therefore run the script. However it is is important to keep
in mind that some action types don't call certain variables and/or call them at
different times than other actions.

To get a sense for how the variables are called/run, you can use print statements and
observe the output from the command line.

## Hello World

This section explains how add small script to an existing action in order make it print something
to the console.

### Printing Demo

Print statements are an important tool for understanding and debugging shimeji scripts.

Make a copy of the default conf folder and place it inside a copy of a default image set folder
(create an isolated image set).

Open the actions.xml file in your preferred text edit and replace the action at line 46
with the following action. The rest of this page uses this action as an example.

```xml

<Action Name="Sit" Type="Stay" BorderType="Floor">
    <Animation Condition="${print('Hello world!');true}">
        <Pose Image="/shime11.png" ImageAnchor="64,128" Velocity="0,0" Duration="250"/>
    </Animation>
</Action>
```

Run shimeji from the command line and select the ``SitDown`` action from the behavior menu.
You should see the following output.

```
Hello world!
```

Now try changing the variable type from a stored variable to a repeating variable by replacing
the dollar sign (`$`) with a hashtag (`#`) as such.

```xml

<Action Name="Sit" Type="Stay" BorderType="Floor">
    <Animation Condition="#{print('Hello world!');true}">
        <Pose Image="/shime11.png" ImageAnchor="64,128" Velocity="0,0" Duration="250"/>
    </Animation>
</Action>
```

Running this will result in the message being printed each frame.

You can also change the message being printed to something more useful. The following example prints the
total number of frames since that mascot has been created.

```xml

<Action Name="Sit" Type="Stay" BorderType="Floor">
    <Animation Condition="#{print(mascot.time);true}">
        <Pose Image="/shime11.png" ImageAnchor="64,128" Velocity="0,0" Duration="250"/>
    </Animation>
</Action>
```

### Printing Explanation

The action adds a `Condition` statement to the `Animation` block.
This is one of the most predictable and reliable ways to run scripts with side
effects since the first `Animation` block of an action is guaranteed to be evaluated as long the
action plays an animation.

```js
// Content of the 'Hello World!' script
print('Hello world!');
true
```

The semicolon separates the statements of the script.

The `true` at the end of the script is the return value of the script since it is
the last statement to be evaluated.

The condition being `true` is very important in this case since actions containing animations need to
have at least one valid `Animation` block (The program crashes otherwise).

```js
// Content of the frame printing script
print(mascot.time);
true
```

The frame printing script uses the `mascot` object's `time` property to get the amount of
frames since the mascot was created.

## Other Examples

### Dynamic Animation

The following script uses `shime11.png` as the image while the cursor is to the left of the
mascot/shimeji. It uses `shime28.png` as the image otherwise.

```xml

<Action Name="Sit" Type="Stay" BorderType="Floor">
    <Animation Condition="#{mascot.anchor.x &gt; mascot.environment.cursor.x}">
        <Pose Image="/shime11.png" ImageAnchor="64,128" Velocity="0,0" Duration="250"/>
    </Animation>
    <Animation>
        <Pose Image="/shime28.png" ImageAnchor="64,128" Velocity="0,0" Duration="250"/>
    </Animation>
</Action>
```

### Sine Wave

```xml

<Action Type="Sequence" Name="SineHover">
    <ActionReference Name="def_SineHover"
                     SinA="${20}" SinB="${8}" Speed="${6}"
                     TargetX="${mascot.environment.workArea.right - 10}"/>
    <ActionReference Name="Fall"/>
</Action>

<!--
The Sine wave is completely inaccurate to the parameters.
It looks good enough so ¯\_(ツ)_/¯, ignore the parameter names and just mess w them until it looks right

params: SinA, SinB, Speed
-->
<Action Name="def_SineHover" Type="Move" count="${0}">
<Animation Condition="#{
        var roundedSpeed = Speed >= 0 ? Math.ceil(Speed) : Math.floor(Speed);
        mascot.anchor.translate(
            roundedSpeed,
            Math.round((SinA) * Math.sin(count/SinB))
        );
        count++;
        true
    }">
    <Pose Image="/balloonRed.png" ImageAnchor="15,22" Velocity="0,0" Duration="5"/>
</Animation>
</Action>
```

#### Actions as Functions

`<Action/>`:
Similar to a function definition.
You can use variables in the scripting without providing them directly in the implementation.
You can also provide default values, these can be overridden by the caller.

`<ActionReference/>`:
equivalent to calling the `<Action/>` "functions".
The `Name` parameter is used specify which action it calls.
Other parameters are passed as variables to the action it is calling, these can be modified by the
function if they are `${}` scripts (they'll be reset otherwise).

### Drag No Follow-Through

Removes/counteracts the built-in follow through from the drag action

```xml

<Action Name="DefaultDrag" Type="Embedded" Class="com.group_finity.mascot.action.Dragged"
        animVal="#{
            av = mascot.environment.cursor.x - FootX;
            cdx = mascot.environment.cursor.dx;
            mismatchSigns = ((cdx &gt; 0) != (av  &gt; 0));
            ((cdx == 0) || mismatchSigns) ? 0 : av
        }"
        cx="#{mascot.environment.cursor.x}">
    <Animation Condition="#{animVal &gt; 15}">
        <Pose Image="/drag-tiltleft.png" ImageAnchor="64,128" Velocity="0,0" Duration="5"/>
    </Animation>
    <Animation Condition="#{Math.abs(animVal) &lt; 16}">
        <Pose Image="/drag-center01.png" ImageAnchor="64,128" Velocity="0,0" Duration="5"/>
        <Pose Image="/drag-center02.png" ImageAnchor="64,128" Velocity="0,0" Duration="5"/>
    </Animation>
    <Animation>
        <Pose Image="/drag-tiltright.png" ImageAnchor="64,128" Velocity="0,0" Duration="5"/>
    </Animation>
</Action>
```

#### Pseudocode

Basically, if the cursor's x velocity is going the same way as the mascot, allow the anim value through. Otherwise,
block it.

```js
animValue = Cx - Fx // the usual drag cond (cursor x - foot x)

realAnim = 0
// check if cursor dx has same sign as av
if ((Cdx < 0) == (animValue < 0)) {
    realAnim = animValue
}
```

### Time-Based Conditions

Behaviour condition that's only true if it's 8:00am-8:59am local time.

```xml

<Condition Condition="#{new Date().getHours() == 8}">
    <Behavior Name="Example" Frequency="40"/>
</Condition>
```

See the [MDN Docs](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date) for more info.

### Randomize Animation

Uses JavaScript's Math module to pick a random action.

```xml

<Action Type="Animate" Name="ChooseImg" imgNum="#{Math.floor(Math.random() * 3)}">
    <Animation Condition="${imgNum == 0}">
        <Pose Image="/img0.png" ImageAnchor="64,128" Velocity="0,0" Duration="5"/>
    </Animation>
    <Animation Condition="${imgNum == 1}">
        <Pose Image="/img1.png" ImageAnchor="64,128" Velocity="0,0" Duration="5"/>
    </Animation>
    <Animation Condition="${imgNum == 2}">
        <Pose Image="/img2.png" ImageAnchor="64,128" Velocity="0,0" Duration="5"/>
    </Animation>
</Action>
```

See [this thread](https://stackoverflow.com/questions/4959975/generate-random-number-between-two-numbers-in-javascript) for more info.