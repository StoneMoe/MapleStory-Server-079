//////////////////////////////
//Æß±¦*èªç±åé©å²*æå·åæ////
//1346464664/992916233//////
///////////////////////////
var ca = java.util.Calendar.getInstance();
var year = ca.get(java.util.Calendar.YEAR); //è·å¾å¹´ä»½
var month = ca.get(java.util.Calendar.MONTH) + 1; //è·å¾æä»½
var day = ca.get(java.util.Calendar.DATE);//è·åæ¥
var hour = ca.get(java.util.Calendar.HOUR_OF_DAY); //è·å¾å°æ¶
var minute = ca.get(java.util.Calendar.MINUTE);//è·å¾åé
var second = ca.get(java.util.Calendar.SECOND); //è·å¾ç§
var weekday = ca.get(java.util.Calendar.DAY_OF_WEEK);
var Z = "#fUI/GuildMark.img/Mark/Letter/00005025/1#";
var Y = "#fUI/GuildMark.img/Mark/Letter/00005024/3#";
var X = "#fUI/GuildMark.img/Mark/Letter/00005023/1#";
var D = "#fUI/GuildMark.img/Mark/Letter/00005003/1#";
var M = "#fUI/GuildMark.img/Mark/Letter/00005012/1#";
var A = "#fUI/GuildMark.img/Mark/Letter/00005000/1#";
var P = "#fUI/GuildMark.img/Mark/Letter/00005015/1#";
var Z = "#fUI/GuildMark.img/Mark/Letter/00005025/9#";



function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 0 && mode == 0) {
        cm.dispose();
        return;
    }
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
	    if ( cm.getMapId() == 10000) {
            cm.sendOk(" èª ç± å é© å² å¢ å¤ æ  æ³ ä½¿ ç¨ æ­¤ å è½ ã");
            cm.dispose();
        }


  
    else if (status == 0) {
		
		
   var  
	    selStr = "#rèªç±åé©å²æ´»å¨åºï¼\r\n";
		selStr += "#e#L0#è¿åçé¢#l\r\n";
		
		selStr += "#L1##bèå¥å¤§å¸#l\r\n";

		cm.sendSimple(selStr);
    } else if (status == 1) {
        switch (selection) {
        case 0:
            cm.dispose();
            cm.openNpc(9900004,0);	
            break;
		case 1:
		if ( cm.getHour() == 20){ 
            cm.dispose();
            cm.openNpc(2007,10);	
			} else {
		  cm.sendOk("èå¥å¤§å¸å¼å¯æ¶é´ä¸ºæ¯å¤©;#r20:00-20:59");
		  cm.dispose();
		  }
            break;
			
	 
			 
			 
			 
		}
    }
}
