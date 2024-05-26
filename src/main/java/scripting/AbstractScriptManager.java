package scripting;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import utils.FileoutputUtil;
import networking.packet.MaplePacketCreator;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.nio.file.Files;

@Slf4j
public abstract class AbstractScriptManager {
    private static final ScriptEngineManager sem;

    protected Invocable getInvocable(final String path, final MapleClient c) {
        return this.getInvocable(path, c, false);
    }

    protected Invocable getInvocable(String path, final MapleClient c, final boolean npc) {
        InputStream fr = null;
        try {
            String scriptsPath = System.getProperty("scripts_path");
            path = scriptsPath + File.separator + path;
            ScriptEngine engine = null;
            if (c != null) {
                engine = c.getScriptEngine(path);
            }
            if (engine == null) {
                final File scriptFile = new File(path);
                if (!scriptFile.exists()) {
                    return null;
                }
                engine = AbstractScriptManager.sem.getEngineByName("javascript");
                if (c != null) {
                    c.setScriptEngine(path, engine);
                }

                String encoding = EncodingDetect.getJavaEncode(scriptFile);

                String scriptPatch = "load(\"nashorn:mozilla_compat.js\");\n";
                InputStream patchStream = new ByteArrayInputStream(scriptPatch.getBytes(encoding));

                fr = Files.newInputStream(scriptFile.toPath());
                SequenceInputStream combinedStream = new SequenceInputStream(patchStream, fr);

                BufferedReader bf = new BufferedReader(new InputStreamReader(combinedStream, encoding));
                engine.eval(bf);
            } else if (c != null && npc) {
                NPCScriptManager.getInstance().dispose(c);
                c.getSession().write(MaplePacketCreator.enableActions());
            }
            return (Invocable) engine;
        } catch (Exception e) {
            log.error("Error executing script. Path: " + path + "\nException " + e);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing script. Path: " + path + "\nException " + e);
            return null;
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException ex3) {
            }
        }
    }

    static {
        System.setProperty("nashorn.args", "--no-deprecation-warning"); // TODO: replace with newer engine
        sem = new ScriptEngineManager();
    }
}
