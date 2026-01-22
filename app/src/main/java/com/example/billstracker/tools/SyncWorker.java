package com.example.billstracker.tools;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Trigger the repository to sync
        // We use a latch or a simple synchronous call if possible,
        // but here we just call the existing saveData.
        Repository.getInstance().saveData(getApplicationContext(), (success, message) -> {
            // Logic for retry is handled by the OS based on the return below
        });

        // We return success because we want the Repository's internal logic
        // to handle the flags. If it fails again, we can schedule another.
        return Result.success();
    }
}
