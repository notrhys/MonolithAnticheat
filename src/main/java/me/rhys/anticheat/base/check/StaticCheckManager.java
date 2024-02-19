package me.rhys.anticheat.base.check;

import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.checks.combat.HitboxA;
import me.rhys.anticheat.checks.combat.Reach;
import me.rhys.anticheat.checks.combat.aim.AimA;
import me.rhys.anticheat.checks.combat.aim.AimB;
import me.rhys.anticheat.checks.combat.aim.AimC;
import me.rhys.anticheat.checks.combat.aim.AimD;
import me.rhys.anticheat.checks.combat.autoclicker.*;
import me.rhys.anticheat.checks.combat.killaura.KillauraA;
import me.rhys.anticheat.checks.combat.velocity.VelocityA;
import me.rhys.anticheat.checks.movement.Sprint;
import me.rhys.anticheat.checks.movement.fly.FlyA;
import me.rhys.anticheat.checks.movement.fly.FlyB;
import me.rhys.anticheat.checks.movement.fly.FlyC;
import me.rhys.anticheat.checks.movement.invalid.InvalidA;
import me.rhys.anticheat.checks.movement.invalid.InvalidB;
import me.rhys.anticheat.checks.movement.speed.SpeedA;
import me.rhys.anticheat.checks.movement.speed.SpeedB;
import me.rhys.anticheat.checks.movement.speed.SpeedC;
import me.rhys.anticheat.checks.other.PayloadA;
import me.rhys.anticheat.checks.other.TimerA;
import me.rhys.anticheat.checks.other.badpackets.*;
import me.rhys.anticheat.config.CheckFile;
import me.rhys.anticheat.util.StaticUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class StaticCheckManager {
    private final List<Check> checkList = new ArrayList<>();
    private boolean allChecksReady = true;

    public void setup() {
        this.addCheck(

                new AutoClickerA(),
                new AutoClickerB(),
                new AutoClickerC(),
                new AutoClickerD(),
                new AutoClickerE(),
                new AutoClickerF(),
                new AutoClickerG(),
                new AutoClickerH(),
                new AutoClickerI(),
                new AutoClickerJ(),
                new AutoClickerK(),
                new AutoClickerL(),
                new AutoClickerM(),
                new AutoClickerN(),
                new AutoClickerO(),

                new SpeedA(),
                new SpeedB(),
                new SpeedC(),

                new FlyA(),
                new FlyB(),
                new FlyC(),

                new InvalidA(),
                new InvalidB(),

                new Sprint(),

                new HitboxA(),
                new Reach(),

                new KillauraA(),

                new AimA(),
                new AimB(),
                new AimC(),
                new AimD(),

                new VelocityA(),

                new TimerA(),

                new BadPacketsA(),
                new BadPacketsB(),
                new BadPacketsC(),
                new BadPacketsD(),
                new BadPacketsE(),
                new BadPacketsF(),
                new BadPacketsG(),
               // new BadPacketsH(),
                new BadPacketsI(),
                new BadPacketsJ(),
              //  new BadPacketsK(),
               // new BadPacketsL(),
                new BadPacketsM(),

                new PayloadA()
        );
    }

    public void reload() {
        this.checkList.clear();
        this.setup();
        Plugin.getInstance().getCommandManager().reload();
    }

    public void saveChecks() {
        CheckFile checkFile = new CheckFile();
        checkFile.setup(Plugin.getServerInstance());

        this.checkList.forEach(check -> {
            String checkName = check.getName() + check.getType();

            String enablePath = String.format("Checks.%s.enabled", checkName);
            String punishPath = String.format("Checks.%s.punish.enabled", checkName);
            String banVLPath = String.format("Checks.%s.punish.max", checkName);

            checkFile.getFileConfiguration().set(enablePath, check.isEnabled());
            checkFile.getFileConfiguration().set(punishPath, check.isBan());
            checkFile.getFileConfiguration().set(banVLPath, check.getBanVL());
        });

        checkFile.saveData();
    }

    void addCheck(Check... checks) {

        CheckFile checkFile = new CheckFile();
        checkFile.setup(Plugin.getServerInstance());

        Arrays.asList(checks).forEach(check -> {
            check.getInformation();

            String checkName = check.getName() + check.getType();

            String enablePath = String.format("Checks.%s.enabled", checkName);
            String punishPath = String.format("Checks.%s.punish.enabled", checkName);
            String banVLPath = String.format("Checks.%s.punish.max", checkName);

            if (!checkFile.getFileConfiguration().contains(enablePath)) {
                checkFile.getFileConfiguration().set(enablePath, check.isEnabled());
            } else {
                check.setEnabled(checkFile.getFileConfiguration().getBoolean(enablePath));
            }

            if (!checkFile.getFileConfiguration().contains(punishPath)) {
                checkFile.getFileConfiguration().set(punishPath, check.isBan());
            } else {
                check.setBan(checkFile.getFileConfiguration().getBoolean(punishPath));
            }

            if (!checkFile.getFileConfiguration().contains(banVLPath)) {
                checkFile.getFileConfiguration().set(banVLPath, check.getBanVL());
            } else {
                check.setBanVL(checkFile.getFileConfiguration().getInt(banVLPath));
            }

            this.checkList.add(check);
        });

        checkFile.saveData();
    }

    public List<Check> cloneChecks() {
        List<Check> checks = new ArrayList<>();
        this.checkList.forEach(check -> checks.add(check.clone()));
        return checks;
    }
}
