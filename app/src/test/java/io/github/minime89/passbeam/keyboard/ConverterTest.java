/*
 * Copyright (C) 2015 Marcel Lehwald
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.minime89.passbeam.keyboard;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;

import io.github.minime89.passbeam.FileManager;
import io.github.minime89.passbeam.PassBeamApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PassBeamApplication.class)
public class ConverterTest {
    @Before
    public void before() {
        PassBeamApplication app = Mockito.mock(PassBeamApplication.class);

        PowerMockito.mockStatic(PassBeamApplication.class);
        Mockito.when(PassBeamApplication.getInstance()).thenReturn(app);

        Context context = Mockito.mock(Context.class);
        Mockito.when(context.getExternalFilesDir(null)).thenReturn(new File(System.getProperty("user.dir"), "src/main/assets/app"));
        Mockito.when(app.getContext()).thenReturn(context);
    }

    @Test
    public void testLayouts() throws Exception {
        Socket virtualKeyboard = new Socket("localhost", 4242);
        InputStream virtualKeyboardInput = virtualKeyboard.getInputStream();
        OutputStream virtualKeyboardOutput = virtualKeyboard.getOutputStream();

        FileManager fileManager = new FileManager();
        Converter converter = new Converter();
        Layouts layouts = Layouts.load();

        assertEquals("expect that all layouts in the keycodes directory are valid and could be loaded", fileManager.getKeycodesFiles().size(), layouts.getLayouts().size());

        for (Layout layout : layouts.getLayouts()) {
            converter.load(layout);

            Process process = Runtime.getRuntime().exec("xkb-exporter --device 14 --layout " + layout.getLayoutName() + " --variant " + layout.getVariantName());
            int ret = process.waitFor();

            assertEquals("expect that xkb-exporter process changes keyboard layout", 0, ret);

            Keycodes keycodes = converter.getKeycodes();
            Collection<Symbol> symbols = keycodes.findPrintable();

            assertTrue(String.format("expect that keycodes '%s' contain at least one printable symbol", layout.getId()), symbols.size() > 0);

            StringBuilder inputStrBuilder = new StringBuilder();
            for (Symbol symbol : symbols) {
                Keysym keysym = symbol.getKeysym();
                inputStrBuilder.append(keysym.getUnicode().getCharacter());
            }
            String inputStr = inputStrBuilder.toString();

            StringBuilder outputStrBuilder = new StringBuilder();
            Collection<byte[]> data = converter.convert(inputStr);
            for (byte[] bytes : data) {
                assertEquals("expect that the keyboard event is 16 bytes long (see USB HID specification)", bytes.length, 16);

                virtualKeyboardOutput.write(bytes);

                byte[] buffer = new byte[256];
                int len = virtualKeyboardInput.read(buffer);
                outputStrBuilder.append(new String(buffer, 0, len, "UTF-8"));
            }
            String outputStr = outputStrBuilder.toString();

            if (outputStr.equals(inputStr)) {
                System.out.println(String.format("[layout: %s, variant: %s] ok", layout.getLayoutName(), layout.getVariantName()));
                System.out.println(String.format("input:    %s", inputStr));
                System.out.println(String.format("output:   %s", outputStr));
            } else {
                System.err.println(String.format("[layout: %s, variant: %s] failed", layout.getLayoutName(), layout.getVariantName()));
                System.err.println(String.format("input:    %s", inputStr));
                System.err.println(String.format("output:   %s", outputStr));
            }
        }
    }

}