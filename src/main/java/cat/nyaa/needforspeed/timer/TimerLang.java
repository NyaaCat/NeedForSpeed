package cat.nyaa.needforspeed.timer;

import land.melon.lab.simplelanguageloader.components.Text;

public class TimerLang {
    private static TimerLang INSTANCE;
    private TimerLang(){}
    public static TimerLang getInstance(){
        if (INSTANCE == null){
            synchronized (TimerLang.class){
                if (INSTANCE == null){
                    INSTANCE = new TimerLang();
                }
            }
        }
        return INSTANCE;
    }

    // todo
    public Text pointBroadcast = Text.of("计时器 {name} 已启用");
    public Text finishBroadcast = Text.of("计时器 {name} 已禁用");
    public Text status = Text.of("计时器: {timer}, 状态: {status}");
    public Text info = Text.of("计时器: {timer}, 检查点: {checkPoint}, 状态: {status}, 是否广播检查点: {pointBroadcast}, 是否广播结束点: {finishBroadcast}");
}
