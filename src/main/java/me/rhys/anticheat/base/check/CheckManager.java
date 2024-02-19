package me.rhys.anticheat.base.check;

import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.user.User;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"deprecation"})
@Getter
public class CheckManager {
    private final User user;
    private final List<Check> checks = new ArrayList<>();
    private boolean loadedAll;

    public CheckManager(User user) {
        this.user = user;
    }

    public void registerChecks() {
        List<Check> clonedChecks = Plugin.getInstance().getStaticCheckManager().cloneChecks();
        clonedChecks.forEach(this::addCheck);
        clonedChecks.clear();
    }

    void addCheck(Check check) {
        this.register(check, user);
        this.checks.add(check);
        this.setup(check, user);
    }

    private void setup(Check check, User user) {
        check.onSetup(user);
    }

    private void register(Check check, User user) {
        check.register(user);
    }
}
