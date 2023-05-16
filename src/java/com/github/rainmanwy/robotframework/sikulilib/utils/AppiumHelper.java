package com.github.rainmanwy.robotframework.sikulilib.utils;

import com.github.rainmanwy.robotframework.sikulilib.keywords.AppiumKeywords;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.KeyEventMetaModifier;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.ScreenshotException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;

public class AppiumHelper {

    private int timeOut = 10;

    public AppiumDriver connect(String appiumHubUrl, Map<String, String> capabilities) throws MalformedURLException {
        URL hubUrl = new URL(appiumHubUrl);
        DesiredCapabilities caps = new DesiredCapabilities();

        for (Map.Entry<String, String> entry : capabilities.entrySet()) {
            caps.setCapability(entry.getKey(), entry.getValue());
        }
        caps.setCapability("newCommandTimeout", 60000);
        if (caps.getCapability("platformName").toString().equalsIgnoreCase("Android")) {
            AppiumKeywords.platform = "Android";
            return new AndroidDriver(hubUrl, caps);
        } else if (caps.getCapability("platformName").toString().equalsIgnoreCase("iOS")) {
            AppiumKeywords.platform = "iOS";
            return new IOSDriver(hubUrl, caps);
        } else {
            throw new IllegalArgumentException("Unsupported platform: ");
        }
    }

    public WebElement findElements(AppiumDriver appiumDriver, String locator) throws ScreenshotException {
        if (!locator.contains("=")) {
            locator = "text=" + locator;
        }
        String[] parts = locator.split("=", 2);
        String prefix = parts[0];
        String value = parts[1];

        switch (prefix.toLowerCase()) {
            case "id":
                return appiumDriver.findElement(AppiumBy.id(value));
            case "name":
                return appiumDriver.findElement(AppiumBy.name(value));
            case "classname":
            case "class":
                return appiumDriver.findElement(AppiumBy.className(value));
            case "accessibilityid":
                return appiumDriver.findElement(AppiumBy.accessibilityId(value));
            case "xpath":
                return appiumDriver.findElement(AppiumBy.xpath(value));
            case "cssselector":
            case "css":
                return appiumDriver.findElement(AppiumBy.cssSelector(value));
            case "linktext":
                return appiumDriver.findElement(AppiumBy.linkText(value));
            case "partiallinktext":
                return appiumDriver.findElement(AppiumBy.partialLinkText(value));
            case "tagname":
            case "tag":
                return appiumDriver.findElement(AppiumBy.tagName(value));
            case "ioslabel":
                return appiumDriver.findElement(AppiumBy.iOSNsPredicateString("label == '" + value + "'"));
            case "iospredict":
                return appiumDriver.findElement(AppiumBy.iOSNsPredicateString(value));
            default:
                return appiumDriver.findElement(AppiumBy.xpath("//*[contains(@value,'" + value + "') or contains(@name,'" + value + "') or contains(@text,'" + value + "')]"));
        }
    }

    private String locatorWithSensitiveCase(String text, boolean exactMatch) {
        String locator = "";
        if (AppiumKeywords.platform.equalsIgnoreCase("iOS")) {
            if (exactMatch) {
                locator = "label == '" + text + "' || value == '" + text + "'";
            } else {
                locator = "label CONTAINS '" + text + "' || value CONTAINS '" + text + "'";
            }
            return locator;
        } else if (AppiumKeywords.platform.equalsIgnoreCase("Android")) {
            if (exactMatch) {
                locator = "//*[@text='" + text + "']";
            } else {
                locator = "//*[contains(@text,'" + text + "')]";
            }
            return locator;
        } else {
            System.out.println("Unsupported platform");
            return null;
        }
    }

    private String locator(String text, boolean exactMatch) {
        String locator = "";
        if (AppiumKeywords.platform.equalsIgnoreCase("iOS")) {
            if (exactMatch) {
                locator = "//*[@label='" + text + "' or @value='" + text + "']";
            } else {
                locator = "//*[contains(@label,'" + text + "') or contains(@value,'" + text + "')]";
            }
            return locator;
        } else if (AppiumKeywords.platform.equalsIgnoreCase("Android")) {
            if (exactMatch) {
                locator = "//*[@text='" + text + "']";
            } else {
                locator = "//*[contains(@text,'" + text + "')]";
            }
            return locator;
        } else {
            System.out.println("Unsupported platform");
            return null;
        }
    }

    public void appiumInputText(AppiumDriver appiumDriver, String locator, String text) {
        findElements(appiumDriver, locator).sendKeys(text);
    }

    public void appiumClearText1(AppiumDriver appiumDriver, String text, boolean exactMatch) {
        String locator = locator(text, exactMatch);
        WebDriverWait wait = new WebDriverWait(appiumDriver, Duration.ofSeconds(10));
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath(locator)));
        el.clear();
    }

    public void appiumClearText(AppiumDriver appiumDriver, String locator) {
        findElements(appiumDriver, locator).clear();
    }

    public void appiumClickElement(AppiumDriver appiumDriver, String locator) {
        findElements(appiumDriver, locator).click();
    }

    public void appiumClickText(AppiumDriver appiumDriver, String textClick, boolean exactMatch) {
        try {
            String locator = locator(textClick, exactMatch);
            appiumDriver.findElement(AppiumBy.xpath(locator)).click();
        } catch (Exception e) {
            throw new ScreenshotException("Cannot find element has " + textClick);
        }
    }

    public void appiumSendText(AppiumDriver appiumDriver, String textClick, String textInput, boolean exactMatch) throws InterruptedException {
        if (AppiumKeywords.platform.equalsIgnoreCase("iOS")) {
            WebElement el = appiumDriver.findElement(AppiumBy.xpath(locator(textClick, exactMatch)));
            el.click();
            el.clear();
            Thread.sleep(1000);
            el.sendKeys(textInput);
            iosHideKeyboard(appiumDriver);
        } else if (AppiumKeywords.platform.equalsIgnoreCase("Android")) {
            WebElement el = appiumDriver.findElement(AppiumBy.xpath(locator(textClick, exactMatch)));
            el.click();
            el.clear();
            Thread.sleep(1000);
            el.sendKeys(textInput);
            androidHideKeyboard((AndroidDriver) appiumDriver);
        } else {
            System.out.println("Unsupported platform");
        }
    }

    public void appiumCloseCurrentApp(AppiumDriver appiumDriver) {
        appiumDriver.close();
    }

    public void appiumGoBack(AppiumDriver appiumDriver) {
        appiumDriver.navigate().back();
    }

    public void androidHideKeyboard(AndroidDriver androidDriver) {
        androidDriver.hideKeyboard();
    }

    public void iosHideKeyboard(AppiumDriver iOSDriver) {
        findElements(iOSDriver, "xpath=(//XCUIElementTypeOther[@name='Done'])[5]").click();
    }

    public void appiumSwipe(AppiumDriver appiumDriver, int startX, int startY, int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));

        swipe.addAction(finger.createPointerMove(Duration.ofMillis(700), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        appiumDriver.perform(Arrays.asList(swipe));
    }

    public void swipeUntilTextIsVisible(AppiumDriver appiumDriver, String text) throws Exception {
        boolean isTextVisible = false;
        int count = 10;
        String locator = locator(text, false);
        while (!isTextVisible) {
            try {
                WebElement element = appiumDriver.findElement(AppiumBy.xpath(locator));
                if (element != null && element.isDisplayed()) {
                    isTextVisible = true;
                }
            } catch (Exception e) {
                // Element not found, continue swiping
            }

            Dimension size = appiumDriver.manage().window().getSize();
            int startX = (int) (size.width * 0.2);
            int startY = (int) (size.height * 0.3);
            int endX = startX;
            int endY = (int) (size.height * 0.2);

            appiumSwipe(appiumDriver, startX, startY, endX, endY);
            Thread.sleep(500);
            count--;
            if (count == 0) {
                break;
            }
        }
        if (!isTextVisible && count == 0) {
            throw new ScreenshotException("Failed to find " + text + " when swipe");
        }
    }

    public void androidTypeString(AndroidDriver androidDriver, String text) {
        Map<Character, AndroidKey> keyMap = new HashMap<>();
        // Define the mapping of characters to AndroidKey values
        keyMap.put('a', AndroidKey.A);
        keyMap.put('b', AndroidKey.B);
        keyMap.put('c', AndroidKey.C);
        keyMap.put('d', AndroidKey.D);
        keyMap.put('e', AndroidKey.E);
        keyMap.put('f', AndroidKey.F);
        keyMap.put('g', AndroidKey.G);
        keyMap.put('h', AndroidKey.H);
        keyMap.put('i', AndroidKey.I);
        keyMap.put('j', AndroidKey.J);
        keyMap.put('k', AndroidKey.K);
        keyMap.put('l', AndroidKey.L);
        keyMap.put('m', AndroidKey.M);
        keyMap.put('n', AndroidKey.N);
        keyMap.put('o', AndroidKey.O);
        keyMap.put('p', AndroidKey.P);
        keyMap.put('q', AndroidKey.Q);
        keyMap.put('r', AndroidKey.R);
        keyMap.put('s', AndroidKey.S);
        keyMap.put('t', AndroidKey.T);
        keyMap.put('u', AndroidKey.U);
        keyMap.put('v', AndroidKey.V);
        keyMap.put('w', AndroidKey.W);
        keyMap.put('x', AndroidKey.X);
        keyMap.put('y', AndroidKey.Y);
        keyMap.put('z', AndroidKey.Z);
        keyMap.put('A', AndroidKey.A);
        keyMap.put('B', AndroidKey.B);
        keyMap.put('C', AndroidKey.C);
        keyMap.put('D', AndroidKey.D);
        keyMap.put('E', AndroidKey.E);
        keyMap.put('F', AndroidKey.F);
        keyMap.put('G', AndroidKey.G);
        keyMap.put('H', AndroidKey.H);
        keyMap.put('I', AndroidKey.I);
        keyMap.put('J', AndroidKey.J);
        keyMap.put('K', AndroidKey.K);
        keyMap.put('L', AndroidKey.L);
        keyMap.put('M', AndroidKey.M);
        keyMap.put('N', AndroidKey.N);
        keyMap.put('O', AndroidKey.O);
        keyMap.put('P', AndroidKey.P);
        keyMap.put('Q', AndroidKey.Q);
        keyMap.put('R', AndroidKey.R);
        keyMap.put('S', AndroidKey.S);
        keyMap.put('T', AndroidKey.T);
        keyMap.put('U', AndroidKey.U);
        keyMap.put('V', AndroidKey.V);
        keyMap.put('W', AndroidKey.W);
        keyMap.put('X', AndroidKey.X);
        keyMap.put('Y', AndroidKey.Y);
        keyMap.put('Z', AndroidKey.Z);
        keyMap.put(',', AndroidKey.COMMA);
        keyMap.put('.', AndroidKey.PERIOD);
        keyMap.put(' ', AndroidKey.SPACE);
        keyMap.put('-', AndroidKey.MINUS);
        keyMap.put('=', AndroidKey.EQUALS);
        keyMap.put('[', AndroidKey.LEFT_BRACKET);
        keyMap.put(']', AndroidKey.RIGHT_BRACKET);
        keyMap.put('\\', AndroidKey.BACKSLASH);
        keyMap.put(';', AndroidKey.SEMICOLON);
        keyMap.put('\'', AndroidKey.APOSTROPHE);
        keyMap.put('`', AndroidKey.GRAVE);
        keyMap.put('/', AndroidKey.SLASH);
        keyMap.put('@', AndroidKey.AT);
        keyMap.put('+', AndroidKey.PLUS);
        keyMap.put('#', AndroidKey.POUND);
        keyMap.put('*', AndroidKey.STAR);
        keyMap.put('!', AndroidKey.DIGIT_1);
        keyMap.put('$', AndroidKey.DIGIT_4);
        keyMap.put('%', AndroidKey.DIGIT_5);
        keyMap.put('^', AndroidKey.DIGIT_6);
        keyMap.put('&', AndroidKey.DIGIT_7);
        keyMap.put('(', AndroidKey.DIGIT_9);
        keyMap.put(')', AndroidKey.DIGIT_0);
        keyMap.put('0', AndroidKey.DIGIT_0);
        keyMap.put('1', AndroidKey.DIGIT_1);
        keyMap.put('2', AndroidKey.DIGIT_2);
        keyMap.put('3', AndroidKey.DIGIT_3);
        keyMap.put('4', AndroidKey.DIGIT_4);
        keyMap.put('5', AndroidKey.DIGIT_5);
        keyMap.put('6', AndroidKey.DIGIT_6);
        keyMap.put('7', AndroidKey.DIGIT_7);
        keyMap.put('8', AndroidKey.DIGIT_8);
        keyMap.put('9', AndroidKey.DIGIT_9);

        List<String> shiftMappings = new ArrayList<String>(Arrays.asList("!", "$", "%", "^", "&", "(", ")", "~", "<", ">", "_", "+", "{", "}", "|", "\"", "?", ":"));

        // Convert the input string to an array of characters
        char[] chars = text.toCharArray();

        // Send KeyEvents for each character in the input string
        for (char c : chars) {
            if (!shiftMappings.contains(Character.toString(c))) {
                if (Character.isUpperCase(c)) {
                    AndroidKey key = keyMap.get(c);
                    androidDriver.pressKey(new KeyEvent(key).withMetaModifier(KeyEventMetaModifier.SHIFT_ON));
                } else {
                    AndroidKey key = keyMap.get(c);
                    if (key != null) {
                        androidDriver.pressKey(new KeyEvent(key));
                    } else {
                        // Handle unknown characters
                        System.out.println("Unsupported character: " + c);
                    }
                }
            } else {
                AndroidKey key = keyMap.get(c);
                androidDriver.pressKey(new KeyEvent(key).withMetaModifier(KeyEventMetaModifier.SHIFT_ON));
            }
        }
    }

    public void iosTypeString(AppiumDriver iosDriver, String text) {
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                // Check if the current keyboard is not the alphabet keyboard
                Boolean isVisible = pageShouldContainElement(iosDriver, "ioslabel=letters");
                if (isVisible) {
                    // Switch to the alphabet keyboard
                    findElements(iosDriver, "ioslabel=letters").click();
                }
                if (!pageShouldContainElement(iosDriver, "accessibilityid=" + c)) {
                    findElements(iosDriver, "ioslabel=shift").click();
                }
                iosDriver.findElement(AppiumBy.accessibilityId(Character.toString(c))).click();

            } else if (Character.isDigit(c)) {
                // Check if the current keyboard is not the number keyboard
                Boolean isVisible = pageShouldContainElement(iosDriver, "ioslabel=numbers");
                if (isVisible) {
                    // Switch to the number keyboard
                    findElements(iosDriver, "ioslabel=numbers").click();
                }
                iosDriver.findElement(AppiumBy.accessibilityId(Character.toString(c))).click();

            } else if (Character.isWhitespace(c)) {
                // Press the space key
                iosDriver.findElement(AppiumBy.accessibilityId("space")).click();
            } else {
                if (!pageShouldContainElement(iosDriver, "accessibilityid=" + c)) {
                    Boolean isVisible = pageShouldContainElement(iosDriver, "ioslabel=symbols");
                    if (!isVisible) {
                        // Switch to the special keyboard
                        findElements(iosDriver, "ioslabel=numbers").click();
                        findElements(iosDriver, "ioslabel=symbols").click();
                    } else {
                        findElements(iosDriver, "ioslabel=symbols").click();
                    }
                }
                iosDriver.findElement(AppiumBy.accessibilityId(Character.toString(c))).click();

            }
        }
    }

    public void iosLongDelete(AppiumDriver appiumDriver, int len) {
        for (int i = 0; i < len + 2; i++) {
            appiumDriver.findElement(AppiumBy.accessibilityId("delete")).click();
        }
    }

    public boolean elementShouldContainText(AppiumDriver appiumDriver, String locator, String text) {
        return findElements(appiumDriver, locator).getText().contains(text);
    }

    public boolean elementShouldNotContainText(AppiumDriver appiumDriver, String locator, String text) {
        return !findElements(appiumDriver, locator).getText().contains(text);
    }

    public boolean elementTextShouldBe(AppiumDriver appiumDriver, String locator, String text) {
        return findElements(appiumDriver, locator).getText().equalsIgnoreCase(text);
    }

    public boolean pageShouldContainElement(AppiumDriver appiumDriver, String locator) {
        try {
            findElements(appiumDriver, locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean pageShouldContainText(AppiumDriver appiumDriver, String text) {
        try {
            String pageSource = appiumDriver.getPageSource();
            return pageSource.contains(text);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean pageShouldNotContainText(AppiumDriver appiumDriver, String text) {
        String pageSource = appiumDriver.getPageSource();
        return !pageSource.contains(text);
    }

    public void waitUntilPageContainsText(AppiumDriver appiumDriver, String text, Boolean exactMatch, Integer time) {
        try {
            String xpath = locator(text, exactMatch);
            WebDriverWait wait = new WebDriverWait(appiumDriver, Duration.ofSeconds(time));
            wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath(xpath)));
        } catch (Exception e) {
            throw new ScreenshotException("Timed out waiting for page to contain text: " + text);
        }
    }

    public void waitElementLoaded(AppiumDriver appiumDriver, String text, Boolean exactMatch, Integer time) throws Exception {
        boolean isTextVisible = false;
        int count = 0;
        String xpath = locator(text, exactMatch);
        while (!isTextVisible) {
            count++;
            try {
                WebElement element = appiumDriver.findElement(AppiumBy.xpath(xpath));
                if (element != null && element.isDisplayed()) {
                    isTextVisible = true;
                }
            } catch (Exception e) {
                // Element not found, continue swiping
            }
            if (count > time) {
                break;
            }
            Thread.sleep(1000);
        }
        if (!isTextVisible) {
            throw new ScreenshotException("Failed to find " + text + " when swipe");
        }
    }


    public void waitUntilPageDoesNotContain(AppiumDriver appiumDriver, String text, Integer time) {
        try {
            String xpath = locator(text, false);
            WebDriverWait wait = new WebDriverWait(appiumDriver, Duration.ofSeconds(time));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(AppiumBy.xpath(xpath)));
        } catch (Exception e) {
            throw new ScreenshotException("Timed out waiting for page to contain text: " + text);
        }
    }

    public void tapOnCoordinates(AppiumDriver appiumDriver, int x, int y) {
        PointerInput finger = new PointerInput(org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence clickPosition = new org.openqa.selenium.interactions.Sequence(finger, 1);
        clickPosition.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y)).addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg())).addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        appiumDriver.perform(Arrays.asList(clickPosition));
    }

    public void tapElement(AppiumDriver appiumDriver, WebElement element) {
        PointerInput finger = new PointerInput(org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence clickPosition = new org.openqa.selenium.interactions.Sequence(finger, 1);
        clickPosition.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), element.getLocation().x, element.getLocation().y)).addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg())).addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        appiumDriver.perform(Arrays.asList(clickPosition));
    }

    public void datePicker(AppiumDriver appiumDriver, String date, String month, String year) throws InterruptedException {
        if (AppiumKeywords.platform.equalsIgnoreCase("iOS")) {

            WebElement yearEl = appiumDriver.findElement(AppiumBy.xpath("//XCUIElementTypeOther[@name='Select date']/XCUIElementTypeDatePicker/XCUIElementTypePicker/XCUIElementTypePickerWheel[3]"));
            yearEl.clear();
            yearEl.sendKeys(year);

            WebElement monEl = appiumDriver.findElement(AppiumBy.xpath("//XCUIElementTypeOther[@name='Select date']/XCUIElementTypeDatePicker/XCUIElementTypePicker/XCUIElementTypePickerWheel[1]"));
            monEl.clear();
            monEl.sendKeys(month);

            WebElement dateEl = appiumDriver.findElement(AppiumBy.xpath("//XCUIElementTypeOther[@name='Select date']/XCUIElementTypeDatePicker/XCUIElementTypePicker/XCUIElementTypePickerWheel[2]"));
            dateEl.clear();
            dateEl.sendKeys(date);

        } else if (AppiumKeywords.platform.equalsIgnoreCase("Android")) {

            for (int i = 0; i < 100; i++) {
                if (!findElements(appiumDriver, "xpath=//android.widget.NumberPicker[3]//android.widget.EditText").getText().equalsIgnoreCase(year)) {
                    WebElement preYear = appiumDriver.findElement(AppiumBy.xpath("//android.widget.NumberPicker[3]/android.widget.Button"));
                    tapElement(appiumDriver, preYear);
                } else {
                    break;
                }
                Thread.sleep(1000);
            }

            for (int i = 0; i < 31; i++) {
                if (!findElements(appiumDriver, "xpath=//android.widget.NumberPicker[2]/android.widget.EditText").getText().equalsIgnoreCase(date)) {
                    WebElement nextDate = appiumDriver.findElement(AppiumBy.xpath("//android.widget.NumberPicker[2]/android.widget.Button[1]"));
                    tapElement(appiumDriver, nextDate);
                } else {
                    break;
                }
                Thread.sleep(1000);
            }

            for (int i = 0; i < 12; i++) {
                if (!findElements(appiumDriver, "xpath=//android.widget.NumberPicker[1]/android.widget.EditText").getText().equalsIgnoreCase(month)) {
                    WebElement nextMonth = appiumDriver.findElement(AppiumBy.xpath("//android.widget.NumberPicker[1]/android.widget.Button[1]"));
                    tapElement(appiumDriver, nextMonth);
                } else {
                    break;
                }
                Thread.sleep(1000);
            }


        } else {
            System.out.println("Unsupported platform");
        }
    }

    public String getTextFromMobileElement(AppiumDriver appiumDriver, String locator){
        String text = findElements(appiumDriver, locator).getText();
        return text;
    }

}
