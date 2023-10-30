package dev.anhcraft.vouchers.util;

import com.vlkan.rfos.RotatingFileOutputStream;
import com.vlkan.rfos.RotationConfig;
import com.vlkan.rfos.policy.DailyRotationPolicy;
import com.vlkan.rfos.policy.SizeBasedRotationPolicy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PluginLogger {
    private final RotationConfig config;
    private final List<String> queue = new CopyOnWriteArrayList<>();

    public PluginLogger(File dir) {
        dir.mkdirs();
        this.config = RotationConfig
                .builder()
                .file(dir.getAbsolutePath()+"/latest.log")
                .filePattern(dir.getAbsolutePath()+"/%d{yyyyMMdd-HHmmss.SSS}.log")
                .policy(new SizeBasedRotationPolicy(1024 * 1024))
                .compress(true)
                .policy(DailyRotationPolicy.getInstance())
                .build();
    }

    public void writeRaw(String str, Object... args) {
        queue.add(String.format(str, args) + "\n");
    }

    public ScopedLog scope(String scope) {
        return new ScopedLog(this, scope);
    }

    public synchronized void flush() {
        if (queue.isEmpty()) return;
        try {
            try (RotatingFileOutputStream stream = new RotatingFileOutputStream(config)) {
                for (String str : queue) {
                    stream.write(str.getBytes(StandardCharsets.UTF_8));
                }
                stream.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        queue.clear();
    }
}
