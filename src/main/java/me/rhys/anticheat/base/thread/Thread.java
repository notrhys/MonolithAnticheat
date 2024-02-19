package me.rhys.anticheat.base.thread;

import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class Thread {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    public int count;
}