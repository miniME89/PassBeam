package io.github.minime89.keepasstransfer;

import android.app.Application;
import android.content.Context;

import io.github.minime89.keepasstransfer.keyboard.CharacterConverter;
import io.github.minime89.keepasstransfer.keyboard.KeycodeMapper;
import io.github.minime89.keepasstransfer.keyboard.KeysymMapper;
import io.github.minime89.keepasstransfer.keyboard.ScancodeMapper;

public class KeePassTransfer extends Application {
    private static final String TAG = KeePassTransfer.class.getSimpleName();

    private static Context context;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        FileManager.getInstance().install();

        CharacterConverter characterConverter = CharacterConverter.getInstance();
        KeycodeMapper keycodeMapper = null;
        try {
            ScancodeMapper scancodeMapper = new ScancodeMapper("default");
            KeysymMapper keysymMapper = new KeysymMapper("default");
            keycodeMapper = new KeycodeMapper("de", scancodeMapper, keysymMapper);
        } catch (ScancodeMapper.ScancodeMapperException e) {
            e.printStackTrace();
        } catch (KeysymMapper.KeysymMapperException e) {
            e.printStackTrace();
        } catch (KeycodeMapper.KeycodeMapperException e) {
            e.printStackTrace();
        }
        characterConverter.setKeycodeMapper(keycodeMapper);
    }

    public static Context getContext() {
        return context;
    }
}
