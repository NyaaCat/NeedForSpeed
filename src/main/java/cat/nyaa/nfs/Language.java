package cat.nyaa.nfs;

import land.melon.lab.simplelanguageloader.components.Text;

public class Language {
    public Text usage = Text.of();
    public Text playerOnlyCommand = Text.of("&7You should use this command as a player.");
    public Text firstCheckAreaNotice = Text.of("&7You've joined &a{groupName}&7, timing starts!");
    public Text firstCheckAreaSubtitle = Text.of("&a{groupName} &f| &aStart!");
    public Text checkAreaPassNotice = Text.of("&7You've passed CheckArea &a{groupName}&f#&a{checkAreaNumber}&7 in &a{partTime}&7s, &a{totalTime}s &7elapsed totally!");
    public Text checkAreaPassSubtitle = Text.of("&f#&a{checkAreaNumber} ✓ &f| &a{time}s");
    public Text finishNotice = Text.of("&7You've finished &a{groupName} &7in &f{time} seconds&7! &6Congratulations!");
    public Text newRecordNotice = Text.of("&6⭐ New Personal Record!");
    public Text timerResetAuto = Text.of("&c{groupName} &f| &c❌");
    public Text finishSubtitle = Text.of("&a{groupName} {completeTag} &f| {time}s");
    public Text newRecordTag = Text.of("&6⭐");
    public Text normalCompleteTag = Text.of("&a⭐");
    public Text enableBeforeExportRank = Text.of("You should enable {groupName} before export its rank.");
    public Text rankTitle = Text.of("=== Rank of {groupName} ===");
    public Text rankLine = Text.of("No.{number}  {playerNameAligned}  {time}s");
    public Text checkAreaGroupCreated = Text.of("&7CheckAreaGroup &c{groupName} &7created");
    public Text checkAreaGroupNotFound = Text.of("&7CheckAreaGroup &c{groupName} &7not found");
    public Text checkAreaCreated = Text.of("&7CheckArea &a{groupName}&f#&a{checkAreaNumber}&7 created");
    public Text checkAreaDeleted = Text.of("&7CheckArea &a{groupName}&f#&a{checkAreaNumber}&7 deleted");
    public Text failedToDelete = Text.of("&cFailed to delete, it might be a bug.");
    public Text checkAreaGroupDeleted = Text.of("&7{groupName} deleted.");
    public Text notValidIndex = Text.of("&c{input}&7 is not a valid index between &c{indexMin}&7 and &c{indexMax}&7！");
    public Text failedToApplyOperation = Text.of("Failed to apply operation, please check console for more information");
    public Text needSelectPoint = Text.of("&7You need to select two point with WorldEdit before create new CheckArea.");
    public Text needDisableFirst = Text.of("&7You should use command &f/nfs checkareagroup disable {name} to disable {name} before delete it");
    public Text disabledSuccessful = Text.of("&7Disabled &a{groupName}");
    public Text enabledSuccessful = Text.of("&7Enabled &a{groupName}");
    public Text failedToDisable = Text.of("&7Failed to disable &c{groupName}&7, it might already been disabled");
    public Text failedToEnable = Text.of("&7Failed to enable &c{groupName}&7, it might already been enabled");
    public Text checkAreaEdited = Text.of("&7CheckArea &a{groupName}&f&a#{checkAreaNumber}&7 edited");
    public Text checkAreaInfo = Text.of(
            "&7CheckArea {groupName}#{CheckAreaNumberAligned}: {locationAligned} {teleportButton}"
    );
    //⭐
    public Text nameUsed = Text.of("&7name &c{name}&7 is used, please change another one.");
}
