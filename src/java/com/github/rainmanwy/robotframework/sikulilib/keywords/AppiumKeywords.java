package com.github.rainmanwy.robotframework.sikulilib.keywords;

import com.github.rainmanwy.robotframework.sikulilib.exceptions.ScreenOperationException;
import com.github.rainmanwy.robotframework.sikulilib.exceptions.TimeoutException;
import com.github.rainmanwy.robotframework.sikulilib.utils.AppiumHelper;
import com.github.rainmanwy.robotframework.sikulilib.utils.CaptureFolder;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.KeyEventMetaModifier;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;


@RobotKeywords
public class AppiumKeywords {

    private static double DEFAULT_TIMEOUT = 3.0;
    private double timeOut;
    private long stepWait = 2;

    private boolean clearText = false;

    public static AppiumDriver driver = null;
    private static ImageAsScreen screen = new ImageAsScreen();
    private static Region region;

    private Boolean isCaptureMatchedImage = true;

    public AppiumKeywords() {
        timeOut = DEFAULT_TIMEOUT;
    }

    public AppiumHelper helper = new AppiumHelper();

    public int widthDevice;
    public int heightDevice;
    public int widthRes;
    public int heightRes;
    public static String platform;

    private Pattern getPattern(String locator) {
        /**
         * Parse locator string. It can be either of the following:
         * - Image.png
         * - Text
         * - Image.png = 0.9
         * This will return pattern and similarity by parsing above.
         */
        Pattern pattern = null;
        if (locator.contains(".png")) {
            if (locator.contains("=")) {
                locator = locator.replace(" ", "");
                pattern = new Pattern(locator.substring(0, locator.indexOf("="))).similar(Float.parseFloat(locator.substring(locator.indexOf("=") + 1)));
            } else {
                pattern = new Pattern(locator).similar((float) Settings.MinSimilarity);
            }
        } else {
            pattern = new Pattern(locator);
        }

        return pattern;
    }

    private Match wait(String image, String timeout) throws TimeoutException {
        try {
            screen.setImage(getScreenshot());
            Match match = region.wait(getPattern(image), Double.parseDouble(timeout));
            capture(match);
            return match;
        } catch (FindFailed e) {
            capture(region);
            throw new TimeoutException("Timeout happened, could not find " + getPattern(image).toString(), e);
        }
    }

    private Match waitText(String text, String timeout) throws TimeoutException {
        try {
            screen.setImage(getScreenshot());
            Match match = region.waitText(text, Double.parseDouble(timeout));
            capture(match);
            return match;
        } catch (FindFailed e) {
            capture(region);
            throw new TimeoutException("Timeout happened, could not find " + text, e);
        }
    }

    private Match waitBest(String[] images, Integer timeout) throws Exception {
        boolean isMatched = false;
        Match match = null;
        for (int i = 0; i < timeout; i++) {
            try {
                screen.setImage(getScreenshot());
                match = region.waitBest(1, images);
                capture(match);
                isMatched = true;
                break;
            } catch (Exception e) {

            }
        }
        if (isMatched) {
            return match;
        } else {
            capture(region);
            throw new ScreenOperationException("Timeout happened, could not find any best");
        }

    }


    private String capture(Region region) {
        return capture(region, null);
    }

    private String capture(Region region, String imageName) {
        if (isCaptureMatchedImage) {
            ScreenImage image = screen.capture(region);
            return saveImage(image, imageName);
        }
        return null;
    }

    private int[] regionFromMatch(Match match) {
        int[] reg = new int[4];
        reg[0] = match.getX();
        reg[1] = match.getY();
        reg[2] = match.getW();
        reg[3] = match.getH();
        return reg;
    }

    private static String saveImage(ScreenImage image) {
        return saveImage(image, null);
    }

    private static String saveImage(ScreenImage image, String name) {
        String imagePath;
        if (name == null || name.equals("")) {
            imagePath = image.save(CaptureFolder.getInstance().getCaptureFolder());
        } else {
            imagePath = image.save(CaptureFolder.getInstance().getCaptureFolder(), name);
        }
        System.out.println("*DEBUG* Saved path: " + imagePath);
        File file = new File(imagePath);
        String fileName = file.getName();
        System.out.println("*HTML* <img src='" + CaptureFolder.getInstance().getSubFolder() + "/" + fileName + "'/>");
        return imagePath;
    }

    public BufferedImage getScreenshot() {
        File screenshotFile = driver.getScreenshotAs(OutputType.FILE);
        try {
            BufferedImage full = (ImageIO.read(screenshotFile));
            widthRes = full.getWidth();
            heightRes = full.getHeight();
            return full;
        } catch (IOException e) {
        }
        return null;
    }

    private String capture() {
        ScreenImage image = screen.capture(region);
        return saveImage(image);
    }

    private Location dpi(Location location) {
        double density = widthRes / widthDevice;
        return new Location((int) (location.getX() / density), (int) (location.getY() / density));
    }

    public static ImageAsScreen getScreen() {
        return screen;
    }

    @RobotKeyword("Set Mobile TimeOut"
            + "\nExamples:"
            + "\n| Set Mobile TimeOut | 10 |")
    @ArgumentNames({"timeout"})
    public String setMobileTimeout(String timeout) {
        double oldTimeout = this.timeOut;
        this.timeOut = Double.parseDouble(timeout);
        return Double.toString(oldTimeout);
    }

    @RobotKeyword("Set Appium Implicit Wait"
            + "\nExamples:"
            + "\n| Set Appium Implicit Wait | MILLISECONDS |")
    @ArgumentNames({"millisecond"})
    public void setAppiumImplicitWait(String millisecond) {
        driver.manage().timeouts().implicitlyWait(Long.parseLong(millisecond), TimeUnit.MILLISECONDS);
    }

    @RobotKeyword("Clear Text Before"
            + "\nExamples:"
            + "\n| Clear Text Before | true or false |")
    @ArgumentNames({"enable"})
    public void clearTextBefore(boolean enable) {
        this.clearText = enable;
    }

    @RobotKeyword("Set Mobile Step Wait"
            + "\nExamples:"
            + "\n| Set Mobile Step Wait | 10 |")
    @ArgumentNames({"time"})
    public String setMobileStepWait(String time) {
        Long oldStepWait = this.stepWait;
        this.stepWait = Long.parseLong(time);
        return Long.toString(oldStepWait);
    }

    @RobotKeyword("Open Mobile Application"
            + "\nExamples:"
            + "\n| Mobile Open Application | appiumUrl | capabilities")
    @ArgumentNames({"appiumUrl", "capabilities"})
    public void openMobileApplication(String appiumUrl, Map<String, String> capabilities) throws Exception {
        driver = helper.connect(appiumUrl, capabilities);

        widthDevice = driver.manage().window().getSize().getWidth();
        heightDevice = driver.manage().window().getSize().getHeight();
        getScreenshot();
        screen.setDimension(new Dimension(widthRes, heightRes));
        region = screen.newRegion(0, 0, widthRes, heightRes);
    }

    @RobotKeyword("Configure Appium TimeOuts"
            + "\nExamples:"
            + "\n| Configure Appium TimeOuts | time |")
    @ArgumentNames({"time"})
    public void configureAppiumTimeOuts(int time) throws Exception {
        driver.manage().timeouts().pageLoadTimeout(time, TimeUnit.SECONDS);
    }

    @RobotKeyword("Capture Mobile Screen"
            + "\nExamples:"
            + "\n| Mobile Capture Screen | demo.png |")
    @ArgumentNames({"imagePath"})
    public String captureMobileScreen(String imagePath) {
        screen.setImage(getScreenshot());
        ScreenImage image = AppiumKeywords.getScreen().capture();
        return saveImage(image, imagePath);
    }

    @RobotKeyword("Tap On Mobile Image"
            + "\n\nTap On Mobile an image with similarity and offset."
            + "\nExamples:"
            + "\n| Mobile Tap On Image | hello.png |")
    @ArgumentNames({"image"})
    public int[] tapOnMobileImage(String image) throws Exception {
        Match match = wait(image, Double.toString(this.timeOut));
        try {
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);
            return regionFromMatch(match);
        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap " + image + " failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Tap On Best Mobile Image"
            + "\n\nTap an best image with similarity and offset."
            + "\nExamples:"
            + "\n| Mobile Tap On Best Image | hello.png |")
    @ArgumentNames({"images"})
    public int[] tapOnBestMobileImage(String images) throws Exception {
        Match match = waitBest(images.split(","), 10);
        try {
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);
            return regionFromMatch(match);
        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Tap On Best Mobile Image And Wait Text"
            + "\n\nTap an best image with similarity and offset."
            + "\nExamples:"
            + "\n| Mobile Tap On Best Image | hello.png | text |")
    @ArgumentNames({"images", "text"})
    public int[] tapOnBestMobileImageAndWaitText(String images, String text) throws Exception {
        Match match = waitBest(images.split(","), 10);
        try {
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);

            Thread.sleep(2000);
            waitText(text, Double.toString(this.timeOut));

            return regionFromMatch(match);
        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Tap Word On Mobile"
            + "\n\nTap an Tap Word On Mobile  with similarity and offset."
            + "\nExamples:"
            + "\n| Tap Word On Mobile | Hello |  Text Color : 0 (default) 1 (light white) 2 (light gray)")
    @ArgumentNames({"text", "colorOption=0"})
    public int[] tapWordOnMobile(String text, Integer colorOption) throws Exception {
        if (colorOption == 2) {
            OCR.globalOptions().grayFont();
        } else if (colorOption == 1) {
            OCR.globalOptions().lightFont();
        } else {
            System.out.println("Default");
        }
        screen.setImage(getScreenshot());
        try {
            Match match = region.findWord(text);
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);

            if ( colorOption > 0){
                OCR.globalOptions().resetFontSetting();
            }

            return regionFromMatch(match);
        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap " + text + " failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Tap Word And Send Text On Mobile"
            + "\n\nTap an Tap Word And Send Text On Mobile  with similarity and offset."
            + "\nExamples:"
            + "\n| Tap Word And Send Text On Mobile | Hello | World | Text Color : 0 (default) 1 (light white) 2 (light gray)")
    @ArgumentNames({"textTap", "textInput", "colorOption=0"})
    public int[] tapWordAndSendTextOnMobile(String textTap, String textInput, Integer colorOption) throws Exception {
        if (colorOption == 2) {
            OCR.globalOptions().grayFont();
        } else if (colorOption == 1) {
            OCR.globalOptions().lightFont();
        } else {
            System.out.println("Default");
        }
        screen.setImage(getScreenshot());
        try {
            Match match = region.findWord(textTap);
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);
            driver.switchTo().activeElement().sendKeys(textInput);
            if (colorOption > 0) {
                OCR.globalOptions().resetFontSetting();
            }
            return regionFromMatch(match);
        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap " + textTap + " failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Tap Word And Clear On Mobile"
            + "\n\nTap an Tap Word And Clear Text On Mobile  with similarity and offset."
            + "\nExamples:"
            + "\n| Tap Word And Clear On Mobile | Hello | Text Color : 0 (default) 1 (light white) 2 (light gray)")
    @ArgumentNames({"textTap", "colorOption=0"})
    public int[] tapWordAndClearOnMobile(String textTap, Integer colorOption) throws Exception {
        if (colorOption == 2) {
            OCR.globalOptions().grayFont();
        } else if (colorOption == 1) {
            OCR.globalOptions().lightFont();
        } else {
            System.out.println("Default");
        }
        screen.setImage(getScreenshot());
        try {
            Match match = region.findWord(textTap);
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);
            if (platform.equalsIgnoreCase("Android")) {
                ((AndroidDriver) driver).longPressKey(new KeyEvent(AndroidKey.DEL));
            } else if (platform.equalsIgnoreCase("iOS")) {
                helper.iosLongDelete(driver, textTap.length());
            } else {
                System.out.println("Unsupported platform");
            }
            if (colorOption > 0) {
                OCR.globalOptions().resetFontSetting();
            }
            return regionFromMatch(match);
        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap " + textTap + " failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Tap Text On Mobile"
            + "\n\nTap an Tap Text On Mobile  with similarity and offset."
            + "\nExamples:"
            + "\n| Tap Text On Mobile | Hello World |")
    @ArgumentNames({"text"})
    public int[] tapTextOnMobile(String text) throws Exception {
        screen.setImage(getScreenshot());
        try {
            Match match = region.findText(text);
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);
            return regionFromMatch(match);
        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap " + text + " failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Tap Text And Send Text On Mobile"
            + "\n\nTap an Tap Text And Send Text On Mobile  with similarity and offset."
            + "\nExamples:"
            + "\n| Tap Text And Send Text On Mobile | Hello World | Text | Text Color : 0 (default) 1 (light white) 2 (light gray)")
    @ArgumentNames({"textTap", "textInput", "colorOption=0"})
    public int[] tapTextAndSendTextOnMobile(String textTap, String textInput, Integer colorOption) throws Exception {
        if (colorOption == 2) {
            OCR.globalOptions().grayFont();
        } else if (colorOption == 1) {
            OCR.globalOptions().lightFont();
        } else {
            System.out.println("Default");
        }
        screen.setImage(getScreenshot());
        try {
            Match match = region.findText(textTap);
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);
            driver.switchTo().activeElement().sendKeys(textInput);
            if (colorOption > 0) {
                OCR.globalOptions().resetFontSetting();
            }
            return regionFromMatch(match);
        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap " + textTap + " failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Tap Mobile Image And Wait Image"
            + "\n\nTap Image On Mobile And Wait image with similarity and offset."
            + "\nExamples:"
            + "\n| Mobile Tap Image And Wait Image | tap.png | wait.png")
    @ArgumentNames({"image", "check"})
    public void tapMobileImageAndWaitImage(String image, String check) throws Exception {
        Match match = wait(image, Double.toString(this.timeOut));
        try {
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);
            Thread.sleep(2000);
            wait(check, Double.toString(this.timeOut));

        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap " + image + " wait " + check + " failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Tap Mobile Image And Wait Best Image"
            + "\n\nTap Image On Mobile And Wait Best Image with similarity and offset."
            + "\nExamples:"
            + "\n| Mobile Tap Image And Wait Best Image | tap.png | wait.png")
    @ArgumentNames({"image", "checks"})
    public void tapMobileImageAndWaitBestImage(String image, String checks) throws Exception {
        Match match = wait(image, Double.toString(this.timeOut));
        try {
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);
            Thread.sleep(2000);

            waitBest(checks.split(","), 10);

        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap " + image + " wait best image failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Tap On Best Mobile Image And Wait Best Image"
            + "\n\nTap Best Image On Mobile And Wait Best Image with similarity and offset."
            + "\nExamples:"
            + "\n| Mobile Tap On Best Image And Wait Best Image | tap.png | wait.png")
    @ArgumentNames({"images", "checks"})
    public void tapOnBestMobileImageAndWaitBestImage(String images, String checks) throws Exception {
        Match match = waitBest(images.split(","), 10);
        try {
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);
            Thread.sleep(2000);

            waitBest(checks.split(","), 10);

        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap matched image and wait best image failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Tap Mobile Image And Wait Text"
            + "\n\nTap Image On Mobile And Wait Text with similarity and offset."
            + "\nExamples:"
            + "\n| Mobile Tap Image And Wait Text | tap.png | text")
    @ArgumentNames({"image", "check"})
    public void tapMobileImageAndWaitText(String image, String text) throws Exception {
        Match match = wait(image, Double.toString(this.timeOut));
        try {
            Location center = match.getCenter();
            //Ex : 335 - 410 / 67 - 85
            Location pos = dpi(center);
            int newX = pos.getX();
            int newY = pos.getY();
            helper.tapOnCoordinates(driver, newX, newY);
            Thread.sleep(2000);
            waitText(text, Double.toString(this.timeOut));

        } catch (Exception e) {
            capture();
            throw new ScreenOperationException("Tap " + image + " wait " + text + " failed" + e.getMessage(), e);
        }
    }

    @RobotKeyword("Clear Text On Mobile Element"
            + "\nExamples:"
            + "\n| Clear Text On Mobile Element | locator |")
    @ArgumentNames({"locator"})
    public void clearTextOnMobileElement(String locator) throws Exception {
        helper.appiumClearText(driver, locator);
    }

    @RobotKeyword("Input Text On Mobile Element"
            + "\nExamples:"
            + "\n| Mobile Input Text On Element | locator | text |")
    @ArgumentNames({"locator", "text"})
    public void inputTextOnMobileElement(String locator, String text) throws Exception {
        helper.appiumInputText(driver, locator, text);
    }

    @RobotKeyword("Click Mobile Element"
            + "\nExamples:"
            + "\n| Mobile Click Element | locator |")
    @ArgumentNames({"locator"})
    public void clickMobileElement(String locator) throws Exception {
        Thread.sleep(stepWait * 1000);
        helper.appiumClickElement(driver, locator);
    }

    @RobotKeyword("Click Text On Mobile"
            + "\nExamples:"
            + "\n| Click Text On Mobile | textClick | exactMatch | click 1 time")
    @ArgumentNames({"textClick", "exactMatch=True", "number=1"})
    public void clickTextOnMobile(String textClick, boolean exactMatch, Integer number) throws Exception {
        Thread.sleep(stepWait * 1000);
        for (int i = 0; i < number; i++) {
            helper.appiumClickText(driver, textClick, exactMatch);
        }
    }

    @RobotKeyword("Click Text And Wait Text On Mobile"
            + "\nExamples:"
            + "\n| Click Text And Wait Text On Mobile | textClick | textWait | timeOut | exactMatch | ")
    @ArgumentNames({"textClick", "textWait", "timeOut=10", "exactMatch=True"})
    public void clickTextAndWaitTextOnMobile(String textClick, String textWait, Integer timeOut, boolean exactMatch) throws Exception {
        helper.appiumClickText(driver, textClick, exactMatch);
        helper.waitUntilPageContainsText(driver, textWait, exactMatch, timeOut);
    }

    @RobotKeyword("Close Mobile Application")
    public void closeMobileApplication() throws Exception {
        helper.appiumCloseCurrentApp(driver);
    }

    @RobotKeyword("Go Back On Mobile Application")
    public void goBackOnMobileApplication() throws Exception {
        helper.appiumGoBack(driver);
    }

    @RobotKeyword("Hide Mobile Keyboard")
    public void hideMobileKeyboard() throws Exception {
        if (platform.equalsIgnoreCase("Android")) {
            helper.androidHideKeyboard((AndroidDriver) driver);
        } else if (platform.equalsIgnoreCase("iOS")) {
            helper.iosHideKeyboard((IOSDriver) driver);
        } else {
            System.out.println("Unsupported platform");
        }
    }

    @RobotKeyword("Swipe In Mobile"
            + "\nExamples:"
            + "\n| Swipe In Mobile | start x | start y | end x | end y |")
    @ArgumentNames({"startX", "startY", "endX", "endY"})
    public void swipeInMobile(int startX, int startY, int endX, int endY) throws Exception {
        helper.appiumSwipe(driver, startX, startY, endX, endY);
    }

    @RobotKeyword("Swipe Until Text Visible"
            + "\nExamples:"
            + "\n| Swipe Until Text Visible | text |")
    @ArgumentNames({"text"})
    public void swipeUntilTextVisible(String text) throws Exception {
        helper.swipeUntilTextIsVisible(driver, text);
    }

    @RobotKeyword("Type Text On Mobile"
            + "\nExamples:"
            + "\n| Type Text On Mobile | text |")
    @ArgumentNames({"text"})
    public void typeTextOnMobile(String text) throws Exception {
        if (platform.equalsIgnoreCase("Android")) {
            helper.androidTypeString((AndroidDriver) driver, text);
        } else if (platform.equalsIgnoreCase("iOS")) {
            helper.iosTypeString((IOSDriver) driver, text);
        } else {
            System.out.println("Unsupported platform");
        }
    }

    @RobotKeyword("Click And Type Text On Mobile"
            + "\nExamples:"
            + "\n| Click And Type Text On Mobile | text_click | text | hide keyboard")
    @ArgumentNames({"textClick", "textType", "exactMatch=True", "hide=False"})
    public void clickAndTypeTextOnMobile(String textClick, String textType, Boolean exactMatch, boolean hide) throws Exception {
        if (platform.equalsIgnoreCase("Android")) {
            boolean isKeyboardShown = false;
            helper.appiumClickText(driver, textClick, exactMatch);
            Thread.sleep(stepWait * 1000);
            helper.androidTypeString((AndroidDriver) driver, textType);
            if (hide) {
                helper.androidHideKeyboard((AndroidDriver) driver);
            }

        } else if (platform.equalsIgnoreCase("iOS")) {
            boolean isKeyboardShown = false;
            helper.appiumClickText(driver, textClick, exactMatch);
            Thread.sleep(stepWait * 1000);
            helper.iosTypeString(driver, textType);
            if (hide) {
                helper.iosHideKeyboard(driver);
            }

        } else {
            System.out.println("Unsupported platform");
        }

    }

    @RobotKeyword("Click And Send Text On Mobile"
            + "\nExamples:"
            + "\n| Click And Send Text On Mobile | textClick | textInput")
    @ArgumentNames({"textClick", "textInput"})
    public void clickAndSendTextOnMobile(String textClick, String textInput) throws Exception {
        Thread.sleep(stepWait * 1000);
        helper.appiumSendText(driver, textClick, textInput, true);
    }

    @RobotKeyword("Clear Text On Mobile"
            + "\nExamples:"
            + "\n| Clear Text On Mobile | text |")
    @ArgumentNames({"text", "exactMatch=true"})
    public void clearTextOnMobile(String text, boolean exactMatch) throws Exception {
        helper.appiumClearText(driver, text);
    }

    @RobotKeyword("Mobile Element Should Contain Text"
            + "\nExamples:"
            + "\n| Mobile Element Should Contain Text | locator | text |")
    @ArgumentNames({"locator", "text"})
    public void mobileElementShouldContainText(String locator, String text) throws Exception {
        boolean result = helper.elementShouldContainText(driver, locator, text);
        if (result != true) {
            throw new Exception("Failed to check mobile element should contain text.");
        }
    }

    @RobotKeyword("Mobile Element Should Not Contain Text"
            + "\nExamples:"
            + "\n| Mobile Element Should Not Contain Text | locator | text |")
    @ArgumentNames({"locator", "text"})
    public void mobileElementShouldNotContainText(String locator, String text) throws Exception {
        boolean result = helper.elementShouldNotContainText(driver, locator, text);
        if (result != true) {
            throw new Exception("Failed to check mobile element should not contain text.");
        }
    }

    @RobotKeyword("Mobile Element Should Be"
            + "\nExamples:"
            + "\n| Mobile Element Should Be | locator | text |")
    @ArgumentNames({"locator", "text"})
    public void mobileElementShouldBe(String locator, String text) throws Exception {
        boolean result = helper.elementTextShouldBe(driver, locator, text);
        if (result != true) {
            throw new Exception("Failed to check mobile element should be.");
        }
    }

    @RobotKeyword("Mobile Page Should Contain Element"
            + "\nExamples:"
            + "\n| Mobile Page Should Contain Element | locator |")
    @ArgumentNames({"locator"})
    public void mobilePageShouldContainElement(String locator) throws Exception {
        boolean result = helper.pageShouldContainElement(driver, locator);
        if (result != true) {
            throw new Exception("Failed to check mobile page should contain element.");
        }
    }

    @RobotKeyword("Mobile Page Should Contain Text"
            + "\nExamples:"
            + "\n| Mobile Page Should Contain Element | text |")
    @ArgumentNames({"text"})
    public void mobilePageShouldContainText(String text) throws Exception {
        boolean result = helper.pageShouldContainText(driver, text);
        if (result != true) {
            throw new Exception("Failed to check mobile page should contain text.");
        }
    }

    @RobotKeyword("Mobile Page Should Not Contain Text"
            + "\nExamples:"
            + "\n| Mobile Page Should Not Contain Text | text |")
    @ArgumentNames({"text"})
    public void mobilePageShouldNotContainText(String text) throws Exception {
        boolean result = helper.pageShouldNotContainText(driver, text);
        if (result != true) {
            throw new Exception("Failed to check mobile page should not contain text.");
        }
    }

    @RobotKeyword("Wait Mobile Page Contain Text"
            + "\nExamples:"
            + "\n| Wait Mobile Page Contain Text | text | exactMatch=true | timeOut: default 10s")
    @ArgumentNames({"text", "exactMatch=true", "timeOut=10"})
    public void waitMobilePageContainText(String text, boolean exactMatch,Integer timeOut) throws Exception {
        helper.waitUntilPageContainsText(driver, text, exactMatch, timeOut);
    }

    @RobotKeyword("Wait Mobile Page Not Contain Text"
            + "\nExamples:"
            + "\n| Wait Mobile Page Not Contain Text | text | timeOut: default 10s")
    @ArgumentNames({"text", "timeOut=10"})
    public void waitMobilePageNotContainText(String text, Integer timeOut) {
        helper.waitUntilPageDoesNotContain(driver, text, timeOut);
    }

    @RobotKeyword("Wait Until Mobile Screen Contain"
            + "\nExamples:"
            + "\n| Wait Until Mobile Screen Contain | image | timeOut: default 10s")
    @ArgumentNames({"image", "timeOut"})
    public void waitUntilMobileScreenContain(String image, String timeOut) throws TimeoutException {
        wait(image, timeOut);
    }

    @RobotKeyword("Wait Until Mobile Screen Contain Any"
            + "\nExamples:"
            + "\n| Wait Until Mobile Screen Contain Any | images | timeOut: default 10s")
    @ArgumentNames({"images", "timeOut=10"})
    public void waitUntilMobileScreenContainAny(String images, Integer timeOut) throws Exception {
        waitBest(images.split(","), timeOut);
    }

    @RobotKeyword("Is Text Exist On Mobile Screen"
            + "\nExamples:"
            + "\n| Is Text Exist On Mobile Screen | text |")
    @ArgumentNames({"text"})
    public boolean isTextExistOnMobileScreen(String text) throws Exception {
        return helper.pageShouldContainText(driver, text);
    }

    @RobotKeyword("Pick Date"
            + "\nExamples:"
            + "\n| Pick Date | 10 | November | 2000 |")
    @ArgumentNames({"date", "month", "year"})
    public void pickDate(String date, String month, String year) throws Exception {
        helper.datePicker(driver, date, month, year);
    }

    @RobotKeyword("Get Text From Mobile Element"
            + "\nExamples:"
            + "\n| Get Text From Mobile Element | locator |")
    @ArgumentNames({"locator"})
    public String getTextFromMobileElement(String locator) {
        return helper.getTextFromMobileElement(driver, locator);
    }

    @RobotKeyword("Quit Mobile Session")
    public void quitMobileSession() {
        driver.quit();
    }

    @RobotKeyword("Get Current Mobile Session")
    public String getMobileSession() {
        return driver.getSessionId().toString();
    }

}
