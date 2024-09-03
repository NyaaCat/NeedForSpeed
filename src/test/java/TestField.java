import java.text.DecimalFormat;

public class TestField {
    public static void main(String[] args) {
        var format = new DecimalFormat("00.00");
        var times = new long[]{1661000761901L,
                1661000771007L,
                1661000778276L,
                1661000785035L,
                1661000792113L,
                1661000798923L,
                1661000801965L,
                1661000806232L,
                1661000818218L,
                1661000822923L,
                1661000828165L,
                1661000839922L,
                1661000843716L,
                1661000853541L,
                1661000859583L,
                1661000863884L,
                1661000878366L,
                1661000885614L,
                1661000894404L,
                1661000904066L,
                1661000910514L,
                1661000945189L,
                1661000950520L};
        long last;
        for (int i = 0; i < times.length; i++) {
            System.out.println("通过路径点 " + String.format("%-2d", i) + "，耗时 " + format.format((times[i] - times[0]) / 1000D) + " 秒 (+" + format.format((times[i] - times[Math.max(i - 1, 0)]) / 1000D) + "秒)");
        }
    }

    record A(int a, int b) {
    }
}
