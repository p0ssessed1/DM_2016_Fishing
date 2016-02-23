package dynamicArea;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;
import fishing.Fishing;

public class DynamicArea {
	final int MAX_AREA_SIZE = 3;
	
	Fishing fish;

	public DynamicArea(Fishing fish){
		this.fish = fish;
	}

	public List<Area> CreateAreas(List<NPC> spots) {
		List<Area> areas = new LinkedList<Area>();
		for (NPC n : spots) {
			if (fish.isSpotValid(n)) {
				fish.script.log("Added area to return: " + n.getArea(MAX_AREA_SIZE));
				areas.add(n.getArea(MAX_AREA_SIZE));
			}
		}
		
		fish.script.log("is areas empty?." + areas.isEmpty());
		return areas;
	}
	
	/**
	 * Get the closest area in which to fish that does not contain the player.
	 * 
	 * @return Area: The area in which to fish.
	 */
	public Area getClosestArea(List<Area> areas) {
		Position myPosition = fish.script.myPlayer().getPosition();
		Position tryPosition = areas.get(0).getRandomPosition();
		Area closestArea = areas.get(0);
		
		for(Area a: areas)
		{
			if(myPosition.distance(a.getRandomPosition()) < myPosition.distance(tryPosition)
					&& !a.contains(fish.script.myPlayer())){
				tryPosition = a.getRandomPosition();
				closestArea = a;
			}
		}
		return closestArea;
	}
	
	public boolean addExclusiveAreas(List<NPC> spots, List<Area> currentAreas){
		/* TODO - create method to add exclusive areas. */
		List<Area> areas = new LinkedList<Area>();
		areas = currentAreas;
		boolean added = false;
		List<Position> currentAreaPositions = new LinkedList<Position>();
		for (int i = 0; i < spots.size(); i++) {
			if (fish.isSpotValid(spots.get(i))) {
				currentAreaPositions = currentAreas.get(i).getPositions();
				if(currentAreaPositions.contains(spots.get(i).getPosition())){
					fish.script.log("Added area: " + spots.get(i).getArea(MAX_AREA_SIZE));
					areas.add(spots.get(i).getArea(MAX_AREA_SIZE));
					added = true;
				}
			}
		}
		
		return added;
	}
}
