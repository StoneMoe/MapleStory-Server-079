package handling;

import client.MapleClient;
import constants.ServerConstants;
import handling.cashshop.CashShopServer;
import handling.cashshop.handler.CashShopOperation;
import handling.cashshop.handler.MTSOperation;
import handling.channel.ChannelServer;
import handling.channel.handler.AllianceHandler;
import handling.channel.handler.BBSHandler;
import handling.channel.handler.BeanGame;
import handling.channel.handler.BuddyListHandler;
import handling.channel.handler.ChatHandler;
import handling.channel.handler.DueyHandler;
import handling.channel.handler.FamilyHandler;
import handling.channel.handler.GuildHandler;
import handling.channel.handler.HiredMerchantHandler;
import handling.channel.handler.InterServerHandler;
import handling.channel.handler.InventoryHandler;
import handling.channel.handler.ItemMakerHandler;
import handling.channel.handler.MobHandler;
import handling.channel.handler.MonsterCarnivalHandler;
import handling.channel.handler.NPCHandler;
import handling.channel.handler.PartyHandler;
import handling.channel.handler.PetHandler;
import handling.channel.handler.PlayerHandler;
import handling.channel.handler.PlayerInteractionHandler;
import handling.channel.handler.PlayersHandler;
import handling.channel.handler.StatsHandling;
import handling.channel.handler.SummonHandler;
import handling.channel.handler.UserInterfaceHandler;
import handling.login.LoginServer;
import handling.login.handler.CharLoginHandler;
import handling.login.handler.PacketErrorHandler;
import handling.mina.MaplePacketDecoder;
import handling.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import server.MTSStorage;
import server.Randomizer;
import server.ServerProperties;
import utils.FileoutputUtil;
import utils.HexTool;
import networking.MapleAESOFB;
import utils.datastructures.Pair;
import networking.input.ByteArrayByteStream;
import networking.input.GenericSeekableLittleEndianAccessor;
import networking.input.SeekableLittleEndianAccessor;
import networking.packet.LoginPacket;

@Slf4j
public class MapleServerHandler extends IoHandlerAdapter {
    private static boolean debugMode = Boolean.parseBoolean(ServerProperties.getProperty("RoyMS.Debug", "false"));
    private int channel;
    private boolean cs;
    private List<String> BlockedIP;
    private Map<String, Pair<Long, Byte>> loginTracker;

    public static void log(final SeekableLittleEndianAccessor packet, final RecvPacketOpcode op, final MapleClient c, final IoSession io) {
        String ip = io.getRemoteAddress().toString();
        String accountName = (c == null || c.getAccountName() == null) ? "[Null]" : c.getAccountName();
        String characterName = (c == null || c.getPlayer() == null || c.getPlayer().getName() == null) ? "[Null]" : c.getPlayer().getName();
        int accountID = (c == null) ? -1 : c.getAccID();
        log.info("[IP: {}] [{}|{}|{}] [Op: {}] [Data: {}]", ip, accountID, accountName, characterName, op.toString(), packet.toString());
    }

    public MapleServerHandler(final int channel, final boolean cs) {
        this.BlockedIP = new ArrayList<String>();
        this.loginTracker = new ConcurrentHashMap<String, Pair<Long, Byte>>();
        this.channel = channel;
        this.cs = cs;
    }

    public void messageSent(final IoSession session, final Object message) throws Exception {
        final Runnable r = ((MaplePacket) message).getOnSend();
        if (r != null) {
            r.run();
        }
        super.messageSent(session, message);
    }

    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
    }

    public void sessionOpened(final IoSession session) throws Exception {
        final String address = session.getRemoteAddress().toString().split(":")[0];
        session.setAttribute("REMOTE_ADDRESS", session.getRemoteAddress().toString());
        final Pair<Long, Byte> track = this.loginTracker.get(address);
        byte count;
        if (track == null) {
            count = 1;
        } else {
            count = track.right;
            final long difference = System.currentTimeMillis() - track.left;
            if (difference < 2000L) {
                ++count;
            } else if (difference > 20000L) {
                count = 1;
            }
            if (count >= 10) {
                log.info("Login rate limit reached");
                this.BlockedIP.add(address);
                this.loginTracker.remove(address);
                session.closeNow();
                return;
            }
        }
        this.loginTracker.put(address, new Pair<Long, Byte>(System.currentTimeMillis(), count));
        if (this.channel > -1) {
            if (ChannelServer.getInstance(this.channel).isShutdown()) {
                log.info("频道服务器尚未开启,发现连接进入，该连接被断开");
                session.closeNow();
                return;
            }
        } else if (this.cs) {
            if (CashShopServer.isShutdown()) {
                log.info("商城服务器尚未开启,发现连接进入，该连接被断开");
                session.closeNow();
                return;
            }
        } else if (LoginServer.isShutdown()) {
            log.info("登录服务器尚未开启,发现连接进入，该连接被断开");
            session.closeNow();
            return;
        }
        final byte[] serverRecv = {70, 114, 122, (byte) Randomizer.nextInt(255)};
        final byte[] serverSend = {82, 48, 120, (byte) Randomizer.nextInt(255)};
        final byte[] ivRecv = ServerConstants.Use_Fixed_IV ? new byte[]{9, 0, 5, 95} : serverRecv;
        final byte[] ivSend = ServerConstants.Use_Fixed_IV ? new byte[]{1, 95, 4, 63} : serverSend;
        final MapleClient client = new MapleClient(new MapleAESOFB(ivSend, (short) (65535 - ServerConstants.MAPLE_VERSION)), new MapleAESOFB(ivRecv, ServerConstants.MAPLE_VERSION), session);
        client.setChannel(this.channel);
        final MaplePacketDecoder.DecoderState decoderState = new MaplePacketDecoder.DecoderState();
        session.setAttribute(MaplePacketDecoder.DECODER_STATE_KEY, decoderState);
        session.write(LoginPacket.getHello(ServerConstants.MAPLE_VERSION, ServerConstants.Use_Fixed_IV ? serverSend : ivSend, ServerConstants.Use_Fixed_IV ? serverRecv : ivRecv));
        session.setAttribute(MapleClient.CLIENT_KEY, client);
        session.setAttribute(IdleStatus.READER_IDLE, 60);
        session.setAttribute(IdleStatus.WRITER_IDLE, 60);
        final StringBuilder sb = new StringBuilder();
        if (this.channel > -1) {
            sb.append("[频道服务器] 频道 ").append(this.channel).append(" : ");
        } else if (this.cs) {
            sb.append("[商城服务器]");
        } else {
            sb.append("[登录服务器]");
        }
        sb.append("IoSession opened ").append(address);
        log.info(sb.toString());
        World.Client.addClient(client);
    }

    public void sessionClosed(final IoSession session) throws Exception {
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            try {
                client.disconnect(true, this.cs);
            } catch (Exception ex) {
                log.error("sessionClosed", ex);
            } finally {
                World.Client.removeClient(client);
                session.closeNow();
                session.removeAttribute(MapleClient.CLIENT_KEY);
            }
        }
        super.sessionClosed(session);
    }

    public void messageReceived(final IoSession session, final Object message) {
        try {
            final SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream((byte[]) message));
            if (slea.available() < 2L) {
                return;
            }
            final short header_num = slea.readShort();
            final RecvPacketOpcode[] values = RecvPacketOpcode.values();
            final int length = values.length;
            int i = 0;
            while (i < length) {
                final RecvPacketOpcode recv = values[i];
                if (recv.getValue() == header_num) {
                    if (log.isDebugEnabled() && !RecvPacketOpcode.isSpamHeader(recv)) {
                        log.info("Received data 已處理 :{}\n{}\n{}", recv, HexTool.toString((byte[]) message), HexTool.toStringFromAscii((byte[]) message));
                    }
                    final MapleClient c = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
                    if (!c.isReceiving()) {
                        return;
                    }
                    if (recv.NeedsChecking() && !c.isLoggedIn()) {
                        return;
                    }
                    if (log.isDebugEnabled()) {
                        log(slea, recv, c, session);
                    }
                    handlePacket(recv, slea, c, this.cs);
                    return;
                } else {
                    ++i;
                }
            }
            if (MapleServerHandler.debugMode) {
                log.info("Received data 未處理 : {}\n{}", HexTool.toString((byte[]) message), HexTool.toStringFromAscii((byte[]) message));
            }
        } catch (Exception ex) {
            log.error("messageReceived", ex);
        }
    }

    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            client.sendPing();
            super.sessionIdle(session, status);
            return;
        }
        session.closeNow();
    }

    public static void handlePacket(final RecvPacketOpcode header, final SeekableLittleEndianAccessor slea, final MapleClient c, final boolean cs) throws Exception {
        switch (header) {
            case PONG: {
                c.pongReceived();
            }
            case PACKET_ERROR: {
                PacketErrorHandler.handlePacket(slea, c);
            }
            case LOGIN_PASSWORD: {
                CharLoginHandler.login(slea, c);
                break;
            }
            case SERVERLIST_REQUEST: {
                CharLoginHandler.ServerListRequest(c);
                break;
            }
            case LICENSE_REQUEST: {
                CharLoginHandler.ServerListRequest(c);
                break;
            }
            case CHARLIST_REQUEST: {
                CharLoginHandler.CharlistRequest(slea, c);
                break;
            }
            case SERVERSTATUS_REQUEST: {
                CharLoginHandler.ServerStatusRequest(c);
                break;
            }
            case CHECK_CHAR_NAME: {
                CharLoginHandler.CheckCharName(slea.readMapleAsciiString(), c);
                break;
            }
            case CREATE_CHAR: {
                CharLoginHandler.CreateChar(slea, c);
                break;
            }
            case CHAR_SELECT: {
                CharLoginHandler.Character_WithoutSecondPassword(slea, c);
                break;
            }
            case SET_GENDER: {
                CharLoginHandler.SetGenderRequest(slea, c);
                break;
            }
            case RSA_KEY: {
                c.getSession().write(LoginPacket.StrangeDATA());
                break;
            }
            case CHANGE_CHANNEL: {
                InterServerHandler.ChangeChannel(slea, c, c.getPlayer());
                break;
            }
            case PLAYER_LOGGEDIN: {
                final int playerid = slea.readInt();
                if (cs) {
                    CashShopOperation.EnterCS(playerid, c);
                    break;
                }
                InterServerHandler.Loggedin(playerid, c);
                break;
            }
            case ENTER_CASH_SHOP: {
                slea.readInt();
                InterServerHandler.EnterCS(c, c.getPlayer());
                break;
            }
            case ENTER_MTS: {
                InterServerHandler.EnterMTS(c, c.getPlayer());
                break;
            }
            case PLAYER_UPDATE: {
                PlayerHandler.UpdateHandler(slea, c, c.getPlayer());
                break;
            }
            case MOVE_PLAYER: {
                PlayerHandler.MovePlayer(slea, c, c.getPlayer());
                break;
            }
            case CHAR_INFO_REQUEST: {
                c.getPlayer().updateTick(slea.readInt());
                PlayerHandler.CharInfoRequest(slea.readInt(), c, c.getPlayer());
                break;
            }
            case CLOSE_RANGE_ATTACK: {
                PlayerHandler.closeRangeAttack(slea, c, c.getPlayer(), false);
                break;
            }
            case RANGED_ATTACK: {
                PlayerHandler.rangedAttack(slea, c, c.getPlayer());
                break;
            }
            case MAGIC_ATTACK: {
                PlayerHandler.MagicDamage(slea, c, c.getPlayer());
                break;
            }
            case SPECIAL_MOVE: {
                PlayerHandler.SpecialMove(slea, c, c.getPlayer());
                break;
            }
            case PASSIVE_ENERGY: {
                PlayerHandler.closeRangeAttack(slea, c, c.getPlayer(), true);
                break;
            }
            case FACE_EXPRESSION: {
                PlayerHandler.ChangeEmotion(slea.readInt(), c.getPlayer());
                break;
            }
            case TAKE_DAMAGE: {
                PlayerHandler.TakeDamage(slea, c, c.getPlayer());
                break;
            }
            case HEAL_OVER_TIME: {
                PlayerHandler.Heal(slea, c.getPlayer());
                break;
            }
            case CANCEL_BUFF: {
                PlayerHandler.CancelBuffHandler(slea.readInt(), c.getPlayer());
                break;
            }
            case CANCEL_ITEM_EFFECT: {
                PlayerHandler.CancelItemEffect(slea.readInt(), c.getPlayer());
                break;
            }
            case USE_CHAIR: {
                PlayerHandler.UseChair(slea.readInt(), c, c.getPlayer());
                break;
            }
            case CANCEL_CHAIR: {
                PlayerHandler.CancelChair(slea.readShort(), c, c.getPlayer());
                break;
            }
            case USE_ITEMEFFECT: {
                PlayerHandler.UseItemEffect(slea.readInt(), c, c.getPlayer());
                break;
            }
            case SKILL_EFFECT: {
                PlayerHandler.SkillEffect(slea, c.getPlayer());
                break;
            }
            case MESO_DROP: {
                c.getPlayer().updateTick(slea.readInt());
                PlayerHandler.DropMeso(slea.readInt(), c.getPlayer());
                break;
            }
            case MONSTER_BOOK_COVER: {
                PlayerHandler.ChangeMonsterBookCover(slea.readInt(), c, c.getPlayer());
                break;
            }
            case CHANGE_KEYMAP: {
                PlayerHandler.ChangeKeymap(slea, c.getPlayer());
                break;
            }
            case CHANGE_MAP: {
                if (cs) {
                    if (ServerConstants.调试输出封包) {
                        log.info("退出商城");
                    }
                    CashShopOperation.LeaveCS(slea, c, c.getPlayer());
                    break;
                }
                PlayerHandler.ChangeMap(slea, c, c.getPlayer());
                break;
            }
            case CHANGE_MAP_SPECIAL: {
                PlayerHandler.ChangeMapSpecial(slea, c, c.getPlayer());
                break;
            }
            case USE_INNER_PORTAL: {
                slea.skip(1);
                PlayerHandler.InnerPortal(slea, c, c.getPlayer());
                break;
            }
            case TROCK_ADD_MAP: {
                PlayerHandler.TrockAddMap(slea, c, c.getPlayer());
                break;
            }
            case LIE_DETECTOR: {
                PlayersHandler.LieDetector(slea, c, c.getPlayer(), false);
                break;
            }
            case LIE_DETECTOR_RESPONSE: {
                PlayersHandler.LieDetectorResponse(slea, c);
                break;
            }
            case LIE_DETECTOR_REFRESH: {
                PlayersHandler.LieDetectorRefresh(slea, c);
                break;
            }
            case ARAN_COMBO: {
                PlayerHandler.AranCombo(c, c.getPlayer());
                break;
            }
            case SKILL_MACRO: {
                PlayerHandler.ChangeSkillMacro(slea, c.getPlayer());
                break;
            }
            case ITEM_BAOWU: {
                InventoryHandler.UsePenguinBox(slea, c);
                break;
            }
            case ITEM_SUNZI: {
                InventoryHandler.SunziBF(slea, c);
                break;
            }
            case GIVE_FAME: {
                PlayersHandler.GiveFame(slea, c, c.getPlayer());
                break;
            }
            case TRANSFORM_PLAYER: {
                PlayersHandler.TransformPlayer(slea, c, c.getPlayer());
                break;
            }
            case NOTE_ACTION: {
                PlayersHandler.Note(slea, c.getPlayer());
                break;
            }
            case USE_DOOR: {
                PlayersHandler.UseDoor(slea, c.getPlayer());
                break;
            }
            case DAMAGE_REACTOR: {
                PlayersHandler.HitReactor(slea, c);
                break;
            }
            case TOUCH_REACTOR: {
                PlayersHandler.TouchReactor(slea, c);
                break;
            }
            case CLOSE_CHALKBOARD: {
                c.getPlayer().setChalkboard(null);
                break;
            }
            case ITEM_MAKER: {
                ItemMakerHandler.ItemMaker(slea, c);
                break;
            }
            case ITEM_SORT: {
                InventoryHandler.ItemSort(slea, c);
                break;
            }
            case ITEM_GATHER: {
                InventoryHandler.ItemGather(slea, c);
                break;
            }
            case ITEM_MOVE: {
                InventoryHandler.ItemMove(slea, c);
                break;
            }
            case ITEM_PICKUP: {
                InventoryHandler.Pickup_Player(slea, c, c.getPlayer());
                break;
            }
            case USE_CASH_ITEM: {
                InventoryHandler.UseCashItem(slea, c);
                break;
            }
            case QUEST_KJ: {
                InventoryHandler.QuestKJ(slea, c, c.getPlayer());
                break;
            }
            case USE_ITEM: {
                InventoryHandler.UseItem(slea, c, c.getPlayer());
                break;
            }
            case USE_RETURN_SCROLL: {
                InventoryHandler.UseReturnScroll(slea, c, c.getPlayer());
                break;
            }
            case USE_UPGRADE_SCROLL: {
                c.getPlayer().updateTick(slea.readInt());
                InventoryHandler.UseUpgradeScroll((byte) slea.readShort(), (byte) slea.readShort(), (byte) slea.readShort(), c, c.getPlayer());
                break;
            }
            case USE_SUMMON_BAG: {
                InventoryHandler.UseSummonBag(slea, c, c.getPlayer());
                break;
            }
            case ITEM_MZD: {
                InventoryHandler.UseTreasureChest(slea, c, c.getPlayer());
                break;
            }
            case USE_SKILL_BOOK: {
                InventoryHandler.UseSkillBook(slea, c, c.getPlayer());
                break;
            }
            case USE_CATCH_ITEM: {
                InventoryHandler.UseCatchItem(slea, c, c.getPlayer());
                break;
            }
            case USE_MOUNT_FOOD: {
                InventoryHandler.UseMountFood(slea, c, c.getPlayer());
                break;
            }
            case MOVE_LIFE: {
                MobHandler.MoveMonster(slea, c, c.getPlayer());
                break;
            }
            case AUTO_AGGRO: {
                MobHandler.AutoAggro(slea.readInt(), c.getPlayer());
                break;
            }
            case FRIENDLY_DAMAGE: {
                MobHandler.FriendlyDamage(slea, c.getPlayer());
                break;
            }
            case MONSTER_BOMB: {
                MobHandler.MonsterBomb(slea.readInt(), c.getPlayer());
                break;
            }
            case NPC_SHOP: {
                NPCHandler.NPCShop(slea, c, c.getPlayer());
                break;
            }
            case NPC_TALK: {
                NPCHandler.NPCTalk(slea, c, c.getPlayer());
                break;
            }
            case NPC_TALK_MORE: {
                NPCHandler.NPCMoreTalk(slea, c);
                break;
            }
            case NPC_ACTION: {
                NPCHandler.NPCAnimation(slea, c);
                break;
            }
            case QUEST_ACTION: {
                NPCHandler.QuestAction(slea, c, c.getPlayer());
                break;
            }
            case STORAGE: {
                NPCHandler.Storage(slea, c, c.getPlayer());
                break;
            }
            case GENERAL_CHAT: {
                ChatHandler.GeneralChat(slea.readMapleAsciiString(), slea.readByte(), c, c.getPlayer());
                break;
            }
            case PARTYCHAT: {
                ChatHandler.Others(slea, c, c.getPlayer());
                break;
            }
            case WHISPER: {
                ChatHandler.Whisper_Find(slea, c);
                break;
            }
            case MESSENGER: {
                ChatHandler.Messenger(slea, c);
                break;
            }
            case AUTO_ASSIGN_AP: {
                StatsHandling.AutoAssignAP(slea, c, c.getPlayer());
                break;
            }
            case DISTRIBUTE_AP: {
                StatsHandling.DistributeAP(slea, c, c.getPlayer());
                break;
            }
            case DISTRIBUTE_SP: {
                c.getPlayer().updateTick(slea.readInt());
                StatsHandling.DistributeSP(slea.readInt(), c, c.getPlayer());
                break;
            }
            case PLAYER_INTERACTION: {
                PlayerInteractionHandler.PlayerInteraction(slea, c, c.getPlayer());
                break;
            }
            case GUILD_OPERATION: {
                GuildHandler.Guild(slea, c);
                break;
            }
            case DENY_GUILD_REQUEST: {
                slea.skip(1);
                GuildHandler.DenyGuildRequest(slea.readMapleAsciiString(), c);
                break;
            }
            case ALLIANCE_OPERATION: {
                AllianceHandler.HandleAlliance(slea, c, false);
                break;
            }
            case DENY_ALLIANCE_REQUEST: {
                AllianceHandler.HandleAlliance(slea, c, true);
                break;
            }
            case BBS_OPERATION: {
                BBSHandler.BBSOperatopn(slea, c);
                break;
            }
            case PARTY_OPERATION: {
                PartyHandler.PartyOperatopn(slea, c);
                break;
            }
            case DENY_PARTY_REQUEST: {
                PartyHandler.DenyPartyRequest(slea, c);
                break;
            }
            case BUDDYLIST_MODIFY: {
                BuddyListHandler.BuddyOperation(slea, c);
                break;
            }
            case CYGNUS_SUMMON: {
                UserInterfaceHandler.CygnusSummon_NPCRequest(c);
                break;
            }
            case SHIP_OBJECT: {
                UserInterfaceHandler.ShipObjectRequest(slea.readInt(), c);
                break;
            }
            case BUY_CS_ITEM: {
                CashShopOperation.BuyCashItem(slea, c, c.getPlayer());
                break;
            }
            case TOUCHING_CS: {
                CashShopOperation.TouchingCashShop(c);
                break;
            }
            case COUPON_CODE: {
                FileoutputUtil.log(FileoutputUtil.PacketEx_Log, "Coupon : \n" + slea.toString(true));
                log.info(slea.toString());
                slea.skip(2);
                CashShopOperation.CouponCode(slea.readMapleAsciiString(), c);
                break;
            }
            case CS_UPDATE: {
                CashShopOperation.CSUpdate(c);
                break;
            }
            case TOUCHING_MTS: {
                MTSOperation.MTSUpdate(MTSStorage.getInstance().getCart(c.getPlayer().getId()), c);
                break;
            }
            case MTS_TAB: {
                MTSOperation.MTSOperation(slea, c);
                break;
            }
            case DAMAGE_SUMMON: {
                SummonHandler.DamageSummon(slea, c.getPlayer());
                break;
            }
            case MOVE_SUMMON: {
                SummonHandler.MoveSummon(slea, c.getPlayer());
                break;
            }
            case SUMMON_ATTACK: {
                SummonHandler.SummonAttack(slea, c, c.getPlayer());
                break;
            }
            case PET_EXCEPTIONLIST: {
                PetHandler.PickExceptionList(slea, c, c.getPlayer());
                break;
            }
            case SPAWN_PET: {
                PetHandler.SpawnPet(slea, c, c.getPlayer());
                break;
            }
            case MOVE_PET: {
                PetHandler.MovePet(slea, c.getPlayer());
                break;
            }
            case PET_CHAT: {
                if (slea.available() < 12L) {
                    break;
                }
                PetHandler.PetChat((int) slea.readLong(), slea.readShort(), slea.readMapleAsciiString(), c.getPlayer());
                break;
            }
            case PET_COMMAND: {
                PetHandler.PetCommand(slea, c, c.getPlayer());
                break;
            }
            case PET_FOOD: {
                PetHandler.PetFood(slea, c, c.getPlayer());
                break;
            }
            case PET_LOOT: {
                InventoryHandler.Pickup_Pet(slea, c, c.getPlayer());
                break;
            }
            case PET_AUTO_POT: {
                PetHandler.Pet_AutoPotion(slea, c, c.getPlayer());
                break;
            }
            case MONSTER_CARNIVAL: {
                MonsterCarnivalHandler.MonsterCarnival(slea, c);
                break;
            }
            case DUEY_ACTION: {
                DueyHandler.DueyOperation(slea, c);
                break;
            }
            case USE_HIRED_MERCHANT: {
                HiredMerchantHandler.UseHiredMerchant(slea, c);
                break;
            }
            case MERCH_ITEM_STORE: {
                HiredMerchantHandler.MerchantItemStore(slea, c);
            }
            case LEFT_KNOCK_BACK: {
                PlayerHandler.leftKnockBack(slea, c);
                break;
            }
            case SNOWBALL: {
                PlayerHandler.snowBall(slea, c);
                break;
            }
            case ChatRoom_SYSTEM: {
                PlayersHandler.ChatRoomHandler(slea, c);
                break;
            }
            case COCONUT: {
                PlayersHandler.hitCoconut(slea, c);
                break;
            }
            case OWL: {
                InventoryHandler.Owl(slea, c);
                break;
            }
            case OWL_WARP: {
                InventoryHandler.OwlWarp(slea, c);
                break;
            }
            case USE_OWL_MINERVA: {
                InventoryHandler.OwlMinerva(slea, c);
                break;
            }
            case RPS_GAME: {
                NPCHandler.RPSGame(slea, c);
                break;
            }
            case UPDATE_QUEST: {
                NPCHandler.UpdateQuest(slea, c);
                break;
            }
            case RING_ACTION: {
                PlayersHandler.RingAction(slea, c);
                break;
            }
            case REQUEST_FAMILY: {
                FamilyHandler.RequestFamily(slea, c);
                break;
            }
            case OPEN_FAMILY: {
                FamilyHandler.OpenFamily(slea, c);
                break;
            }
            case FAMILY_OPERATION: {
                FamilyHandler.FamilyOperation(slea, c);
                break;
            }
            case DELETE_JUNIOR: {
                FamilyHandler.DeleteJunior(slea, c);
                break;
            }
            case DELETE_SENIOR: {
                FamilyHandler.DeleteSenior(slea, c);
                break;
            }
            case USE_FAMILY: {
                FamilyHandler.UseFamily(slea, c);
                break;
            }
            case FAMILY_PRECEPT: {
                FamilyHandler.FamilyPrecept(slea, c);
                break;
            }
            case FAMILY_SUMMON: {
                FamilyHandler.FamilySummon(slea, c);
                break;
            }
            case ACCEPT_FAMILY: {
                FamilyHandler.AcceptFamily(slea, c);
                break;
            }
            case BEANS_GAME1: {
                BeanGame.BeanGame1(slea, c);
                break;
            }
            case BEANS_GAME2: {
                BeanGame.BeanGame2(slea, c);
                break;
            }
            case MOONRABBIT_HP: {
                PlayerHandler.Rabbit(slea, c);
                break;
            }
        }
    }
}
