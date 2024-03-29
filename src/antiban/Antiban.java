package antiban;

import java.util.Random;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.input.mouse.EntityDestination;
import org.osbot.rs07.script.Script;

import fishing.Fishing;
import main.Timer;

public class Antiban {
	final int UP_KEY = 38;
	final int LEFT_KEY = 37;
	final int DOWN_KEY = 40;
	final int RIGHT_KEY = 39;	
	
	Random rn;
	Script script;
	Fishing fish;
	Timer t = new Timer();

	public Antiban(Script script, Fishing fish) {
		this.script = script;
		this.fish = fish;
		rn = new Random(script.myPlayer().getId());
	}

	/**
	 * All anti ban methods other than move camera
	 * @return
	 * @throws InterruptedException
	 */
	public boolean AntibanHandler() throws InterruptedException {
		if (!fish.isFishing()) {
			return false;
		}
		switch (rn.nextInt(8)) {
		case 0:
		case 1:
			/* Move Mouse */
			script.log("Move Mouse.");
			return moveMouse();
		case 2:
			/* Move Camera */
			script.log("Move Camera.");
			return moveCamera();
		case 3:
			/* Right Click Hover */
			script.log("Right Click.");
			return rightClickNext();
		case 4:
			/* Move Camera */
			script.log("Move Camera.");
			moveCamera();
		case 5:
			/* Move Mouse */
			script.log("Move Mouse.");
			return moveMouse();
		case 6:
		case 7:
			/* Sleep until 1s after done fishing. */
			script.log("Sleep.");
			while (fish.isFishing()) {
				Script.sleep(rn.nextInt(100) + 100);
			}
			return true;
		case 8:
			/* Hover Fishing. */
			script.log("Check XP.");
			if(hoverFishing()){
				while (fish.isFishing()) {
					Script.sleep(rn.nextInt(100) + 100);
				}
				script.getTabs().open(Tab.INVENTORY);
				return true;
			}
			return false;
		case 9:
			script.log("Check XP & move mouse.");
			if(hoverFishing()){
				Script.sleep(rn.nextInt(900)+500);
				return moveMouse();
			}
			return false;
		default:
			/* Move Mouse */
			return moveMouse();
		}
	}

	private boolean moveCamera() {
		if (!script.getMouse().isOnScreen()) {
			return false;
		}
		int camera_end_val = rn.nextInt(7) + 60;
		if (script.getCamera().getPitchAngle() < 60) {
			script.getKeyboard().pressKey(UP_KEY);
			if (rn.nextInt(1) == 0) {
				script.getKeyboard().pressKey(LEFT_KEY);
			} else if (rn.nextInt(1) == 0) {
				script.getKeyboard().pressKey(RIGHT_KEY);
			}
			t.reset();
			while (script.getCamera().getPitchAngle() < camera_end_val && !script.getBot().isHumanInputEnabled() &&
					t.timer(2000)) {
			}
			script.getKeyboard().releaseKey(UP_KEY);
			script.getKeyboard().releaseKey(LEFT_KEY);
			script.getKeyboard().releaseKey(RIGHT_KEY);
			return true;
		}
		return false;
	}

	/**
	 * Move mouse off the screen until player is no longer fishing.
	 * 
	 * @return True if Successful. False if failure.
	 * @throws InterruptedException 
	 */
	private boolean moveMouse() throws InterruptedException {
		if (script.getMouse().moveOutsideScreen()) {
			while (fish.isFishing()) {
				Script.sleep(rn.nextInt(100) + 100);
			}
			return true;
		}
		return false;
	}

	private boolean rightClickNext() throws InterruptedException {
		NPC next = fish.getNextSpot();
		if (next.isVisible()) {
			EntityDestination targetDest = new EntityDestination(script.getBot(), next);

			script.getMouse().click(targetDest, true);
			t.reset();
			while (!script.getMenuAPI().isOpen() && t.timer(rn.nextInt(1000)+150)) {
				Script.sleep(50);
			}
		} else {
			script.getCamera().toEntity(next);
			if (next.isVisible()) {
				EntityDestination targetDest = new EntityDestination(script.getBot(), next);

				script.getMouse().click(targetDest, true);
				t.reset();
				while (!script.getMenuAPI().isOpen() && t.timer(rn.nextInt(1000)+150)) {
					Script.sleep(50);
				}
			}
			else{
				return false;
			}
		}
		while (fish.isFishing()) {
			Script.sleep(rn.nextInt(100) + 100);
		}
		return fish.clickNextFish(next);
	}

	public boolean hoverFishing() throws InterruptedException{
		if(script.getSkills().hoverSkill(Skill.FISHING)){
			return true;
		}
		return false;
	}
}
