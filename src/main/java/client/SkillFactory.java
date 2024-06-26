package client;

import configuration.EnvProperties;
import lombok.extern.slf4j.Slf4j;
import provider.*;
import utils.StringUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class SkillFactory {
    private static final Map<Integer, ISkill> skills;
    private static final Map<Integer, List<Integer>> skillsByJob;
    private static final Map<Integer, SummonSkillEntry> SummonSkillInformation;
    private static final MapleData stringData;
    private static final MapleDataProvider datasource;

    public static Integer Initialize() {
        final MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(Paths.get(EnvProperties.wzPath, "Skill.wz"));
        final MapleDataDirectoryEntry root = datasource.getRoot();
        for (final MapleDataFileEntry topDir : root.getFiles()) {
            if (topDir.getName().length() <= 8) {
                for (final MapleData data : datasource.getData(topDir.getName())) {
                    if (data.getName().equals("skill")) {
                        for (final MapleData data2 : data) {
                            if (data2 != null) {
                                final int skillId = Integer.parseInt(data2.getName());
                                final Skill skill = Skill.loadFromData(skillId, data2);
                                List<Integer> job = SkillFactory.skillsByJob.computeIfAbsent(skillId / 10000, k -> new ArrayList<Integer>());
                                job.add(skillId);
                                skill.setName(getName(skillId));
                                SkillFactory.skills.put(skillId, skill);
                                final MapleData summon_data = data2.getChildByPath("summon/attack1/info");
                                if (summon_data == null) {
                                    continue;
                                }
                                final SummonSkillEntry sse = new SummonSkillEntry();
                                sse.attackAfter = (short) MapleDataTool.getInt("attackAfter", summon_data, 999999);
                                sse.type = (byte) MapleDataTool.getInt("type", summon_data, 0);
                                sse.mobCount = (byte) MapleDataTool.getInt("mobCount", summon_data, 1);
                                SkillFactory.SummonSkillInformation.put(skillId, sse);
                            }
                        }
                    }
                }
            }
        }
        return skills.size();
    }

    public static ISkill getSkill(final int id) {
        return SkillFactory.skills.get(id);
    }

    public static ISkill getSkill1(final int id) {
        ISkill ret = SkillFactory.skills.get(id);
        if (ret != null) {
            return ret;
        }
        synchronized (SkillFactory.skills) {
            ret = SkillFactory.skills.get(id);
            if (ret == null) {
                final int job = id / 10000;
                final MapleData skillRoot = SkillFactory.datasource.getData(StringUtil.getLeftPaddedStr(String.valueOf(job), '0', 3) + ".img");
                final MapleData skillData = skillRoot.getChildByPath("skill/" + StringUtil.getLeftPaddedStr(String.valueOf(id), '0', 7));
                if (skillData != null) {
                    ret = Skill.loadFromData(id, skillData);
                }
                SkillFactory.skills.put(id, ret);
            }
            return ret;
        }
    }

    public static List<Integer> getSkillsByJob(final int jobId) {
        return SkillFactory.skillsByJob.get(jobId);
    }

    public static String getSkillName(final int id) {
        final ISkill skill = getSkill(id);
        if (skill != null) {
            return skill.getName();
        }
        return null;
    }

    public static String getName(final int id) {
        String strId = Integer.toString(id);
        strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
        final MapleData skillRoot = SkillFactory.stringData.getChildByPath(strId);
        if (skillRoot != null) {
            return MapleDataTool.getString(skillRoot.getChildByPath("name"), "");
        }
        return null;
    }

    public static SummonSkillEntry getSummonData(final int skillid) {
        return SkillFactory.SummonSkillInformation.get(skillid);
    }

    public static Collection<ISkill> getAllSkills() {
        return SkillFactory.skills.values();
    }

    static {
        skills = new HashMap<Integer, ISkill>();
        skillsByJob = new HashMap<Integer, List<Integer>>();
        SummonSkillInformation = new HashMap<Integer, SummonSkillEntry>();
        stringData = MapleDataProviderFactory.getDataProvider(Paths.get(EnvProperties.wzPath, "String.wz")).getData("Skill.img");
        datasource = MapleDataProviderFactory.getDataProvider(Paths.get(EnvProperties.wzPath, "Skill.wz"));
    }
}
