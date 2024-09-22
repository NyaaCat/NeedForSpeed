import cat.nyaa.nfs.dataclasses.Objective;
import cat.nyaa.nfs.dataclasses.Point;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CheckRangeResizer {
    // to make the max point of a range increase by 1 so it will be the correct point of the AABB box
    public static void main(String[] args) throws IOException {
        var objectiveDir = new File("/home/langua/Softwares/paper1.21.1/plugins/NeedForSpeed/objectives/");
        var gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        for (var file : objectiveDir.listFiles()) {
            if (file.getName().endsWith(".json")) {
                var objective = gson.fromJson(new FileReader(file), Objective.class);
                objective.setFirstRangeCountsCheckNumber(true);
                for (var checkRange : objective.getCheckRanges()) {
                    var a = checkRange.getMax();
                    checkRange.setMax(new Point(a.x + 1, a.y + 1, a.z + 1));
                }
                try (var writer = new FileWriter(file)) {
                    writer.write(gson.toJson(objective));
                }
            }
        }
    }
}
