package io.github.minime89.keepasstransfer.hooks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.github.minime89.keepasstransfer.KeePassTransferService;

public class BootListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        KeePassTransferService.start(context);
    }
}
