package cz.vutbr.fit.xhalas10.bp;

import android.content.Context;
import android.widget.Toast;

import java.util.concurrent.CopyOnWriteArrayList;

public class AndroidUtils implements Utils {
    AndroidLauncher context;

    public AndroidUtils(AndroidLauncher context) {
        this.context = context;
    }

    @Override
    public void showToast(String string) {
        new Thread() {
            public void run() {
                context.runOnUiThread(() -> Toast.makeText(context, string, Toast.LENGTH_SHORT).show());
            }
        }.start();
    }
}
