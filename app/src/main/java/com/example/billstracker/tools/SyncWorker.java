package com.example.billstracker.tools;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SyncWorker extends Worker {

    Context context;
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseTools.saveData(getApplicationContext(), (success, message) -> {
        });
        return Result.success();
    }
}
