package io.github.minime89.passbeam.hooks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.github.minime89.passbeam.PassBeamService;

public class BootListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PassBeamService.start(context);
    }
}
