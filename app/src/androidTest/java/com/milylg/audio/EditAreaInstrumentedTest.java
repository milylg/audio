package com.milylg.audio;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.milylg.audio.editor.EditArea;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class EditAreaInstrumentedTest {

    private Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private EditArea editArea;

    @Before
    public void setup() {
        editArea = new EditArea(appContext);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testInitEditArea() {
        assertEquals(0, editArea.getChildCount());
    }

    @Test
    public void testInputText() {

    }
}