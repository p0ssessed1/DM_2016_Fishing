package fishing;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;

import dynamicArea.DynamicArea;
import main.Timer;

public class Fishing {

	/* TODO - Implement different fish types. */
	Random rn;
	String fishName;
	SpotType spot;
	public Script script;
	DynamicArea dynamicArea;
	String action;
	NPC current;

	List<Area> fishingAreas = new LinkedList<Area>();
	ActionFilter<NPC> actionFilter;
	ActionFilter<NPC> AllActions = new ActionFilter("Harpoon", "Cage", "Net", "Bait", "Lure");
	Timer t;

	public Fishing(Script script) {
		this.script = script;
		this.dynamicArea = new DynamicArea(this);
		rn = new Random(script.myPlayer().getId());
		fishingAreas = dynamicArea.CreateAreas(script.getNpcs().getAll());
	}

	/**
	 * Check if the player is fishing via a poling method. May take 2s if not
	 * fishing.
	 * 
	 * @return True if fishing (animating) False otherwise
	 * @throws InterruptedException
	 */
	public boolean isFishing() throws InterruptedException {
		if (isInArea()) {
			for (int i = 0; i < 10; i++) {
				Script.sleep(rn.nextInt(200) + 50);
				if (script.myPlayer().isAnimating()) {
					return true;
				} else if (i == 9) {
					return false;
				}
			}
		}

		return false;
	}

	/**
	 * CODE SNIPPED: Optional<NPC> lobster =
	 * getNpcs().getAll().stream().filter(o -> o.hasAction("Cage")).min(new
	 * Comparator<NPC>() {
	 * 
	 * @Override public int compare(NPC a, NPC b) { return
	 *           getMap().distance(a.getPosition()) -
	 *           getMap().distance(b.getPosition()); } });
	 *           if(lobster.isPresent()){ lobster.get().interact("Cage"); }
	 */

	/**
	 * Walk to the closest fishing area. Uses WebWalk.
	 * 
	 * @return True if walking is successful. False if already in area or
	 *         unsuccessful.
	 */
	public boolean walkToArea() {
		if (!fishingAreas.contains(script.myPlayer())) {
			return script.getWalking().webWalk(dynamicArea.getClosestArea(fishingAreas));
		} else {
			return false;
		}
	}

	/**
	 * Attemps to fish with a timeout. Walks to nearest fishing area, Looks for
	 * fish that has been previously set up. Attemps to fish from that location.
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public boolean fish() throws InterruptedException {
		boolean animated = false;
		if (!fishingAreas.contains(script.myPlayer())) {
			if (!walkToArea()) {
				return false;
			}
		}
		if (isInArea()) {
			Timer local_timer = new Timer();
			while (!local_timer.timer(30000)) {
				NPC fishingSpot = script.getNpcs().closest(true, n -> actionFilter.match(n));
				if (isSpotValid(fishingSpot)) {
					List<FishAction> fishActionList = getFishFromSpot(fishingSpot);
					if (fishActionList.contains(fishName)) {
						fishingSpot.interact(action);
						t.reset();
						while (!script.myPlayer().isMoving() && !t.timer(rn.nextInt(1000) + 5000)) {
							Script.sleep(rn.nextInt(200) + 100);
						}
						t.reset();
						while (!(animated = script.myPlayer().isAnimating()) && !t.timer(rn.nextInt(500) + 2500)) {
							Script.sleep(rn.nextInt(200) + 100);
						}
						if (animated) {
							current = fishingSpot;
						} else {
							current = null;
						}
						return animated;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if spot is valid as a fishing spot.
	 * 
	 * @param npc
	 * @return True : Fishing spot valid. False : Not a fishing spot.
	 */
	public boolean isSpotValid(NPC npc) {
		if (npc != null) {
			if (!npc.hasAction("Attack") && AllActions.match(npc)) {
				int id = npc.getId();
				if (id != -1) {
					for (NPC i : script.getNpcs().get(npc.getX(), npc.getY())) {
						if (i.getId() == id) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Return if the player.
	 * 
	 * @return True : Player is in the fishing area. False: Player is not in the
	 *         fishing area.
	 */
	public boolean isInArea() {
		return fishingAreas.contains(script.myPosition());
	}

	/**
	 * Set action locally.
	 * 
	 * @param action
	 *            : Action to set.
	 */
	private void setAction(String action) {
		this.action = action;
		actionFilter = new ActionFilter<NPC>(action);
	}

	/**
	 * Set the type of fish that one wishes to fish. Also sets the action that
	 * corresponds to the fish and the type of spot.
	 * 
	 * @param fish
	 *            : Name of fish to fish.
	 */
	public void setFish(String fish) {
		this.fishName = fish;
		setAction(getAction(fish));
		this.spot = getSpotType(fish);
	}

	/**
	 * Gets the next spot to fish that is not the current spot.
	 * 
	 * @return NPC : npc of next spot that is closest
	 *               null if none.
	 */
	public NPC getNextSpot() {
		List<NPC> npcs = script.getNpcs().getAll();
		List<FishAction> fishActions;
		Position myPosition = script.myPosition();
		NPC temp1;
		NPC temp2;
		NPC nearest = null;
		for(int i = 1; i < npcs.size(); i++){
			temp1 = npcs.get(i);
			temp2 = npcs.get(i-1);
			if(myPosition.distance(temp1) > myPosition.distance(temp2)){
				if (actionFilter.match(temp2)) {
					fishActions = getFishFromSpot(temp2);
					if (fishActions.contains(fishName)) {
						if (temp2 != current) {
							nearest = temp2;
						}
					}
				}
			}else{
				if (actionFilter.match(temp1)) {
					fishActions = getFishFromSpot(temp1);
					if (fishActions.contains(fishName)) {
						if (temp1 != current) {
							nearest = temp1;
						}
					}
				}
			}
		}

		return nearest;
	}
	
	public boolean clickNextFish(NPC npc) throws InterruptedException{
		if(script.getMenuAPI().isOpen()){
			if(script.getMenuAPI().selectAction(action)){
				while(script.myPlayer().isMoving() && t.timer(rn.nextInt(2500)+1000)){
					Script.sleep(100);
				}
				if(isFishing()){
					current = npc;
				} else{
					current = null;
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Function relates the fishing spot with what fish are contained there and
	 * what action corresponds to getting each fish.
	 * 
	 * @param spot
	 *            : The spot to query.
	 * @return List<FishAction> A list of fish names and actions relating to the
	 *         spot provided.
	 */
	public List<FishAction> getFishFromSpot(NPC spot) {
		String[] actions = spot.getActions();
		List<FishAction> fish_list = new LinkedList<FishAction>();
		switch (actions[0]) {
		case "Cage":
			fish_list.add(new FishAction("Lobster", "Cage"));
			fish_list.add(new FishAction("Swordfish", "Harpoon"));
			fish_list.add(new FishAction("Tuna", "Harpoon"));
			break;
		case "Net":
			if (actions[1] == "Bait") {
				fish_list.add(new FishAction("Anchovies", "Net"));
				fish_list.add(new FishAction("Shrimp", "Net"));
				fish_list.add(new FishAction("Sardines", "Bait"));
				fish_list.add(new FishAction("Herring", "Bait"));
			} else if (actions[1] == "Harpoon") {
				fish_list.add(new FishAction("Shark", "Harpoon"));
				fish_list.add(new FishAction("Mackerel", "Net"));
				fish_list.add(new FishAction("Bass", "Net"));
			}
			break;
		case "Lure":
			fish_list.add(new FishAction("Salmon", "Lure"));
			fish_list.add(new FishAction("Trout", "Lure"));
			fish_list.add(new FishAction("Pike", "Bait"));
			break;
		}
		return fish_list;
	}

	/**
	 * Returns the action to perform to obtain the type of fish passed.
	 * 
	 * @param fish
	 *            name : Name of fish to get action for.
	 * @return String : Action to perform to get the fish.
	 */
	private String getAction(String fish_name) {
		String local_action = null;

		switch (fish_name) {

		case "Lobster":
			local_action = "Cage";
			break;
		case "Tuna":
		case "Swordfish":
		case "Shark":
			local_action = "Harpoon";
			break;
		case "Pike":
		case "Herring":
		case "Sardines":
			local_action = "Bait";
			break;
		case "Trout":
		case "Salmon":
			local_action = "Lure";
			break;
		case "Shrimps":
		case "Anchovies":
		case "Bass":
		case "Mackerel":
		case "Cod":
			local_action = "Net";
			break;
		}

		return local_action;
	}

	/**
	 * Returns the spot type of the fish to obtain the type of fish passed.
	 * 
	 * @param fish
	 *            name : Name of fish to get action for.
	 * @return String : Action to perform to get the fish.
	 */
	private SpotType getSpotType(String fish_name) {
		SpotType sType = null;

		switch (fish_name) {
		case "Tuna":
		case "Swordfish":
		case "Lobster":
			sType = new SpotType("Cage", "Harpoon");
			break;
		case "Bass":
		case "Mackerel":
		case "Cod":
		case "Shark":
			sType = new SpotType("Net", "Harpoon");
			break;
		case "Shrimps":
		case "Anchovies":
		case "Herring":
		case "Sardines":
			sType = new SpotType("Net", "Bait");
			break;
		case "Pike":
		case "Trout":
		case "Salmon":
			sType = new SpotType("Bait", "Lure");
			break;
		}

		return sType;
	}
}
