package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import antiban.Antiban;
import banking.Banking;
import dynamicArea.DynamicArea;
import fishing.Fishing;
import simpleGui.SimpleGui;

@ScriptManifest(author = "EmbeddedJ", info = "Dynamic Fisher", name = "Beta Dynamic Fisher v0.1", version = .1, logo = "")
public class Main extends Script {
	Banking bank;
	Fishing fish;
	Antiban antiban;
	long timeStart;
	int startFishingLvl;
	int startFishingXp;

	@Override
	public void onStart() throws InterruptedException {
		log("Starting.");
		bank = new Banking(this);
		log("Initialized banks");
		fish = new Fishing(this);
		log("Initialized fish");
		antiban = new Antiban(this, fish);
		log("Initialized antiban");
		getKeyboard().initializeModule();
		getCamera().initializeModule();
		timeStart = System.currentTimeMillis();
		startFishingLvl = getExperienceTracker().getGainedLevels(Skill.FISHING);
		startFishingXp = getExperienceTracker().getGainedXP(Skill.FISHING);
		
		SimpleGui gui = new SimpleGui(this, this.fish, this.bank);
		log("Initialized gui");
		gui.Setup();
		log("Setup Gui");
		gui.Display();
	}

	@Override
	public int onLoop() throws InterruptedException {
		sleep(100);
		if(!fish.isFishing()){
			if (fish.fish()) {
				log("Fishing.");
				sleep(10000);
				if (antiban.AntibanHandler()) {
					log("Performed Antiban");
				} else{
					log("Antiban Failed.");
				}
			} else{
				log("Fishing False");
			}
		}
		if (getInventory().isFull()) {
			if (bank.bank()) {
				log("Banking Succesful");
			}
			else{
				log("Banking Failed");
			}
		}

		return 0;
	}

	@Override
	public void onMessage(Message message) {

	}
	
	@Override
	public void onPaint(Graphics2D g) {
		long timeElapsed = System.currentTimeMillis() - timeStart;
		long seconds = (timeElapsed / 1000) % 60;
		long minutes = (timeElapsed / (1000 * 60)) % 60;
		long hours = (timeElapsed / (1000 * 60 * 60)) % 24;
		g.setFont(new Font("Trebuchet MS", Font.PLAIN, 14));
		g.setColor(Color.white);

		g.drawString("x", (int) getMouse().getPosition().getX() - 4, (int) getMouse().getPosition().getY() + 5);
		g.drawString("Time Running: " + (hours >= 10 ? "" + hours : "0" + hours) + ":"
				+ (minutes >= 10 ? "" + minutes : "0" + minutes) + ":" + (seconds >= 10 ? "" + seconds : "0" + seconds),
				8, 65);
		g.drawString("Fishing XP: " + (getExperienceTracker().getGainedXP(Skill.FISHING) - startFishingXp) + " ("
				+ (getExperienceTracker().getGainedLevels(Skill.FISHING) - startFishingLvl) + ")", 8, 80);
		
	}
}
