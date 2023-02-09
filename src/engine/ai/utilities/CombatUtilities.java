// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.ai.utilities;

import engine.Enum;
import engine.Enum.*;
import engine.ai.MobileFSM.STATE;
import engine.gameManager.ChatManager;
import engine.gameManager.CombatManager;
import engine.gameManager.PowersManager;
import engine.math.Vector3fImmutable;
import engine.net.DispatchMessage;
import engine.net.client.msg.PerformActionMsg;
import engine.net.client.msg.TargetedActionMsg;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static engine.math.FastMath.sqr;
import static java.lang.Math.pow;

public class CombatUtilities {

	public static boolean inRangeToAttack(Mob agent,AbstractWorldObject target){

		if (Float.isNaN(agent.getLoc().x))
			return false;

		try{
			Vector3fImmutable sl = agent.getLoc();
			Vector3fImmutable tl = target.getLoc();
			
			//add Hitbox's to range.
			float range = agent.getRange();
			range += CombatManager.calcHitBox(target) + CombatManager.calcHitBox(agent);
			//if (target instanceof AbstractCharacter)
			//				if (((AbstractCharacter)target).isMoving())
			//					range+= 5;

			return !(sl.distanceSquared(tl) > sqr(range));
		}catch(Exception e){
			Logger.error( e.toString());
			return false;
		}

	}
	public static boolean inRangeToAttack2D(Mob agent,AbstractWorldObject target){

		if (Float.isNaN(agent.getLoc().x))
			return false;

		try{
			Vector3fImmutable sl = agent.getLoc();
			Vector3fImmutable tl = target.getLoc();
			
			//add Hitbox's to range.
			float range = agent.getRange();
			range += CombatManager.calcHitBox(target) + CombatManager.calcHitBox(agent);
			//if (target instanceof AbstractCharacter)
			//				if (((AbstractCharacter)target).isMoving())
			//					range+= 5;

			return !(sl.distanceSquared2D(tl) > sqr(range));
		}catch(Exception e){
			Logger.error( e.toString());
			return false;
		}

	}
	public static boolean inRangeToCast2D(Mob agent,AbstractWorldObject target, PowersBase power) {

		if (Float.isNaN(agent.getLoc().x))
			return false;
		try {
			Vector3fImmutable sl = agent.getLoc();
			Vector3fImmutable tl = target.getLoc();
			float range = power.getRange();
			range += CombatManager.calcHitBox(target) + CombatManager.calcHitBox(agent);
			return !(sl.distanceSquared2D(tl) > sqr(range));
		} catch (Exception e) {
			Logger.error(e.toString());
			return false;
		}
	}
	public static void swingIsBlock(Mob agent,AbstractWorldObject target, int animation) {

		if (!target.isAlive())
			return;

		TargetedActionMsg msg = new TargetedActionMsg(agent,animation, target, MBServerStatics.COMBAT_SEND_BLOCK);

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			DispatchMessage.dispatchMsgToInterestArea(target, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true,false);
		else
			DispatchMessage.sendToAllInRange(agent,msg);

	}
	public static void swingIsParry(Mob agent,AbstractWorldObject target, int animation) {

		if (!target.isAlive())
			return;

		TargetedActionMsg msg = new TargetedActionMsg(agent,animation, target,  MBServerStatics.COMBAT_SEND_PARRY);

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			DispatchMessage.dispatchMsgToInterestArea(target, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true,false);
		else
			DispatchMessage.sendToAllInRange(agent,msg);

	}
	public static void swingIsDodge(Mob agent,AbstractWorldObject target, int animation) {

		if (!target.isAlive())
			return;

		TargetedActionMsg msg = new TargetedActionMsg(agent,animation, target, MBServerStatics.COMBAT_SEND_DODGE);

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			DispatchMessage.dispatchMsgToInterestArea(target, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true,false);
		else
			DispatchMessage.sendToAllInRange(agent,msg);
	}
	public static void swingIsDamage(Mob agent,AbstractWorldObject target, float damage, int animation){
		float trueDamage = 0;

		if (!target.isAlive())
			return;

		if (AbstractWorldObject.IsAbstractCharacter(target))
			trueDamage = ((AbstractCharacter) target).modifyHealth(-damage, agent, false);
		else if (target.getObjectType() == GameObjectType.Building)
			trueDamage = ((Building) target).modifyHealth(-damage, agent);

		//Don't send 0 damage kay thanx.

		if (trueDamage == 0)
			return;

		TargetedActionMsg msg = new TargetedActionMsg(agent,target, damage, animation);

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			DispatchMessage.dispatchMsgToInterestArea(target, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true,false);
		else
			DispatchMessage.sendToAllInRange(agent,msg);

		//check damage shields
		if(AbstractWorldObject.IsAbstractCharacter(target) && target.isAlive() && target.getObjectType() != GameObjectType.Mob)
			CombatManager.handleDamageShields(agent,(AbstractCharacter)target, damage);
	}
	public static boolean canSwing(Mob agent) {
		return (agent.isAlive() && !agent.getBonuses().getBool(ModType.Stunned, SourceType.None));
	}
	public static void swingIsMiss(Mob agent,AbstractWorldObject target, int animation) {

		TargetedActionMsg msg = new TargetedActionMsg(agent,target, 0f, animation);

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			DispatchMessage.dispatchMsgToInterestArea(target, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true,false);
		else
			DispatchMessage.sendToAllInRange(agent,msg);

	}
	public static boolean triggerDefense(Mob agent, AbstractWorldObject target) {
		int defenseScore = 0;
		int attackScore = agent.getAtrHandOne();
		switch (target.getObjectType()) {
		case PlayerCharacter:
			defenseScore = ((AbstractCharacter) target).getDefenseRating();
			break;
		case Mob:

			Mob mob = (Mob)target;
			if (mob.isSiege())
				defenseScore = attackScore;
			break;
		case Building:
			return false;
		}



		int hitChance;
		if (attackScore > defenseScore || defenseScore == 0)
			hitChance = 94;
		else if (attackScore == defenseScore && target.getObjectType() == GameObjectType.Mob)
			hitChance = 10;
		else {
			float dif = attackScore / defenseScore;
			if (dif <= 0.8f)
				hitChance = 4;
			else
				hitChance = ((int)(450 * (dif - 0.8f)) + 4);
			if (target.getObjectType() == GameObjectType.Building)
				hitChance = 100;
		}
		return ThreadLocalRandom.current().nextInt(100) > hitChance;
	}
	public static boolean triggerBlock(Mob agent,AbstractWorldObject ac) {
		return triggerPassive(agent,ac, "Block");
	}
	public static boolean triggerParry(Mob agent,AbstractWorldObject ac) {
		return triggerPassive(agent,ac, "Parry");
	}
	public static boolean triggerDodge(Mob agent,AbstractWorldObject ac) {
		return triggerPassive(agent,ac, "Dodge");
	}
	public static boolean triggerPassive(Mob agent,AbstractWorldObject ac, String type) {
		float chance = 0;
		if (AbstractWorldObject.IsAbstractCharacter(ac))
			chance = ((AbstractCharacter)ac).getPassiveChance(type, agent.getLevel(), true);

		if (chance > 75f)
			chance = 75f;
		if (agent.isSiege() && AbstractWorldObject.IsAbstractCharacter(ac))
			chance = 100;

		return ThreadLocalRandom.current().nextInt(100) < chance;
	}
	public static void combatCycle(Mob agent,AbstractWorldObject target, boolean mainHand, ItemBase wb) {

		if (!agent.isAlive() || !target.isAlive()) return;

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			if (!((PlayerCharacter)target).isActive())
				return;

		int anim = 75;
		float speed = 30f;
		if (mainHand)
			speed = agent.getSpeedHandOne();
		else
			speed = agent.getSpeedHandTwo();
		DamageType dt = DamageType.Crush;
		if (agent.isSiege())
			dt = DamageType.Siege;
		if (wb != null) {
			anim = CombatManager.getSwingAnimation(wb, null,mainHand);
			dt = wb.getDamageType();
		} else if (!mainHand)
			return;
		Resists res = null;
		PlayerBonuses bonus = null;
		switch(target.getObjectType()){
		case Building:
			res = ((Building)target).getResists();
			break;
		case PlayerCharacter:
			res = ((PlayerCharacter)target).getResists();
			bonus = ((PlayerCharacter)target).getBonuses();
			break;
		case Mob:
			Mob mob = (Mob)target;
			res = mob.getResists();
			bonus = ((Mob)target).getBonuses();
			break;
		}

		//must not be immune to all or immune to attack

		if (bonus != null && !bonus.getBool(ModType.NoMod, SourceType.ImmuneToAttack))
			if (res != null &&(res.immuneToAll() || res.immuneToAttacks() || res.immuneTo(dt)))
				return;

		int passiveAnim =  CombatManager.getSwingAnimation(wb, null,mainHand);
		if(canSwing(agent)) {
			if(triggerDefense(agent,target)) {
				swingIsMiss(agent, target, passiveAnim);
				return;
			}
			else if(triggerDodge(agent,target)) {
				swingIsDodge(agent, target, passiveAnim);
				return;
			}
			else if (triggerParry(agent, target)){
					swingIsParry(agent, target, passiveAnim);

				return;
			}
			else if(triggerBlock(agent,target)) {
				swingIsBlock(agent, target, passiveAnim);
				return;
			}
				swingIsDamage(agent,target, determineDamage(agent,target), anim);

			if (agent.getWeaponPower() != null)
				agent.getWeaponPower().attack(target, MBServerStatics.ONE_MINUTE);
		}
		
		if (target.getObjectType().equals(GameObjectType.PlayerCharacter)){
			PlayerCharacter player = (PlayerCharacter)target;
			if (player.getDebug(64)){
				ChatManager.chatSayInfo(player, "Debug Combat: Mob UUID " + agent.getObjectUUID() + " || Building ID  = " + agent.getBuildingID() + " || Floor = " + agent.getInFloorID() + " || Level = " + agent.getInBuilding() );//combat debug
			}
		}

		//SIEGE MONSTERS DO NOT ATTACK GUARDSs
		if (target.getObjectType() == GameObjectType.Mob)
			if (((Mob)target).isSiege())
				return;

		//handle the retaliate

		if (AbstractWorldObject.IsAbstractCharacter(target))
			CombatManager.handleRetaliate((AbstractCharacter)target, agent);

		if (target.getObjectType() == GameObjectType.Mob){
			Mob targetMob = (Mob)target;
			if (targetMob.isSiege())
				return;
		}


	}
	public static float determineDamage(Mob agent,AbstractWorldObject target) {
		if(agent == null || target == null){
			//early exit for null
			return 0;
		}
		//set default values
			float min = 40;
			float max = 60;
		if(agent.getLevel() == 85){
			min = agent.getMinDamageHandOne();
			max = agent.getMaxDamageHandOne();
		}
			float range;
			float damage;
			float dmgMultiplier = 1 + agent.getBonuses().getFloatPercentAll(ModType.MeleeDamageModifier, SourceType.None);
		if(agent.isSummonedPet() == true || agent.isPet() == true || agent.isNecroPet() == true) {
			//damage calc for pet
			float str = agent.getStatStrCurrent();
			float dex = agent.getStatDexCurrent();
			double minDmg =  getMinDmg(min,str,dex,agent.getLevel());
			double maxDmg =  getMaxDmg(max,str,dex,agent.getLevel());
			dmgMultiplier += agent.getLevel() / 10;
			range = (float) (maxDmg - minDmg);
			damage = min + ((ThreadLocalRandom.current().nextFloat() * range) + (ThreadLocalRandom.current().nextFloat() * range)) / 2;
			if (AbstractWorldObject.IsAbstractCharacter(target))
				if (((AbstractCharacter) target).isSit())
					damage *= 2.5f; //increase damage if sitting

			if (AbstractWorldObject.IsAbstractCharacter(target))
				return ((AbstractCharacter) target).getResists().getResistedDamage(agent, (AbstractCharacter) target, DamageType.Crush, damage, 0) * dmgMultiplier;

			if (target.getObjectType() == GameObjectType.Building) {
				Building building = (Building) target;
				Resists resists = building.getResists();
				return (damage * (1 - (resists.getResist(DamageType.Crush, 0) / 100))) * dmgMultiplier;
			}
		}else if(agent.isPlayerGuard() == true){
			//damage calc for guard
			ItemBase weapon = agent.getEquip().get(1).getItemBase();
			double minDmg = weapon.getMinDamage();
			double maxDmg = weapon.getMaxDamage();
			float str = agent.getStatStrCurrent();
			float dex = agent.getStatDexCurrent();
			min = (float) getMinDmg(minDmg,str,dex,agent.getLevel());
			max = (float) getMaxDmg(maxDmg,str,dex,agent.getLevel());
			DamageType dt = weapon.getDamageType();
			range = max - min;
			damage = min + ((ThreadLocalRandom.current().nextFloat() * range) + (ThreadLocalRandom.current().nextFloat() * range)) / 2;
			if (AbstractWorldObject.IsAbstractCharacter(target))
				if (((AbstractCharacter) target).isSit())
					damage *= 2.5f; //increase damage if sitting
			if (AbstractWorldObject.IsAbstractCharacter(target))
				return ((AbstractCharacter) target).getResists().getResistedDamage(agent, (AbstractCharacter) target, dt, damage, 0) * dmgMultiplier;
		}
		else{
			//damage calc for regular mob
			if(agent.getLevel() > 85) {
				min = agent.getMobBase().getDamageMin();
				max = agent.getMobBase().getMaxDmg();
			}

			DamageType dt = DamageType.Crush;
			if(agent.getEquip().get(1) != null && agent.getEquip().get(2) == null){
				min = agent.getEquip().get(1).getItemBase().getMinDamage();
				max = agent.getEquip().get(1).getItemBase().getMaxDamage();
			} else if(agent.getEquip().get(1) == null && agent.getEquip().get(2) != null){
				min = agent.getEquip().get(2).getItemBase().getMinDamage();
				max = agent.getEquip().get(2).getItemBase().getMaxDamage();
			} else if(agent.getEquip().get(1) != null && agent.getEquip().get(2) != null){
				min = agent.getEquip().get(1).getItemBase().getMinDamage() + agent.getEquip().get(2).getItemBase().getMinDamage();
				max = agent.getEquip().get(1).getItemBase().getMaxDamage() + agent.getEquip().get(2).getItemBase().getMaxDamage();
			}
			range = max - min;
			damage = min + ((ThreadLocalRandom.current().nextFloat() * range) + (ThreadLocalRandom.current().nextFloat() * range)) / 2;
			if (AbstractWorldObject.IsAbstractCharacter(target))
				if (((AbstractCharacter) target).isSit())
					damage *= 2.5f; //increase damage if sitting

			if (AbstractWorldObject.IsAbstractCharacter(target))
				return ((AbstractCharacter) target).getResists().getResistedDamage(agent, (AbstractCharacter) target, dt, damage, 0) * dmgMultiplier;

			if (target.getObjectType() == GameObjectType.Building) {
				Building building = (Building) target;
				Resists resists = building.getResists();
				return (damage * (1 - (resists.getResist(dt, 0) / 100))) * dmgMultiplier;
			}
		}
		//impossible to get this far
		return 0;
	}
	public static double getMinDmg(double min, float str, float dex, int level){
		if(str == 0){
			str = 1;
		}
		if(dex == 0){
			dex = 1;
		}
		return (min * pow((0.0048*str +.049*(str-0.75)),pow(0.5 + 0.0066*dex + 0.064*(dex-0.75),0.5 + 0.01*(200/level))));
	}
	public static double getMaxDmg(double max, float str, float dex, int level){
		if(str == 0){
			str = 1;
		}
		if(dex == 0){
			dex = 1;
		}
		return (max * pow((0.0124*str +0.118*(str-0.75)),pow(0.5 + 0.0022*dex + 0.028*(dex-0.75),0.5 + 0.0075*(200/level))));
	}
	public static boolean RunAIRandom(){
		int random = ThreadLocalRandom.current().nextInt(4);

		return random == 0;
	}
}
