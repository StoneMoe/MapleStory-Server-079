var status = -1;

function start(mode, type, selection) {
    qm.sendNext("谢谢你。");
    //qm.gainItem(1142077, 1);
    qm.forceCompleteQuest();
    qm.dispose();
}

function end(mode, type, selection) {
    qm.forceCompleteQuest();
    qm.dispose();
}
