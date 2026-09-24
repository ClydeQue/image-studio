# Image Studio

A Java Swing program for basic image processing. Midterm project, Computer Science Department, Ateneo de Zamboanga University.

## Goal

Import an image, preview it, then:

- Convert it to grayscale with one of three methods: Average, Luminosity, Lightness
- Flip it horizontally or vertically
- Save the result as PNG or JPG

Every operation is in both the menu bar and the toolbar. The window resizes freely, the toolbar wraps on narrow screens, and View > Full Screen (F11, or Ctrl+Cmd+F on macOS) fills the monitor.

## Download

1. Install Java 21 or newer if you don't have it. Get it free from [adoptium.net](https://adoptium.net).
   Check with `java -version` in a terminal.
2. Download `ImageStudio.jar` from the [latest release](https://github.com/ClydeQue/image-studio/releases/latest).
3. Double-click `ImageStudio.jar`.

If it doesn't open:

- macOS says it can't be opened: right-click the file, choose Open, then Open again.
- Windows opens it as a zip, or nothing happens: open Command Prompt in the Downloads folder and run
  `java -jar ImageStudio.jar`

Using the app:

1. Click Import and pick an image.
2. Pick a grayscale method: Average, Luminosity or Lightness.
3. Click Flip Horizontal or Flip Vertical.
4. Click Save to keep the result.

## How to run from source

You only need a JDK (21 or newer). No libraries.

macOS / Linux:

```
./run.sh
```

Windows:

```
run.bat
```

Or by hand:

```
javac -d classes $(find src -name '*.java')
java -cp classes imagestudio.ImageStudio
```

Try it with the sample image: `./run.sh test-images/color-chart.png`

Run the checks: `java -cp classes imagestudio.verify.SelfTest`

## Screenshots

Imported image

![Imported](screenshots/02-imported-color.png)

Grayscale: Average, Luminosity, Lightness

![Average](screenshots/03-grayscale-average.png)
![Luminosity](screenshots/04-grayscale-luminosity.png)
![Lightness](screenshots/05-grayscale-lightness.png)

Flip horizontal and flip vertical

![Flip horizontal](screenshots/06-flip-horizontal.png)
![Flip vertical](screenshots/07-flip-vertical.png)

Narrow window: the toolbar wraps instead of overflowing

![Narrow window](screenshots/08-narrow-window.png)

Saved output images are in `outputs/`.
