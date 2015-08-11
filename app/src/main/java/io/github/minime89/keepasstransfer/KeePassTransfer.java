package io.github.minime89.keepasstransfer;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import io.github.minime89.keepasstransfer.keyboard.CharacterConverter;
import io.github.minime89.keepasstransfer.keyboard.CharacterMapper;
import io.github.minime89.keepasstransfer.keyboard.MappingManager;
import io.github.minime89.keepasstransfer.keyboard.ScancodeMapper;

public class KeePassTransfer extends Application {
    private static final String TAG = KeePassTransfer.class.getSimpleName();

    private static Context context;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        MappingManager.getInstance().install();

        CharacterConverter characterConverter = CharacterConverter.getInstance();
        characterConverter.setScancodeMapper(new ScancodeMapper("default"));
        characterConverter.setCharacterMapper(new CharacterMapper("de"));
        try {
            characterConverter.load();
        } catch (ScancodeMapper.ScancodeMapperException | CharacterMapper.CharacterMapperException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public static Context getContext() {
        return context;
    }
}
