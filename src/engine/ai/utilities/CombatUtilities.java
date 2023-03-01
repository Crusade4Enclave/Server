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
		float damage = 0;
		DamageType dt = getDamageType(agent);
		if(agent.isSummonedPet() == true || agent.isPet() == true || agent.isNecroPet() == true) {
			damage = calculatePetDamage(agent);
		}else if(agent.isPlayerGuard() == true){
			damage = calculateGuardDamage(agent);
		}else if (agent.getLevel() > 80) {
			damage = calculateEpicDamage(agent);
		} else{
			damage = calculateMobDamage(agent);
		}
		if (AbstractWorldObject.IsAbstractCharacter(target)) {
			if (((AbstractCharacter) target).isSit()) {
				damage *= 2.5f; //increase damage if sitting
			}
			return (int) (((AbstractCharacter) target).getResists().getResistedDamage(agent, (AbstractCharacter) target, dt, damage, 0));
		}
		if (target.getObjectType() == GameObjectType.Building) {
			Building building = (Building) target;
			Resists resists = building.getResists();
			return (int) ((damage * (1 - (resists.getResist(dt, 0) / 100))));
		}
		return damage;
	}
	public static DamageType getDamageType(Mob agent){
		DamageType dt = DamageType.Crush;
		if(agent.getEquip().get(1).getItemBase() != null ){
			return agent.getEquip().get(1).getItemBase().getDamageType();
		}
		if(agent.getEquip().get(2).getItemBase() != null && agent.getEquip().get(2).getItemBase().isShield() == false){
			return agent.getEquip().get(2).getItemBase().getDamageType();
		}
		return dt;
	}
	public static int calculatePetDamage(Mob agent) {
		//damage calc for pet
		float range;
		float damage;
		float min = 40;
		float max = 60;
		AbstractWorldObject target = agent.getCombatTarget();
		float dmgMultiplier = 1 + agent.getBonuses().getFloatPercentAll(ModType.MeleeDamageModifier, SourceType.None);
		float str = agent.getStatStrCurrent();
		float dex = agent.getStatDexCurrent();
		double minDmg =  getMinDmg(min,agent, null);
		double maxDmg =  getMaxDmg(max,agent, null);
		dmgMultiplier += agent.getLevel() / 10;
		range = (float) (maxDmg - minDmg);
		damage = min + ((ThreadLocalRandom.current().nextFloat() * range) + (ThreadLocalRandom.current().nextFloat() * range)) / 2;
		return (int) (damage * dmgMultiplier);
	}
	public static int calculateGuardDamage(Mob agent){
		//damage calc for guard
		ItemBase weapon = agent.getEquip().get(1).getItemBase();
		AbstractWorldObject target = agent.getCombatTarget();
		float dmgMultiplier = 1 + agent.getBonuses().getFloatPercentAll(ModType.MeleeDamageModifier, SourceType.None);
		double minDmg = weapon.getMinDamage();
		double maxDmg = weapon.getMaxDamage();
		float str = agent.getStatStrCurrent();
		float dex = agent.getStatDexCurrent();
		double min = getMinDmg(minDmg,agent, weapon);
		double max = getMaxDmg(maxDmg,agent, weapon);
		DamageType dt = weapon.getDamageType();
		double range = max - min;
		double damage = min + ((ThreadLocalRandom.current().nextFloat() * range) + (ThreadLocalRandom.current().nextFloat() * range)) / 2;
		if (AbstractWorldObject.IsAbstractCharacter(target))
			if (((AbstractCharacter) target).isSit())
				damage *= 2.5f; //increase damage if sitting
		if (AbstractWorldObject.IsAbstractCharacter(target))
			return (int) (((AbstractCharacter) target).getResists().getResistedDamage((AbstractCharacter) agent, (AbstractCharacter) target, dt, (float) damage, 0) * dmgMultiplier);
		return 0;
	}
	public static int calculateEpicDamage(Mob agent){
		//handle r8 mob damage
		DamageType dt = DamageType.Crush;
		AbstractWorldObject target = agent.getCombatTarget();
		float dmgMultiplier = 1 + agent.getBonuses().getFloatPercentAll(ModType.MeleeDamageModifier, SourceType.None);
		if (agent.getEquip().get(1).getItemBase() != null) {
			dt = agent.getEquip().get(1).getItemBase().getDamageType();
		} else if(agent.getEquip().get(2).getItemBase() != null && agent.getEquip().get(2).getItemBase().isShield() == false){
			dt = agent.getEquip().get(2).getItemBase().getDamageType();
		}
		double min = agent.getMobBase().getMinDmg();
		double max = agent.getMobBase().getMaxDmg();
		double range = max - min;
		double damage = min + ((ThreadLocalRandom.current().nextFloat() * range) + (ThreadLocalRandom.current().nextFloat() * range)) / 2;
		return (int) (((AbstractCharacter) target).getResists().getResistedDamage((AbstractCharacter) agent, (AbstractCharacter) target, dt, (float) damage, 0) * dmgMultiplier);
	}
	public static int calculateMobDamage(Mob agent){
		ItemBase weapon = null;
		double minDmg;
		double maxDmg;
		DamageType dt;
		if(agent.getEquip().get(1).getItemBase() != null){
			//mainhand damage
			weapon = agent.getEquip().get(1).getItemBase();
		} else if(agent.getEquip().get(2).getItemBase() != null){
			//offhand damage
			weapon = agent.getEquip().get(2).getItemBase();
		}
		if(weapon != null){
			minDmg = weapon.getMinDamage();
			maxDmg = weapon.getMaxDamage();
			dt = weapon.getDamageType();
		}else{
			minDmg = agent.getMobBase().getMinDmg();
			maxDmg = agent.getMobBase().getMaxDmg();
			dt = DamageType.Crush;
		}
		AbstractWorldObject target = agent.getCombatTarget();
		float dmgMultiplier = 1 + agent.getBonuses().getFloatPercentAll(ModType.MeleeDamageModifier, SourceType.None);
		float str = agent.getStatStrCurrent();
		float dex = agent.getStatDexCurrent();
		double min = getMinDmg(minDmg,agent, weapon);
		double max = getMaxDmg(maxDmg,agent, weapon);

		double range = max - min;
		double damage = min + ((ThreadLocalRandom.current().nextFloat() * range) + (ThreadLocalRandom.current().nextFloat() * range)) / 2;
		if (AbstractWorldObject.IsAbstractCharacter(target))
			if (((AbstractCharacter) target).isSit())
				damage *= 2.5f; //increase damage if sitting
		if (AbstractWorldObject.IsAbstractCharacter(target))
			return (int) (((AbstractCharacter) target).getResists().getResistedDamage((AbstractCharacter) agent, (AbstractCharacter) target, dt, (float) damage, 0) * dmgMultiplier);
		return 0;
	}
	public static double getMinDmg(double min, Mob agent, ItemBase weapon){
		int primary = 0;
		int secondary = 0;
		int focusLevel = 0;
		int masteryLevel = 0;
		if(weapon.isStrBased() == true){
			primary = agent.getStatStrCurrent();
			secondary = agent.getStatDexCurrent();
		}
		else{
		primary = agent.getStatDexCurrent();
		secondary = agent.getStatStrCurrent();
		}
		if (weapon != null) {
			focusLevel = (int) agent.getSkills().get(weapon.getSkillRequired()).getModifiedAmount();
			masteryLevel = (int) agent.getSkills().get(weapon.getMastery()).getModifiedAmount();
		}
		return min * (pow(0.0048*primary +.049*(primary-0.75),0.5) + pow(0.0066*secondary + 0.064*(secondary-0.75),0.5) + + 0.01*(focusLevel + masteryLevel));
	}
	public static double getMaxDmg(double max, Mob agent, ItemBase weapon){
		int primary = 0;
		int secondary = 0;
		int focusLevel = 0;
		int masteryLevel = 0;
		if(weapon.isStrBased() == true){
			primary = agent.getStatStrCurrent();
			secondary = agent.getStatDexCurrent();
		}
		else{
			primary = agent.getStatDexCurrent();
			secondary = agent.getStatStrCurrent();
		}
		if (weapon != null) {
			focusLevel = (int) agent.getSkills().get(weapon.getSkillRequired()).getModifiedAmount();
			masteryLevel = (int) agent.getSkills().get(weapon.getMastery()).getModifiedAmount();
		}
		return max * (pow(0.0124*primary + 0.118*(primary -0.75),0.5) + pow(0.0022*secondary + 0.028*(secondary-0.75),0.5) + 0.0075*(focusLevel + masteryLevel));
	}
	public static boolean RunAIRandom(){
		int random = ThreadLocalRandom.current().nextInt(4);

		return random == 0;
	}
}
