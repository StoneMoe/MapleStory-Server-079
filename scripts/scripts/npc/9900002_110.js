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

var ç®­å¤´ = "#fUI/Basic/BtHide3/mouseOver/0#";

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
	
	var MC = cm.getServerName();
	var h1 = cm.getBossRank("é¶è¡è´¦æ·1",2);
	var h2 = cm.getBossRank("é¶è¡è´¦æ·2",2);
	var h3 = cm.getBossRank("é¶è¡è´¦æ·3",2);
	var h4 = cm.getBossRank("é¶è¡è´¦æ·4",2);
	var h5 = cm.getBossRank("é¶è¡è´¦æ·5",2);
	var h6 = cm.getBossRank("é¶è¡è´¦æ·6",2);
	var h7 = cm.getBossRank("é¶è¡è´¦æ·7",2);
	var h8 = cm.getBossRank("é¶è¡è´¦æ·8",2);
	var h9 = cm.getBossRank("é¶è¡è´¦æ·9",2);
	var r1 = Math.ceil(Math.random() * 9);
	var r2 = Math.ceil(Math.random() * 9);
	var r3 = Math.ceil(Math.random() * 9);
	var r4 = Math.ceil(Math.random() * 9);
	var r5 = Math.ceil(Math.random() * 9);
	var r6 = Math.ceil(Math.random() * 9);
	var r7 = Math.ceil(Math.random() * 9);
	var r8 = Math.ceil(Math.random() * 9);
	var r9 = Math.ceil(Math.random() * 9);
    
	    if ( cm.getMapId() == 10000) {
            cm.sendOk(" èª ç± å é© å² å¢ å¤ æ  æ³ ä½¿ ç¨ æ­¤ å è½ ã");
            cm.dispose();
        }


  
    else if (status == 0) {

    var  
	    selStr = "âââââââ#e#rãèªç±åé©å²æ°çé¶è¡ã#kâââââââ#n\r\n";


		selStr += "\t\t\t #L1#"+ç®­å¤´+"#béç½®å¯ç ä¸º111111#l \r\n\r\n\r\n";
		
		

		
		
		
		
		
		
		//selStr += " #b#e#L0#"+æ«å¶å¤©æ¢¯+"#l #L1#"+éèº«ä»åº+"#l #L2#"+é³ä¹ç¹æ­+"#l #L3#"+å¿«æ·ååº+"#l\r\n";
		///selStr += " #L99995#"+è¯ç¼ä¸åº+"#l #L5#"+æ¸çèå+"#l #L99997#"+é»é æèº+"#l #L7#"+ä»»å¡æå+"#l \r\n";
		//selStr += " #L20#"+ç¤¼åååº+"#l #L21#"+ç°éååº+"#l #r#e#L23#"+è³å°åå­+"#l #L24#"+è§è²è£æ®+"#l\r\n";
		//selStr += " #L100000#"+å¿«éè½¬è+"#l #L99996#"+æ´»å¨ä¸åº+"#l #L99994#"+å¤©èµ+"#l #L6#"+ä¸ªäººä¿¡æ¯+"#l\r\n";

		
        //selStr += "\r\n\t\t\t\t\t\t\t\t\t\t\t  #L9999##rè®¾ç½®#l#k ";

		cm.sendSimple(selStr);
    } else if (status == 1) {
        switch (selection) {
          
						
		case 1:
		    cm.setBossRankCount("é¶è¡å¯ç ","-"+cm.getBossRank("é¶è¡å¯ç ",2)+"");
			cm.setBossRankCount("é¶è¡å¯ç ",111111);
            cm.dispose();
            cm.openNpc(9900002,101);	
            break;
				 
		




































	
			 
			} 
		}
    }
