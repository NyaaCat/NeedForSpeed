import cat.nyaa.nfs.dataclasses.CheckRange;
import cat.nyaa.nfs.dataclasses.Objective;
import cat.nyaa.nfs.dataclasses.Point;
import com.google.gson.GsonBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class YamlReader {
    private final Map<String, Object> yamlData;

    public YamlReader(String filePath) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        yamlData = yaml.load(fileInputStream);
    }

    public Object getValue(String key) {
        return getValueHelper(key, yamlData);
    }

    private Object getValueHelper(String key, Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object k = entry.getKey();
            Object v = entry.getValue();

            if (k.toString().equals(key)) {
                return v;
            }

            if (v instanceof Map) {
                Object result = getValueHelper(key, (Map<?, ?>) v);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public static void main(String[] args) {
        var jsonBase = new File("/home/langua/Softwares/paper1.21.1/plugins/NeedForSpeed/objectives/");
        var gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        try {
            YamlReader reader = new YamlReader("/home/langua/Downloads/Telegram Desktop/timers.yml");

            // Access data by key
            var timers = (Map) reader.getValue("timers");
            for (var k : timers.keySet()) {
                var records = (Map) timers.get(k);
                var name = (String) records.get("name");
                var enabled = (Boolean) records.get("enable");
                var checkpoints = (Map) records.get("checkpoint");
                var checkRanges = new ArrayList<CheckRange>();

                for (int i = 1; checkpoints.containsKey(String.valueOf(i)); i++) {
                    var cp = (Map) checkpoints.get(String.valueOf(i));
                    var world = (String) cp.get("world");
                    var x1 = (int) cp.get("max_x");
                    var y1 = (int) cp.get("max_y");
                    var z1 = (int) cp.get("max_z");
                    var x2 = (int) cp.get("min_x");
                    var y2 = (int) cp.get("min_y");
                    var z2 = (int) cp.get("min_z");
                    checkRanges.add(
                            new CheckRange(world,
                                    new Point(x1, y1, z1),
                                    new Point(x2, y2, z2)
                            )
                    );
                }

                var uniqueID = UUID.randomUUID();
                var checkAreaGroup = new Objective(uniqueID, name, enabled, true, false, true, checkRanges);
                var fos = new FileOutputStream(new File(jsonBase, name + ".json"), false);
                fos.write(gson.toJson(checkAreaGroup).getBytes(StandardCharsets.UTF_8));
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}