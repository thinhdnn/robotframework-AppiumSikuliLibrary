package com.github.rainmanwy.robotframework.sikulilib.keywords;

import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;
import org.sikuli.basics.Settings;
import org.sikuli.script.OCR;


/**
 * Created by Wang Yang on 2016/5/5.
 * 20201018 addings:
 *   - Set OCR language keyword
 *	 - Set OCD Read documentation clarified
 */

@RobotKeywords
public class SettingsKeywords {

    @RobotKeyword("Set min similarity (accuracy of matching elements)."
            + "\n\nminSimilarity can be a decimal number between 0 and 1"
            + "\n\nExample:"
            + "\n\n| Set Min Similarity | 0.85 |")
    @ArgumentNames({"minSimilarity"})
    public String setMinSimilarity(String minSimilarity) {
        double prevMinSimilarity = Settings.MinSimilarity;
        Settings.MinSimilarity = Double.parseDouble(minSimilarity);
        return Double.toString(prevMinSimilarity);
    }

    @RobotKeyword("OCR text read"
				+ "\nIf needed use Set OCR Language before."
				+ "`\nDefault language : English (eng).")
    @ArgumentNames({"ocrTextRead"})
    public void setOcrTextRead(boolean ocrTextRead) {
        Settings.OcrTextRead = ocrTextRead;
    }
	
	@RobotKeyword("Set OCR language"
				+ "\n\nSet OCR language"
                + "\nThree letters parameter"
				+ "\nDefault : eng for English language"
				+ "\nExamples:"
                + "\n| Set OCR Language | eng |"
				+ "\n| Set OCR Language | fra |")
    @ArgumentNames({"ocrTextLanguage"})
    public void setOcrLanguage(String ocrTextLanguage) {
        Settings.OcrLanguage = ocrTextLanguage;
    }

    @RobotKeyword("Set show actions")
    @ArgumentNames({"showActions"})
    public void setShowActions(boolean showActions) {
        Settings.setShowActions(showActions);
    }

    @RobotKeyword("Set move mouse delay")
    @ArgumentNames({"delay"})
    public void setMoveMouseDelay(float delay) {
        Settings.MoveMouseDelay = delay;
    }

    @RobotKeyword("Set slow motion delay"
            + "\n Control the duration of the visual effect (seconds).")
    @ArgumentNames({"delay"})
    public void setSlowMotionDelay(float delay) {
        Settings.SlowMotionDelay = delay;
    }

    @RobotKeyword("Set wait scan rate"
            + "\n Specify the number of times actual search operations are performed per second while waiting for a pattern to appear or vanish.")
    @ArgumentNames({"delay"})
    public void setWaitScanRate(float scanRate) {
        Settings.WaitScanRate = scanRate;
    }

    @RobotKeyword("Set Always Resize"
            + "\nA decimal value greater 0 and not equal to 1 to switch the feature on."
            + "\nWith this setting you can tell SikuliX to generally resize all given images before a search operation using the given factor, which is applied to both width and height. The implementation internally uses the standard behavior of resizing a Java-AWT-BufferedImage."
            + "\n\nTo switch the feature off again, just assign 0 or 1."
    )
    @ArgumentNames({"resize"})
    public void setAlwaysResize(float resize) {
        Settings.AlwaysResize = resize;
    }

    @RobotKeyword("Set Light Font"
            + "\nTurn on OCR Light Font Option"
    )
    public void setLightFont() {
        OCR.globalOptions().lightFont();
    }

    @RobotKeyword("Set Gray Font"
            + "\nTurn on OCR Gray Font Option"
    )
    public void setGrayFont() {
        OCR.globalOptions().grayFont();
    }

    @RobotKeyword("Set Small Font"
            + "\nSet Small Font Option"
    )
    public void setSmallFont() {
        OCR.globalOptions().smallFont();
    }

    @RobotKeyword("Reset OCR Setting"
            + "\nSet Reset Font Setting"
    )
    public void resetOCRSetting() {
        OCR.globalOptions().reset();
    }

    @RobotKeyword("Reset Font Setting"
            + "\nSet Reset Font Setting"
    )
    public void resetFontSetting() {
        OCR.globalOptions().resetFontSetting();
    }
}
