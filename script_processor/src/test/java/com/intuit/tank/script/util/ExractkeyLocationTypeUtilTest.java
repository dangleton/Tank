package com.intuit.tank.script.util;

import static org.junit.Assert.*;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import com.intuit.tank.test.TestGroups;

public class ExractkeyLocationTypeUtilTest {

    private String EmptyStringKeyAndLocationType = ""; // Test Case 1 Empty string for Key and Location Type
    private String NullForKeyAndLocaitonType = null; // Test Case 2 null for Key and Location Type
    private String KeyAndLocationType = "Cookie:Key1"; // Test Case 3 both Key and Location Type defined
    private String NoLocationType = ":Key2"; // Test Case 4 no Location Type with Key defined
    private String NoKey = "Header:"; // Test Case 5 no key with Location Type defined
    private String ColonAsPartOfKey = "Body::Key3"; // Test Case 6 colon as part of key with Location Type defined
    private String ColonAsKey = "Cookie::"; // Test Case 7 colon as key with Location Type defined
    private String KeyWithColonNoLocationType = "key4:NoLocType"; // Test Case 8 Key contains colon as part of the key
                                                                  // with no Location Type defined

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void emptyStringForKeyTestCase1() {
        assertTrue(StringUtils.isEmpty(ExractkeyLocationTypeUtil.getKey(EmptyStringKeyAndLocationType)));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void emptyStringForLocationTypeTestCase1() {
        assertTrue(ExractkeyLocationTypeUtil.getLocationType(EmptyStringKeyAndLocationType).equals("Body"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void nullForKeyTestCase2() {
        assertTrue(StringUtils.isBlank(ExractkeyLocationTypeUtil.getKey(NullForKeyAndLocaitonType)));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void nullForLocationTypeTestCase2() {
        assertTrue(ExractkeyLocationTypeUtil.getLocationType(NullForKeyAndLocaitonType).equals("Body"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void keyDefinedTestCase3() {
        assertTrue(ExractkeyLocationTypeUtil.getKey(KeyAndLocationType).equals("Key1"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void locationTypeDefinedAsCookieTestCase3() {
        assertTrue(ExractkeyLocationTypeUtil.getLocationType(KeyAndLocationType).equals("Cookie"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void keyDefinedTestCase4() {
        assertTrue(ExractkeyLocationTypeUtil.getKey(NoLocationType).equals("Key2"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void noLocationTypeDefinedTestCase4() {
        assertTrue(ExractkeyLocationTypeUtil.getLocationType(NoLocationType).equals("Body"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void noKeyDefinedTestCase5() {
        assertTrue(StringUtils.isEmpty(ExractkeyLocationTypeUtil.getKey(NoKey)));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void locationTypeDefinedAsHeaderTestCase5() {
        assertTrue(ExractkeyLocationTypeUtil.getLocationType(NoKey).equals("Header"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void colonAsPartOfKeyTestCase6() {
        assertTrue(ExractkeyLocationTypeUtil.getKey(ColonAsPartOfKey).equals(":Key3"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void locationTypeDefinedAsBodyTestCase6() {
        assertTrue(ExractkeyLocationTypeUtil.getLocationType(ColonAsPartOfKey).equals("Body"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void colonAsKeyTestCase7() {
        assertTrue(ExractkeyLocationTypeUtil.getKey(ColonAsKey).equals(":"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void locationTypeAsCookieDefinedTestCase7() {
        assertTrue(ExractkeyLocationTypeUtil.getLocationType(ColonAsKey).equals("Cookie"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void keyDefinedTestCase8() {
        assertTrue(ExractkeyLocationTypeUtil.getKey(KeyWithColonNoLocationType).equals("key4:NoLocType"));
    }

    @Test(groups = { TestGroups.FUNCTIONAL })
    public void emptyStringForLocationTypeTestCase8() {
        assertTrue(ExractkeyLocationTypeUtil.getLocationType(KeyWithColonNoLocationType).equals("Body"));
    }
}
